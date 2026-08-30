package app.nodeloc.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import app.nodeloc.BuildConfig

/**
 * 全局 MessageBus 实例
 */
val LocalMessageBus = staticCompositionLocalOf<MessageBusClient?> { null }

/**
 * 提供 MessageBus 实例的组件
 */
@Composable
fun ProvideMessageBus(content: @Composable () -> Unit) {
    val scope = rememberCoroutineScope()
    val messageBus = remember {
        MessageBusClient(scope).apply {
            // 订阅全局频道
            subscribe("/latest")
            subscribe("/categories")
            subscribe("/delete")
            subscribe("/recover")
            subscribe("/destroy")
            subscribe("/global/asset-version")
            subscribe("/file-change")
            subscribe("/site/banner")
            subscribe("/site/read-only")
            subscribe("/site/house-creatives/anonymous")
        }
    }

    DisposableEffect(Unit) {
        // 启动 MessageBus 长轮询
        messageBus.start()

        // 应用启动时检查更新（只检查一次）
        AppUpdateManager.checkForUpdates(
            currentVersionName = BuildConfig.VERSION_NAME,
            scope = scope
        )

        onDispose {
            messageBus.stop()
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalMessageBus provides messageBus,
        content = content
    )
}
