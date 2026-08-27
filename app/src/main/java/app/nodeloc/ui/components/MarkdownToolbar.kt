package app.nodeloc.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    Bold, Italic, Heading, Link, Quote, Code, BulletList, OrderedList
}

@Composable
fun MarkdownToolbar(onAction: (MarkdownAction) -> Unit, modifier: Modifier = Modifier) {
    val nc = LocalNodelocColors.current
    Row(modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 2.dp)) {
        ToolbarButton("B", FontWeight.Bold) { onAction(MarkdownAction.Bold) }
        ToolbarButton("I", FontWeight.Normal) { onAction(MarkdownAction.Italic) }
        ToolbarButton("H", FontWeight.Bold) { onAction(MarkdownAction.Heading) }
        ToolbarButton("🔗", FontWeight.Normal) { onAction(MarkdownAction.Link) }
        ToolbarButton("❝", FontWeight.Bold) { onAction(MarkdownAction.Quote) }
        ToolbarButton("</>", FontWeight.Normal) { onAction(MarkdownAction.Code) }
        ToolbarButton("•≡", FontWeight.Normal) { onAction(MarkdownAction.BulletList) }
        ToolbarButton("1.≡", FontWeight.Normal) { onAction(MarkdownAction.OrderedList) }
    }
    HorizontalDivider(color = nc.outlineVariant)
}

@Composable
private fun ToolbarButton(label: String, weight: FontWeight, onClick: () -> Unit) {
    val nc = LocalNodelocColors.current
    TextButton(onClick = onClick) {
        Text(label, fontWeight = weight, color = nc.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

/** 把工具栏点击映射到 [MarkdownEditing] 的文本操作,官网中文占位符文案照抄 composer.*_text i18n key。 */
object MarkdownEditingActions {
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
    }
}
