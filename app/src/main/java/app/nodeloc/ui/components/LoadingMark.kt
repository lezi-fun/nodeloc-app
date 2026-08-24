package app.nodeloc.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

private const val HTML_HEAD =
    "<!doctype html><html><head><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">" +
    "<style>html,body{margin:0;padding:0;height:100%;background:%BG%;overflow:hidden}" +
    "body{display:flex;align-items:center;justify-content:center}" +
    "#wrap svg{height:100%!important;width:auto!important;display:block}" +
    "</style></head><body><div id=\"wrap\">"

private const val HTML_TAIL =
    "</div><script>" +
    "setInterval(function(){var w=document.getElementById('wrap');" +
    "w.innerHTML=w.innerHTML;},2600);" +
    "</script></body></html>"

/**
 * 官方加载动画:直接渲染 assets/loading.svg(字标描线),
 * 注入自适应 CSS 保证缩放入视图,JS 定时重放形成循环。
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoadingMark(modifier: Modifier = Modifier, height: Dp = 64.dp) {
    val bg = MaterialTheme.colorScheme.background
    AndroidView(
        modifier = modifier.then(Modifier.height(height).aspectRatio(960f / 280f)),
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
            }
        },
        update = { web ->
            val argb = Integer.toHexString(bg.toArgb())
            val svg = web.context.assets.open("loading.svg").bufferedReader().use { it.readText() }
            web.loadDataWithBaseURL(
                null,
                HTML_HEAD.replace("%BG%", "#" + argb.substring(2)) + svg + HTML_TAIL,
                "text/html", "utf-8", null,
            )
        },
    )
}
