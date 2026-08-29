package app.nodeloc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val commonEmoji = listOf(
    "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "🙂", "🙃", "😉",
    "😍", "🥰", "😘", "😎", "🤔", "🤗", "🤩", "🥳", "😴", "😢", "😭", "😡",
    "👍", "👎", "👏", "🙌", "🙏", "💪", "🎉", "🔥", "❤️", "💯", "✨", "✅",
)

/** 轻量复刻官网 emoji-picker；插入 Unicode 后由 Discourse cooked 渲染为标准表情。 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EmojiPickerSheet(onDismiss: () -> Unit, onPick: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) { commonEmoji.filter { query.isBlank() || it.contains(query) } }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            label = { Text("搜索表情") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            filtered.forEach { emoji ->
                AssistChip(
                    onClick = { onPick(emoji) },
                    label = { Text(emoji) },
                )
            }
        }
        TextButton(onClick = onDismiss, modifier = Modifier.padding(bottom = 8.dp)) { Text("关闭") }
    }
}
