package app.nodeloc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.nodeloc.data.DiscourseApi
import app.nodeloc.data.SiteRepo
import app.nodeloc.data.model.BadgeStyleDto
import app.nodeloc.data.model.UserActionDto
import app.nodeloc.data.model.UserProfileDto
import app.nodeloc.data.model.UserSummaryDto
import app.nodeloc.ui.components.Avatar
import app.nodeloc.ui.components.BadgeTitleText
import app.nodeloc.ui.components.LoadingMark
import app.nodeloc.ui.theme.LocalNodelocColors
private sealed interface ProfileState {
    data object Loading : ProfileState
    data class Error(val message: String) : ProfileState
    data class Ready(
        val profile: UserProfileDto,
        val summary: UserSummaryDto?,
        val badgeStyles: List<BadgeStyleDto>,
        val actions: List<UserActionDto>,
    ) : ProfileState
}

/** 对齐官网 /u/{username} 用户主页:头像+称号(可带动效)+简介+统计信息+最近帖子。 */
@Composable
fun UserProfileScreen(username: String, onBack: () -> Unit, onOpenTopic: (Long) -> Unit) {
    val nc = LocalNodelocColors.current
    var state by remember(username) { mutableStateOf<ProfileState>(ProfileState.Loading) }
    var showMessageDialog by remember { mutableStateOf(false) }

    LaunchedEffect(username) {
        state = ProfileState.Loading
        try {
            val profile = DiscourseApi.userProfile(username)
            val summary = runCatching { DiscourseApi.userSummary(username) }.getOrNull()
            val badgeStyles = runCatching { DiscourseApi.badgeStyles() }.getOrDefault(emptyList())
            val actions = runCatching { DiscourseApi.userActions(username) }.getOrDefault(emptyList())
            state = ProfileState.Ready(profile, summary, badgeStyles, actions)
        } catch (e: Throwable) {
            state = ProfileState.Error(e.message ?: "加载失败")
        }
    }

    Column(Modifier.fillMaxSize().background(nc.background)) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).background(nc.headerBg).padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = nc.onBackground)
            }
            Text(
                "个人资料",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = nc.onBackground,
            )
        }
        HorizontalDivider(color = nc.outlineVariant)

        when (val s = state) {
            ProfileState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingMark() }
            is ProfileState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.message, color = nc.onSurfaceVariant)
                    TextButton(onClick = { /* 触发 LaunchedEffect 重新拉取需要 key 变化,这里让用户手动返回重进 */ onBack() }) {
                        Text("返回", color = nc.primary)
                    }
                }
            }
            is ProfileState.Ready -> ProfileContent(s, onOpenTopic, onSendMessage = { showMessageDialog = true })
        }
    }

    // 发私信对话框
    if (showMessageDialog && state is ProfileState.Ready) {
        app.nodeloc.ui.components.SendMessageDialog(
            recipientUsername = (state as ProfileState.Ready).profile.username,
            onDismiss = { showMessageDialog = false },
            onSent = { showMessageDialog = false }
        )
    }
}

@Composable
private fun ProfileContent(state: ProfileState.Ready, onOpenTopic: (Long) -> Unit, onSendMessage: () -> Unit) {
    val nc = LocalNodelocColors.current
    val profile = state.profile
    val badgeStyle = profile.title?.let { title -> state.badgeStyles.firstOrNull { it.name == title } }

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Avatar(
                    profile.username,
                    profile.animatedAvatar?.let { app.nodeloc.util.absoluteUrl(it, DiscourseApi.BASE) }
                        ?: SiteRepo.avatarUrl(profile.avatarTemplate, 200),
                    88.dp,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    profile.name.ifBlank { profile.username },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = nc.onBackground,
                )
                if (profile.name.isNotBlank()) {
                    Text("@" + profile.username, style = MaterialTheme.typography.bodySmall, color = nc.onSurfaceVariant)
                }
                profile.title?.takeIf { it.isNotBlank() }?.let { title ->
                    Spacer(Modifier.height(2.dp))
                    BadgeTitleText(title, badgeStyle, nc.onSurfaceVariant)
                }
                profile.bioExcerpt?.takeIf { it.isNotBlank() }?.let { bio ->
                    Spacer(Modifier.height(8.dp))
    Text(
                        bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = nc.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(14.dp))

                // 发私信按钮
                Button(
                    onClick = onSendMessage,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = nc.primary,
                        contentColor = nc.onPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("发私信")
                }

                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    StatChip("能量", profile.gamificationScore.toString())
                    StatChip("徽章", profile.badgeCount.toString())
                    StatChip("关注者", profile.totalFollowers.toString())
                }
            }
            HorizontalDivider(color = nc.outlineVariant)
        }

        state.summary?.let { summary ->
            item {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("统计信息", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = nc.onBackground)
                    Spacer(Modifier.height(10.dp))
                    val stats = listOf(
                        "创建话题" to summary.topicCount.toString(),
                        "创建帖子" to summary.postCount.toString(),
                        "已送出赞" to summary.likesGiven.toString(),
                        "已收到赞" to summary.likesReceived.toString(),
                        "已读帖子" to summary.postsReadCount.toString(),
                        "访问天数" to summary.daysVisited.toString(),
                    )
                    stats.chunked(3).forEach { row ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            row.forEach { (label, value) ->
                                Column(Modifier.weight(1f)) {
                                    Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = nc.onBackground)
                                    Text(label, style = MaterialTheme.typography.labelSmall, color = nc.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(color = nc.outlineVariant)
            }
        }

        item {
            Text(
                "最近帖子",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = nc.onBackground,
                modifier = Modifier.padding(16.dp),
            )
        }
        items(state.actions, key = { it.topicId.toString() + "-" + it.postNumber }) { action ->
            Surface(
                onClick = { onOpenTopic(action.topicId) },
                color = nc.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(
                        action.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = nc.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    action.excerpt?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(3.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall, color = nc.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(SiteRepo.relativeTime(action.createdAt), style = MaterialTheme.typography.labelSmall, color = nc.onSurfaceVariant)
                }
            }
            HorizontalDivider(color = nc.outlineVariant)
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    val nc = LocalNodelocColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = nc.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = nc.onSurfaceVariant)
    }
}

