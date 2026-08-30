package app.nodeloc.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.nodeloc.data.ApiException
import app.nodeloc.data.DiscourseApi
import app.nodeloc.data.SiteRepo
import app.nodeloc.data.model.PostDto
import app.nodeloc.data.model.TopicDetailDto
import app.nodeloc.ui.DetailArgs
import app.nodeloc.ui.components.Avatar
import app.nodeloc.ui.components.BadgeTitleText
import app.nodeloc.ui.components.CategoryDot
import app.nodeloc.ui.components.CookedText
import app.nodeloc.ui.components.LoadingMark
import app.nodeloc.ui.components.EditPostDialog
import app.nodeloc.ui.components.GifSearchSheet
import app.nodeloc.ui.components.LotteryCard
import app.nodeloc.ui.components.MarkdownAction
import app.nodeloc.ui.components.MarkdownEditingActions
import app.nodeloc.ui.components.MarkdownToolbar
import app.nodeloc.ui.components.PostActionsSheet
import app.nodeloc.ui.components.ReactionButton
import app.nodeloc.ui.components.RewardDialog
import app.nodeloc.ui.components.TagChip
import app.nodeloc.ui.theme.LocalNodelocColors
import app.nodeloc.util.hexColor
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.nodeloc.data.TopicScreenTracker
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.min

