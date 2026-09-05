package app.nodeloc.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.nodeloc.MainActivity
import app.nodeloc.R
import app.nodeloc.data.model.NotificationDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/** 检查 Discourse 通知并投递 Android 系统通知，按通知 ID 去重。 */
object NotificationSync {
    private const val CHANNEL_ID = "site_notifications"
    private const val SYSTEM_NOTIFICATION_ID = 4001
    private const val OPEN_NOTIFICATIONS = "open_notifications"
    private const val PERIODIC_WORK_NAME = "discourse-notification-sync"
    private val mutex = Mutex()

    fun createChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "社区通知",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "NodeLoc 的提及、回复、点赞和私信通知"
            },
        )
    }

    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    suspend fun check(context: Context): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (SessionRepo.currentUser.value == null) return@withLock true
            createChannel(context)
            if (!canPostNotifications(context)) return@withLock true

            val response = runCatching { DiscourseApi.notifications() }.getOrNull() ?: return@withLock false
            val lastId = SessionStore.lastNotifiedNotificationId()
            val fresh = response.notifications
                .asSequence()
                .filter { !it.read && it.id > lastId }
                .sortedBy { it.id }
                .toList()
            if (fresh.isEmpty()) return@withLock true

            val latest = fresh.last()
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(OPEN_NOTIFICATIONS, true)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                4001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setContentTitle(if (fresh.size == 1) "NodeLoc 通知" else "NodeLoc 有 ${fresh.size} 条新通知")
                .setContentText(notificationText(latest))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setNumber(fresh.size)

            if (fresh.size > 1) {
                builder.setStyle(
                    NotificationCompat.InboxStyle().also { style ->
                        fresh.takeLast(5).forEach { style.addLine(notificationText(it)) }
                        if (fresh.size > 5) style.setSummaryText("还有 ${fresh.size - 5} 条通知")
                    },
                )
            }
            val delivered = if (
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    NotificationManagerCompat.from(context)
                        .notify(SYSTEM_NOTIFICATION_ID, builder.build())
                    true
                } catch (_: SecurityException) {
                    false
                }
            } else {
                false
            }
            if (!delivered) return@withLock false
            SessionStore.markNotificationNotified(latest.id)
            SessionRepo.refresh()
            true
        }
    }

    private fun canPostNotifications(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private fun notificationText(notification: NotificationDto): String {
        val actor = notification.data.displayUsername
            ?: notification.data.originalUsername
            ?: notification.actingUserName
            ?: "有人"
        val action = when (notification.notificationType) {
            1 -> "提到了你"
            2 -> "回复了你"
            3 -> "引用了你的发言"
            5 -> "喜欢了你的发言"
            6 -> "发来了一条私信"
            9 -> "发布了新话题"
            12 -> "让你获得了徽章"
            17 -> "在聊天中提到了你"
            else -> "有一条新通知"
        }
        val topic = notification.data.topicTitle ?: notification.fancyTitle
        return if (topic.isNullOrBlank()) "$actor $action" else "$actor $action：$topic"
    }
}

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result =
        if (NotificationSync.check(applicationContext)) Result.success() else Result.retry()
}
