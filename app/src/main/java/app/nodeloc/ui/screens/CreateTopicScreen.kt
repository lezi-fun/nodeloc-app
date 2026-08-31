package app.nodeloc.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import app.nodeloc.data.DiscourseApi
import app.nodeloc.data.SiteRepo
import app.nodeloc.data.model.CategoryDto
import app.nodeloc.ui.components.CategoryDot
import app.nodeloc.ui.components.CategoryPickerSheet
import app.nodeloc.ui.components.CookedText
import app.nodeloc.ui.components.EmojiPickerSheet
import app.nodeloc.ui.components.GifSearchSheet
import app.nodeloc.ui.components.MarkdownAction
import app.nodeloc.ui.components.MarkdownEditingActions
import app.nodeloc.ui.components.MarkdownToolbar
import app.nodeloc.ui.theme.LocalNodelocColors
import kotlinx.coroutines.launch

/**
 * 对齐官网"新建话题"编辑器:节点选择 + 标题 + 正文(Markdown 工具栏),
 * 发布走 POST /posts 同一端点(title+raw+category,不带 topic_id 即视为新建话题)。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTopicScreen(onBack: () -> Unit, onCreated: (Long) -> Unit) {
    val nc = LocalNodelocColors.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var categories by remember { mutableStateOf<List<CategoryDto>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<CategoryDto?>(null) }
    var categoryMenuOpen by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf(TextFieldValue("")) }
    var submitting by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var gifSheetOpen by remember { mutableStateOf(false) }
    var emojiSheetOpen by remember { mutableStateOf(false) }
    var previewMode by remember { mutableStateOf(false) }
    var previewHtml by remember { mutableStateOf("") }
    var uploading by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            uploading = true
            errorMsg = null
            runCatching { app.nodeloc.ui.components.uploadEditorFile(context, uri) }
                .onSuccess { url -> body = MarkdownEditingActions.insertAttachment(body, url) }
                .onFailure { errorMsg = it.message ?: "上传失败，请稍后再试" }
            uploading = false
        }
    }

    LaunchedEffect(Unit) {
        categories = SiteRepo.categories().values.sortedBy { it.position }
    }

    val canSubmit = title.trim().length >= 3 && body.text.trim().length >= 3 && selectedCategory != null && !submitting

    fun submit() {
        val category = selectedCategory ?: return
        if (!canSubmit) return
        submitting = true
        errorMsg = null
        scope.launch {
            runCatching { DiscourseApi.createTopic(title.trim(), body.text.trim(), category.id) }
                .onSuccess { topicId -> onCreated(topicId) }
                .onFailure { errorMsg = it.message ?: "发布失败，请稍后再试" }
            submitting = false
        }
    }

    Column(Modifier.fillMaxSize().background(nc.background)) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).background(nc.headerBg).padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = nc.onBackground)
            }
            Text(
                "创建话题",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = nc.onBackground,
                modifier = Modifier.weight(1f),
            )
            if (submitting) {
                CircularProgressIndicator(color = nc.primary, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(16.dp))
            } else {
                TextButton(onClick = ::submit, enabled = canSubmit) {
                    Text("发布", color = if (canSubmit) nc.primary else nc.onSurfaceVariant)
                }
            }
        }
        HorizontalDivider(color = nc.outlineVariant)

        Surface(
            onClick = { categoryMenuOpen = true },
            shape = RoundedCornerShape(10.dp),
            color = nc.background,
            border = BorderStroke(1.dp, nc.outlineVariant),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                selectedCategory?.let { cat ->
                    CategoryDot(cat, size = 18.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    selectedCategory?.name ?: "选择节点…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedCategory == null) nc.onSurfaceVariant else nc.onBackground,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.Filled.ArrowDropDown, null, tint = nc.onSurfaceVariant)
            }
        }
        if (categoryMenuOpen) {
            CategoryPickerSheet(
                categories = categories,
                onDismiss = { categoryMenuOpen = false },
                onPick = { cat -> selectedCategory = cat; categoryMenuOpen = false },
            )
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("输入标题，或在此处粘贴链接", color = nc.onSurfaceVariant) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = nc.outlineVariant,
                focusedBorderColor = nc.primary,
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
        )

        MarkdownToolbar(
            onAction = { action ->
                when (action) {
                    MarkdownAction.Gif -> gifSheetOpen = true
                    MarkdownAction.Emoji -> emojiSheetOpen = true
                    MarkdownAction.Attachment -> filePicker.launch("*/*")
                    MarkdownAction.TogglePreview -> {
                        if (!previewMode) {
                            val text = body.text.trim()
                            if (text.isEmpty()) {
                                previewHtml = ""
                                previewMode = true
                            } else {
                                errorMsg = null
                                scope.launch {
                                    runCatching { DiscourseApi.previewPost(text) }
                                        .onSuccess { previewHtml = it.cooked; previewMode = true }
                                        .onFailure { errorMsg = it.message ?: "预览失败，请稍后再试" }
                                }
                            }
                        } else {
                            previewMode = false
                            errorMsg = null
                        }
                    }
                    else -> body = MarkdownEditingActions.apply(action, body)
                }
            },
        )
        Text(
            if (previewMode) "A · 渲染预览" else "M · Markdown",
            style = MaterialTheme.typography.labelSmall,
            color = nc.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )

        errorMsg?.let { msg ->
            Text(
                msg,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        if (previewMode) {
            CookedText(previewHtml, Modifier.fillMaxSize().padding(16.dp))
        } else OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            placeholder = {
                Text(
                    if (selectedCategory == null) "先选择一个节点，然后在此处输入。" else "在此处输入。使用 Markdown 排版。",
                    color = nc.onSurfaceVariant,
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
            ),
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
        )
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
    if (emojiSheetOpen) {
        EmojiPickerSheet(
            onDismiss = { emojiSheetOpen = false },
            onPick = { emoji ->
                body = MarkdownEditingActions.insertEmoji(body, emoji)
                emojiSheetOpen = false
            },
        )
    }
}
