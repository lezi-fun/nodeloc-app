package app.nodeloc.ui.components

import app.nodeloc.data.DiscourseApi
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

/**
 * 本地 Markdown 预览渲染。
 *
 * 官网 composer 的预览完全跑在浏览器里(markdown-it),Discourse 没有对应的服务端预览端点
 * (POST /posts/preview.json 返回 404),所以预览必须在客户端渲染。这里用 CommonMark
 * 走同一份规范,并补上官网启用的 GFM 表格、删除线、自动链接扩展,产出的 HTML 交给
 * [CookedText] —— 与帖子正文共用一套排版和图片/链接处理。
 */
object MarkdownPreview {
    private val extensions = listOf(
        TablesExtension.create(),
        StrikethroughExtension.create(),
        AutolinkExtension.create(),
    )

    private val parser: Parser = Parser.builder().extensions(extensions).build()

    private val renderer: HtmlRenderer = HtmlRenderer.builder()
        .extensions(extensions)
        .build()

    /** Discourse 的 :emoji: 简码,官网渲染成 emoji 图片,这里复用同一份 CDN 路径 */
    private val emojiShortcode = Regex(""":([a-z0-9_+-]+):""")

    /** 把编辑器里的 Markdown 原文渲染成与 cooked 同构的 HTML */
    fun toHtml(raw: String): String {
        if (raw.isBlank()) return ""
        val html = renderer.render(parser.parse(raw))
        return expandEmoji(html)
    }

    private fun expandEmoji(html: String): String = emojiShortcode.replace(html) { match ->
        val name = match.groupValues[1]
        """<img src="${DiscourseApi.BASE}/images/emoji/twitter/$name.png?v=12" """ +
            """alt="$name" title="$name" class="emoji" loading="lazy">"""
    }
}
