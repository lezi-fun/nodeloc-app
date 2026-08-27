package app.nodeloc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.nodeloc.data.DiscourseApi
import app.nodeloc.data.model.GifResultDto
import app.nodeloc.ui.theme.LocalNodelocColors
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * 对齐官网工具栏"插入 GIF"按钮:经站点后端代理的 KLIPY 搜索(GET /gifs/search.json)。
 * 选中后交由 [onPick] 拼出官网同款 markdown ![标题|宽x高](webp地址),不在这里处理插入逻辑,
 * 方便回复栏和创建话题两处复用同一套 MarkdownEditing 逻辑。
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun GifSearchSheet(onDismiss: () -> Unit, onPick: (GifResultDto) -> Unit) {
    val nc = LocalNodelocColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<GifResultDto>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        snapshotFlow { query }
            .debounce(400)
            .distinctUntilChanged()
            .collectLatest { q ->
                if (q.isBlank()) { results = emptyList(); errorMsg = null; return@collectLatest }
                loading = true
                errorMsg = null
                runCatching { DiscourseApi.gifSearch(q) }
                    .onSuccess { results = it.results }
                    .onFailure { errorMsg = it.message ?: "搜索失败" }
                loading = false
            }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = nc.background) {
        Column(Modifier.fillMaxSize().heightIn(min = 400.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.ArrowBack, "关闭", tint = nc.onSurfaceVariant)
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("搜索 GIF", color = nc.onSurfaceVariant) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = nc.onSurfaceVariant) },
                    shape = RoundedCornerShape(999.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = nc.outlineVariant,
                        focusedBorderColor = nc.primary,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }
            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = nc.primary, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                }
                errorMsg != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMsg!!, color = nc.onSurfaceVariant)
                }
                results.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (query.isBlank()) "输入关键词搜索 GIF" else "没有找到相关 GIF",
                        color = nc.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(results, key = { it.id }) { gif ->
                        val dims = gif.mediaFormats.webp.dims
                        val ratio = if (dims.size == 2 && dims[1] > 0) dims[0].toFloat() / dims[1] else 1f
                        AsyncImage(
                            model = gif.mediaFormats.webp.url,
                            contentDescription = gif.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(ratio.coerceIn(0.4f, 2.5f))
                                .clip(RoundedCornerShape(8.dp))
                                .background(nc.surfaceVariant)
                                .clickable { onPick(gif) },
                        )
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.Center) {
                Text("由 KLIPY 提供支持", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = nc.onSurfaceVariant)
            }
        }
    }
}
