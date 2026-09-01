package app.nodeloc.ui.components

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * 对齐官网 D-Editor(discourse/lib/textarea-text-manipulation.ts)的核心 Markdown 包裹/切换规则,
 * 在 [TextFieldValue] 上重新实现(而非搬运官网压缩后的 Ember 代码 —— 那段逻辑混在框架里且被拆到懒加载
 * chunk,没有 source map,无法作为可复用代码提取)。
 *
 * 简化点(相对官网):
 * - 官网 applyList/applySurround 会在插入列表/引用时按需补空行,把它们隔离成独立段落;
 *   这里跳过该段落隔离逻辑,直接按行加前缀,更适合手机端的短回复场景。
 * - 标题(heading)官网按 selection 逐行套用;这里只对光标所在的当前行生效,多行标题在移动端基本用不到。
 */
object MarkdownEditing {

    /**
     * 对称包裹(粗体/斜体/行内代码等 head==tail 或成对符号)。
     * 无选中内容时插入占位示例文本并选中它;有选中内容时按行切换包裹/去包裹 ——
     * 首行决定这次操作是"包裹"还是"去包裹",之后所有行沿用同一决定(与官网 _getMultilineContents 一致)。
     */
    fun applySurround(
        value: TextFieldValue,
        head: (String?) -> String,
        tail: String,
        placeholder: String,
    ): TextFieldValue {
        val text = value.text
        val start = value.selection.min
        var end = value.selection.max
        while (end > start && text[end - 1].isWhitespace()) end--
        val pre = text.substring(0, start)
        val post = text.substring(end)

        if (start == end) {
            val hval = head(null)
            val inserted = hval + placeholder + tail
            val selStart = pre.length + hval.length
            return TextFieldValue(pre + inserted + post, TextRange(selStart, selStart + placeholder.length))
        }

        val selValue = text.substring(start, end)
        val lines = selValue.split("\n")
        var hval = head(null)
        var operation = 0 // 0=未定 1=去包裹 2=包裹
        val out = StringBuilder()
        lines.forEachIndexed { idx, line ->
            if (idx > 0) out.append('\n')
            val matches = line.startsWith(hval) && line.endsWith(tail)
            when {
                operation != 2 && matches -> {
                    operation = 1
                    out.append(line.substring(hval.length, line.length - tail.length))
                    hval = head(hval)
                }
                operation == 0 -> {
                    operation = 2
                    out.append(hval).append(line).append(tail)
                    hval = head(hval)
                }
                operation == 1 -> out.append(line)
                else -> {
                    out.append(hval).append(line).append(tail)
                    hval = head(hval)
                }
            }
        }
        val contents = out.toString()
        val newText = pre + contents + post
        val newSel = if (lines.size == 1) {
            val hlen = head(null).length
            TextRange(start + hlen, start + hlen + selValue.length)
        } else {
            TextRange(start, start + contents.length)
        }
        return TextFieldValue(newText, newSel)
    }

    /**
     * 逐行前缀(无序/有序列表、块引用)。head 是根据上一行前缀算下一行前缀的函数,
     * 有序列表借此实现 "1. " → "2. " 递增;固定前缀(如块引用 "> ")传常量函数即可。
     */
    fun applyLinePrefix(
        value: TextFieldValue,
        head: (String?) -> String,
        placeholder: String,
    ): TextFieldValue {
        val text = value.text
        val start = value.selection.min
        var end = value.selection.max
        while (end > start && text[end - 1].isWhitespace()) end--
        val pre = text.substring(0, start)
        val post = text.substring(end)
        val selValue = if (start == end) placeholder else text.substring(start, end)

        val lines = selValue.split("\n")
        var hval = head(null)
        var operation = 0
        val out = StringBuilder()
        lines.forEachIndexed { idx, line ->
            if (idx > 0) out.append('\n')
            if (line.isEmpty()) { out.append(line); return@forEachIndexed }
            when {
                operation != 2 && line.startsWith(hval) -> {
                    operation = 1
                    out.append(line.substring(hval.length))
                    hval = head(hval)
                }
                operation == 0 -> {
                    operation = 2
                    out.append(hval).append(line)
                    hval = head(hval)
                }
                operation == 1 -> out.append(line)
                else -> {
                    out.append(hval).append(line)
                    hval = head(hval)
                }
            }
        }
        val contents = out.toString()
        return TextFieldValue(pre + contents + post, TextRange(start, start + contents.length))
    }

