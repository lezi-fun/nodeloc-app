package app.nodeloc.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.nodeloc.data.DiscourseApi
import app.nodeloc.data.model.PostDto
import app.nodeloc.ui.theme.LocalNodelocColors
import kotlinx.coroutines.launch

/**
 * 官网"为何进行编辑?"编辑器的简化版:全屏对话框,Markdown 工具栏 + 正文,
 * 不做节点/标题/阅读权限编辑(那些属于话题级设置,超出"帖子编辑"范围)。
 * 保存走 PUT /posts/{id},服务端用 original_text 做冲突检测。
 */
@Composable
fun EditPostDialog(post: PostDto, onDismiss: () -> Unit, onEdited: (PostDto) -> Unit) {
    val nc = LocalNodelocColors.current
    val scope = rememberCoroutineScope()
    val originalRaw = post.raw.orEmpty()
    var body by remember(post.id) { mutableStateOf(TextFieldValue(originalRaw)) }
    var gifSheetOpen by remember(post.id) { mutableStateOf(false) }
    var saving by remember(post.id) { mutableStateOf(false) }
    var errorMsg by remember(post.id) { mutableStateOf<String?>(null) }

    fun save() {
        val newRaw = body.text
        if (newRaw.isBlank() || saving) return
        saving = true
        errorMsg = null
        scope.launch {
            runCatching { DiscourseApi.editPost(post.id, post.topicId, newRaw, originalRaw) }
                .onSuccess { onEdited(it) }
                .onFailure { errorMsg = it.message ?: "保存失败，请稍后再试" }
            saving = false
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(Modifier.fillMaxSize().background(nc.background)) {
            Row(
                Modifier.fillMaxWidth().height(56.dp).background(nc.headerBg).padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, "关闭", tint = nc.onBackground)
                }
                Text(
                    "编辑帖子",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = nc.onBackground,
                    modifier = Modifier.weight(1f),
                )
                if (saving) {
                    CircularProgressIndicator(color = nc.primary, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(16.dp))
                } else {
                    TextButton(onClick = ::save, enabled = body.text.isNotBlank()) {
                        Text("保存编辑", color = nc.primary)
                    }
                }
            }
            HorizontalDivider(color = nc.outlineVariant)

            MarkdownToolbar(
                onAction = { action ->
                    if (action == MarkdownAction.Gif) gifSheetOpen = true
                    else body = MarkdownEditingActions.apply(action, body)
                },
            )

            errorMsg?.let { msg ->
                Text(
                    msg,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
            )
        }
    }

    if (gifSheetOpen) {
        GifSearchSheet(
            onDismiss = { gifSheetOpen = false },
            onPick = { gif ->
                body = MarkdownEditingActions.insertGif(body, gif)
                gifSheetOpen = false
            },
        )
    }
}
