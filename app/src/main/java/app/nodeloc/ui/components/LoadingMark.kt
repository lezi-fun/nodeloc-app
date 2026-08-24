package app.nodeloc.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

private const val LOADING_HTML_PREFIX =
    "<!doctype html><html><head><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\" />" +
    "<style>html,body{margin:0;padding:0;background:transparent;height:100%;display:flex;" +
    "align-items:center;justify-content:center;overflow:hidden}</style></head>" +
    "<body><div id=\"wrap\">"

private const val LOADING_HTML_SUFFIX = "</div>" +
    "<script>" +
    "setInterval(function(){var w=document.getElementById('wrap');" +
    "w.innerHTML=w.innerHTML;},2600);" +
    "</script></body></html>"

/**
 * 官方加载动画:直接渲染 assets/loading.svg(字标描线),
 * JS 定时重放以形成循环,视觉与网页完全一致。
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoadingMark(modifier: Modifier = Modifier, height: Dp = 56.dp) {
    val bg = MaterialTheme.colorScheme.background
    AndroidView(
        modifier = modifier.then(Modifier.size(height * (960f / 280f)).aspectRatio(960f / 280f)),
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            }
        },
        update = { web ->
            val svg = web.context.assets.open("loading.svg").bufferedReader().use { it.readText() }
            val html = LOADING_HTML_PREFIX + svg + LOADING_HTML_SUFFIX +
                "<style>body{background:" + Integer.toHexString(bg.toArgb()) + "}</style>"
            web.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
        },
    )
}
