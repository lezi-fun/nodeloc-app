package app.nodeloc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
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
 * 与官网 emoji-picker 一致优先展示站点自定义 emoji。选择自定义 emoji 时插入 :name:，
 * 由 Discourse 服务端在预览/发布时转换为对应图片，而不是把图片文件名写进正文。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EmojiPickerSheet(onDismiss: () -> Unit, onPick: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var customEmojis by remember { mutableStateOf<List<CustomEmojiDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val allEmojis = runCatching { DiscourseApi.customEmojis() }.getOrDefault(emptyList())
        // 只保留自定义上传的表情（路径包含 uploads），过滤掉标准 Unicode 表情
        customEmojis = allEmojis.filter { it.url.contains("/uploads/") }
        loading = false
    }

    val filteredByGroup = remember(query, customEmojis) {
        customEmojis
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .groupBy { it.group }
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
                filteredByGroup.forEach { (group, emojis) ->
                    Text(
                        group.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        emojis.forEach { emoji ->
                            AssistChip(
                                onClick = { onPick(":${emoji.name}:") },
                                label = {
                                    AsyncImage(
                                        model = absoluteUrl(emoji.url, DiscourseApi.BASE),
                                        contentDescription = emoji.name,
                                        modifier = Modifier.size(28.dp),
                                    )
                                },
                            )
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
