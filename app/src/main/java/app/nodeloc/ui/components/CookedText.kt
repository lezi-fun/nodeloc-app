package app.nodeloc.ui.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import app.nodeloc.MiniAppActivity
import app.nodeloc.data.DiscourseApi
import app.nodeloc.data.SessionStore
import app.nodeloc.util.absoluteUrl
import app.nodeloc.util.isExternalHttpUrl
import app.nodeloc.ui.theme.LocalNodelocColors
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

/** 与官网 img.emoji 规则一致:所有表情(系统/自定义)均为行内 20px */
private val EmojiSize = 20.dp

private sealed interface ParagraphPart {
    data class Inline(val element: Element) : ParagraphPart
    data class Image(val element: Element) : ParagraphPart
}

/** Discourse cooked HTML → Compose 富文本，按网页规则区分系统 emoji、自定义表情与正文图片。 */
@Composable
fun CookedText(
    html: String,
    modifier: Modifier = Modifier,
    topicReferer: String? = null,
) {
    val context = LocalContext.current
    val body = remember(html) { Jsoup.parseBodyFragment(html, DiscourseApi.BASE).body() }
    var previewUrl by remember { mutableStateOf<String?>(null) }
    var pendingExternalUrl by remember { mutableStateOf<String?>(null) }
    fun launchUrl(url: String) {
        runCatching {
            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
        }
    }
    fun openUrl(url: String) {
        if (isExternalHttpUrl(url)) pendingExternalUrl = url else launchUrl(url)
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        body.childNodes().forEach {
            RenderBlock(it, topicReferer, onPreview = { previewUrl = it }, onOpenUrl = ::openUrl)
        }
    }
    previewUrl?.let { url -> ImagePreviewDialog(url, onDismiss = { previewUrl = null }) }
    pendingExternalUrl?.let { url ->
        val host = android.net.Uri.parse(url).host ?: url
        AlertDialog(
            onDismissRequest = { pendingExternalUrl = null },
            title = { Text("打开外部链接？") },
            text = {
                Text(
                    "你即将离开 NodeLoc，外部网站的内容和安全性由其运营方负责。\n\n$host",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingExternalUrl = null
                        launchUrl(url)
                    },
                ) { Text("继续打开") }
            },
            dismissButton = {
                TextButton(onClick = { pendingExternalUrl = null }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun RenderBlock(
    node: Node,
    topicReferer: String?,
    onPreview: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    val nc = MaterialTheme.colorScheme
    when (node) {
        is TextNode -> if (node.text().isNotBlank()) {
            Text(node.text().trim(), style = MaterialTheme.typography.bodyMedium, color = nc.onSurface)
        }
        is Element -> when {
            // 官网信任等级限制提示区块:锁图标 + 文案居中卡片
            node.hasClass("read-permission-notice") -> PermissionNotice(node)
            // discourse-apps 插件的小程序/小游戏嵌入点:cooked 里只有一个空占位 div,
            // 真实界面由 /apps/installs/{id}/webview 这个自包含页面提供
            node.hasClass("discourse-app-embed") -> AppEmbedBlock(node, topicReferer)
            node.hasClass("onebox") || node.hasClass("onebox-body") -> OneboxBlock(node, onOpenUrl)
            else -> when (node.tagName().lowercase()) {
                "p" -> Paragraph(node, topicReferer, onPreview, onOpenUrl)
                "img" -> if (node.isEmoji()) InlineFlow(Element("span").appendChild(node.clone()), onOpenUrl) else ContentImage(node, onPreview)
                "a" -> node.nonEmojiImage()?.let { ContentImage(it, onPreview) } ?: LinkBlock(node, onOpenUrl)
                "blockquote", "aside" -> QuoteBlock(node, topicReferer, onPreview, onOpenUrl)
                "details" -> DetailsBlock(node, topicReferer, onPreview, onOpenUrl)
                "pre" -> CodeBlock(node)
                "ul", "ol" -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    node.children().forEachIndexed { i, li ->
                        Row {
                            Text(
                                if (node.tagName() == "ol") "${i + 1}. " else "• ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = nc.onSurfaceVariant,
                            )
                            Column { li.childNodes().forEach { RenderBlock(it, topicReferer, onPreview, onOpenUrl) } }
                        }
                    }
                }
                "hr" -> Box(Modifier.fillMaxWidth().height(1.dp).background(nc.outlineVariant))
                "h1", "h2", "h3", "h4", "h5", "h6" -> Text(
                    node.text(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = nc.onBackground,
                )
                "br" -> Unit
                else -> if (node.childNodeSize() > 0 || node.text().isNotBlank()) InlineFlow(node, onOpenUrl)
            }
        }
        else -> Unit
    }
}

@Composable
private fun Paragraph(
    element: Element,
    topicReferer: String?,
    onPreview: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    val parts = remember(element) {
        buildList {
            var inline = Element("span")
            fun flushInline() {
                if (inline.childNodeSize() > 0) add(ParagraphPart.Inline(inline))
                inline = Element("span")
            }
            element.childNodes().forEach { child ->
                val image = when (child) {
                    is Element -> when {
                        child.tagName().equals("img", true) && !child.isEmoji() -> child
                        child.tagName().equals("a", true) -> child.imageForDisplay()
                        else -> null
                    }
                    else -> null
                }
                if (image != null) {
                    flushInline()
                    add(ParagraphPart.Image(image))
                } else {
                    inline.appendChild(child.clone())
                }
            }
            flushInline()
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        parts.forEach { part ->
            when (part) {
                is ParagraphPart.Inline -> InlineFlow(part.element, onOpenUrl)
                is ParagraphPart.Image -> ContentImage(part.element, onPreview)
            }
        }
    }
}

@Composable
private fun InlineFlow(element: Element, onOpenUrl: (String) -> Unit) {
    val nc = MaterialTheme.colorScheme
    val inline = remember(element) { mutableMapOf<String, InlineTextContent>() }
    val annotated = remember(element) {
        buildAnnotatedString {
            fun walk(n: Node, bold: Boolean, italic: Boolean, mono: Boolean, link: String?) {
                when (n) {
                    is TextNode -> {
                        if (n.text().isEmpty()) return

                        // 处理 Markdown 图片语法 ![alt](url)
                        val text = n.text()
                        val markdownImageRegex = Regex("""!\[([^\]]*)\]\(([^)]+)\)""")
                        val matches = markdownImageRegex.findAll(text).toList()

                        if (matches.isNotEmpty()) {
                            var lastIndex = 0
                            matches.forEach { match ->
                                // 添加图片前的文本
                                if (match.range.first > lastIndex) {
                                    val beforeText = text.substring(lastIndex, match.range.first)
                                    withStyle(
                                        SpanStyle(
                                            fontWeight = if (bold) FontWeight.Bold else null,
                                            fontStyle = if (italic) FontStyle.Italic else null,
                                            fontFamily = if (mono) FontFamily.Monospace else null,
                                            color = if (link != null) Color(0xFF009966) else Color.Unspecified,
                                            textDecoration = link?.let { TextDecoration.Underline },
                                        ),
                                    ) {
                                        append(beforeText)
                                    }
                                }

                                // 添加图片占位符
                                val alt = match.groupValues[1]
                                val imageUrl = match.groupValues[2]
                                val resolvedUrl = resolveUrl(imageUrl)
                                if (resolvedUrl != null) {
                                    val key = "md-image-${inline.size}"
                                    inline[key] = InlineTextContent(
                                        Placeholder(20.sp, 20.sp, PlaceholderVerticalAlign.TextCenter),
                                    ) {
                                        AsyncImage(
                                            model = resolvedUrl,
                                            contentDescription = alt.ifBlank { null },
                                            modifier = Modifier.size(20.dp),
                                            contentScale = ContentScale.Fit,
                                        )
                                    }
                                    appendInlineContent(key, alt.ifBlank { "[图片]" })
                                }

                                lastIndex = match.range.last + 1
                            }

                            // 添加最后一个图片后的文本
                            if (lastIndex < text.length) {
                                val afterText = text.substring(lastIndex)
                                withStyle(
                                    SpanStyle(
                                        fontWeight = if (bold) FontWeight.Bold else null,
                                        fontStyle = if (italic) FontStyle.Italic else null,
                                        fontFamily = if (mono) FontFamily.Monospace else null,
                                        color = if (link != null) Color(0xFF009966) else Color.Unspecified,
                                        textDecoration = link?.let { TextDecoration.Underline },
                                    ),
                                ) {
                                    append(afterText)
                                }
                            }
                        } else {
                            // 没有 Markdown 图片，正常处理
                            withStyle(
                                SpanStyle(
                                    fontWeight = if (bold) FontWeight.Bold else null,
                                    fontStyle = if (italic) FontStyle.Italic else null,
                                    fontFamily = if (mono) FontFamily.Monospace else null,
                                    color = if (link != null) Color(0xFF009966) else Color.Unspecified,
                                    textDecoration = link?.let { TextDecoration.Underline },
                                ),
                            ) {
                                if (link != null) {
                                    pushStringAnnotation("URL", resolveUrl(link) ?: link)
                                    append(text)
                                    pop()
                                } else {
                                    append(text)
                                }
                            }
                        }
                    }
                    is Element -> when (n.tagName().lowercase()) {
                        "img" -> n.attr("src").takeIf { it.isNotBlank() }?.let { src ->
                            val url = resolveUrl(src)
                            if (url != null) {
                                val key = "emoji-${inline.size}"
                                inline[key] = InlineTextContent(
                                    Placeholder(EmojiSize.value.sp, EmojiSize.value.sp, PlaceholderVerticalAlign.TextCenter),
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = n.attr("alt").ifBlank { null },
                                        modifier = Modifier.width(EmojiSize).height(EmojiSize),
                                        contentScale = ContentScale.Fit,
                                    )
                                }
                                appendInlineContent(key, n.attr("alt").ifBlank { "[emoji]" })
                            }
                        }
                        "br" -> append('\n')
                        "a" -> n.childNodes().forEach { walk(it, bold, italic, mono, n.attr("href")) }
                        "b", "strong" -> n.childNodes().forEach { walk(it, true, italic, mono, link) }
                        "i", "em" -> n.childNodes().forEach { walk(it, bold, true, mono, link) }
                        "code" -> n.childNodes().forEach { walk(it, bold, italic, true, link) }
                        else -> n.childNodes().forEach { walk(it, bold, italic, mono, link) }
                    }
                }
            }
            element.childNodes().forEach { walk(it, false, false, false, null) }
        }
    }

    var textLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium.copy(color = nc.onSurface),
        inlineContent = inline,
        onTextLayout = { textLayout = it },
        modifier = Modifier.pointerInput(annotated) {
            detectTapGestures { position ->
                val offset = textLayout?.getOffsetForPosition(position) ?: return@detectTapGestures
                annotated.getStringAnnotations("URL", offset, offset).firstOrNull()?.let { annotation ->
                    onOpenUrl(annotation.item)
                }
            }
        },
    )
}

@Composable
private fun ContentImage(node: Element, onPreview: (String) -> Unit) {
    val url = resolveUrl(node.attr("src")) ?: return
    val declaredWidth = node.attr("width").toFloatOrNull()?.takeIf { it > 0f }
    val declaredHeight = node.attr("height").toFloatOrNull()?.takeIf { it > 0f }
    val ratio = if (declaredWidth != null && declaredHeight != null) declaredWidth / declaredHeight else null

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        AsyncImage(
            model = url,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (ratio != null) Modifier.aspectRatio(ratio) else Modifier.wrapContentHeight())
                .clip(RoundedCornerShape(8.dp))
                // 与官网 lightbox 一致:点击在应用内放大查看,而非打开外部浏览器
                .clickable { onPreview(url) },
            contentScale = ContentScale.Fit,
        )
    }
}

/**
 * discourse-apps 小程序嵌入(如贪吃蛇等站内小游戏)。
 * 显示为可点击的卡片，点击后打开 MiniAppActivity。
 */
@Composable
private fun AppEmbedBlock(node: Element, topicReferer: String?) {
    val nc = MaterialTheme.colorScheme
    val context = LocalContext.current
    val installId = node.attr("data-app-install").takeIf { it.isNotBlank() } ?: return
    val appName = node.attr("data-app-name").takeIf { it.isNotBlank() } ?: "小程序"
    val url = DiscourseApi.BASE + "/apps/installs/" + installId + "/webview"

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = nc.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = android.content.Intent(context, MiniAppActivity::class.java).apply {
                    putExtra("url", url)
                    putExtra("name", appName)
                }
                context.startActivity(intent)
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Apps,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = nc.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.titleMedium,
                    color = nc.onSurface
                )
                Text(
                    text = "点击打开小程序",
                    style = MaterialTheme.typography.bodySmall,
                    color = nc.onSurfaceVariant
                )
            }
            Icon(
                imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = nc.onSurfaceVariant
            )
        }
    }
}

