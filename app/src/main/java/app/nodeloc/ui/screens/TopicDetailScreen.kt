package app.nodeloc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
import app.nodeloc.util.hexColor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.min

private sealed interface DetailState {
    data object Loading : DetailState
    data class Error(val message: String) : DetailState
    data class Ready(val detail: TopicDetailDto, val nested: Boolean) : DetailState
}

private data class DisplayPost(val post: PostDto, val depth: Int, val isOp: Boolean = false)

private fun initiallyExpanded(posts: List<PostDto>): Set<Long> = buildSet {
    fun visit(post: PostDto) {
        val children = post.children.orEmpty()
        if (children.isNotEmpty()) add(post.id)
        children.forEach(::visit)
    }
    posts.forEach(::visit)
}

@Composable
fun TopicDetailScreen(args: DetailArgs, onBack: () -> Unit) {
    val nc = LocalNodelocColors.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var state by remember(args.id) { mutableStateOf<DetailState>(DetailState.Loading) }
    var loggedIn by remember(args.id) { mutableStateOf(false) }
    var posts by remember(args.id) { mutableStateOf<List<PostDto>>(emptyList()) }
    var streamIds by remember(args.id) { mutableStateOf<List<Long>>(emptyList()) }
    var opPost by remember(args.id) { mutableStateOf<PostDto?>(null) }
    var roots by remember(args.id) { mutableStateOf<List<PostDto>>(emptyList()) }
    var nestedPage by remember(args.id) { mutableStateOf(0) }
    var nestedSort by remember(args.id) { mutableStateOf("top") }
    var hasMoreRoots by remember(args.id) { mutableStateOf(false) }
    var expandedPosts by remember(args.id) { mutableStateOf<Set<Long>>(emptySet()) }
    var childrenByPost by remember(args.id) { mutableStateOf<Map<Long, List<PostDto>>>(emptyMap()) }
    var childPages by remember(args.id) { mutableStateOf<Map<Long, Int>>(emptyMap()) }
    var childHasMore by remember(args.id) { mutableStateOf<Map<Long, Boolean>>(emptyMap()) }
    var childLoading by remember(args.id) { mutableStateOf<Set<Long>>(emptySet()) }
    var loadingMore by remember(args.id) { mutableStateOf(false) }
    var loadError by remember(args.id) { mutableStateOf<String?>(null) }
    var reloadToken by remember(args.id) { mutableIntStateOf(0) }

    val loadedIds = remember(posts) { posts.mapTo(HashSet()) { it.id } }
    val canLoadMoreClassic = streamIds.any { it !in loadedIds }
    val displayPosts = remember(state, posts, opPost, roots, expandedPosts, childrenByPost) {
        if ((state as? DetailState.Ready)?.nested != true) {
            posts.map { DisplayPost(it, 0, it.postNumber == 1) }
        } else buildList {
            opPost?.let { add(DisplayPost(it, 0, true)) }
            fun appendTree(nodes: List<PostDto>, depth: Int) {
                nodes.forEach { post ->
                    add(DisplayPost(post, depth))
                    if (post.id in expandedPosts) {
                        appendTree(childrenByPost[post.id] ?: post.children.orEmpty(), depth + 1)
                    }
                }
            }
            appendTree(roots, 0)
        }
    }

    suspend fun loadMoreClassic() {
        if (loadingMore || !canLoadMoreClassic) return
        val ids = streamIds.filterNot { it in loadedIds }.take(20)
        if (ids.isEmpty()) return
        loadingMore = true
        loadError = null
        runCatching { DiscourseApi.posts(args.id, ids) }.onSuccess { chunk ->
            val known = posts.associateBy { it.id }.toMutableMap()
            chunk.postStream.posts.forEach { known[it.id] = it }
            posts = streamIds.mapNotNull { known[it] }
            if (chunk.postStream.posts.isEmpty()) loadError = "没有拿到新的楼层"
        }.onFailure { loadError = it.message ?: "加载更多回复失败" }
        loadingMore = false
    }

    suspend fun loadMoreRoots() {
        val detail = (state as? DetailState.Ready)?.detail ?: return
        if (loadingMore || !hasMoreRoots) return
        loadingMore = true
        loadError = null
        runCatching { DiscourseApi.nestedTopic(detail.slug, args.id, nestedPage + 1, nestedSort) }.onSuccess { response ->
            val known = roots.associateBy { it.id }.toMutableMap()
            response.roots.forEach { known[it.id] = it }
            roots = known.values.toList()
            nestedPage = response.page
            nestedSort = response.effectiveSort
            hasMoreRoots = response.hasMoreRoots
            expandedPosts = expandedPosts + initiallyExpanded(response.roots)
        }.onFailure { loadError = it.message ?: "加载更多回复失败" }
        loadingMore = false
    }

    fun toggleChildren(item: DisplayPost) {
        val post = item.post
        if (post.id in expandedPosts) {
            expandedPosts = expandedPosts - post.id
            return
        }
        if (childrenByPost.containsKey(post.id) || post.children != null) {
            expandedPosts = expandedPosts + post.id
            return
        }
        if (post.totalDescendantCount <= 0 || post.id in childLoading) return
        val detail = (state as? DetailState.Ready)?.detail ?: return
        childLoading = childLoading + post.id
        scope.launch {
            runCatching {
                DiscourseApi.nestedChildren(detail.slug, args.id, post.postNumber, item.depth + 1, sort = nestedSort)
            }.onSuccess { response ->
                childrenByPost = childrenByPost + (post.id to response.children)
                childPages = childPages + (post.id to response.page)
                childHasMore = childHasMore + (post.id to response.hasMore)
                expandedPosts = expandedPosts + post.id + initiallyExpanded(response.children)
            }.onFailure { loadError = it.message ?: "加载回复串失败" }
            childLoading = childLoading - post.id
        }
    }

    fun loadMoreChildren(item: DisplayPost) {
        val post = item.post
        if (post.id in childLoading || childHasMore[post.id] != true) return
        val detail = (state as? DetailState.Ready)?.detail ?: return
        childLoading = childLoading + post.id
        scope.launch {
            runCatching {
                DiscourseApi.nestedChildren(
                    detail.slug, args.id, post.postNumber, item.depth + 1,
                    page = (childPages[post.id] ?: 0) + 1, sort = nestedSort,
                )
            }.onSuccess { response ->
                val known = childrenByPost[post.id].orEmpty().associateBy { it.id }.toMutableMap()
                response.children.forEach { known[it.id] = it }
                childrenByPost = childrenByPost + (post.id to known.values.toList())
                childPages = childPages + (post.id to response.page)
                childHasMore = childHasMore + (post.id to response.hasMore)
                expandedPosts = expandedPosts + initiallyExpanded(response.children)
            }.onFailure { loadError = it.message ?: "加载更多回复失败" }
            childLoading = childLoading - post.id
        }
    }

    LaunchedEffect(args.id, reloadToken) {
        state = DetailState.Loading
        loggedIn = false
        posts = emptyList()
        streamIds = emptyList()
        opPost = null
        roots = emptyList()
        expandedPosts = emptySet()
        childrenByPost = emptyMap()
        childPages = emptyMap()
        childHasMore = emptyMap()
        childLoading = emptySet()
        loadingMore = false
        loadError = null
        launch { loggedIn = runCatching { DiscourseApi.hasActiveSession() }.getOrDefault(false) }
        try {
            val detail = DiscourseApi.topic(args.id)
            if (detail.isNestedView) {
                val nested = DiscourseApi.nestedTopic(detail.slug, args.id)
                state = DetailState.Ready(nested.topic, true)
                opPost = nested.opPost
                roots = nested.roots
                nestedPage = nested.page
                nestedSort = nested.effectiveSort
                hasMoreRoots = nested.hasMoreRoots
                expandedPosts = initiallyExpanded(nested.roots)
            } else {
                state = DetailState.Ready(detail, false)
                posts = detail.postStream.posts
                streamIds = detail.postStream.stream.ifEmpty { detail.postStream.posts.map { it.id } }
            }
        } catch (error: Throwable) {
            state = DetailState.Error(error.message ?: "网络错误")
        }
    }

    LaunchedEffect(listState, displayPosts.size, hasMoreRoots, canLoadMoreClassic) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }.collectLatest { index ->
            if (index >= displayPosts.size - 4) {
                if ((state as? DetailState.Ready)?.nested == true) loadMoreRoots() else loadMoreClassic()
            }
        }
    }

    var catName by remember(args.id) { mutableStateOf<String?>(null) }
    var catColor by remember(args.id) { mutableStateOf("#0088CC") }
    LaunchedEffect(args.categoryId) {
        SiteRepo.category(args.categoryId)?.let { catName = it.name; catColor = "#" + it.color }
    }

    Column(Modifier.fillMaxSize().background(nc.background)) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).background(nc.headerBg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = nc.onBackground)
            }
            Text(
                args.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = nc.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = {}) {
                Icon(Icons.Filled.MoreVert, "更多", tint = nc.onSurfaceVariant)
            }
        }
        HorizontalDivider(color = nc.outlineVariant)

        Row(
            Modifier.fillMaxWidth().background(nc.headerBg).padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(8.dp).background(hexColor(catColor), CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(catName ?: "", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = nc.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            (state as? DetailState.Ready)?.detail?.let { detail ->
                Text(
                    "${(detail.postsCount - 1).coerceAtLeast(0)} 回复 · ${detail.views} 浏览",
                    style = MaterialTheme.typography.labelSmall,
                    color = nc.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider(color = nc.outlineVariant)

        when (val current = state) {
            DetailState.Loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { LoadingMark() }
            is DetailState.Error -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(current.message, color = nc.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { reloadToken++ }) { Text("重试", color = nc.primary) }
                        TextButton(onClick = onBack) { Text("返回列表", color = nc.primary) }
                    }
                }
            }
            is DetailState.Ready -> LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                if (displayPosts.isEmpty()) {
                    item(key = "empty-posts") {
                        Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                            Text("暂无回复", color = nc.onSurfaceVariant)
                        }
                    }
                }
                items(displayPosts, key = { "post-${it.post.id}" }) { item ->
                    val post = item.post
                    val hasDescendants = post.totalDescendantCount > 0 || post.children.orEmpty().isNotEmpty() ||
                        childrenByPost[post.id].orEmpty().isNotEmpty()
                    PostItem(
                        item, current.nested, post.id in expandedPosts, hasDescendants,
                        post.id in childLoading, childHasMore[post.id] == true, loggedIn,
                        { toggleChildren(item) }, { loadMoreChildren(item) },
                    )
                    HorizontalDivider(color = nc.outlineVariant)
                }
                item(key = "pagination") {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                        if (loadingMore) Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = nc.primary, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        } else if ((current.nested && hasMoreRoots) || (!current.nested && canLoadMoreClassic)) {
                            TextButton(onClick = { scope.launch { if (current.nested) loadMoreRoots() else loadMoreClassic() } }) {
                                Text("继续加载回复", color = nc.primary)
                            }
                        } else Text("共 ${(current.detail.postsCount - 1).coerceAtLeast(0)} 条回复",
                            style = MaterialTheme.typography.labelSmall, color = nc.onSurfaceVariant)
                        loadError?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = nc.onSurfaceVariant) }
                    }
                }
            }
        }

        if (loggedIn) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 0.dp,
                modifier = Modifier.navigationBarsPadding().imePadding(),
            ) {
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
                        enabled = false,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = nc.primary,
                            contentColor = nc.onPrimary,
                        ),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "发送")
                    }
                }
            }
        }
    }
}

