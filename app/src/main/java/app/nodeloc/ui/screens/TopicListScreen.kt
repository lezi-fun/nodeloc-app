package app.nodeloc.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
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
import app.nodeloc.data.SiteRepo
import app.nodeloc.data.model.CategoryDto
import app.nodeloc.data.model.TopicDto
import app.nodeloc.data.model.UserDto
import app.nodeloc.ui.components.Avatar
import app.nodeloc.ui.components.LoadingMark
import app.nodeloc.ui.theme.LocalNodelocColors
import app.nodeloc.util.hexColor
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

private data class TopicFilter(val name: String, val color: Color? = null)

private val topicFilters = listOf(
    TopicFilter("全部"),
    TopicFilter("互联网服务", Color(0xFF2CB2B5)),
    TopicFilter("VPS", Color(0xFFE45735)),
    TopicFilter("AI", Color(0xFF0088CC)),
    TopicFilter("羊毛党", Color(0xFF0088CC)),
    TopicFilter("数码与硬件", Color(0xFF3184C4)),
    TopicFilter("优惠情报", Color(0xFFDD3C3C)),
)

@Composable
fun TopicListScreen(
    onOpenTopic: (TopicDto) -> Unit,
    onOpenDrawer: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
) {
    val nc = LocalNodelocColors.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<ListState>(ListState.Loading) }
    var page by remember { mutableIntStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    var appending by remember { mutableStateOf(false) }
    var appendError by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableIntStateOf(0) }

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

    val ready = state as? ListState.Ready
    val visibleTopics = remember(ready, selectedFilter) {
        ready?.let { current ->
            val filterName = topicFilters.getOrNull(selectedFilter)?.name
            if (filterName.isNullOrBlank() || filterName == "全部") current.topics
            else current.topics.filter { topic ->
                current.cats[topic.categoryId]?.name == filterName || topic.title.contains(filterName, ignoreCase = true)
            }
        } ?: emptyList()
    }
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

    Column(Modifier.fillMaxSize().background(nc.background)) {
        OfficialTopBar(onOpenDrawer = onOpenDrawer, onOpenSearch = onOpenSearch)
        FilterStrip(selectedFilter = selectedFilter, onSelect = { selectedFilter = it })

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
                    Text(if (s.topics.isEmpty()) "暂无话题" else "该筛选暂无话题", color = nc.onSurfaceVariant)
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
}

@Composable
private fun OfficialTopBar(onOpenDrawer: () -> Unit, onOpenSearch: () -> Unit) {
    val nc = LocalNodelocColors.current
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
        IconButton(onClick = onOpenSearch) {
            Icon(Icons.Filled.Search, contentDescription = "搜索", tint = nc.onSurfaceVariant)
        }
        IconButton(onClick = {}) {
            Icon(Icons.Filled.Notifications, contentDescription = "通知", tint = nc.onSurfaceVariant)
        }
    }
}

@Composable
private fun FilterStrip(selectedFilter: Int, onSelect: (Int) -> Unit) {
    val nc = LocalNodelocColors.current
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        topicFilters.forEachIndexed { index, filter ->
            Surface(
                onClick = { onSelect(index) },
                shape = RoundedCornerShape(999.dp),
                color = if (index == selectedFilter) nc.secondaryContainer else nc.surface,
                tonalElevation = if (index == selectedFilter) 0.dp else 1.dp,
            ) {
                Row(
                    Modifier.height(34.dp).padding(horizontal = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    filter.color?.let { Box(Modifier.size(8.dp).background(it, CircleShape)) }
                    Text(
                        filter.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (index == selectedFilter) FontWeight.Bold else FontWeight.Medium,
                        color = if (index == selectedFilter) nc.onSecondaryContainer else nc.onSurfaceVariant,
                        maxLines = 1,
                    )
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
                    if (t.isPinned) {
                        Text(
                            "置顶",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1592A),
                            modifier = Modifier.padding(end = 7.dp),
                        )
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
                        Box(Modifier.size(8.dp).background(hexColor(c.color), CircleShape))
                        Spacer(Modifier.width(5.dp))
                        Text(c.name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = nc.onSurfaceVariant)
                        Spacer(Modifier.width(9.dp))
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
