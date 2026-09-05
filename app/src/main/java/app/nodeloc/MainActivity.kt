package app.nodeloc

import android.os.Bundle
import androidx.compose.runtime.mutableStateOf
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.nodeloc.data.NotificationSync
import app.nodeloc.ui.AppRoot
import app.nodeloc.ui.theme.NodelocTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val openNotifications = mutableStateOf(false)
    private val notificationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openNotifications.value = intent.getBooleanExtra("open_notifications", false)
        setContent {
            NodelocTheme {
                AppRoot(
                    openNotifications = openNotifications.value,
                    onNotificationsOpened = { openNotifications.value = false },
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("open_notifications", false)) {
            openNotifications.value = true
        }
    }

    override fun onResume() {
        super.onResume()
        notificationScope.launch {
            NotificationSync.check(this@MainActivity)
        }
    }

    override fun onDestroy() {
        notificationScope.cancel()
        super.onDestroy()
    }
}
