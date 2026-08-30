package app.nodeloc.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private sealed interface SearchState {
    data object Idle : SearchState
    data object Loading : SearchState
    data class Ready(val result: SearchDto) : SearchState
    data class Failed(val code: Int, val message: String) : SearchState
}

/**
 * 高级筛选器,字段与官网全页搜索一致;序列化后经 rememberSaveable 保存。
 * 全部为默认值时视为"快速搜索",查询词不加任何语法(与主页搜索一致)。
 */
@Serializable
data class AdvancedFilters(
    val searchType: String = "", // "" = 话题/帖子, "categories" = 节点/标签, "users" = 用户
    val categorySlug: String = "",
    val tags: String = "",
    val inFilter: String = "",
    val status: String = "",
    val author: String = "",
    val dateMode: String = "", // "before" / "after"
    val date: String = "",     // yyyy-MM-dd
    val minReplies: String = "",
    val minViews: String = "",
    val order: String = "",
    val postsOnly: Boolean = false, // 类型下拉:话题/帖子
) {
    val isDefault: Boolean
        get() = this == AdvancedFilters()

    /** 拼接为 Discourse 搜索语法,追加在关键词之后 */
    fun toQuerySuffix(): String {
        if (isDefault) return ""
        val sb = StringBuilder()
        if (inFilter.isNotBlank()) sb.append(" in:").append(inFilter)
        if (status.isNotBlank()) sb.append(" status:").append(status)
        tags.split(',', '，').map { it.trim() }.filter { it.isNotEmpty() }
            .forEach { sb.append(" tag:").append(it) }
        if (categorySlug.isNotBlank()) sb.append(" category:").append(categorySlug)
        if (author.isNotBlank()) sb.append(" @").append(author.trim())
        if (date.isNotBlank() && (dateMode == "before" || dateMode == "after")) {
            sb.append(' ').append(dateMode).append(':').append(date)
        }
        minReplies.toIntOrNull()?.takeIf { it > 0 }?.let { sb.append(" min_post_count:").append(it) }
        minViews.toIntOrNull()?.takeIf { it > 0 }?.let { sb.append(" min_views:").append(it) }
        if (order.isNotBlank()) sb.append(" order:").append(order)
        return sb.toString()
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        fun encode(f: AdvancedFilters): String = json.encodeToString(AdvancedFilters.serializer(), f)
        fun decode(s: String): AdvancedFilters =
            if (s.isBlank()) AdvancedFilters()
            else runCatching { decodeFromString(s) }.getOrDefault(AdvancedFilters())

        private fun decodeFromString(s: String): AdvancedFilters = json.decodeFromString(s)
    }
}

