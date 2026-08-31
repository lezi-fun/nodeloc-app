package app.nodeloc.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.nodeloc.data.DiscourseApi
import app.nodeloc.ui.theme.LocalNodelocColors

/**
 * 应用内第三方登录。
 *
 * Discourse 的 OAuth 会话完全建立在服务端:`/auth/{provider}` 302 到提供商,授权后回到
 * 站点自己的 `/auth/{provider}/callback`(redirect_uri 注册在提供商侧,改不了),
 * Discourse 在这一步建立会话并下发 `_t` / `_forum_session` cookie,然后 302 回首页。
 *
 * 所以"换 token"这一步并不存在,也不能在后台重放 callback —— OAuth 的 state 是一次性的,
 * 重放会失败。正确做法是让 WebView 走完 callback 拿到 cookie,拦到跳转离开 callback 时
 * 把 cookie 交给调用方同步进 SessionStore,随后关闭 WebView。
 */
@Composable
fun OAuthLoginDialog(
    providerName: String,
    providerLabel: String,
    onDismiss: () -> Unit,
    onAuthenticated: (cookieHeader: String) -> Unit,
) {
    val nc = LocalNodelocColors.current
    var progress by remember { mutableStateOf(0) }
    var pageTitle by remember { mutableStateOf(providerLabel) }
    // callback 命中后只回调一次,避免后续 302 重复触发
    val handled = remember { java.util.concurrent.atomic.AtomicBoolean(false) }

    /** 命中回调:抓取站点域下的 cookie 并交回调用方 */
    fun finishIfAuthenticated(): Boolean {
        if (handled.get()) return true
        val cookies = CookieManager.getInstance().getCookie(DiscourseApi.BASE)
        // Discourse 建立会话的标志是下发了 _t(长期登录)或 _forum_session
        if (cookies.isNullOrBlank() || !cookies.contains("_t=")) return false
        if (!handled.compareAndSet(false, true)) return true
        onAuthenticated(cookies)
        return true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BackHandler { onDismiss() }
        Column(Modifier.fillMaxSize().background(nc.background)) {
            Row(
                Modifier.fillMaxWidth().height(56.dp).background(nc.headerBg).padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, "关闭", tint = nc.onBackground)
                }
                Text(
                    pageTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = nc.onBackground,
                    maxLines = 1,
                    modifier = Modifier.padding(end = 12.dp),
                )
            }
            if (progress in 1..99) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    color = nc.primary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Box(Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        CookieManager.getInstance().setAcceptCookie(true)
                        @SuppressLint("SetJavaScriptEnabled")
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                ): Boolean {
                                    val url = request?.url?.toString() ?: return false
                                    // 命中站点回调:会话已在服务端建立,拦下来收 cookie
                                    if (isAuthCallback(url) && finishIfAuthenticated()) return true
                                    return false
                                }

                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    // callback 常以 302 直接跳走,不经 shouldOverrideUrlLoading,这里补一次
                                    if (url != null && isAuthCallback(url)) finishIfAuthenticated()
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    progress = 100
                                    view?.title?.takeIf { it.isNotBlank() }?.let { pageTitle = it }
                                    // 授权完成后 Discourse 会 302 回站内页面,此时 cookie 已就绪
                                    if (url != null && url.startsWith(DiscourseApi.BASE)) finishIfAuthenticated()
                                }
                            }
                            loadUrl("${DiscourseApi.BASE}/auth/$providerName")
                        }
                    },
                )
            }
        }
    }
}

/** Discourse 的第三方回调地址形如 /auth/{provider}/callback */
private fun isAuthCallback(url: String): Boolean =
    url.startsWith(DiscourseApi.BASE) && url.contains("/auth/") && url.contains("/callback")
