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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.nodeloc.data.ApiException
import app.nodeloc.data.DiscourseApi
import app.nodeloc.data.SiteRepo
import app.nodeloc.data.model.CategoryDto
import app.nodeloc.data.model.SearchPostDto
import app.nodeloc.data.model.SearchDto
import app.nodeloc.data.model.TopicDto
import app.nodeloc.ui.components.LoadingMark
import app.nodeloc.ui.theme.LocalNodelocColors
import app.nodeloc.util.hexColor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

private sealed interface SearchState {
    data object Idle : SearchState
    data object Loading : SearchState
    data class Ready(val result: SearchDto, val blurbs: Map<Long, SearchPostDto>) : SearchState
    data class Failed(val code: Int, val message: String) : SearchState
}

@Composable
fun SearchScreen(onBack: () -> Unit, onOpenTopic: (TopicDto) -> Unit) {
    val nc = LocalNodelocColors.current
    val focusRequester = remember { FocusRequester() }
    var query by rememberSaveable { mutableStateOf("") }
    var state by remember { mutableStateOf<SearchState>(SearchState.Idle) }
    var catMap by remember { mutableStateOf<Map<Int, CategoryDto>>(emptyMap()) }

    LaunchedEffect(Unit) {
        catMap = runCatching { SiteRepo.categories() }.getOrDefault(emptyMap<Int, CategoryDto>())
        focusRequester.requestFocus()
    }

    // 输入防抖 400ms 后自动搜索;collectLatest 取消上一次未完成请求
    LaunchedEffect(Unit) {
        snapshotFlow { query }
            .debounce(400L)
            .collectLatest { raw ->
                val term = raw.trim()
                when {
                    term.isEmpty() -> state = SearchState.Idle
                    else -> {
                        state = SearchState.Loading
                        try {
                            val result = DiscourseApi.search(term)
                            state = SearchState.Ready(result, result.posts.associateBy { it.topicId })
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: ApiException) {
                            state = SearchState.Failed(e.code, e.message ?: "网络错误")
                        } catch (e: Throwable) {
                            state = SearchState.Failed(0, e.message ?: "网络错误")
                        }
                    }
                }
            }
    }

    Column(Modifier.fillMaxSize().background(nc.background)) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).background(nc.headerBg).padding(start = 4.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = nc.onBackground)
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                placeholder = { Text("搜索话题…", color = nc.onSurfaceVariant) },
                shape = RoundedCornerShape(21.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = nc.outlineVariant,
                    focusedBorderColor = nc.primary,
                    cursorColor = nc.primary,
                ),
                modifier = Modifier.weight(1f).focusRequester(focusRequester),
            )
            if (query.isNotEmpty()) {
                IconButton(onClick = { query = "" }) {
                    Icon(Icons.Filled.Close, "清空", tint = nc.onSurfaceVariant)
                }
            }
        }
        HorizontalDivider(color = nc.outlineVariant)

        when (val s = state) {
            SearchState.Idle -> Box(
                Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text("输入关键词开始搜索", color = nc.onSurfaceVariant)
            }
            SearchState.Loading -> Box(
                Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) { LoadingMark() }
            is SearchState.Failed -> Box(
                Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (s.code == 403) Icons.Filled.Lock else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = nc.onSurfaceVariant,
                        modifier = Modifier.size(36.dp),
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (s.code == 403) "无权搜索该内容" else s.message,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = nc.onBackground,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            is SearchState.Ready -> if (s.result.topics.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("没有找到相关话题", color = nc.onSurfaceVariant)
                }
            } else LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                items(s.result.topics, key = { it.id }) { topic ->
                    SearchResultRow(
                        topic = topic,
                        blurb = s.blurbs[topic.id],
                        category = catMap[topic.categoryId] ?: s.result.categories.firstOrNull { it.id == topic.categoryId },
                        onClick = { onOpenTopic(topic) },
                    )
                    HorizontalDivider(color = nc.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(topic: TopicDto, blurb: SearchPostDto?, category: CategoryDto?, onClick: () -> Unit) {
    val nc = LocalNodelocColors.current
    Surface(onClick = onClick, color = nc.surface, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                topic.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                color = nc.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            blurb?.blurb?.takeIf { it.isNotBlank() }?.let { text ->
                Text(
                    text,
                    style = MaterialTheme.typography.bodySmall,
                    color = nc.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                category?.let { cat ->
                    Box(Modifier.size(8.dp).background(hexColor(cat.color), CircleShape))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        cat.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = nc.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(9.dp))
                }
                Text(
                    (topic.postsCount - 1).coerceAtLeast(0).toString() + " 回复",
                    style = MaterialTheme.typography.labelSmall,
                    color = nc.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Text(SiteRepo.relativeTime(topic.bumpedAt), style = MaterialTheme.typography.labelSmall, color = nc.onSurfaceVariant)
            }
        }
    }
}
