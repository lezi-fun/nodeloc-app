package app.nodeloc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.nodeloc.data.DiscourseApi
import app.nodeloc.data.SessionRepo
import app.nodeloc.data.SiteRepo
import app.nodeloc.data.model.NotificationDto
import app.nodeloc.ui.components.Avatar
import app.nodeloc.ui.components.LoadingMark
import app.nodeloc.ui.theme.LocalNodelocColors
import kotlinx.coroutines.launch

private sealed interface NotificationsState {
    data object Loading : NotificationsState
    data class Error(val message: String) : NotificationsState
    data class Ready(val notifications: List<NotificationDto>) : NotificationsState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenTopic: (Long) -> Unit,
) {
    val nc = LocalNodelocColors.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<NotificationsState>(NotificationsState.Loading) }
    var markingAll by remember { mutableStateOf(false) }

    suspend fun load() {
        state = NotificationsState.Loading
        runCatching { DiscourseApi.notifications() }
            .onSuccess { response -> state = NotificationsState.Ready(response.notifications) }
            .onFailure { state = NotificationsState.Error(it.message ?: "加载通知失败") }
    }

    fun markAllRead() {
        val ready = state as? NotificationsState.Ready ?: return
        if (markingAll || ready.notifications.none { !it.read }) return
        markingAll = true
        scope.launch {
            runCatching { DiscourseApi.markNotificationsRead() }
                .onSuccess {
                    val current = state as? NotificationsState.Ready
                    if (current != null) {
                        state = current.copy(notifications = current.notifications.map { it.copy(read = true) })
                    }
                    SessionRepo.refresh()
                }
            markingAll = false
        }
    }

    fun openNotification(notification: NotificationDto) {
        scope.launch {
            if (!notification.read) {
                runCatching { DiscourseApi.markNotificationRead(notification.id) }
                    .onSuccess {
                        val current = state as? NotificationsState.Ready
                        if (current != null) {
                            state = current.copy(
                                notifications = current.notifications.map {
                                    if (it.id == notification.id) it.copy(read = true) else it
                                },
                            )
                        }
                        SessionRepo.refresh()
                    }
            }
            notification.topicId?.let(onOpenTopic)
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("通知") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    val ready = state as? NotificationsState.Ready
                    if (ready?.notifications?.any { !it.read } == true) {
                        TextButton(onClick = ::markAllRead, enabled = !markingAll) {
                            if (markingAll) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = nc.primary,
                                )
                            } else {
                                Text("全部已读", color = nc.primary)
                            }
                        }
                    }
                },
            )
        },
        containerColor = nc.background,
    ) { padding ->
        when (val s = state) {
            NotificationsState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { LoadingMark() }

            is NotificationsState.Error -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.message, color = nc.onSurfaceVariant)
                    TextButton(onClick = { scope.launch { load() } }) {
                        Text("重试", color = nc.primary)
                    }
                }
            }

            is NotificationsState.Ready -> if (s.notifications.isEmpty()) {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) { Text("暂无通知", color = nc.onSurfaceVariant) }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    items(s.notifications, key = { it.id }) { notification ->
                        NotificationRow(
                            notification = notification,
                            onClick = { openNotification(notification) },
                        )
                        HorizontalDivider(color = nc.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun UserNotificationsMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onViewAll: () -> Unit,
    onOpenTopic: (Long) -> Unit,
) {
    val nc = LocalNodelocColors.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<NotificationsState>(NotificationsState.Loading) }
    var markingAll by remember { mutableStateOf(false) }

    suspend fun loadRecent() {
        state = NotificationsState.Loading
        runCatching { DiscourseApi.recentNotifications() }
            .onSuccess { response -> state = NotificationsState.Ready(response.notifications) }
            .onFailure { state = NotificationsState.Error(it.message ?: "加载通知失败") }
    }

    fun markAllRead() {
        val ready = state as? NotificationsState.Ready ?: return
        if (markingAll || ready.notifications.none { !it.read }) return
        markingAll = true
        scope.launch {
            runCatching { DiscourseApi.markNotificationsRead() }
                .onSuccess {
                    val current = state as? NotificationsState.Ready
                    if (current != null) {
                        state = current.copy(notifications = current.notifications.map { it.copy(read = true) })
                    }
                    SessionRepo.refresh()
                }
            markingAll = false
        }
    }

    fun openNotification(notification: NotificationDto) {
        onDismissRequest()
        scope.launch {
            if (!notification.read) {
                runCatching { DiscourseApi.markNotificationRead(notification.id) }
                    .onSuccess {
                        val current = state as? NotificationsState.Ready
                        if (current != null) {
                            state = current.copy(
                                notifications = current.notifications.map {
                                    if (it.id == notification.id) it.copy(read = true) else it
                                },
                            )
                        }
                        SessionRepo.refresh()
                    }
            }
            notification.topicId?.let(onOpenTopic)
        }
    }

    LaunchedEffect(expanded) {
        if (expanded) loadRecent()
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.widthIn(min = 300.dp, max = 360.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("通知", style = MaterialTheme.typography.titleMedium, color = nc.onBackground)
            Spacer(Modifier.weight(1f))
            val ready = state as? NotificationsState.Ready
            if (ready?.notifications?.any { !it.read } == true) {
                TextButton(onClick = ::markAllRead, enabled = !markingAll) {
                    if (markingAll) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            strokeWidth = 2.dp,
                            color = nc.primary,
                        )
                    } else {
                        Text("全部已读", color = nc.primary)
                    }
                }
            }
        }
        HorizontalDivider(color = nc.outlineVariant)

        when (val menuState = state) {
            NotificationsState.Loading -> Box(
                modifier = Modifier.fillMaxWidth().padding(28.dp),
                contentAlignment = Alignment.Center,
            ) { LoadingMark() }

            is NotificationsState.Error -> Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(menuState.message, color = nc.onSurfaceVariant)
                TextButton(onClick = { scope.launch { loadRecent() } }) {
                    Text("重试", color = nc.primary)
                }
            }

            is NotificationsState.Ready -> if (menuState.notifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(28.dp),
                    contentAlignment = Alignment.Center,
                ) { Text("暂无通知", color = nc.onSurfaceVariant) }
            } else {
                menuState.notifications.forEach { notification ->
                    NotificationRow(
                        notification = notification,
                        compact = true,
                        onClick = { openNotification(notification) },
                    )
                    HorizontalDivider(color = nc.outlineVariant)
                }
            }
        }

        TextButton(
            onClick = {
                onDismissRequest()
                onViewAll()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("查看全部通知", color = nc.primary)
        }
    }
}

