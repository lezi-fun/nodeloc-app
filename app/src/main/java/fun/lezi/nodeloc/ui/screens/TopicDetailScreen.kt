package fun.lezi.nodeloc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fun.lezi.nodeloc.data.DiscourseApi
import fun.lezi.nodeloc.data.SiteRepo
import fun.lezi.nodeloc.data.model.PostDto
import fun.lezi.nodeloc.data.model.TopicDetailDto
import fun.lezi.nodeloc.ui.DetailArgs
import fun.lezi.nodeloc.ui.components.Avatar
import fun.lezi.nodeloc.ui.theme.LocalNodelocColors
import fun.lezi.nodeloc.util.cookedToText

private sealed interface DetailState {
    data object Loading : DetailState
    data class Error(val message: String) : DetailState
    data class Ready(val detail: TopicDetailDto) : DetailState
}

@Composable
fun TopicDetailScreen(args: DetailArgs, onBack: () -> Unit) {
    val nc = LocalNodelocColors.current
    var state by remember { mutableStateOf<DetailState>(DetailState.Loading) }

    LaunchedEffect(args.id) {
        runCatching { DiscourseApi.topic(args.id) }
            .onSuccess { state = DetailState.Ready(it) }
            .onFailure { state = DetailState.Error(it.message ?: "网络错误") }
    }

    Column(Modifier.fillMaxSize().background(nc.background)) {
        // ── 导航栏(B 方向深色顶栏语言) ──
        Row(
            Modifier.fillMaxWidth().height(56.dp).background(nc.headerBg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = nc.onBackground)
            }
            Text(
                text = args.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = nc.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = {}) {
                Icon(Icons.Filled.MoreVert, null, tint = nc.onSurfaceVariant)
            }
        }

        // ── 分类元信息行 ──
        var catName by remember(args.id) { mutableStateOf<String?>(null) }
        var catColor by remember(args.id) { mutableStateOf("#0088CC") }
        LaunchedEffect(args.categoryId) {
            SiteRepo.category(args.categoryId)?.let { catName = it.name; catColor = "#" + it.color }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(8.dp).background(Color(android.graphics.Color.parseColor(catColor)), CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(catName ?: "", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = nc.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            when (val s = state) {
                is DetailState.Ready -> Text(
                    (s.detail.postsCount - 1).toString() + " 回复 · " + s.detail.views + " 浏览",
                    style = MaterialTheme.typography.labelMedium,
                    color = nc.onSurfaceVariant,
                )
                else -> {}
            }
        }

        // ── 楼层流 ──
        when (val s = state) {
            is DetailState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = nc.primary)
            }
            is DetailState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.message, color = nc.onSurfaceVariant)
                    TextButton(onClick = onBack) { Text("返回列表", color = nc.primary) }
                }
            }
            is DetailState.Ready -> LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(s.detail.postStream.posts, key = { p -> p.postNumber }) { post ->
                    PostItem(post)
                }
                item(key = "end") {
                    Text(
                        "共 " + (s.detail.postsCount - 1) + " 条回复 · 已加载 " + s.detail.postStream.posts.size + " 层",
                        style = MaterialTheme.typography.labelSmall,
                        color = nc.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
            }
        }

        // ── 回复输入条(视觉件) ──
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 0.dp) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("回复此话题…", color = nc.onSurfaceVariant) },
                    shape = RoundedCornerShape(21.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = nc.outlineVariant,
                        focusedBorderColor = nc.primary,
                    ),
                    modifier = Modifier.weight(1f).heightIn(min = 42.dp),
                )
                Spacer(Modifier.width(10.dp))
                FilledIconButton(
                    onClick = {},
                    shape = CircleShape,
                    colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(
                        containerColor = nc.primary, contentColor = nc.onPrimary,
                    ),
                ) { Icon(Icons.Filled.Send, null) }
            }
        }
    }
}

@Composable
private fun PostItem(post: PostDto) {
    val nc = LocalNodelocColors.current
    val likes = post.actionsSummary.firstOrNull { it.id == 2 }?.count ?: 0
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Avatar(name = post.username, url = SiteRepo.avatarUrl(post.avatarTemplate), size = 42.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(post.username, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = nc.onBackground)
                if (post.username.equals("James", ignoreCase = true)) {
                    Spacer(Modifier.width(7.dp))
                    Box(
                        Modifier.background(nc.adminBadge, RoundedCornerShape(5.dp)).padding(horizontal = 6.dp, vertical = 1.5.dp)
                    ) {
                        Text("ADMIN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                            color = if (nc.background.luminance() > 0.5f) Color.White else Color(0xFF12100D))
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(SiteRepo.relativeTime(post.createdAt), style = MaterialTheme.typography.labelSmall, color = nc.onSurfaceVariant)
            }
            Text(
                post.username + " · " + post.postNumber + " 楼",
                style = MaterialTheme.typography.labelSmall,
                color = nc.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                text = cookedToText(post.cooked),
                style = MaterialTheme.typography.bodyMedium,
                color = nc.onSurface,
                modifier = Modifier.padding(top = 7.dp),
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 9.dp)) {
                Icon(Icons.Filled.Favorite, null, tint = nc.onSurfaceVariant, modifier = Modifier.size(15.dp))
                if (likes > 0) {
                    Spacer(Modifier.width(4.dp))
                    Text(likes.toString(), style = MaterialTheme.typography.labelMedium, color = nc.onSurfaceVariant)
                }
                Spacer(Modifier.width(22.dp))
                Text("回复", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = nc.onSurfaceVariant)
                Spacer(Modifier.width(22.dp))
                Icon(Icons.Filled.Share, null, tint = nc.onSurfaceVariant, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("分享", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = nc.onSurfaceVariant)
            }
        }
    }
}
