package app.nodeloc.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
                                append(n.text())
                                pop()
                            } else {
                                append(n.text())
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
 * data-app-install 是安装 ID,拼出的 webview 地址本身是自包含页面(内部已用 srcdoc iframe 隔离),
 * 直接交给 WebView 加载即可,无需再单独请求 /apps/installs/{id}/render。
 */
@Composable
private fun AppEmbedBlock(node: Element, topicReferer: String?) {
    val nc = MaterialTheme.colorScheme
    val installId = node.attr("data-app-install").takeIf { it.isNotBlank() } ?: return
    val url = DiscourseApi.BASE + "/apps/installs/" + installId + "/webview"
    var loadFailed by remember(url) { mutableStateOf(false) }
    Surface(shape = RoundedCornerShape(12.dp), color = nc.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
        if (loadFailed) {
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Text("小程序加载失败", color = nc.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            key(url) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            // 小程序的 srcdoc 子 iframe 里内嵌了 data: URI 字体等资源,
                            // 系统 WebView 默认对混合来源较严格,需要放开才能正常渲染
                            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            settings.allowFileAccess = false
                            settings.userAgentString = settings.userAgentString + " NodeLocAndroid/0.1"
                            CookieManager.getInstance().setAcceptCookie(true)
                            buildString {
                                SessionStore.tCookie?.let { append("_t=").append(it).append(';') }
                                SessionStore.sessionCookie?.let { append("_forum_session=").append(it).append(';') }
                            }.takeIf { it.isNotBlank() }?.let {
                                CookieManager.getInstance().setCookie(DiscourseApi.BASE, it)
                            }
                            CookieManager.getInstance().flush()
                            webViewClient = object : WebViewClient() {
                                override fun onReceivedError(
                                    view: WebView,
                                    request: android.webkit.WebResourceRequest,
                                    error: android.webkit.WebResourceError,
                                ) {
                                    if (request.isForMainFrame) loadFailed = true
                                }
                            }
                            loadUrl(url, topicReferer?.let { mapOf("Referer" to it) } ?: emptyMap())
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(520.dp),
                )
            }
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
        node.selectFirst(":scope > summary")?.text()?.trim().takeUnless { it.isNullOrBlank() } ?: "显示隐藏内容"
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

@Composable
private fun OneboxBlock(node: Element, onOpenUrl: (String) -> Unit) {
    val nc = MaterialTheme.colorScheme
    val link = node.selectFirst("a[href]") ?: node.closest("a[href]")
    val href = link?.absUrl("href")?.takeIf { it.isNotBlank() }
        ?: node.attr("data-onebox-src").takeIf { it.isNotBlank() }
        ?: return
    val title = node.selectFirst(".onebox-body h3, .onebox-body h4, h3, h4, .title")?.text()?.trim()
        .takeUnless { it.isNullOrBlank() } ?: href.removePrefix("https://").removePrefix("http://")
    val description = node.selectFirst(".description, .onebox-body p, .excerpt")?.text()?.trim()
    val image = node.selectFirst("img[src]")?.let { resolveUrl(it.attr("src")) }
    Surface(
        onClick = { onOpenUrl(href) },
        shape = RoundedCornerShape(12.dp),
        color = nc.surfaceVariant.copy(alpha = 0.65f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (image != null) {
                AsyncImage(
                    model = image,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.width(10.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = nc.onSurface, maxLines = 2)
                Text(href.removePrefix("https://").removePrefix("http://"), style = MaterialTheme.typography.labelSmall, color = nc.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!description.isNullOrBlank()) Text(description, style = MaterialTheme.typography.bodySmall, color = nc.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
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