/** 官网 read-permission-notice 区块:信任等级不足,锁图标与文案居中于圆角卡片 */
@Composable
private fun PermissionNotice(node: Element) {
    val nc = MaterialTheme.colorScheme
    val text = remember(node) {
        node.selectFirst(".read-permission-notice__text")?.text()?.trim().takeUnless { it.isNullOrBlank() }
            ?: node.text().trim()
    }
    Surface(shape = RoundedCornerShape(12.dp), color = nc.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 28.dp),
        ) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = null,
                tint = nc.onSurfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.size(36.dp),
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = nc.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DetailsBlock(
    node: Element,
    topicReferer: String?,
    onPreview: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    val nc = MaterialTheme.colorScheme
    var expanded by remember(node) { mutableStateOf(node.hasAttr("open")) }
    val summary = remember(node) {
        node.children().firstOrNull { it.tagName() == "summary" }?.text()?.trim().takeUnless { it.isNullOrBlank() } ?: "显示隐藏内容"
    }
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = nc.surfaceVariant.copy(alpha = 0.55f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "收起" else "展开",
                    tint = nc.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(summary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = nc.onSurface)
            }
            if (expanded) {
                Column(
                    Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    node.childNodes().forEach { child ->
                        if (child is Element && child.tagName().equals("summary", true)) return@forEach
                        RenderBlock(child, topicReferer, onPreview, onOpenUrl)
                    }
                }
            }
        }
    }
}