private sealed interface DetailState {
    data object Loading : DetailState
    /** [code] 为 0 表示非 HTTP 语义错误(网络中断等) */
    data class Error(val code: Int, val message: String) : DetailState
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
fun TopicDetailScreen(
    args: DetailArgs,
    onBack: () -> Unit,
    onOpenLogin: () -> Unit = {},
    onOpenProfile: (String) -> Unit = {},
) {
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

    // MessageBus: 订阅该话题的实时更新
    val messageBus = app.nodeloc.data.LocalMessageBus.current
    DisposableEffect(args.id) {
        val channel = "/topic/${args.id}"
        messageBus?.subscribe(channel) { msg ->
            // 收到话题更新消息时触发重新加载
            reloadToken++
        }

        onDispose {
            messageBus?.unsubscribe(channel)
        }
    }

    // 称号动效样式表:全站共用,进程内已缓存,这里只是拿一份引用给各楼层匹配称号颜色
    var badgeStyles by remember { mutableStateOf<List<app.nodeloc.data.model.BadgeStyleDto>>(emptyList()) }
    LaunchedEffect(Unit) {
        badgeStyles = runCatching { DiscourseApi.badgeStyles() }.getOrDefault(emptyList())
    }

    // 底部回复栏:Markdown 编辑器(工具栏+文本框)、发送中与发送失败文案
    val replyFocusRequester = remember(args.id) { FocusRequester() }
    var replyField by remember(args.id) { mutableStateOf(TextFieldValue("")) }
    var replyToPostNumber by remember(args.id) { mutableIntStateOf(0) }
    var sending by remember(args.id) { mutableStateOf(false) }
    var replyError by remember(args.id) { mutableStateOf<String?>(null) }
    var gifSheetOpen by remember(args.id) { mutableStateOf(false) }
    var emojiSheetOpen by remember(args.id) { mutableStateOf(false) }
    var previewMode by remember(args.id) { mutableStateOf(false) }
    var previewHtml by remember(args.id) { mutableStateOf("") }
    var uploading by remember(args.id) { mutableStateOf(false) }
    val context = LocalContext.current
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            uploading = true
            runCatching { app.nodeloc.ui.components.uploadEditorFile(context, uri) }
                .onSuccess { url -> replyField = MarkdownEditingActions.insertAttachment(replyField, url) }
                .onFailure { replyError = it.message ?: "上传失败，请稍后再试" }
            uploading = false
        }
    }

    /** 官网点击楼层"回复"按钮的行为:设置 reply_to_post_number 并聚焦编辑器，不插入 @ */
    fun replyToUser(username: String, postNumber: Int) {
        replyToPostNumber = postNumber
        runCatching { replyFocusRequester.requestFocus() }
    }

    fun sendReply() {
        val text = replyField.text.trim()
        if (text.isEmpty() || sending) return
        sending = true
        replyError = null
        scope.launch {
            runCatching { DiscourseApi.createPost(args.id, text, replyToPostNumber.takeIf { it > 0 }) }
                .onSuccess {
                    replyField = TextFieldValue("")
                    replyToPostNumber = 0
                    // 刷新楼层以显示新回复;保留登录态避免输入栏闪烁
                    reloadToken++
                }
                .onFailure { replyError = it.message ?: "发送失败，请稍后再试" }
            sending = false
        }
    }

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
        runCatching { DiscourseApi.nestedTopicPage(detail.slug, args.id, nestedPage + 1, nestedSort) }.onSuccess { response ->
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

    // 反应切换后用服务端返回的最新 PostDto 替换本地缓存;帖子可能分布在四处不同结构里(经典流/嵌套树的根与子层)
    fun updatePost(updated: PostDto) {
        posts = posts.map { if (it.id == updated.id) updated else it }
        if (opPost?.id == updated.id) opPost = updated
        roots = roots.map { if (it.id == updated.id) updated else it }
        childrenByPost = childrenByPost.mapValues { (_, children) ->
            children.map { if (it.id == updated.id) updated else it }
        }
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
            state = DetailState.Error((error as? ApiException)?.code ?: 0, error.message ?: "网络错误")
        }
    }

    LaunchedEffect(listState, displayPosts.size, hasMoreRoots, canLoadMoreClassic) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }.collectLatest { index ->
            if (index >= displayPosts.size - 4) {
                if ((state as? DetailState.Ready)?.nested == true) loadMoreRoots() else loadMoreClassic()
            }
        }
    }

    // 阅读进度上报(screen-track):按 post_id 前缀从可见 LazyColumn item key 里解析出楼层号,
    // 每秒累加,满足条件时上报 POST /topics/timings。仅登录用户需要,官网匿名用户走本地缓存不上报。
    val postNumberById = remember(displayPosts) { displayPosts.associate { it.post.id to it.post.postNumber } }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(args.id, loggedIn) {
        if (!loggedIn) return@DisposableEffect onDispose {}
        val tracker = TopicScreenTracker(args.id)
        var tickJob: Job? = null

        fun visiblePostNumbers(): Set<Int> =
            listState.layoutInfo.visibleItemsInfo.mapNotNullTo(mutableSetOf()) { info ->
                val key = info.key as? String
                val postId = key?.removePrefix("post-")?.toLongOrNull()
                postId?.let { postNumberById[it] }
            }

        suspend fun flushNow() {
            val pending = tracker.drainPending() ?: return
            runCatching { DiscourseApi.postTopicTimings(args.id, pending.timingsMs, pending.topicTimeMs) }
                .onFailure { tracker.restore(pending) }
        }

        tickJob = scope.launch {
            while (true) {
                delay(1000)
                if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    tracker.tick(1000, visiblePostNumbers())
                    if (tracker.shouldFlush()) flushNow()
                }
            }
        }
        onDispose {
            tickJob?.cancel()
            // 离开详情页前做最后一次上报:Composable 已在销毁过程中,scope 即将被取消,
            // 这里特意脱离它的生命周期,确保这次收尾请求不会被半路打断。
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch { flushNow() }
        }
    }

    var category by remember(args.id) { mutableStateOf<app.nodeloc.data.model.CategoryDto?>(null) }
    LaunchedEffect(args.categoryId) {
        category = SiteRepo.category(args.categoryId)
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
                (state as? DetailState.Ready)?.detail?.title?.takeIf { it.isNotBlank() } ?: args.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = nc.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
        HorizontalDivider(color = nc.outlineVariant)

        Row(
            Modifier.fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .background(nc.headerBg)
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryDot(category, size = 14.dp)
            Spacer(Modifier.width(6.dp))
            Text(category?.name ?: "", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = nc.onSurfaceVariant)
            (state as? DetailState.Ready)?.detail?.tags.orEmpty().forEach { tag ->
                Spacer(Modifier.width(7.dp))
                TagChip(tag.name)
            }
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 32.dp),
                ) {
                    when {
                        // 无权访问(含未登录):对齐官网 forbidden 页的锁图标与文案层级
                        current.code == 403 || current.code == 401 -> {
                            Box(
                                Modifier.size(76.dp).background(nc.secondaryContainer, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = nc.primary, modifier = Modifier.size(32.dp))
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                if (current.code == 401) "请先登录" else "无权访问该话题",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = nc.onBackground,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                current.message + "\n该话题可能需要登录或更高的会员等级才能查看",
                                style = MaterialTheme.typography.bodySmall,
                                color = nc.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(20.dp))
                            TextButton(onClick = onBack) { Text("返回列表", color = nc.primary) }
                        }
                        // 不存在或已删除:对齐官网 not_found 页文案
                        current.code == 404 -> {
                            Box(
                                Modifier.size(76.dp).background(nc.surfaceVariant, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.Info, contentDescription = null, tint = nc.onSurfaceVariant, modifier = Modifier.size(30.dp))
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "找不到页面",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = nc.onBackground,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "该话题不存在、已被删除或链接有误",
                                style = MaterialTheme.typography.bodySmall,
                                color = nc.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(20.dp))
                            TextButton(onClick = onBack) { Text("返回列表", color = nc.primary) }
                        }
                        else -> {
                            Text(current.message, color = nc.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { reloadToken++ }) { Text("重试", color = nc.primary) }
                                TextButton(onClick = onBack) { Text("返回列表", color = nc.primary) }
                            }
                        }
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
                        onReactionUpdated = ::updatePost,
                        onOpenProfile = onOpenProfile,
                        badgeStyles = badgeStyles,
                        topicSlug = current.detail.slug,
                        onPostDeleted = { reloadToken++ },
                        onReply = ::replyToUser,
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

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.dp,
            modifier = Modifier.navigationBarsPadding().imePadding(),
        ) {
            if (loggedIn) {
                Column(Modifier.fillMaxWidth()) {
                    replyError?.let { msg ->
                        Text(
                            msg,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                        )
                    }
                    MarkdownToolbar(
                        onAction = { action ->
                            when (action) {
                                MarkdownAction.Gif -> gifSheetOpen = true
                                MarkdownAction.Emoji -> emojiSheetOpen = true
                                MarkdownAction.TogglePreview -> {
                                    if (!previewMode) {
                                        scope.launch {
                                            runCatching { DiscourseApi.previewPost(replyField.text) }
                                                .onSuccess { previewHtml = it.cooked; previewMode = true }
                                                .onFailure { replyError = it.message ?: "预览失败，请稍后再试" }
                                        }
                                    } else previewMode = false
                                }
                                MarkdownAction.Attachment -> filePicker.launch("*/*")
                                else -> replyField = MarkdownEditingActions.apply(action, replyField)
                            }
                        },
                    )
                    if (uploading) {
                        Text("正在上传…", style = MaterialTheme.typography.labelSmall, color = nc.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
                    }
                    if (previewMode) {
                        CookedText(previewHtml, Modifier.fillMaxWidth().padding(16.dp))
                    } else Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
                        // 显示正在回复的提示
                        if (replyToPostNumber > 0) {
                            val replyingToPost = postFlow?.value?.posts?.find { it.postNumber == replyToPostNumber }
                            replyingToPost?.let { post ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "正在回复 @${post.username}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = nc.primary
                                    )
                                    Spacer(Modifier.weight(1f))
                                    IconButton(
                                        onClick = { replyToPostNumber = 0 },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "取消回复",
                                            tint = nc.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                        OutlinedTextField(
                            value = replyField,
                            onValueChange = { replyField = it },
                            placeholder = { Text("回复此话题…使用 Markdown 排版", color = nc.onSurfaceVariant) },
                            shape = RoundedCornerShape(21.dp),
                            maxLines = 8,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = nc.outlineVariant,
                                focusedBorderColor = nc.primary,
                            ),
                            modifier = Modifier.weight(1f).heightIn(min = 42.dp).focusRequester(replyFocusRequester),
                        )
                        Spacer(Modifier.width(10.dp))
                        FilledIconButton(
                            onClick = ::sendReply,
                            enabled = !sending && replyField.text.isNotBlank(),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = nc.primary,
                                contentColor = nc.onPrimary,
                            ),
                            modifier = Modifier.padding(bottom = 2.dp),
                        ) {
                            if (sending) {
                                CircularProgressIndicator(
                                    color = nc.onPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp),
                                )
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Send, "发送")
                            }
                        }
                        }
                    }
                    if (emojiSheetOpen) {
                        app.nodeloc.ui.components.EmojiPickerSheet(
                            onDismiss = { emojiSheetOpen = false },
                            onPick = { emoji -> replyField = MarkdownEditingActions.insertEmoji(replyField, emoji); emojiSheetOpen = false },
                        )
                    }
                    if (gifSheetOpen) {
                        GifSearchSheet(
                            onDismiss = { gifSheetOpen = false },
                            onPick = { gif ->
                                replyField = MarkdownEditingActions.insertGif(replyField, gif)
                                gifSheetOpen = false
                            },
                        )
                    }
                }
            } else {
                // 未登录:整条回复栏作为登录引导
                Surface(
                    onClick = onOpenLogin,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Lock, null, tint = nc.primary, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "登录后即可回复",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = nc.primary,
                        )
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
    onReactionUpdated: (PostDto) -> Unit,
    onOpenProfile: (String) -> Unit,
    badgeStyles: List<app.nodeloc.data.model.BadgeStyleDto>,
    topicSlug: String,
    onPostDeleted: () -> Unit,
    onReply: (String, Int) -> Unit,
) {
    val post = item.post
    val nc = LocalNodelocColors.current
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
            // 官网行为:帖子流头像为动图(_2.gif),列表等位置为静态(_2.png)
            Avatar(
                post.username,
                SiteRepo.animatedAvatarUrl(post.avatarTemplate),
                if (depth > 0) 36.dp else 42.dp,
                modifier = Modifier.clickable { onOpenProfile(post.username) },
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        post.username,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = nc.onBackground,
                        modifier = Modifier.clickable { onOpenProfile(post.username) },
                    )
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
                post.userTitle?.takeIf { it.isNotBlank() }?.let { title ->
                    val badgeStyle = badgeStyles.firstOrNull { it.name == title }
                    BadgeTitleText(title, badgeStyle, nc.onSurfaceVariant, modifier = Modifier.padding(top = 1.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Text(post.username + " · " + post.postNumber + " 楼", style = MaterialTheme.typography.labelSmall,
                        color = nc.onSurfaceVariant)
                    post.mobileSource?.takeIf { it.isNotBlank() }?.let { device ->
                        Spacer(Modifier.width(6.dp))
                        Box(
                            Modifier
                                .background(nc.surfaceVariant, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("来自 $device", style = MaterialTheme.typography.labelSmall, color = nc.onSurfaceVariant)
                        }
                    }
                }
                // 内容本地化:cooked 默认已是站点语言译文;is_localized 时提供"查看原文"入口,
                // 点击一次性切到原文并隐藏提示条(与官网行为一致,不提供切回译文)。
                var showingOriginal by remember(post.id) { mutableStateOf(false) }
                var originalCooked by remember(post.id) { mutableStateOf<String?>(null) }
                val scope = rememberCoroutineScope()
                if (post.isLocalized && post.locale != null && !showingOriginal) {
                    Surface(
                        onClick = {
                            scope.launch {
                                runCatching { DiscourseApi.postOriginalCooked(post.id) }
                                    .onSuccess { cooked -> originalCooked = cooked; showingOriginal = true }
                            }
                        },
                        color = nc.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    ) {
                        Column(Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                            Text(
                                "此帖子最初使用 " + app.nodeloc.util.LanguageNames.displayName(post.locale) + " 编写，点击查看原文",
                                style = MaterialTheme.typography.labelSmall,
                                color = nc.onSurfaceVariant,
                            )
                            Text(
                                "AI 生成的译文可能不准确。",
                                style = MaterialTheme.typography.labelSmall,
                                color = nc.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }
                    }
                }
                CookedText(
                    originalCooked ?: post.cooked,
                    Modifier.padding(top = 7.dp),
                    topicReferer = DiscourseApi.BASE + "/t/" + post.topicId,
                )
                post.lottery?.let { lottery ->
                    LotteryCard(lottery, Modifier.padding(top = 10.dp))
                }
                if (post.rewards.isNotEmpty()) {
                    RewardBubbleRow(post.rewards, Modifier.padding(top = 8.dp))
                }
                var rewardOpen by remember(post.id) { mutableStateOf(false) }
                var editOpen by remember(post.id) { mutableStateOf(false) }
                var actionsOpen by remember(post.id) { mutableStateOf(false) }
                // 使用 key 确保点赞后重组时按钮不会消失
                key(post.id, canReply) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 9.dp)) {
                        if (canReply) {
                            IconButton(onClick = { rewardOpen = true }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Filled.Bolt, "打赏", tint = nc.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                        }
                        ReactionButton(post, canReact = canReply, onUpdated = onReactionUpdated)
                        if (canReply) {
                            Spacer(Modifier.width(22.dp))
                            IconButton(onClick = { onReply(post.username, post.postNumber) }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.AutoMirrored.Filled.Reply, "回复", tint = nc.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(Modifier.width(22.dp))
                        IconButton(onClick = { actionsOpen = true }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Filled.MoreHoriz, "更多操作", tint = nc.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                if (actionsOpen) {
                    PostActionsSheet(
                        post = post,
                        topicSlug = topicSlug,
                        onDismiss = { actionsOpen = false },
                        onEdit = { editOpen = true },
                        onUpdated = onReactionUpdated,
                        onNeedsReload = { actionsOpen = false; onPostDeleted() },
                    )
                }
                if (rewardOpen) {
                    RewardDialog(
                        targetUsername = post.username,
                        postId = post.id,
                        onDismiss = { rewardOpen = false },
                        onRewarded = { reward ->
                            rewardOpen = false
                            onReactionUpdated(post.copy(rewards = listOf(reward) + post.rewards))
                        },
                    )
                }
                if (editOpen) {
                    EditPostDialog(
                        post = post,
                        onDismiss = { editOpen = false },
                        onEdited = { updated ->
                            editOpen = false
                            onReactionUpdated(updated)
                        },
                    )
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

/**
 * 官网 discourse-rewards 的打赏气泡列表(discourse-rewards__list):每个打赏者一个独立气泡,
 * 头像 + 留言(若有) + 金额,横向自动换行排列,不是汇总成一句"已获得 x 能量"的文案。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RewardBubbleRow(rewards: List<app.nodeloc.data.model.RewardDto>, modifier: Modifier = Modifier) {
    val nc = LocalNodelocColors.current
    FlowRow(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rewards.forEach { reward ->
            Row(
                Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(nc.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(reward.username, SiteRepo.avatarUrl(reward.avatarTemplate, 48), size = 20.dp)
                reward.note?.takeIf { it.isNotBlank() }?.let { note ->
                    Spacer(Modifier.width(5.dp))
                    Text(note, style = MaterialTheme.typography.labelSmall, color = nc.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.width(5.dp))
                Text(
                    (if (reward.isDeduct) "-" else "+") + reward.amount,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (reward.isDeduct) MaterialTheme.colorScheme.error else nc.onSurfaceVariant,
                )
                Spacer(Modifier.width(2.dp))
                Icon(Icons.Filled.Bolt, null, tint = nc.onSurfaceVariant, modifier = Modifier.size(13.dp))
            }
        }
    }
}
