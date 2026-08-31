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
 * 官网 composer 的预览完全跑在浏览器里(discourse-markdown-it),Discourse 没有服务端预览端点
 * (POST /posts/preview.json 一律 404),所以预览只能在客户端渲染。这里用 CommonMark 对齐
 * markdown-it 的同一份规范,产出的 HTML 交给 [CookedText] —— 与帖子正文共用一套排版。
 *
 * 关键选项照 discourse-markdown-it/src/setup.js 的 markdownItOptions 抄:
 *   html: true、xhtmlOut: false、breaks: !traditional_markdown_linebreaks
 * NodeLoc 未开 traditional_markdown_linebreaks(帖子 cooked 里单换行是 <br>),
 * 所以 softbreak 要渲染成 <br>,而不是 CommonMark 默认的换行符。
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
        // 对齐 markdown-it 的 breaks: true
        .softbreak("<br>\n")
        // 对齐 html: true —— 正文里的原始 HTML 交给 CookedText 处理,不在这里转义
        .escapeHtml(false)
        .build()

    /**
     * :emoji: 简码。官网 emoji.js 只在简码命中已知 emoji 表时才替换成图片,
     * 未命中就保留原文;这里没有完整 emoji 表,退一步只认长度合理的简码,
     * 图片 URL 与站点 cooked 保持一致(emoji_set = unicode)。
     */
    private val emojiShortcode = Regex(""":([a-z0-9][a-z0-9_+-]{0,30}):""")

    /** 把编辑器里的 Markdown 原文渲染成与 cooked 同构的 HTML */
    fun toHtml(raw: String): String {
        if (raw.isBlank()) return ""
        return expandEmoji(renderer.render(parser.parse(raw)))
    }

    /** 与官网 emoji.js 产出的 img 属性对齐:title/alt 带冒号,固定 20x20,loading=lazy */
    private fun expandEmoji(html: String): String = emojiShortcode.replace(html) { match ->
        val name = match.groupValues[1]
        val title = ":$name:"
        """<img src="${DiscourseApi.BASE}/images/emoji/unicode/$name.png?v=15" """ +
            """title="$title" class="emoji" alt="$title" loading="lazy" width="20" height="20">"""
    }
}
