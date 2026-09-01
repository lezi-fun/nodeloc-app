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
 * 应用内第三方登录,流程与官网一致:
 *
 *   1. WebView 打开 `/auth/{provider}` —— Discourse 在这里 302 到提供商的授权页
 *      (实测 /auth/github 带的 redirect_uri 是站点自己的 /auth/github/callback,
 *      注册在提供商侧,改不了,所以整个流程必须在同一个浏览器上下文里走完)
 *   2. 用户在提供商页面(GitHub 等)完成授权
 *   3. 提供商跳回站点域 —— 这一跳就是流程终点:Discourse 在 callback 里建立会话
 *      并下发 `_t` / `_forum_session` cookie,随后 302 到站内页面
 *   4. 一旦发现导航回到站点域,取 CookieManager 里的会话 cookie 交给调用方,关闭 WebView
 *
 * 判定"回到站点域"必须先确认离开过站点([leftSite]),否则第 1 步加载的
 * `/auth/{provider}` 本身就在站点域下,会被误判成已完成。
 *
 * 注意没有"用 code 换 token"这一步:会话由 Discourse 在服务端建立,拿到的凭据就是
 * cookie 本身;OAuth 的 state 一次性,也不能在后台重放 callback。
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
    // 是否已跳到提供商域名下。只有离开过站点,再回到站点才算走完授权
    val leftSite = remember { java.util.concurrent.atomic.AtomicBoolean(false) }
    // 只回调一次,避免后续 302 链上重复触发
    val handled = remember { java.util.concurrent.atomic.AtomicBoolean(false) }

    /**
     * 回到站点域后尝试收尾。返回 true 表示已拿到会话并回调。
     * 拿不到 `_t` 说明会话还没建立(例如首次登录需要补全注册表单),
     * 此时保持 WebView 打开,让用户继续在站内完成,后续页面加载会再判一次。
     */
    fun finishIfAuthenticated(): Boolean {
        if (handled.get()) return true
        if (!leftSite.get()) return false
        val cookies = CookieManager.getInstance().getCookie(DiscourseApi.BASE)
        // `_t` 是 Discourse 的长期登录 cookie,它出现才说明会话真的建立了
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
                                    track(url)
                                    // 从提供商跳回站点域 —— 授权走完了,拦下来收 cookie
                                    if (isSiteUrl(url) && finishIfAuthenticated()) return true
                                    return false
                                }

                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    progress = 10
                                    if (url == null) return
                                    track(url)
                                    // 提供商多以 302 直接跳回,不经 shouldOverrideUrlLoading,这里补一次
                                    if (isSiteUrl(url)) finishIfAuthenticated()
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    progress = 100
                                    view?.title?.takeIf { it.isNotBlank() }?.let { pageTitle = it }
                                    // callback 之后 Discourse 还会 302 到站内页面,cookie 到这里必然已就绪
                                    if (url != null && isSiteUrl(url)) finishIfAuthenticated()
                                }

                                /** 记录是否已离开站点域(即已进入提供商的授权页) */
                                private fun track(url: String) {
                                    if (!isSiteUrl(url)) leftSite.set(true)
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

/** 是否是站点自己的地址(用主机名判断,避免 BASE 带不带 www / 尾斜杠的差异) */
private fun isSiteUrl(url: String): Boolean {
    val host = runCatching { android.net.Uri.parse(url).host }.getOrNull() ?: return false
    val siteHost = runCatching { android.net.Uri.parse(DiscourseApi.BASE).host }.getOrNull() ?: return false
    return host.equals(siteHost, ignoreCase = true) ||
        host.removePrefix("www.").equals(siteHost.removePrefix("www."), ignoreCase = true)
}