@Composable
private fun PostItem(
    item: DisplayPost,
    nestedMode: Boolean,
    expanded: Boolean,
    hasDescendants: Boolean,
    loadingChildren: Boolean,
    hasMoreChildren: Boolean,
    canReply: Boolean,
    onToggleChildren: () -> Unit,
    onLoadMoreChildren: () -> Unit,
) {
    val post = item.post
    val nc = LocalNodelocColors.current
    val likes = post.actionsSummary.firstOrNull { it.id == 2 }?.count ?: 0
    val depth = if (nestedMode) min(item.depth, 5) else 0
    val railSpacing = 14.dp
    val railColor = nc.outlineVariant
    val railModifier = Modifier.drawBehind {
        val spacing = railSpacing.toPx()
        repeat(depth) { level ->
            val x = spacing * level + spacing / 2f
            drawLine(railColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(nc.surface)
            .then(railModifier)
            .padding(start = 16.dp + railSpacing * depth, end = 16.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
            if (nestedMode && hasDescendants && expanded) {
                IconButton(onClick = onToggleChildren, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.KeyboardArrowUp, "收起", tint = nc.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(4.dp))
            }
            Avatar(post.username, SiteRepo.avatarUrl(post.avatarTemplate), if (depth > 0) 36.dp else 42.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(post.username, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = nc.onBackground)
                    post.staffBadge?.let { badge ->
                        Spacer(Modifier.width(7.dp))
                        Box(Modifier.background(nc.adminBadge, RoundedCornerShape(5.dp)).padding(horizontal = 6.dp, vertical = 1.5.dp)) {
                            Text(badge, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                                color = if (nc.background.luminance() > 0.5f) Color.White else Color(0xFF12100D))
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text(SiteRepo.relativeTime(post.createdAt), style = MaterialTheme.typography.labelSmall, color = nc.onSurfaceVariant)
                }
                Text(post.username + " · " + post.postNumber + " 楼", style = MaterialTheme.typography.labelSmall,
                    color = nc.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                CookedText(post.cooked, Modifier.padding(top = 7.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 9.dp)) {
                    Icon(Icons.Filled.FavoriteBorder, "点赞", tint = nc.onSurfaceVariant, modifier = Modifier.size(15.dp))
                    if (likes > 0) { Spacer(Modifier.width(4.dp)); Text(likes.toString(), style = MaterialTheme.typography.labelMedium, color = nc.onSurfaceVariant) }
                    if (canReply) { Spacer(Modifier.width(22.dp)); Text("回复", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = nc.onSurfaceVariant) }
                    Spacer(Modifier.width(22.dp)); Icon(Icons.Filled.Share, null, tint = nc.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp)); Text("分享", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = nc.onSurfaceVariant)
                }
            }
        }
        if (nestedMode && hasDescendants && !expanded) {
            Surface(onClick = onToggleChildren, shape = RoundedCornerShape(9.dp), color = nc.secondaryContainer.copy(alpha = 0.65f),
                modifier = Modifier.fillMaxWidth().padding(bottom = 7.dp)) {
                Row(Modifier.fillMaxWidth().heightIn(min = 44.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.KeyboardArrowDown, null, tint = nc.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("展开 ${post.totalDescendantCount.coerceAtLeast(post.directReplyCount)} 条回复",
                        style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = nc.primary)
                    if (loadingChildren) { Spacer(Modifier.width(8.dp)); CircularProgressIndicator(color = nc.primary, strokeWidth = 1.5.dp, modifier = Modifier.size(14.dp)) }
                }
            }
        }
        if (nestedMode && expanded && hasMoreChildren) {
            TextButton(onClick = onLoadMoreChildren, enabled = !loadingChildren) {
                Text(if (loadingChildren) "加载中…" else "加载更多此回复串", color = nc.primary)
            }
        }
    }
}
