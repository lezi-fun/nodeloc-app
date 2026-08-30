package app.nodeloc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.nodeloc.ui.AppRoot
import app.nodeloc.ui.theme.NodelocTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NodelocTheme { AppRoot() } }
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val data: Uri = intent.data ?: return

        // 处理 nodeloc://auth 回调
        if (data.scheme == "nodeloc" && data.host == "auth") {
            val payload = data.getQueryParameter("payload")
            if (payload != null) {
                // 通知 AppRoot 处理登录回调
                AuthCallbackHandler.onAuthCallback(payload)
                Log.d("MainActivity", "Auth callback received: payload length=${payload.length}")
            }
        }
    }
}

object AuthCallbackHandler {
    private var callback: ((String) -> Unit)? = null

    fun setCallback(cb: (String) -> Unit) {
        callback = cb
    }

    fun onAuthCallback(payload: String) {
        callback?.invoke(payload)
    }

    fun clearCallback() {
        callback = null
    }
}