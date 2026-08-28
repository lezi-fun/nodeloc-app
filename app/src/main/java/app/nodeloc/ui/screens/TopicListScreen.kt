package app.nodeloc.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.nodeloc.R
import app.nodeloc.data.DiscourseApi
import app.nodeloc.data.SessionRepo
import app.nodeloc.data.SiteRepo
import app.nodeloc.data.model.CategoryDto
import app.nodeloc.data.model.TopicDto
import app.nodeloc.data.model.UserDto
import app.nodeloc.ui.components.Avatar
import app.nodeloc.ui.components.CategoryDot
import app.nodeloc.ui.components.LoadingMark
import app.nodeloc.ui.components.TagChip
import app.nodeloc.ui.theme.LocalNodelocColors
import app.nodeloc.util.hexColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed interface ListState {
    data object Loading : ListState
    data class Error(val message: String) : ListState
    data class Ready(
        val topics: List<TopicDto>,
        val users: Map<Int, UserDto>,
        val cats: Map<Int, CategoryDto>,
    ) : ListState
}

@Composable
fun TopicListScreen(
    onOpenTopic: (TopicDto) -> Unit,
    onOpenDrawer: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onOpenLogin: () -> Unit = {},
    onOpenCreateTopic: () -> Unit = {},
) {
    val nc = LocalNodelocColors.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<ListState>(ListState.Loading) }
    var page by remember { mutableIntStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    var appending by remember { mutableStateOf(false) }
    var appendError by remember { mutableStateOf<String?>(null) }
    // 官网行为:停留期间定期检查,有新话题时顶部出现绿色横幅
    var newCount by remember { mutableIntStateOf(0) }

    suspend fun refresh() {
        state = ListState.Loading
        appendError = null
        runCatching { DiscourseApi.latest(0) }
            .onSuccess { r ->
                page = 0
                hasMore = !r.topicList.moreTopicsUrl.isNullOrBlank()
                state = ListState.Ready(
                    r.topicList.topics,
                    r.users.associateBy { it.id },
                    SiteRepo.categories(),
                )
            }
            .onFailure { e -> state = ListState.Error(e.message ?: "网络错误") }
    }

    fun appendMore(current: ListState.Ready) {
        if (appending || !hasMore) return
        appending = true
        appendError = null
        scope.launch {
            runCatching { DiscourseApi.latest(page + 1) }
                .onSuccess { r ->
                    page += 1
                    hasMore = !r.topicList.moreTopicsUrl.isNullOrBlank()
                    val known = current.topics.associateBy { it.id }.toMutableMap()
                    r.topicList.topics.forEach { known.putIfAbsent(it.id, it) }
                    state = current.copy(
                        topics = known.values.toList(),
                        users = current.users + r.users.associateBy { it.id },
                    )
                }
                .onFailure { e -> appendError = e.message ?: "加载更多失败" }
            appending = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    // 每 60s 拉第一页对比,发现新话题则显示横幅;点击横幅后静默刷新
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            runCatching { DiscourseApi.latest(0) }.onSuccess { r ->
                val cur = state as? ListState.Ready ?: return@onSuccess
                val known = cur.topics.asSequence().map { it.id }.toHashSet()
                val fresh = r.topicList.topics.count { it.id !in known }
                if (fresh > 0) newCount = fresh
            }
        }
    }

    val ready = state as? ListState.Ready
    val visibleTopics = ready?.topics ?: emptyList()
    val listState = rememberLazyListState()
    // 基于真实渲染条数(含筛选与 footer)判断接近底部;appending 结束后若仍在底部会自动续载
    val nearEnd by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            info.totalItemsCount > 0 && lastVisible >= info.totalItemsCount - 5
        }
    }
    LaunchedEffect(nearEnd, ready, hasMore, appending, appendError) {
        // appendError 非空时停下等待手动重试,避免失败后无限循环请求
        if (nearEnd && hasMore && !appending && appendError == null) ready?.let(::appendMore)
    }

    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize().background(nc.background)) {
        OfficialTopBar(onOpenDrawer = onOpenDrawer, onOpenSearch = onOpenSearch, onOpenLogin = onOpenLogin)
        // 官网风格新话题横幅:浅绿底品牌绿字,点击静默刷新并回顶
        if (newCount > 0) {
            Surface(
                onClick = {
                    newCount = 0
                    scope.launch {
                        runCatching { DiscourseApi.latest(0) }.onSuccess { r ->
                            page = 0
                            hasMore = !r.topicList.moreTopicsUrl.isNullOrBlank()
                            val cur = state as? ListState.Ready
                            state = if (cur != null) {
                                cur.copy(topics = r.topicList.topics, users = r.users.associateBy { it.id })
                            } else {
                                ListState.Ready(r.topicList.topics, r.users.associateBy { it.id }, SiteRepo.categories())
                            }
                            listState.scrollToItem(0)
                        }
                    }
                },
                color = nc.primary.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(Modifier.fillMaxWidth().padding(vertical = 7.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "查看 $newCount 个新的或更新的话题",
                        color = nc.primary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        when (val s = state) {
            is ListState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingMark()
            }
            is ListState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.message, color = nc.onSurfaceVariant)
                    TextButton(onClick = { scope.launch { refresh() } }) {
                        Text("重试", color = nc.primary)
                    }
                }
            }
            is ListState.Ready -> if (visibleTopics.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无话题", color = nc.onSurfaceVariant)
                }
            } else LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                items(visibleTopics, key = { it.id }) { t ->
                    TopicRow(
                        t = t,
                        op = t.posters.firstOrNull()?.user_id?.let { s.users[it] },
                        cat = s.cats[t.categoryId],
                        onClick = { onOpenTopic(t) },
                    )
                }
                item(key = "footer") {
                    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (appending) {
                            CircularProgressIndicator(
                                color = nc.primary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp),
                            )
                        } else if (!appendError.isNullOrBlank()) {
                            Text(appendError!!, style = MaterialTheme.typography.labelSmall, color = nc.onSurfaceVariant)
                            TextButton(onClick = { appendError = null; appendMore(s) }) { Text("重试加载", color = nc.primary) }
                        } else if (hasMore) {
                            TextButton(onClick = { appendMore(s) }) { Text("继续加载", color = nc.primary) }
                        } else {
                            Text("已经到底了", style = MaterialTheme.typography.labelSmall, color = nc.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
        // 官网移动端"新建话题"是绿色圆角胶囊按钮而非悬浮 FAB,
        // 但用户要求的是右下角常驻绿色圆形加号,故这里用 FAB 形式还原官网配色。
        val me = SessionRepo.currentUser.collectAsState().value
        if (me != null) {
            Surface(
                onClick = onOpenCreateTopic,
                shape = CircleShape,
                color = Color(0xFF009966),
                shadowElevation = 4.dp,
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp).size(56.dp),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Add, contentDescription = "新建话题", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
private fun OfficialTopBar(onOpenDrawer: () -> Unit, onOpenSearch: () -> Unit, onOpenLogin: () -> Unit) {
    val nc = LocalNodelocColors.current
    val me = SessionRepo.currentUser.collectAsState().value
    Row(
        Modifier.fillMaxWidth().height(56.dp).background(nc.headerBg).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onOpenDrawer) {
            Icon(Icons.Filled.Menu, contentDescription = "导航菜单", tint = nc.onSurfaceVariant)
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.nodeloc_logo),
                contentDescription = "NodeLoc",
                modifier = Modifier.height(23.dp),
            )
        }
        if (me != null) {
            // 官网行为:签到状态无独立接口,纯前端 localStorage 记忆;
            // 暂不接后端调用,仅还原图标位置与形态
            IconButton(onClick = {}) {
                Icon(
                    painter = painterResource(R.drawable.ic_calendar_heart),
                    contentDescription = "每日签到",
                    tint = nc.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        IconButton(onClick = onOpenSearch) {
            Icon(Icons.Filled.Search, contentDescription = "搜索", tint = nc.onSurfaceVariant)
        }
        if (me != null) {
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Notifications, contentDescription = "通知", tint = nc.onSurfaceVariant)
            }
            Spacer(Modifier.width(2.dp))
            Avatar(name = me.username, url = SiteRepo.avatarUrl(me.avatarTemplate, 48), size = 28.dp)
            Spacer(Modifier.width(4.dp))
        } else {
            Spacer(Modifier.width(4.dp))
            Surface(
                onClick = onOpenLogin,
                shape = RoundedCornerShape(999.dp),
                color = nc.primary,
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("登录", color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun TopicRow(t: TopicDto, op: UserDto?, cat: CategoryDto?, onClick: () -> Unit) {
    val nc = LocalNodelocColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        color = nc.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Avatar(name = op?.username ?: "?", url = SiteRepo.avatarUrl(op?.avatarTemplate), size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 与官网一致:置顶显示图钉,信任等级限制显示锁
                    if (t.isPinned) {
                        Icon(
                            painter = painterResource(R.drawable.ic_pin),
                            contentDescription = "置顶",
                            tint = Color(0xFFF1592A),
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(3.dp))
                    }
                    if (t.hasReadPermissionRestriction) {
                        // 官网 lc-lock 线框锁:未登录统一金色 #FF9800;
                        // 登录后当前用户等级达标为绿色 #4CAF50,不达标仍为金色
                        val me = SessionRepo.currentUser.collectAsState().value
                        val allowed = me != null && t.readPermissionTrustLevel != null &&
                            me.trustLevel >= t.readPermissionTrustLevel!!
                        Icon(
                            painter = painterResource(R.drawable.ic_lock_lc),
                            contentDescription = if (allowed) "信任等级达标" else "需要更高信任等级",
                            tint = if (allowed) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(3.dp))
                    }
                    Text(
                        t.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal,
                        color = nc.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    cat?.let { c ->
                        CategoryDot(c, size = 13.dp)
                        Spacer(Modifier.width(5.dp))
                        Text(c.name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = nc.onSurfaceVariant)
                        Spacer(Modifier.width(9.dp))
                    }
                    t.tags.forEach { tag ->
                        TagChip(tag.name)
                        Spacer(Modifier.width(5.dp))
                    }
                    Text(
                        (t.postsCount - 1).coerceAtLeast(0).toString() + " 回复",
                        style = MaterialTheme.typography.labelSmall,
                        color = nc.onSurfaceVariant,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(SiteRepo.relativeTime(t.bumpedAt), style = MaterialTheme.typography.labelSmall, color = nc.onSurfaceVariant)
                }
            }
        }
    }
}