/**
 * 外链 onebox 卡片,对齐官网 cooked 的真实结构:
 *
 *   <aside class="onebox allowlistedgeneric" data-onebox-src="…">
 *     <header class="source"><a href="…">example.com</a></header>
 *     <article class="onebox-body">
 *       <div class="aspect-image" style="--aspect-ratio:690/361"><img class="thumbnail" …></div>
 *       <h3><a href="…">标题</a></h3>
 *       <p>描述</p>
 *     </article>
 *   </aside>
 *
 * 视觉规格来自 common/base/onebox.scss:
 *   aside.onebox { @include onebox-shadow(4px); padding: 1em; background: var(--secondary) }
 *   @mixin onebox-shadow($t) { border: 0;
 *     box-shadow: 0 0 0 1px var(--onebox-border-color), 0 0 0 $t var(--onebox-shadow-color) }
 * 即 1px 描边 + 4px 浅色外圈,**没有左侧竖线**(border-left 只出现在
 * `blockquote { aside.onebox { … } }`,也就是被引用块包住时)。
 *
 * 缩略图有两种形态,取决于 cooked 里有没有 .aspect-image 包裹:
 *   有 → [style*="--aspect-ratio"] > img 是 absolute + width/height 100%,按比例铺满宽度
 *   无 → .onebox-body img 是 float:left; max-width:35%(窄屏); max-height:170px,文字排在右侧
 */