@Composable
private fun NotificationRow(
    notification: NotificationDto,
    compact: Boolean = false,
    onClick: () -> Unit,
) {
    val nc = LocalNodelocColors.current
    val actor = notification.data.displayUsername
        ?: notification.data.originalUsername
        ?: notification.actingUserName
        ?: "有人"
    val topicTitle = notification.data.topicTitle ?: notification.fancyTitle

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (notification.read) Color.Transparent else nc.secondaryContainer.copy(alpha = 0.45f))
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (compact) 12.dp else 16.dp,
                vertical = if (compact) 9.dp else 12.dp,
            ),
        verticalAlignment = Alignment.Top,
    ) {
        Avatar(
            name = actor,
            url = SiteRepo.avatarUrl(notification.actingUserAvatarTemplate),
            size = if (compact) 34.dp else 40.dp,
        )
        Spacer(Modifier.width(if (compact) 10.dp else 12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                notificationDescription(notification, actor),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (notification.read) FontWeight.Normal else FontWeight.SemiBold,
                color = nc.onBackground,
            )
            topicTitle?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = nc.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                SiteRepo.relativeTime(notification.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = nc.onSurfaceVariant,
            )
        }
        if (!notification.read) {
            Box(
                Modifier.padding(start = 8.dp, top = 8.dp).size(8.dp),
            ) {
                Box(Modifier.fillMaxSize().background(nc.primary, androidx.compose.foundation.shape.CircleShape))
            }
        }
    }
}

private fun notificationDescription(notification: NotificationDto, actor: String): String = when (notification.notificationType) {
    1 -> "$actor 提到了你"
    2 -> "$actor 回复了你"
    3 -> "$actor 引用了你的发言"
    5 -> "$actor 喜欢了你的发言"
    6 -> "$actor 发来了一条私信"
    9 -> "$actor 发布了新话题"
    12 -> "你获得了徽章 ${notification.data.badgeName ?: ""}".trim()
    13 -> "${notification.data.groupName ?: actor} 提到了你所在的群组"
    14 -> "${notification.data.groupName ?: actor} 发来了一条群组消息"
    17 -> "$actor 在聊天中提到了你"
    else -> "$actor 有一条新通知"
}
