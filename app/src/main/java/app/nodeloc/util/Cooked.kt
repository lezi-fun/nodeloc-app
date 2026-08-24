package app.nodeloc.util

import android.text.Html

/** cooked HTML → 纯文本(保留段落换行) */
fun cookedToText(cooked: String): String =
    Html.fromHtml(cooked, Html.FROM_HTML_MODE_LEGACY).toString()
        .replace("\n{3,}".toRegex(), "\n\n")
        .trim()