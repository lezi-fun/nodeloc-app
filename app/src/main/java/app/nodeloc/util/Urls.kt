package app.nodeloc.util

/**
 * cooked HTML 里的资源地址有四种形态:绝对 URL、协议相对(`//cdn.example/a.png`)、
 * 站内绝对路径(`/uploads/a.png`)、以及少见的站内相对路径(`uploads/a.png`)。
 *
 * 早先的实现一律做 `BASE + src`,会把协议相对地址拼成 `https://www.nodeloc.com//cdn…`,
 * 导致外链图片/表情整体加载失败。
 */
fun absoluteUrl(src: String?, base: String): String? {
    val s = src?.trim().orEmpty()
    if (s.isEmpty()) return null
    val trimmedBase = base.trimEnd('/')
    return when {
        s.startsWith("//") -> "https:$s"
        s.startsWith("http://") || s.startsWith("https://") -> s
        // data:image/svg+xml;base64,… 之类可直接交给图片加载器
        s.startsWith("data:") -> s
        s.startsWith("/") -> trimmedBase + s
        else -> "$trimmedBase/$s"
    }
}

/** 仅识别需要在离开 NodeLoc 前确认的外部 HTTP(S) 地址。 */
fun isExternalHttpUrl(url: String?, siteHosts: Set<String> = setOf("www.nodeloc.com", "nodeloc.com")): Boolean {
    val uri = runCatching { java.net.URI(url?.trim().orEmpty()) }.getOrNull() ?: return false
    val scheme = uri.scheme?.lowercase() ?: return false
    val host = uri.host?.lowercase() ?: return false
    return scheme in setOf("http", "https") && host !in siteHosts
}