    /** 标题:只对光标/选区所在的当前行生效,已是同级标题则去掉,否则替换成新级别(0 级=正文,去掉标题标记)。 */
    fun applyHeading(value: TextFieldValue, level: Int): TextFieldValue {
        val text = value.text
        val start = value.selection.min
        val end = value.selection.max
        val lineStart = text.lastIndexOf('\n', (start - 1).coerceAtLeast(0)).let { if (start == 0) -1 else it } + 1
        val lineEndIdx = text.indexOf('\n', end)
        val lineEnd = if (lineEndIdx == -1) text.length else lineEndIdx
        val line = text.substring(lineStart, lineEnd)

        val hashCount = line.takeWhile { it == '#' }.length
        val rest = if (hashCount > 0 && line.length > hashCount && line[hashCount] == ' ') {
            line.substring(hashCount + 1)
        } else {
            line.substring(hashCount)
        }
        val newLine = if (level == 0 || hashCount == level) rest else "#".repeat(level) + " " + rest

        val newText = text.substring(0, lineStart) + newLine + text.substring(lineEnd)
        val newCursor = (lineStart + newLine.length).coerceIn(0, newText.length)
        return TextFieldValue(newText, TextRange(newCursor, newCursor))
    }

    /**
     * 插入 GFM 表格骨架,对齐官网 + 菜单的"插入表格"。
     * 光标落在第一个表头单元格上,方便直接改写。
     */
    fun insertTable(value: TextFieldValue): TextFieldValue {
        val header = "列 1"
        val skeleton = "\n|$header | 列 2 |\n|--- | --- |\n| | |\n"
        val cursor = value.selection.max
        val newText = value.text.substring(0, cursor) + skeleton + value.text.substring(cursor)
        // 选中第一个表头单元格的占位文字
        val selStart = cursor + 2
        return TextFieldValue(newText, TextRange(selStart, selStart + header.length))
    }

    /**
     * 代码:无选中且当前行空白 → 插入代码块占位并选中;单行有选中 → 行内反引号包裹/去包裹;
     * 选中内容跨行 → 整体包成三个反引号代码块。
     */
    fun formatCode(value: TextFieldValue, placeholder: String): TextFieldValue {
        val text = value.text
        val start = value.selection.min
        var end = value.selection.max
        while (end > start && text[end - 1].isWhitespace()) end--
        val pre = text.substring(0, start)
        val post = text.substring(end)
        val selValue = text.substring(start, end)
        val hasNewLine = selValue.contains("\n")

        val lineStart = text.lastIndexOf('\n', (start - 1).coerceAtLeast(0)).let { if (start == 0) -1 else it } + 1
        val lineEndIdx = text.indexOf('\n', start)
        val lineEnd = if (lineEndIdx == -1) text.length else lineEndIdx
        val isBlankLine = text.substring(lineStart, lineEnd).isBlank()

        return when {
            !hasNewLine && selValue.isEmpty() && isBlankLine -> {
                val newText = pre + "```\n" + placeholder + "\n```" + post
                val selStart = pre.length + 4
                TextFieldValue(newText, TextRange(selStart, selStart + placeholder.length))
            }
            !hasNewLine -> applySurround(value, { "`" }, "`", placeholder)
            else -> {
                val preNewline = if (pre.isNotEmpty() && !pre.endsWith("\n")) "\n" else ""
                val postNewline = if (post.isNotEmpty() && !post.startsWith("\n")) "\n" else ""
                val block = "$preNewline```\n$selValue\n```$postNewline"
                val newText = pre + block + post
                val selStart = pre.length + preNewline.length + 4
                TextFieldValue(newText, TextRange(selStart, selStart + selValue.length))
            }
        }
    }

    /** 链接:有选中内容时把它包成 [选中文字](url);无选中内容时插入占位链接文字并选中,方便继续改写。 */
    fun applyLink(value: TextFieldValue, url: String, placeholder: String): TextFieldValue {
        val text = value.text
        val start = value.selection.min
        val end = value.selection.max
        val label = if (start == end) placeholder else text.substring(start, end)
        val inserted = "[$label]($url)"
        val newText = text.substring(0, start) + inserted + text.substring(end)
        return if (start == end) {
            val selStart = start + 1
            TextFieldValue(newText, TextRange(selStart, selStart + label.length))
        } else {
            val cursor = start + inserted.length
            TextFieldValue(newText, TextRange(cursor, cursor))
        }
    }
}
