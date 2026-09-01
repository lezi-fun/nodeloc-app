package app.nodeloc.ui.components

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import app.nodeloc.data.DiscourseApi
import app.nodeloc.data.model.CustomEmojiDto
import app.nodeloc.util.absoluteUrl
import coil.compose.AsyncImage


/**
 * 单元格与图片尺寸照 common/components/emoji-picker.scss:
 *   .emoji-picker .emoji { width: 24px; height: 24px; padding: 6px }
 * 即图片 24px、四周各 6px 内边距 → 单元格 36px。间距由这层 padding 提供,
 * 网格本身不再额外留 gap。
 */
private val EmojiCellSize = 36.dp
private val EmojiImageSize = 24.dp

/** .emoji-picker__section-emojis { padding: 0.5rem } */
private val EmojiSectionPadding = 8.dp

/**
 * 与官网 emoji-picker 一致优先展示站点自定义 emoji。选择自定义 emoji 时插入 :name:，
 * 由 Discourse 服务端在预览/发布时转换为对应图片，而不是把图片文件名写进正文。
 *
 * 版式对齐官网:分区标题 + 裸图网格。官网的 emoji 单元格是纯图片按钮,
 * 只有 hover/focus 时才出现 --primary-very-low 底色与圆角,没有 chip 的边框和填充背景。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EmojiPickerSheet(onDismiss: () -> Unit, onPick: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var customEmojis by remember { mutableStateOf<List<CustomEmojiDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    // 表情名称前缀到显示名称的映射
    val groupDisplayNames = mapOf(
        "xhj" to "SIMSIMI",
        "ac" to "AC"
    )

    LaunchedEffect(Unit) {
        val allEmojis = runCatching { DiscourseApi.customEmojis() }.getOrDefault(emptyList())
        // 只保留自定义上传的表情（路径包含 uploads），过滤掉标准 Unicode 表情
        customEmojis = allEmojis.filter { it.url.contains("/uploads/") }
        loading = false
    }

    val filteredByGroup = remember(query, customEmojis) {
        customEmojis
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .groupBy { emoji ->
                // 按名称前缀分组（提取字母部分）
                emoji.name.takeWhile { it.isLetter() }
            }
            .toSortedMap()
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                label = { Text("搜索表情") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (loading) {
                Text("正在加载站点表情…", modifier = Modifier.padding(16.dp))
            } else if (filteredByGroup.isEmpty()) {
                Text("没有找到表情", modifier = Modifier.padding(16.dp))
            } else {
                filteredByGroup.forEach { (prefix, emojis) ->
                    val displayName = groupDisplayNames[prefix] ?: prefix.uppercase()
                    // .emoji-picker__section-title { padding: 0.5rem; color: var(--primary-high);
                    //   font-size: var(--font-down-2); font-weight: 700; text-transform: uppercase }
                    Text(
                        displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = EmojiSectionPadding, vertical = EmojiSectionPadding),
                    )
                    // 单元格自带 6px 内边距,网格不再额外留 gap
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = EmojiSectionPadding),
                    ) {
                        emojis.forEach { emoji ->
                            Box(
                                Modifier
                                    .size(EmojiCellSize)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { onPick(":${emoji.name}:") },
                                contentAlignment = Alignment.Center,
                            ) {
                                AsyncImage(
                                    model = absoluteUrl(emoji.url, DiscourseApi.BASE),
                                    contentDescription = emoji.name,
                                    modifier = Modifier.size(EmojiImageSize),
                                )
                            }
                        }
                    }
                }
            }

            TextButton(onClick = onDismiss, modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
                Text("关闭")
            }
        }
    }
}
