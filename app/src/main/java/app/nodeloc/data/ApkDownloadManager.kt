package app.nodeloc.data

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

/**
 * APK 下载和安装管理器
 */
object ApkDownloadManager {
    private const val TAG = "ApkDownloadManager"

    /**
     * 下载并安装 APK
     * @param context 上下文
     * @param downloadUrl APK 下载 URL
     * @param version 版本号（用于文件名）
     */
    fun downloadAndInstall(context: Context, downloadUrl: String, version: String) {
        val fileName = "nodeloc-v$version-universal.apk"
        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle("NodeLoc 更新")
            setDescription("正在下载 v$version")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setMimeType("application/vnd.android.package-archive")
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        // 注册下载完成监听器
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    installApk(ctx, downloadManager, downloadId, fileName)
                    ctx.unregisterReceiver(this)
                }
            }
        }

        context.registerReceiver(
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            Context.RECEIVER_NOT_EXPORTED
        )

        Log.i(TAG, "Started downloading APK: $downloadUrl")
    }

    /**
     * 安装 APK
     */
    private fun installApk(
        context: Context,
        downloadManager: DownloadManager,
        downloadId: Long,
        fileName: String
    ) {
        val uri = downloadManager.getUriForDownloadedFile(downloadId)
        if (uri == null) {
            Log.e(TAG, "Failed to get downloaded file URI")
            return
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Android 7.0+ 使用 FileProvider
                val file = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    fileName
                )
                val apkUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            } else {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

        try {
            context.startActivity(intent)
            Log.i(TAG, "Started APK installation")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start APK installation", e)
        }
    }
}
