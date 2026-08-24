package fun.lezi.nodeloc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import fun.lezi.nodeloc.ui.AppRoot
import fun.lezi.nodeloc.ui.theme.NodelocTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NodelocTheme { AppRoot() } }
    }
}