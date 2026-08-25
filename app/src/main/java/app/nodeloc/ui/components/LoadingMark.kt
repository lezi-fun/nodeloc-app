package app.nodeloc.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import java.util.Locale

/** 直接渲染与 NodeLoc 首页一致的 assets/loading.svg 描线动画。 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoadingMark(modifier: Modifier = Modifier, height: Dp = 64.dp) {
    val context = LocalContext.current
    val background = MaterialTheme.colorScheme.background
    val svg = remember {
        context.assets.open("loading.svg").bufferedReader().use { it.readText() }
    }
    val html = remember(svg, background) {
        val color = String.format(Locale.US, "#%06X", background.toArgb() and 0xFFFFFF)
        """
            <!doctype html><html><head>
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <style>
              html,body{margin:0;width:100%;height:100%;overflow:hidden;background:$color}
              body,#mark{display:flex;align-items:center;justify-content:center}
              #mark{width:100%;height:100%}
              #mark svg{display:block;width:100%;height:100%}
            </style></head><body><div id="mark">$svg</div></body></html>
        """.trimIndent()
    }

    AndroidView(
        modifier = modifier.then(Modifier.height(height).aspectRatio(960f / 280f)),
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                settings.javaScriptEnabled = true
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
            }
        },
        update = { web ->
            val version = html.hashCode()
            if (web.tag != version) {
                web.tag = version
                web.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null)
            }
        },
    )
}
