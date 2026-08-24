package app.nodeloc.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

private val EmojiBox = 20.dp

/** Discourse cooked HTML → Compose 富文本(emoji 内联 / 图片 / 链接 / 引用 / 代码 / 列表) */
@Composable
fun CookedText(html: String, modifier: Modifier = Modifier) {
    val body = remember(html) { Jsoup.parseBodyFragment(html).body() }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        body.childNodes().forEach { RenderBlock(it) }
    }
}

@Composable
private fun RenderBlock(node: Node) {
    val nc = MaterialTheme.colorScheme
    when (node) {
        is TextNode -> {
            if (node.text().isNotBlank())
                Text(node.text().trim(), style = MaterialTheme.typography.bodyMedium, color = nc.onSurface)
        }
        is Element -> when (node.tagName().lowercase()) {
            "p" -> InlineFlow(node)
            "img" -> BlockImage(node)
            "a" -> LinkBlock(node)
            "blockquote", "aside" -> QuoteBlock(node)
            "pre" -> CodeBlock(node)
            "ul", "ol" -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                node.children().forEachIndexed { i, li ->
                    Row {
                        Text(
                            if (node.tagName() == "ol") (i + 1).toString() + ". " else "• ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = nc.onSurfaceVariant,
                        )
                        Column { li.childNodes().forEach { RenderBlock(it) } }
                    }
                }
            }
            "hr" -> Box(Modifier.fillMaxWidth().height(1.dp).background(nc.outlineVariant))
            "h1", "h2", "h3", "h4", "h5", "h6" ->
                Text(node.text(), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = nc.onBackground)
            "br" -> {}
            else -> if (node.childNodeSize() > 0 || node.text().isNotBlank()) InlineFlow(node)
        }
    }
}

@Composable
private fun InlineFlow(element: Element) {
    val nc = MaterialTheme.colorScheme
    val inline = remember(element) { mutableMapOf<String, InlineTextContent>() }
    val annotated = remember(element) {
        buildAnnotatedString {
            fun walk(n: Node, bold: Boolean, italic: Boolean, mono: Boolean, link: String?) {
                when (n) {
                    is TextNode -> {
                        if (n.text().isEmpty()) return
                        withStyle(SpanStyle(
                            fontWeight = if (bold) FontWeight.Bold else null,
                            fontStyle = if (italic) FontStyle.Italic else null,
                            fontFamily = if (mono) FontFamily.Monospace else null,
                            color = if (link != null) Color(0xFF009966.toInt()) else Color.Unspecified,
                            textDecoration = link?.let { TextDecoration.Underline },
                        )) { append(n.text()) }
                    }
                    is Element -> when (n.tagName().lowercase()) {
                        "img" -> n.attr("src").takeIf { it.isNotBlank() }?.let { src ->
                            val url = resolveUrl(src)
                            if (url != null) {
                                val key = "ic" + inline.size
                                inline[key] = InlineTextContent(
                                    Placeholder(EmojiBox.value.sp, EmojiBox.value.sp, PlaceholderVerticalAlign.TextCenter),
                                ) { AsyncImage(model = url, contentDescription = null,
                                        modifier = Modifier.width(EmojiBox).height(EmojiBox),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit) }
                                appendInlineContent(key, "[emoji]")
                            }
                        }
                        "br" -> append('\n')
                        "a" -> n.childNodes().forEach { walk(it, bold, italic, mono, n.attr("href")) }
                        "b", "strong" -> n.childNodes().forEach { walk(it, true, italic, mono, link) }
                        "i", "em" -> n.childNodes().forEach { walk(it, bold, true, mono, link) }
                        "code" -> n.childNodes().forEach { walk(it, bold, italic, true, link) }
                        "del", "s" -> n.childNodes().forEach { walk(it, bold, italic, mono, link) }
                        else -> n.childNodes().forEach { walk(it, bold, italic, mono, link) }
                    }
                }
            }
            element.childNodes().forEach { walk(it, false, false, false, null) }
        }
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium.copy(color = nc.onSurface),
        inlineContent = inline,
    )
}

@Composable
private fun BlockImage(node: Element) {
    val url = resolveUrl(node.attr("src")) ?: return
    val w = node.attr("width").toFloatOrNull()
    val h = node.attr("height").toFloatOrNull()
    val ratioHeight = if (w != null && h != null && w > 0)
        (h / w * 340f).dp.coerceIn(60.dp, 300.dp) else 200.dp
    AsyncImage(
        model = url, contentDescription = null,
        modifier = Modifier.fillMaxWidth()
            .height(ratioHeight)
            .clip(RoundedCornerShape(12.dp)),
        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
    )
}

@Composable
private fun LinkBlock(node: Element) {
    val nc = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    val href = node.absUrl("href").ifBlank { node.attr("href") }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(nc.secondaryContainer.copy(alpha = 0.55f))
            .clickable(enabled = href.isNotBlank()) {
                runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(href))) }
            }
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            href.removePrefix("https://").removePrefix("http://"),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = nc.primary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun QuoteBlock(node: Element) {
    val nc = MaterialTheme.colorScheme
    val title = node.selectFirst(".title")?.text()?.trim()
    Surface(shape = RoundedCornerShape(12.dp), color = nc.surfaceVariant) {
        Column(Modifier.fillMaxWidth().padding(10.dp)) {
            if (!title.isNullOrBlank())
                Text(title, style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold, color = nc.onSurfaceVariant)
            Column(Modifier.padding(top = if (title.isNullOrBlank()) 0.dp else 4.dp)) {
                node.childNodes().forEach { child ->
                    if (child is Element && child.className().contains("title")) return@forEach
                    RenderBlock(child)
                }
            }
        }
    }
}

@Composable
private fun CodeBlock(node: Element) {
    val code = node.selectFirst("code")?.wholeText() ?: node.wholeText()
    Text(
        code.trimEnd(),
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp, lineHeight = 19.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
            .padding(10.dp),
    )
}

private fun resolveUrl(src: String): String? {
    if (src.isBlank()) return null
    if (src.startsWith("http")) return src
    return app.nodeloc.data.DiscourseApi.BASE + src
}
