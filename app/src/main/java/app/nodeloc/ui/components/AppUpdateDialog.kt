package app.nodeloc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import app.nodeloc.ui.theme.LocalNodelocColors

/**
 * 应用更新提示对话框
 *
 * 当 MessageBus 收到更新消息时显示，从 GitHub Release 获取最新版本
 */
@Composable
fun AppUpdateDialog(
    onDismiss: () -> Unit,
    onUpdate: () -> Unit,
    message: String = "发现新版本，建议立即更新以获得最佳体验。",
) {
    val nc = LocalNodelocColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onUpdate) {
                Text(
                    text = "前往下载",
                    color = nc.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "稍后",
                    color = nc.onSurfaceVariant,
                )
            }
        },
        title = {
            Text(
                text = "发现新版本",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        containerColor = nc.surface,
        titleContentColor = nc.onSurface,
        textContentColor = nc.onSurfaceVariant,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    )
}