@Composable
fun SearchScreen(onBack: () -> Unit, onOpenTopic: (TopicDto) -> Unit) {
    val nc = LocalNodelocColors.current
    val focusRequester = remember { FocusRequester() }
    var query by rememberSaveable { mutableStateOf("") }
    var advJson by rememberSaveable { mutableStateOf("") }
    var adv by remember { mutableStateOf(AdvancedFilters.decode(advJson)) }
    // 每次进入搜索页自动折叠(不跨页面保留展开状态)
    var advOpen by remember { mutableStateOf(false) }
    var state by remember { mutableStateOf<SearchState>(SearchState.Idle) }
    var catMap by remember { mutableStateOf<Map<Int, CategoryDto>>(emptyMap()) }

    fun updateFilters(f: AdvancedFilters) {
        adv = f
        advJson = AdvancedFilters.encode(f)
    }

    LaunchedEffect(Unit) {
        catMap = runCatching { SiteRepo.categories() }.getOrDefault(emptyMap<Int, CategoryDto>())
        focusRequester.requestFocus()
    }

    // 输入防抖 400ms 后自动搜索;collectLatest 取消上一次未完成请求。
    // 关键词与高级筛选任一变化都会触发;无筛选时保持主页快速搜索的默认配置。
    LaunchedEffect(Unit) {
        combine(snapshotFlow { query }, snapshotFlow { adv }) { q, f -> q to f }
            .debounce(400L)
            .collectLatest { (raw, filters) ->
                val term = raw.trim()
                when {
                    term.isEmpty() -> state = SearchState.Idle
                    else -> {
                        state = SearchState.Loading
                        try {
                            val result = DiscourseApi.search(term + filters.toQuerySuffix(), filters.searchType)
                            state = SearchState.Ready(result)
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

        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 10.dp)) {
            // 搜索类型:话题/帖子、节点/标签、用户
            DropdownField(
                header = "话题/帖子",
                options = listOf(
                    "" to "话题/帖子",
                    "categories" to "节点/标签",
                    "users" to "用户",
                ),
                selectedValue = adv.searchType,
                onSelect = { v -> updateFilters(adv.copy(searchType = v ?: "")) },
            )
            Spacer(Modifier.height(8.dp))
            // 高级筛选器折叠条
            Surface(
                onClick = { advOpen = !advOpen },
                shape = RoundedCornerShape(21.dp),
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(1.dp, nc.outlineVariant),
                modifier = Modifier.fillMaxWidth().height(42.dp),
            ) {
                Row(
                    Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        if (adv.isDefault) "高级筛选器" else "高级筛选器(已启用)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (adv.isDefault) nc.onBackground else nc.primary,
                    )
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = nc.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
            AnimatedVisibility(visible = advOpen) {
                AdvancedPanel(
                    filters = adv,
                    categories = catMap.values.sortedBy { it.position },
                    onChange = ::updateFilters,
                )
            }
        }

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
            is SearchState.Ready -> if (s.result.topics.isEmpty() && s.result.posts.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("没有找到相关话题", color = nc.onSurfaceVariant)
                }
            } else {
                LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                    item(key = "order") {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Spacer(Modifier.weight(1f))
                            Text("排序依据", style = MaterialTheme.typography.labelMedium, color = nc.onSurfaceVariant)
                            Spacer(Modifier.width(6.dp))
                            DropdownField(
                                header = "相关度",
                                options = listOf(
                                    "" to "相关度",
                                    "latest_topic" to "最新话题",
                                    "latest" to "最新发帖",
                                    "likes" to "最多点赞",
                                    "views" to "最多浏览",
                                ),
                                selectedValue = adv.order,
                                onSelect = { v -> updateFilters(adv.copy(order = v ?: "")) },
                                compact = true,
                            )
                        }
                    }
                    if (adv.postsOnly) {
                        // 帖子模式:逐条列出匹配的帖子
                        items(s.result.posts, key = { "p${it.id}" }) { post ->
                            PostResultRow(
                                post = post,
                                topicTitle = s.result.topics.firstOrNull { it.id == post.topicId }?.title.orEmpty(),
                                onClick = {
                                    s.result.topics.firstOrNull { it.id == post.topicId }?.let(onOpenTopic)
                                },
                            )
                            HorizontalDivider(color = nc.outlineVariant)
                        }
                    } else {
                        items(s.result.topics, key = { it.id }) { topic ->
                            SearchResultRow(
                                topic = topic,
                                blurb = s.result.posts.associateBy { it.topicId }[topic.id],
                                category = catMap[topic.categoryId]
                                    ?: s.result.categories.firstOrNull { it.id == topic.categoryId },
                                onClick = { onOpenTopic(topic) },
                            )
                            HorizontalDivider(color = nc.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}

/** 官网风格高级筛选面板:分类/标签/仅返回/话题状态/发帖人/时间/最小回复/最小浏览 */
@Composable
private fun AdvancedPanel(
    filters: AdvancedFilters,
    categories: List<CategoryDto>,
    onChange: (AdvancedFilters) -> Unit,
) {
    val nc = LocalNodelocColors.current
    Column(Modifier.fillMaxWidth().padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DropdownField(
            header = "所有节点",
            options = listOf("" to "所有节点") + categories.map { (it.slug.ifBlank { it.id.toString() }) to it.name },
            selectedValue = filters.categorySlug,
            onSelect = { v -> onChange(filters.copy(categorySlug = v ?: "")) },
        )
        OutlinedTextField(
            value = filters.tags,
            onValueChange = { onChange(filters.copy(tags = it)) },
            singleLine = true,
            placeholder = { Text("拥有该标签(逗号分隔)", color = nc.onSurfaceVariant) },
            shape = RoundedCornerShape(21.dp),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )
        DropdownField(
            header = "仅返回话题/帖子…",
            options = listOf(
                "" to "仅返回话题/帖子…",
                "solved" to "已解决",
                "unsolved" to "未解决",
                "pinned" to "已置顶",
                "first" to "仅首帖",
                "seen" to "已看过",
                "unseen" to "未看过",
            ),
            selectedValue = filters.inFilter,
            onSelect = { v -> onChange(filters.copy(inFilter = v ?: "")) },
        )
        DropdownField(
            header = "话题状态",
            options = listOf(
                "" to "任意",
                "open" to "开放",
                "closed" to "关闭",
                "archived" to "归档",
            ),
            selectedValue = filters.status,
            onSelect = { v -> onChange(filters.copy(status = v ?: "")) },
        )
        OutlinedTextField(
            value = filters.author,
            onValueChange = { onChange(filters.copy(author = it)) },
            singleLine = true,
            placeholder = { Text("发帖人用户名", color = nc.onSurfaceVariant) },
            shape = RoundedCornerShape(21.dp),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownField(
                header = "时间",
                options = listOf(
                    "" to "发布时间",
                    "before" to "早于",
                    "after" to "晚于",
                ),
                selectedValue = filters.dateMode,
                onSelect = { v -> onChange(filters.copy(dateMode = v ?: "")) },
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = filters.date,
                onValueChange = { onChange(filters.copy(date = it)) },
                singleLine = true,
                placeholder = { Text("yyyy-MM-dd", color = nc.onSurfaceVariant) },
                shape = RoundedCornerShape(21.dp),
                colors = fieldColors(),
                modifier = Modifier.weight(1.3f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = filters.minReplies,
                onValueChange = { v -> onChange(filters.copy(minReplies = v.filter(Char::isDigit).take(5))) },
                singleLine = true,
                placeholder = { Text("最少回复", color = nc.onSurfaceVariant) },
                shape = RoundedCornerShape(21.dp),
                colors = fieldColors(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = filters.minViews,
                onValueChange = { v -> onChange(filters.copy(minViews = v.filter(Char::isDigit).take(6))) },
                singleLine = true,
                placeholder = { Text("最少浏览", color = nc.onSurfaceVariant) },
                shape = RoundedCornerShape(21.dp),
                colors = fieldColors(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = LocalNodelocColors.current.outlineVariant,
    focusedBorderColor = LocalNodelocColors.current.primary,
    cursorColor = LocalNodelocColors.current.primary,
)

/** 官网风格下拉选择框 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    header: String,
    options: List<Pair<String, String>>,
    selectedValue: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val nc = LocalNodelocColors.current
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = options.firstOrNull { it.first == selectedValue }?.second?.ifBlank { null } ?: header,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            shape = RoundedCornerShape(21.dp),
            colors = fieldColors(),
            textStyle = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .let { if (compact) it.height(48.dp) else it.fillMaxWidth() },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label, fontWeight = if (value == selectedValue) FontWeight.Bold else FontWeight.Normal) },
                    onClick = {
                        onSelect(value.ifBlank { null })
                        expanded = false
                    },
                )
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

/** 帖子模式结果行:作者 + 所属话题标题 + 匹配片段 */
@Composable
private fun PostResultRow(post: SearchPostDto, topicTitle: String, onClick: () -> Unit) {
    val nc = LocalNodelocColors.current
    Surface(onClick = onClick, color = nc.surface, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
            if (topicTitle.isNotBlank()) {
                Text(
                    topicTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Normal,
                    color = nc.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                post.blurb,
                style = MaterialTheme.typography.bodySmall,
                color = nc.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp),
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    post.username,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = nc.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Text(SiteRepo.relativeTime(post.createdAt), style = MaterialTheme.typography.labelSmall, color = nc.onSurfaceVariant)
            }
        }
    }
}
