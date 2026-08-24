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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.nodeloc.data.DiscourseApi
import app.nodeloc.data.SiteRepo
import app.nodeloc.data.model.PostDto
import app.nodeloc.data.model.TopicDetailDto
import app.nodeloc.ui.DetailArgs
import app.nodeloc.ui.components.Avatar
import app.nodeloc.ui.components.CookedText
import app.nodeloc.ui.components.LoadingMark
import app.nodeloc.ui.theme.LocalNodelocColors
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private sealed interface DetailState {
    data object Loading : DetailState
    data class Error(val message: String) : DetailState
    data class Ready(val detail: TopicDetailDto) : DetailState
}

private data class DisplayPost(val post: PostDto, val nested: Boolean)

@Composable
fun TopicDetailScreen(args: DetailArgs, onBack: () -> Unit) {
    val nc = LocalNodelocColors.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var state by remember { mutableStateOf<DetailState>(DetailState.Loading) }
    var posts by remember(args.id) { mutableStateOf<List<PostDto>>(emptyList()) }
    var streamIds by remember(args.id) { mutableStateOf<List<Long>>(emptyList()) }
    var loadingMore by remember(args.id) { mutableStateOf(false) }
    var loadError by remember(args.id) { mutableStateOf<String?>(null) }
    var repliesByPost by remember(args.id) { mutableStateOf<Map<Long, List<PostDto>>>(emptyMap()) }
    var repliesLoading by remember(args.id) { mutableStateOf<Set<Long>>(emptySet()) }
    var historyByPost by remember(args.id) { mutableStateOf<Map<Long, List<PostDto>>>(emptyMap()) }
    var historyLoading by remember(args.id) { mutableStateOf<Set<Long>>(emptySet()) }

    val loadedIds = remember(posts) { posts.mapTo(HashSet()) { it.id } }
    val canLoadMore = streamIds.any { it !in loadedIds }
    val displayPosts = remember(posts, repliesByPost) {
        buildList {
            posts.forEach { post ->
                add(DisplayPost(post, nested = false))
                repliesByPost[post.id].orEmpty().forEach { add(DisplayPost(it, nested = true)) }
            }
        }
    }

    suspend fun loadMorePosts() {
        if (loadingMore || !canLoadMore) return
        val ids = streamIds.filterNot { it in loadedIds }.take(20)
        if (ids.isEmpty()) return
        loadingMore = true
        loadError = null
        runCatching { DiscourseApi.posts(args.id, ids) }
            .onSuccess { chunk ->
                val known = posts.associateBy { it.id }.toMutableMap()
                chunk.postStream.posts.forEach { known[it.id] = it }
                posts = streamIds.mapNotNull { known[it] }
                if (chunk.postStream.posts.isEmpty()) loadError = "没有拿到新的楼层"
            }
            .onFailure { loadError = it.message ?: "加载更多回复失败" }
        loadingMore = false
    }

    fun toggleReplyChildren(post: PostDto) {
        if (repliesByPost.containsKey(post.id)) {
            repliesByPost = repliesByPost - post.id
            return
        }
        if (post.replyCount <= 0 || repliesLoading.contains(post.id)) return
        repliesLoading = repliesLoading + post.id
        scope.launch {
            runCatching { DiscourseApi.replies(post.id) }
                .onSuccess { response -> repliesByPost = repliesByPost + (post.id to response.posts) }
            repliesLoading = repliesLoading - post.id
        }
    }

    fun loadMoreReplyChildren(post: PostDto) {
        val current = repliesByPost[post.id].orEmpty()
        if (repliesLoading.contains(post.id) || current.size >= post.replyCount) return
        repliesLoading = repliesLoading + post.id
        scope.launch {
            runCatching { DiscourseApi.replies(post.id, current.lastOrNull()?.postNumber ?: 1) }
                .onSuccess { response ->
                    val known = current.associateBy { it.id }.toMutableMap()
                    response.posts.forEach { known[it.id] = it }
                    repliesByPost = repliesByPost + (post.id to known.values.sortedBy { it.postNumber })
                }
            repliesLoading = repliesLoading - post.id
        }
    }

    fun toggleReplyHistory(post: PostDto) {
        if (post.replyToPostNumber == null) return
        if (historyByPost.containsKey(post.id)) {
            historyByPost = historyByPost - post.id
            return
        }
        if (historyLoading.contains(post.id)) return
        historyLoading = historyLoading + post.id
        scope.launch {
            runCatching { DiscourseApi.replyHistory(post.id) }
                .onSuccess { response -> historyByPost = historyByPost + (post.id to response.posts) }
            historyLoading = historyLoading - post.id
        }
    }

    LaunchedEffect(args.id) {
        state = DetailState.Loading
        posts = emptyList()
        streamIds = emptyList()
        repliesByPost = emptyMap()
        historyByPost = emptyMap()
        runCatching { DiscourseApi.topic(args.id) }
            .onSuccess { detail ->
                state = DetailState.Ready(detail)
                posts = detail.postStream.posts
                streamIds = detail.postStream.stream.ifEmpty { detail.postStream.posts.map { it.id } }
            }
            .onFailure { state = DetailState.Error(it.message ?: "网络错误") }
    }

    LaunchedEffect(listState, posts.size, streamIds.size, displayPosts.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collectLatest { index -> if (index >= displayPosts.size - 4) loadMorePosts() }
    }

    Column(Modifier.fillMaxSize().background(nc.background)) {
        Row(Modifier.fillMaxWidth().height(56.dp).background(nc.headerBg), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = nc.onBackground) }
            Text(args.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = nc.onBackground,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            IconButton(onClick = {}) { Icon(Icons.Filled.MoreVert, null, tint = nc.onSurfaceVariant) }
        }

        var catName by remember(args.id) { mutableStateOf<String?>(null) }
        var catColor by remember(args.id) { mutableStateOf("#0088CC") }
        LaunchedEffect(args.categoryId) { SiteRepo.category(args.categoryId)?.let { catName = it.name; catColor = "#" + it.color } }
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(Color(android.graphics.Color.parseColor(catColor)), CircleShape))
            Spacer(Modifier.width(6.dp)); Text(catName ?: "", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = nc.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            (state as? DetailState.Ready)?.detail?.let { detail -> Text((detail.postsCount - 1).coerceAtLeast(0).toString() + " 回复 · " + detail.views + " 浏览",
                style = MaterialTheme.typography.labelMedium, color = nc.onSurfaceVariant) }
        }

        when (val s = state) {
            DetailState.Loading -> Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { LoadingMark() }
            is DetailState.Error -> Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(s.message, color = nc.onSurfaceVariant); TextButton(onClick = onBack) { Text("返回列表", color = nc.primary) } }
            }
            is DetailState.Ready -> LazyColumn(state = listState, modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(displayPosts, key = { item -> (if (item.nested) "reply-" else "post-") + item.post.id }) { item ->
                    val post = item.post
                    PostItem(post, item.nested, historyByPost[post.id], post.id in historyLoading, repliesByPost[post.id], post.id in repliesLoading,
                        { toggleReplyHistory(post) }, { toggleReplyChildren(post) }, { loadMoreReplyChildren(post) })
                }
                item(key = "pagination") {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                        if (loadingMore) Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = nc.primary, strokeWidth = 2.dp, modifier = Modifier.size(20.dp)) }
                        else if (canLoadMore) TextButton(onClick = { scope.launch { loadMorePosts() } }) { Text("继续加载楼层", color = nc.primary) }
                        else Text("共 " + (s.detail.postsCount - 1).coerceAtLeast(0) + " 条回复 · 已加载 " + posts.size + " 层", style = MaterialTheme.typography.labelSmall, color = nc.onSurfaceVariant)
                        loadError?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = nc.onSurfaceVariant) }
                    }
                }
            }
        }

        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 0.dp) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = "", onValueChange = {}, readOnly = true, placeholder = { Text("回复此话题…", color = nc.onSurfaceVariant) },
                    shape = RoundedCornerShape(21.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = nc.outlineVariant, focusedBorderColor = nc.primary),
                    modifier = Modifier.weight(1f).heightIn(min = 42.dp))
                Spacer(Modifier.width(10.dp)); FilledIconButton(onClick = {}, shape = CircleShape,
                    colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(containerColor = nc.primary, contentColor = nc.onPrimary)) { Icon(Icons.AutoMirrored.Filled.Send, null) }
            }
        }
    }
}

