package app.nodeloc.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.nodeloc.data.DiscourseApi
import app.nodeloc.data.SiteRepo
import app.nodeloc.data.model.CategoryDto
import app.nodeloc.data.model.TopicDto
import app.nodeloc.data.model.UserDto
import app.nodeloc.R
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

@Composable
fun TopicListScreen(onOpenTopic: (TopicDto) -> Unit) {
    val nc = LocalNodelocColors.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<ListState>(ListState.Loading) }
    var page by remember { mutableIntStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    var appending by remember { mutableStateOf(false) }

    suspend fun refresh() {
        state = ListState.Loading
        runCatching { DiscourseApi.latest(0) }
            .onSuccess { r ->
                page = 0
                hasMore = !r.topicList.moreTopicsUrl.isNullOrBlank()
                state = ListState.Ready(r.topicList.topics, r.users.associateBy { it.id }, SiteRepo.categories())
            }
            .onFailure { e -> state = ListState.Error(e.message ?: "网络错误") }
    }

    fun appendMore(current: ListState.Ready) {
        if (appending || !hasMore) return
        appending = true
        scope.launch {
            runCatching { DiscourseApi.latest(page + 1) }
                .onSuccess { r ->
                    page += 1
                    hasMore = !r.topicList.moreTopicsUrl.isNullOrBlank()
                    val known = current.topics.associateBy { it.id }.toMutableMap()
                    r.topicList.topics.forEach { known.putIfAbsent(it.id, it) }
                    state = current.copy(topics = known.values.toList(), users = current.users + r.users.associateBy { it.id })
                }
            appending = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    val ready = state as? ListState.Ready
    val listState = rememberLazyListState()
    LaunchedEffect(listState, ready?.topics?.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collect { idx ->
                val s = ready ?: return@collect
                if (hasMore && !appending && idx >= s.topics.size - 4) appendMore(s)
            }
    }

    Column(Modifier.fillMaxSize().background(nc.background)) {
        // ── 顶栏(A 方向:标题 + 图标) ──
        Row(
            Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.nodeloc_logo),
                contentDescription = "NodeLoc",
                modifier = Modifier.height(30.dp),
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {}) { Icon(Icons.Filled.Search, null, tint = nc.onSurfaceVariant) }
            IconButton(onClick = {}) { Icon(Icons.Filled.Notifications, null, tint = nc.onSurfaceVariant) }
        }

        when (val s = state) {
            is ListState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingMark()
            }
            is ListState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.message, color = nc.onSurfaceVariant)
                    TextButton(onClick = { scope.launch { refresh() } }) { Text("重试", color = nc.primary) }
                }
            }
            is ListState.Ready -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                items(s.topics, key = { it.id }) { t ->
                    TopicRow(
                        t = t,
                        op = t.posters.firstOrNull()?.user_id?.let { s.users[it] },
                        cat = s.cats[t.categoryId],
                        onClick = { onOpenTopic(t) },
                    )
                }
                if (appending) {
                    item(key = "footer") {
                        Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = nc.primary, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        }
                    }
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
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 12.dp)) {
            Avatar(name = op?.username ?: "?", url = SiteRepo.avatarUrl(op?.avatarTemplate), size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (t.isPinned) {
                        Box(
                            Modifier.background(Color(0xFFF1592A), RoundedCornerShape(6.dp)).padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text("置顶", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(Modifier.width(7.dp))
                    }
                    Text(
                        t.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = nc.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    cat?.let { c ->
                        Box(Modifier.size(8.dp).background(hexColor(c.color), CircleShape))
                        Spacer(Modifier.width(5.dp))
                        Text(c.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = nc.onSurfaceVariant)
                        Spacer(Modifier.width(10.dp))
                    }
                    Text((t.postsCount - 1).coerceAtLeast(0).toString() + " 回复", style = MaterialTheme.typography.labelMedium, color = nc.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    Text(SiteRepo.relativeTime(t.bumpedAt), style = MaterialTheme.typography.labelMedium, color = nc.onSurfaceVariant)
                }
            }
        }
    }
}