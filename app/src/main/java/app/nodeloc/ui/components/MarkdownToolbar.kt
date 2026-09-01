package app.nodeloc.ui.components

import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
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
import androidx.compose.material3.Text
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
    Bold, Italic, Heading, Link, Quote, Code, BulletList, OrderedList, Emoji, Attachment, Gif, TogglePreview,
    // 标题弹出菜单的层级,对齐官网 heading-1..4 / paragraph / small
    Heading1, Heading2, Heading3, Heading4, HeadingParagraph, HeadingSmall,
    // 列表菜单里的任务清单(官网由 checklist 插件注入 list 菜单)
    Checklist,
    // + 菜单:官网 popupMenuOptions 的 format-code / insert-table / apply-wrap
    CodeBlock, InsertTable, ApplyWrap,
}

/**
 * 按官网分组顺序 fontStyles → insertions → extras 排列
 * (frontend/discourse/app/lib/composer/toolbar.ts 的 this.groups)。
 *
 * 与官网一致的两处行为:heading 和 list 是弹出菜单而非直接插入;
 * code 在触屏端不进主栏(官网 `if (!this.capabilities.touch)`),改由 + 菜单的
 * 代码块提供。+ 菜单其余项取官网 popupMenuOptions 里与排版相关的部分。
 */
@Composable
fun MarkdownToolbar(onAction: (MarkdownAction) -> Unit, modifier: Modifier = Modifier) {
    val nc = LocalNodelocColors.current
    var headingMenu by remember { mutableStateOf(false) }
    var listMenu by remember { mutableStateOf(false) }
    var optionsMenu by remember { mutableStateOf(false) }

    Row(modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 2.dp)) {
        // fontStyles
        ToolbarButton(Icons.Filled.FormatBold, "粗体") { onAction(MarkdownAction.Bold) }
        ToolbarButton(Icons.Filled.FormatItalic, "斜体") { onAction(MarkdownAction.Italic) }
        Box {
            ToolbarButton(Icons.Filled.Title, "文字大小") { headingMenu = true }
            ToolbarDropdown(
                expanded = headingMenu,
                onDismiss = { headingMenu = false },
                items = listOf(
                    "标题 1" to MarkdownAction.Heading1,
                    "标题 2" to MarkdownAction.Heading2,
                    "标题 3" to MarkdownAction.Heading3,
                    "标题 4" to MarkdownAction.Heading4,
                    "正文" to MarkdownAction.HeadingParagraph,
                    "小号文字" to MarkdownAction.HeadingSmall,
                ),
                onAction = onAction,
            )
        }
        // insertions
        ToolbarButton(Icons.Filled.Link, "链接") { onAction(MarkdownAction.Link) }
        ToolbarButton(Icons.Filled.FormatQuote, "块引用") { onAction(MarkdownAction.Quote) }
        // extras
        Box {
            ToolbarButton(Icons.Filled.FormatListBulleted, "列表") { listMenu = true }
            ToolbarDropdown(
                expanded = listMenu,
                onDismiss = { listMenu = false },
                items = listOf(
                    "无序列表" to MarkdownAction.BulletList,
                    "有序列表" to MarkdownAction.OrderedList,
                    "任务清单" to MarkdownAction.Checklist,
                ),
                onAction = onAction,
            )
        }
        ToolbarButton(Icons.Outlined.SentimentSatisfiedAlt, "表情") { onAction(MarkdownAction.Emoji) }
        ToolbarButton(Icons.Filled.AttachFile, "上传") { onAction(MarkdownAction.Attachment) }
        ToolbarButton(Icons.Filled.Gif, "插入 GIF") { onAction(MarkdownAction.Gif) }
        ToolbarButton(Icons.Filled.Visibility, "预览") { onAction(MarkdownAction.TogglePreview) }
        Box {
            ToolbarButton(Icons.Filled.AddCircleOutline, "选项") { optionsMenu = true }
            ToolbarDropdown(
                expanded = optionsMenu,
                onDismiss = { optionsMenu = false },
                items = listOf(
                    "代码块" to MarkdownAction.CodeBlock,
                    "插入表格" to MarkdownAction.InsertTable,
                    "应用 wrap" to MarkdownAction.ApplyWrap,
                ),
                onAction = onAction,
            )
        }
    }
    HorizontalDivider(color = nc.outlineVariant)
}

@Composable
private fun ToolbarDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    items: List<Pair<String, MarkdownAction>>,
    onAction: (MarkdownAction) -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        items.forEach { (label, action) ->
            DropdownMenuItem(
                text = { Text(label) },
                onClick = {
                    onDismiss()
                    onAction(action)
                },
            )
        }
    }
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
        // 标题层级:官网 applyHeading(level),level 0 表示去掉行首的 #
        MarkdownAction.Heading1 -> MarkdownEditing.applyHeading(value, 1)
        MarkdownAction.Heading2 -> MarkdownEditing.applyHeading(value, 2)
        MarkdownAction.Heading3 -> MarkdownEditing.applyHeading(value, 3)
        MarkdownAction.Heading4 -> MarkdownEditing.applyHeading(value, 4)
        MarkdownAction.HeadingParagraph -> MarkdownEditing.applyHeading(value, 0)
        // 官网 heading-small 插入 <small></small>
        MarkdownAction.HeadingSmall -> MarkdownEditing.applySurround(value, { "<small>" }, "</small>", "小号文字")
        // 官网 checklist 插件插入 "- [ ] "
        MarkdownAction.Checklist -> MarkdownEditing.applyLinePrefix(value, { "- [ ] " }, "列表条目")
        // + 菜单:代码块走与 code 相同的 formatCode
        MarkdownAction.CodeBlock -> MarkdownEditing.formatCode(value, "在此处键入或粘贴代码")
        MarkdownAction.InsertTable -> MarkdownEditing.insertTable(value)
        // 官网 apply_wrap 插入 [wrap]…[/wrap]
        MarkdownAction.ApplyWrap -> MarkdownEditing.applySurround(value, { "[wrap]" }, "[/wrap]", "内容")
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
        val markdown = "\n![]($url)\n"
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
