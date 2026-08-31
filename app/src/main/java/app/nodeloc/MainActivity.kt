package app.nodeloc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.nodeloc.ui.AppRoot
import app.nodeloc.ui.theme.NodelocTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NodelocTheme { AppRoot() } }
    }
}
