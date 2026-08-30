package app.nodeloc

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.nodeloc.data.SessionStore
import app.nodeloc.ui.theme.NodelocTheme

/**
 * 小程序 WebView Activity
 * 接收参数: url (必需), name (可选，用于标题)
 */
class MiniAppActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.getStringExtra("url") ?: intent.dataString
        val name = intent.getStringExtra("name") ?: "小程序"

        if (url.isNullOrBlank()) {
            finish()
            return
        }

        setContent {
            NodelocTheme {
                var title by remember { mutableStateOf(name) }
                var loading by remember { mutableStateOf(true) }
                var canGoBack by remember { mutableStateOf(false) }
                var webViewRef by remember { mutableStateOf<WebView?>(null) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(title, maxLines = 1) },
                            navigationIcon = {
                                IconButton(onClick = {
                                    if (canGoBack && webViewRef?.canGoBack() == true) {
                                        webViewRef?.goBack()
                                    } else {
                                        finish()
                                    }
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                ) { padding ->
                    Box(Modifier.fillMaxSize().padding(padding)) {
                        MiniAppWebView(
                            url = url,
                            onTitleChanged = { title = it },
                            onLoadingChanged = { loading = it },
                            onCanGoBackChanged = { canGoBack = it },
                            onWebViewCreated = { webViewRef = it }
                        )

                        if (loading) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)
                            )
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MiniAppWebView(
    url: String,
    onTitleChanged: (String) -> Unit,
    onLoadingChanged: (Boolean) -> Unit,
    onCanGoBackChanged: (Boolean) -> Unit,
    onWebViewCreated: (WebView) -> Unit
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                // 设置 Cookie
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this@apply, true)

                // 注入登录 Cookie
                val tCookie = SessionStore.tCookie
                val sessionCookie = SessionStore.sessionCookie
                if (!tCookie.isNullOrBlank()) {
                    cookieManager.setCookie("https://www.nodeloc.com", "_t=$tCookie")
                }
                if (!sessionCookie.isNullOrBlank()) {
                    cookieManager.setCookie("https://www.nodeloc.com", "_forum_session=$sessionCookie")
                }
                cookieManager.flush()

                // WebView 设置
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    userAgentString = WebSettings.getDefaultUserAgent(context)

                    // 安全设置
                    allowFileAccess = false
                    allowContentAccess = false
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        title?.let { onTitleChanged(it) }
                    }

                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        onLoadingChanged(newProgress < 100)
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        onLoadingChanged(true)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onLoadingChanged(false)
                        onCanGoBackChanged(view?.canGoBack() == true)
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        // 允许在 WebView 内导航
                        return false
                    }
                }

                loadUrl(url)
                onWebViewCreated(this)
            }
        },
        update = { webView ->
            // 当 URL 变化时重新加载
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        }
    )
}
