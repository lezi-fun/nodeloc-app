package app.nodeloc.ui.components

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.nodeloc.data.DiscourseApi
import app.nodeloc.data.model.PostDto
import app.nodeloc.ui.theme.LocalNodelocColors
import kotlinx.coroutines.launch

/** 全屏帖子编辑器：Markdown 与渲染预览可切换，并支持表情、GIF 和附件上传。 */
@Composable
fun EditPostDialog(post: PostDto, onDismiss: () -> Unit, onEdited: (PostDto) -> Unit) {
    val nc = LocalNodelocColors.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var originalRaw by remember(post.id) { mutableStateOf(post.raw.orEmpty()) }
    var body by remember(post.id) { mutableStateOf(TextFieldValue(originalRaw)) }
    var gifSheetOpen by remember(post.id) { mutableStateOf(false) }
    var emojiSheetOpen by remember(post.id) { mutableStateOf(false) }
    var previewMode by remember(post.id) { mutableStateOf(false) }
    var uploading by remember(post.id) { mutableStateOf(false) }
    var saving by remember(post.id) { mutableStateOf(false) }
    var loading by remember(post.id) { mutableStateOf(post.raw == null) }
    var errorMsg by remember(post.id) { mutableStateOf<String?>(null) }

    // 如果 post.raw 为 null，通过 API 获取完整内容
    LaunchedEffect(post.id) {
        if (post.raw == null) {
            loading = true
            runCatching { DiscourseApi.getPost(post.id) }
                .onSuccess { fullPost ->
                    originalRaw = fullPost.raw.orEmpty()
                    body = TextFieldValue(originalRaw)
                }
                .onFailure { errorMsg = "无法加载帖子内容：${it.message}" }
            loading = false
        }
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            uploading = true
            errorMsg = null
            runCatching { uploadEditorFile(context, uri) }
                .onSuccess { url -> body = MarkdownEditingActions.insertAttachment(body, url) }
                .onFailure { throwable -> errorMsg = throwable.message ?: "上传失败，请稍后再试" }
            uploading = false
        }
    }

    fun save() {
        val newRaw = body.text
        if (newRaw.isBlank() || saving || uploading) return
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
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, "关闭", tint = nc.onBackground) }
                Text("编辑帖子", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    color = nc.onBackground, modifier = Modifier.weight(1f))
                if (loading || saving || uploading) {
                    CircularProgressIndicator(color = nc.primary, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(16.dp))
                } else TextButton(onClick = ::save, enabled = body.text.isNotBlank()) { Text("保存编辑", color = nc.primary) }
            }
            HorizontalDivider(color = nc.outlineVariant)
            MarkdownToolbar(
                onAction = { action ->
                    when (action) {
                        MarkdownAction.Gif -> gifSheetOpen = true
                        MarkdownAction.Emoji -> emojiSheetOpen = true
                        MarkdownAction.Attachment -> filePicker.launch("*/*")
                        MarkdownAction.TogglePreview -> previewMode = !previewMode
                        else -> body = MarkdownEditingActions.apply(action, body)
                    }
                },
            )
            Text(if (previewMode) "A · 渲染预览" else "M · Markdown",
                style = MaterialTheme.typography.labelSmall, color = nc.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
            errorMsg?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) }
            if (previewMode) {
                CookedText(body.text, Modifier.fillMaxSize().padding(16.dp))
            } else {
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent,
                    ),
                    modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                )
            }
        }
    }
    if (gifSheetOpen) GifSearchSheet(onDismiss = { gifSheetOpen = false }, onPick = {
        body = MarkdownEditingActions.insertGif(body, it); gifSheetOpen = false
    })
    if (emojiSheetOpen) EmojiPickerSheet(onDismiss = { emojiSheetOpen = false }, onPick = {
        body = MarkdownEditingActions.insertEmoji(body, it); emojiSheetOpen = false
    })
}

suspend fun uploadEditorFile(context: Context, uri: android.net.Uri): String {
    val file = java.io.File(context.cacheDir, "nodeloc-upload-${System.currentTimeMillis()}")
    try {
        context.contentResolver.openInputStream(uri)?.use { input -> file.outputStream().use(input::copyTo) }
            ?: throw IllegalStateException("无法读取所选文件")
        val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val response = DiscourseApi.uploadAttachment(file, mime)
        return response.shortUrl ?: response.url ?: throw IllegalStateException(response.error ?: "服务器未返回附件地址")
    } finally {
        file.delete()
    }
}