@Composable
private fun OneboxBlock(node: Element, onOpenUrl: (String) -> Unit) {
    val nc = MaterialTheme.colorScheme
    val body = node.selectFirst("article.onebox-body") ?: node
    // 标题链接优先取 h3/h4 里的 a,它才是目标页面地址
    val titleLink = body.selectFirst("h3 > a[href], h4 > a[href]")
    val href = titleLink?.absUrl("href")?.takeIf { it.isNotBlank() }
        ?: node.attr("data-onebox-src").takeIf { it.isNotBlank() }
        ?: node.selectFirst("header.source a[href]")?.absUrl("href")?.takeIf { it.isNotBlank() }
        ?: node.selectFirst("a[href]")?.absUrl("href")?.takeIf { it.isNotBlank() }
        ?: return
    // header.source 里就是官网显示的来源域名,没有则从 href 退化解析
    val source = node.selectFirst("header.source a")?.text()?.trim()
        ?.takeIf { it.isNotBlank() } ?: hostOf(href)
    val title = titleLink?.text()?.trim()?.takeIf { it.isNotBlank() }
        ?: body.selectFirst("h3, h4, .title")?.text()?.trim()?.takeIf { it.isNotBlank() }
        ?: source
    val description = body.selectFirst("p")?.text()?.trim()?.takeIf { it.isNotBlank() }
    val aspectWrapper = body.selectFirst("[style*=--aspect-ratio]")
    val thumb = aspectWrapper?.selectFirst("img[src]")
        ?: body.selectFirst("img.thumbnail[src], img[src]")
    val thumbUrl = thumb?.let { resolveUrl(it.attr("src")) }
    // 有 .aspect-image 包裹才铺满宽度;比例优先用 wrapper 的 --aspect-ratio,退回 img 的 width/height
    val fullWidthThumb = aspectWrapper != null
    val aspect = aspectRatioOf(aspectWrapper?.attr("style")) ?: thumb?.let { img ->
        val w = img.attr("width").toFloatOrNull()
        val h = img.attr("height").toFloatOrNull()
        if (w != null && h != null && h > 0f) w / h else null
    }
    // 被引用块包住时官网才加左侧竖线(--d-post-aside-border-left: 5px solid var(--primary-300))
    val insideQuote = node.closest("blockquote, aside.quote") != null

    // 外圈:对应 box-shadow 的 4px --onebox-shadow-color(primary-100)
    Box(
        Modifier
            .fillMaxWidth()
            .background(nc.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(4.dp),
    ) {
        Surface(
            onClick = { onOpenUrl(href) },
            shape = RoundedCornerShape(6.dp),
            color = nc.surface,
            border = BorderStroke(1.dp, nc.outlineVariant),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(Modifier.fillMaxWidth()) {
                if (insideQuote) {
                    Box(Modifier.width(5.dp).fillMaxHeight().background(nc.outlineVariant))
                }
                Column(Modifier.weight(1f).padding(14.dp)) {
                    Text(
                        source,
                        style = MaterialTheme.typography.labelSmall,
                        color = nc.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    // header { margin-bottom: 1em }
                    Spacer(Modifier.height(14.dp))
                    if (thumbUrl != null && fullWidthThumb) {
                        AsyncImage(
                            model = thumbUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(aspect ?: (16f / 9f))
                                .clip(RoundedCornerShape(4.dp)),
                        )
                        Spacer(Modifier.height(10.dp))
                        OneboxText(title, description)
                    } else if (thumbUrl != null) {
                        // float:left 的小图:Compose 里用 Row 近似(文字不会绕到图片下方)
                        Row(Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = thumbUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth(0.35f)
                                    .heightIn(max = 170.dp)
                                    .aspectRatio(aspect ?: 1f)
                                    .clip(RoundedCornerShape(4.dp)),
                            )
                            // img { margin-right: 1em }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) { OneboxText(title, description) }
                        }
                    } else {
                        OneboxText(title, description)
                    }
                }
            }
        }
    }
}

/** h3 { font-size: var(--font-up-1); margin-bottom: 10px; a { color: var(--tertiary) } } + 描述段 */
@Composable
private fun OneboxText(title: String, description: String?) {
    val nc = MaterialTheme.colorScheme
    Text(
        title,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = nc.primary,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
    if (description != null) {
        Spacer(Modifier.height(10.dp))
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = nc.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** 从 style="--aspect-ratio:690/361" 解析宽高比 */
private fun aspectRatioOf(style: String?): Float? {
    val raw = style ?: return null
    val m = Regex("--aspect-ratio:\\s*([0-9.]+)\\s*/\\s*([0-9.]+)").find(raw) ?: return null
    val w = m.groupValues[1].toFloatOrNull() ?: return null
    val h = m.groupValues[2].toFloatOrNull() ?: return null
    return if (h > 0f) w / h else null
}

/** 取 URL 的主机名,作为 header.source 缺失时的来源显示 */
private fun hostOf(url: String): String =
    runCatching { android.net.Uri.parse(url).host.orEmpty() }.getOrNull()
        ?.removePrefix("www.")
        ?.takeIf { it.isNotBlank() }
        ?: url.removePrefix("https://").removePrefix("http://").takeWhile { it != '/' }
@Composable
private fun LinkBlock(node: Element, onOpenUrl: (String) -> Unit) {
    val nc = MaterialTheme.colorScheme
    val href = node.absUrl("href").ifBlank { resolveUrl(node.attr("href")).orEmpty() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(nc.secondaryContainer.copy(alpha = 0.55f))
            .clickable(enabled = href.isNotBlank()) { onOpenUrl(href) }
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            node.text().ifBlank { href.removePrefix("https://").removePrefix("http://") },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = nc.primary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun QuoteBlock(node: Element, topicReferer: String?, onPreview: (String) -> Unit, onOpenUrl: (String) -> Unit) {
    val nc = MaterialTheme.colorScheme
    // 站内话题引用:官网 cooked 里是 <aside class="quote" data-topic="…">,
    // 标题行由头像 + 话题链接 + 分类徽章组成,和外链 onebox 完全不同的形态
    val titleEl = node.selectFirst("div.title")
    if (node.hasAttr("data-topic") && titleEl != null) {
        InternalTopicQuote(node, titleEl, topicReferer, onPreview, onOpenUrl)
        return
    }
    val title = node.selectFirst(".title")?.text()?.trim()
    Surface(shape = RoundedCornerShape(12.dp), color = nc.surfaceVariant) {
        Column(Modifier.fillMaxWidth().padding(10.dp)) {
            if (!title.isNullOrBlank()) {
                Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = nc.onSurfaceVariant)
            }
            Column(Modifier.padding(top = if (title.isNullOrBlank()) 0.dp else 4.dp)) {
                node.childNodes().forEach { child ->
                    if (child is Element && child.className().contains("title")) return@forEach
                    RenderBlock(child, topicReferer, onPreview, onOpenUrl)
                }
            }
        }
    }
}

/**
 * 站内话题引用卡片,对齐官网 cooked 的真实结构:
 *
 *   <aside class="quote" data-topic="105934" data-repost-category="218">
 *     <div class="title">
 *       <img class="avatar" src="…" width="24" height="24">
 *       <div class="quote-title__text-content">
 *         <a href="/t/topic/105934">话题标题</a>
 *         <a class="badge-category__wrapper" …>
 *           <span class="badge-category" style="--category-badge-color:#F7941D; …">
 *             <span class="badge-category__name">分类名</span>
 *           </span>
 *         </a>
 *       </div>
 *     </div>
 *     <blockquote>摘要…</blockquote>
 *   </aside>
 *
 * 视觉规格来自 common/base/topic-post.scss 的 aside.quote:
 *   .title { display:flex; align-items:start; gap:var(--space-2);
 *            padding: 0.8em 0.8em 0 0.8em;              // 注意下内边距是 0
 *            background: var(--d-post-aside-background);
 *            border-left: var(--d-post-aside-border-left) }
 *   blockquote { margin-top: 0; padding: 0.75em }
 * 其中 common/base/discourse.scss 定义:
 *   --d-post-aside-background: var(--blend-primary-secondary-5)   // 极浅的底色
 *   --d-post-aside-border-left: 5px solid var(--primary-300)      // 5px 中性灰,不是强调色
 * 标题与正文共用同一块底色,中间没有分隔线。
 */
@Composable
private fun InternalTopicQuote(
    node: Element,
    titleEl: Element,
    topicReferer: String?,
    onPreview: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    val nc = MaterialTheme.colorScheme
    val topicLink = titleEl.selectFirst(".quote-title__text-content > a[href], a[href]:not(.badge-category__wrapper)")
    val href = topicLink?.absUrl("href")?.takeIf { it.isNotBlank() }
    val title = topicLink?.text()?.trim()?.takeIf { it.isNotBlank() }
    val avatar = titleEl.selectFirst("img.avatar[src]")?.let { resolveUrl(it.attr("src")) }
    val badge = titleEl.selectFirst("span.badge-category")
    val badgeName = badge?.selectFirst(".badge-category__name")?.text()?.trim()
        ?: badge?.text()?.trim()
    val badgeColor = cssVarColor(badge?.attr("style"), "--category-badge-color")
    val badgeTextColor = cssVarColor(badge?.attr("style"), "--category-badge-text-color")
    // --d-post-aside-background 是 primary 混入 secondary 5%,整块(标题+正文)共用
    val asideBg = nc.onSurface.copy(alpha = 0.05f)

    Row(Modifier.fillMaxWidth().background(asideBg)) {
        // --d-post-aside-border-left: 5px solid var(--primary-300)
        Box(Modifier.width(5.dp).fillMaxHeight().background(nc.outlineVariant))
        Column(Modifier.weight(1f)) {
            // .title { padding: 0.8em 0.8em 0 0.8em } —— 没有下内边距,也没有分隔线
            Row(
                Modifier
                    .fillMaxWidth()
                    .then(if (href != null) Modifier.clickable { onOpenUrl(href) } else Modifier)
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                if (avatar != null) {
                    AsyncImage(
                        model = avatar,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                if (title != null) {
                    Text(
                        title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = nc.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
                if (!badgeName.isNullOrBlank()) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier
                            .background(badgeColor ?: nc.secondaryContainer, RoundedCornerShape(2.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            badgeName,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeTextColor ?: nc.onSecondaryContainer,
                            maxLines = 1,
                        )
                    }
                }
            }
            // blockquote { margin-top: 0; padding: 0.75em }
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                node.childNodes().forEach { child ->
                    if (child is Element && child.hasClass("title")) return@forEach
                    RenderBlock(child, topicReferer, onPreview, onOpenUrl)
                }
            }
        }
    }
}

/** 解析官网内联 style 里的 CSS 变量颜色,如 --category-badge-color: #F7941D */
private fun cssVarColor(style: String?, name: String): Color? {
    val raw = style ?: return null
    val m = Regex("$name:\\s*#([0-9a-fA-F]{3,8})").find(raw) ?: return null
    val hex = m.groupValues[1]
    val full = when (hex.length) {
        3 -> hex.map { "$it$it" }.joinToString("")
        6, 8 -> hex
        else -> return null
    }
    return runCatching {
        val v = full.toLong(16)
        if (full.length == 8) Color((v ushr 8 or (v shl 24)).toInt())
        else Color(0xFF000000L.or(v).toInt())
    }.getOrNull()
}

@Composable
private fun CodeBlock(node: Element) {
    val code = node.selectFirst("code")?.wholeText() ?: node.wholeText()
    Text(
        code.trimEnd(),
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
            .padding(10.dp),
    )
}

private fun Element.isEmoji(): Boolean = hasClass("emoji")

private fun Element.imageForDisplay(): Element? {
    // 返回链接中的图片元素，无论是否有额外文本
    return selectFirst("img")?.takeUnless { it.isEmoji() }
}

private fun Element.nonEmojiImage(): Element? = selectFirst("img")?.takeUnless { it.isEmoji() }

private fun resolveUrl(src: String): String? = absoluteUrl(src, DiscourseApi.BASE)
