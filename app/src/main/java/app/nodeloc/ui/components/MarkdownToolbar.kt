package app.nodeloc.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import app.nodeloc.ui.theme.LocalNodelocColors

/**
 * 对齐官网 D-Editor 工具栏(discourse/lib/composer/toolbar.ts)的默认按钮子集:
 * 粗体/斜体/标题/链接/引用/代码/无序列表/有序列表。
 * 官网另有 GIF 搜索、多语言、AI 助手三个按钮 —— 这些是 NodeLoc 装的插件(discourse-ai 等),
 * 依赖各自独立的后端服务,不属于本次"编辑排版"范围,故未实现。
 */
enum class MarkdownAction {
    Bold, Italic, Heading, Link, Quote, Code, BulletList, OrderedList, Emoji, Attachment, Gif, TogglePreview
}

/** 图标名对照官网 discourse/lib/composer/toolbar.ts 各按钮的真实图标(bold/italic/link/quote-right/code/list-ul/list-ol) */
@Composable
fun MarkdownToolbar(onAction: (MarkdownAction) -> Unit, modifier: Modifier = Modifier) {
    val nc = LocalNodelocColors.current
    Row(modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 2.dp)) {
        ToolbarButton(Icons.Filled.FormatBold, "粗体") { onAction(MarkdownAction.Bold) }
        ToolbarButton(Icons.Filled.FormatItalic, "斜体") { onAction(MarkdownAction.Italic) }
        ToolbarButton(Icons.Filled.Title, "标题") { onAction(MarkdownAction.Heading) }
        ToolbarButton(Icons.Filled.Link, "链接") { onAction(MarkdownAction.Link) }
        ToolbarButton(Icons.Filled.FormatQuote, "引用") { onAction(MarkdownAction.Quote) }
        ToolbarButton(Icons.Filled.Code, "代码") { onAction(MarkdownAction.Code) }
        ToolbarButton(Icons.Filled.FormatListBulleted, "无序列表") { onAction(MarkdownAction.BulletList) }
        ToolbarButton(Icons.Filled.FormatListNumbered, "有序列表") { onAction(MarkdownAction.OrderedList) }
        ToolbarButton(Icons.Filled.Face, "表情") { onAction(MarkdownAction.Emoji) }
        ToolbarButton(Icons.Filled.AttachFile, "上传附件") { onAction(MarkdownAction.Attachment) }
        ToolbarButton(Icons.Filled.Gif, "插入 GIF") { onAction(MarkdownAction.Gif) }
        ToolbarButton(Icons.Filled.Visibility, "预览") { onAction(MarkdownAction.TogglePreview) }
    }
    HorizontalDivider(color = nc.outlineVariant)
}

@Composable
private fun ToolbarButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    val nc = LocalNodelocColors.current
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription, tint = nc.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

/** 把工具栏点击映射到 [MarkdownEditing] 的文本操作,官网中文占位符文案照抄 composer.*_text i18n key。 */
object MarkdownEditingActions {
    /** Gif 需要先弹出搜索面板异步选图,调用方应在分发前拦截 [MarkdownAction.Gif],此处按原样返回不做转换。 */
    fun apply(action: MarkdownAction, value: TextFieldValue): TextFieldValue = when (action) {
        MarkdownAction.Bold -> MarkdownEditing.applySurround(value, { "**" }, "**", "粗体文本")
        MarkdownAction.Italic -> MarkdownEditing.applySurround(value, { "*" }, "*", "强调文本")
        MarkdownAction.Heading -> MarkdownEditing.applyHeading(value, 3)
        MarkdownAction.Link -> MarkdownEditing.applyLink(value, "https://", "文本")
        MarkdownAction.Quote -> MarkdownEditing.applyLinePrefix(value, { "> " }, "块引用")
        MarkdownAction.Code -> MarkdownEditing.formatCode(value, "在此处键入或粘贴代码")
        MarkdownAction.BulletList -> MarkdownEditing.applyLinePrefix(value, { "* " }, "列表条目")
        MarkdownAction.OrderedList -> MarkdownEditing.applyLinePrefix(
            value,
            { prev -> if (prev == null) "1. " else (prev.trim().removeSuffix(".").toIntOrNull()?.plus(1) ?: 1).toString() + ". " },
            "列表条目",
        )
        MarkdownAction.Emoji, MarkdownAction.Attachment, MarkdownAction.Gif, MarkdownAction.TogglePreview -> value
    }

    fun insertEmoji(value: TextFieldValue, emoji: String): TextFieldValue {
        val insertion = "$emoji "
        val cursor = value.selection.max
        val result = value.text.substring(0, cursor) + insertion + value.text.substring(cursor)
        val position = cursor + insertion.length
        return TextFieldValue(result, TextRange(position, position))
    }

    fun insertAttachment(value: TextFieldValue, url: String): TextFieldValue {
        val markdown = "\n[$url]($url)\n"
        val cursor = value.selection.max
        val text = value.text
        val result = text.substring(0, cursor) + markdown + text.substring(cursor)
        val position = cursor + markdown.length
        return TextFieldValue(result, TextRange(position, position))
    }


    fun insertGif(value: TextFieldValue, gif: app.nodeloc.data.model.GifResultDto): TextFieldValue {
        val dims = gif.mediaFormats.webp.dims
        val dimsSuffix = if (dims.size == 2) "|${dims[0]}x${dims[1]}" else ""
        val markdown = "\n![${gif.title}$dimsSuffix](${gif.mediaFormats.webp.url})\n"
        val text = value.text
        val cursor = value.selection.max
        val newText = text.substring(0, cursor) + markdown + text.substring(cursor)
        val newCursor = cursor + markdown.length
        return TextFieldValue(newText, TextRange(newCursor, newCursor))
    }
}
