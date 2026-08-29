package app.nodeloc.ui.components

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownEditingTest {

    private fun field(text: String, start: Int, end: Int = start) =
        TextFieldValue(text, TextRange(start, end))

    @Test
    fun `bold with no selection inserts placeholder and selects it`() {
        val result = MarkdownEditing.applySurround(field("", 0), { "**" }, "**", "粗体文本")
        assertEquals("**粗体文本**", result.text)
        assertEquals(TextRange(2, 2 + "粗体文本".length), result.selection)
    }

    @Test
    fun `bold wraps selected text`() {
        val text = "hello world"
        val result = MarkdownEditing.applySurround(field(text, 6, 11), { "**" }, "**", "粗体文本")
        assertEquals("hello **world**", result.text)
    }

    @Test
    fun `bold unwraps when selection already surrounded`() {
        val text = "hello **world**"
        // 选中包含 ** 标记的 "**world**"
        val result = MarkdownEditing.applySurround(field(text, 6, 16), { "**" }, "**", "粗体文本")
        assertEquals("hello world", result.text)
    }

    @Test
    fun `bullet list prefixes single line`() {
        val text = "todo item"
        val result = MarkdownEditing.applyLinePrefix(field(text, 0, text.length), { "* " }, "列表条目")
        assertEquals("* todo item", result.text)
    }

    @Test
    fun `bullet list toggles off when already prefixed`() {
        val text = "* todo item"
        val result = MarkdownEditing.applyLinePrefix(field(text, 0, text.length), { "* " }, "列表条目")
        assertEquals("todo item", result.text)
    }

    @Test
    fun `bullet list applies to every selected line`() {
        val text = "one\ntwo\nthree"
        val result = MarkdownEditing.applyLinePrefix(field(text, 0, text.length), { "* " }, "列表条目")
        assertEquals("* one\n* two\n* three", result.text)
    }

    @Test
    fun `ordered list numbers increment per line`() {
        val text = "one\ntwo\nthree"
        val head: (String?) -> String = { prev ->
            if (prev == null) "1. " else (prev.trim().removeSuffix(".").toIntOrNull()?.plus(1) ?: 1).toString() + ". "
        }
        val result = MarkdownEditing.applyLinePrefix(field(text, 0, text.length), head, "列表条目")
        assertEquals("1. one\n2. two\n3. three", result.text)
    }

    @Test
    fun `quote with no selection inserts placeholder`() {
        val result = MarkdownEditing.applyLinePrefix(field("", 0), { "> " }, "块引用")
        assertEquals("> 块引用", result.text)
    }

    @Test
    fun `heading applies to current line only`() {
        val text = "first\nsecond\nthird"
        // 光标落在 "second" 行(索引 6..12)
        val result = MarkdownEditing.applyHeading(field(text, 8, 8), 3)
        assertEquals("first\n### second\nthird", result.text)
    }

    @Test
    fun `heading toggles off when same level already applied`() {
        val text = "### second"
        val result = MarkdownEditing.applyHeading(field(text, 5, 5), 3)
        assertEquals("second", result.text)
    }

    @Test
    fun `heading replaces existing different level`() {
        val text = "## second"
        val result = MarkdownEditing.applyHeading(field(text, 5, 5), 3)
        assertEquals("### second", result.text)
    }

    @Test
    fun `code with empty blank line inserts fenced block placeholder`() {
        val result = MarkdownEditing.formatCode(field("", 0), "在此处键入或粘贴代码")
        assertEquals("```\n在此处键入或粘贴代码\n```", result.text)
    }

    @Test
    fun `code wraps single line selection with backticks`() {
        val text = "hello world"
        val result = MarkdownEditing.formatCode(field(text, 6, 11), "placeholder")
        assertEquals("hello `world`", result.text)
    }

    @Test
    fun `code wraps multiline selection in fenced block`() {
        val text = "one\ntwo"
        val result = MarkdownEditing.formatCode(field(text, 0, text.length), "placeholder")
        assertEquals("```\none\ntwo\n```", result.text)
    }

    @Test
    fun `attachment inserts an image without visible filename`() {
        val result = MarkdownEditingActions.insertAttachment(field("", 0), "https://www.nodeloc.com/uploads/example.png")
        assertEquals("\n![](https://www.nodeloc.com/uploads/example.png)\n", result.text)
    }
    @Test
    fun `link with no selection inserts placeholder label`() {
        val result = MarkdownEditing.applyLink(field("", 0), "https://example.com", "文本")
        assertEquals("[文本](https://example.com)", result.text)
    }

    @Test
    fun `link wraps selected text as label`() {
        val text = "click here"
        val result = MarkdownEditing.applyLink(field(text, 6, 10), "https://example.com", "文本")
        assertEquals("click [here](https://example.com)", result.text)
    }
}