@Composable
private fun PostItem(
    post: PostDto,
    nested: Boolean,
    history: List<PostDto>?,
    historyLoading: Boolean,
    children: List<PostDto>?,
    childrenLoading: Boolean,
    onToggleHistory: () -> Unit,
    onToggleChildren: () -> Unit,
    onLoadMoreChildren: () -> Unit,
) {
    val nc = LocalNodelocColors.current
    val likes = post.actionsSummary.firstOrNull { it.id == 2 }?.count ?: 0
    Column(Modifier.fillMaxWidth().padding(start = if (nested) 38.dp else 16.dp, end = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
            Avatar(name = post.username, url = SiteRepo.avatarUrl(post.avatarTemplate), size = if (nested) 36.dp else 42.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(post.username, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = nc.onBackground)
                    if (post.username.equals("James", ignoreCase = true)) {
                        Spacer(Modifier.width(7.dp)); Box(Modifier.background(nc.adminBadge, RoundedCornerShape(5.dp)).padding(horizontal = 6.dp, vertical = 1.5.dp)) {
                            Text("ADMIN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (nc.background.luminance() > 0.5f) Color.White else Color(0xFF12100D))
                        }
                    }
                    Spacer(Modifier.weight(1f)); Text(SiteRepo.relativeTime(post.createdAt), style = MaterialTheme.typography.labelSmall, color = nc.onSurfaceVariant)
                }
                Text(post.username + " · " + post.postNumber + " 楼", style = MaterialTheme.typography.labelSmall, color = nc.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                post.replyToPostNumber?.let { target ->
                    Surface(onClick = onToggleHistory, shape = RoundedCornerShape(8.dp), color = nc.secondaryContainer.copy(alpha = 0.7f), modifier = Modifier.padding(top = 7.dp)) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = nc.primary, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(5.dp))
                            Text("回复 " + (post.replyToUser?.username?.takeIf { it.isNotBlank() } ?: "#" + target), style = MaterialTheme.typography.labelMedium, color = nc.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (historyLoading) { Spacer(Modifier.width(6.dp)); CircularProgressIndicator(color = nc.primary, strokeWidth = 1.5.dp, modifier = Modifier.size(12.dp)) }
                        }
                    }
                }
                history?.takeIf { it.isNotEmpty() }?.let { chain ->
                    Surface(shape = RoundedCornerShape(10.dp), color = nc.surfaceVariant, modifier = Modifier.padding(top = 5.dp)) {
                        Column(Modifier.padding(8.dp)) { chain.takeLast(3).forEach { parent ->
                            Text("#" + parent.postNumber + "  " + parent.username, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = nc.onSurfaceVariant)
                            Text(parent.cooked.replace(Regex("<[^>]+>"), " ").trim().replace(Regex("\\s+"), " "), style = MaterialTheme.typography.bodySmall, color = nc.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(bottom = 4.dp))
                        } }
                    }
                }
                CookedText(post.cooked, Modifier.padding(top = 7.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 9.dp)) {
                    Icon(Icons.Filled.Favorite, null, tint = nc.onSurfaceVariant, modifier = Modifier.size(15.dp))
                    if (likes > 0) { Spacer(Modifier.width(4.dp)); Text(likes.toString(), style = MaterialTheme.typography.labelMedium, color = nc.onSurfaceVariant) }
                    Spacer(Modifier.width(22.dp)); Text("回复", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = nc.onSurfaceVariant)
                    Spacer(Modifier.width(22.dp)); Icon(Icons.Filled.Share, null, tint = nc.onSurfaceVariant, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text("分享", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = nc.onSurfaceVariant)
                }
                if (!nested && post.replyCount > 0) {
                    val expanded = children != null
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 5.dp)) {
                        TextButton(onClick = onToggleChildren) { Icon(if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(3.dp)); Text(if (expanded) "收起回复" else "查看 " + post.replyCount + " 条回复", color = nc.primary) }
                        if (expanded && children.orEmpty().size < post.replyCount) TextButton(onClick = onLoadMoreChildren, enabled = !childrenLoading) { Text(if (childrenLoading) "加载中…" else "加载更多", color = nc.primary) }
                    }
                }
            }
        }
    }
}
