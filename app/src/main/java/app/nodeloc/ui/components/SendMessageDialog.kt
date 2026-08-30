package app.nodeloc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.nodeloc.data.DiscourseApi
import app.nodeloc.ui.theme.LocalNodelocColors
import kotlinx.coroutines.launch

/** 发私信对话框：标题 + 正文 + 发送按钮 */
@Composable
fun SendMessageDialog(recipientUsername: String, onDismiss: () -> Unit, onSent: () -> Unit) {
    val nc = LocalNodelocColors.current
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    fun send() {
        if (title.trim().isEmpty() || body.trim().isEmpty() || sending) return
        sending = true
        errorMsg = null
        scope.launch {
            runCatching { DiscourseApi.sendPrivateMessage(recipientUsername, title.trim(), body.trim()) }
                .onSuccess { onSent() }
                .onFailure { errorMsg = it.message ?: "发送失败，请稍后再试" }
            sending = false
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier
                .fillMaxWidth(0.92f)
                .background(nc.background, shape = MaterialTheme.shapes.large)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(nc.headerBg)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, "关闭", tint = nc.onBackground) }
                Text(
                    "发私信给 @$recipientUsername",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = nc.onBackground,
                    modifier = Modifier.weight(1f)
                )
                if (sending) {
                    CircularProgressIndicator(
                        color = nc.primary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                } else {
                    TextButton(
                        onClick = ::send,
                        enabled = title.trim().isNotEmpty() && body.trim().isNotEmpty()
                    ) {
                        Text("发送", color = nc.primary)
                    }
                }
            }
            HorizontalDivider(color = nc.outlineVariant)

            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = nc.primary,
                        unfocusedBorderColor = nc.outlineVariant,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("消息内容") },
                    minLines = 6,
                    maxLines = 10,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = nc.primary,
                        unfocusedBorderColor = nc.outlineVariant,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                errorMsg?.let { error ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        error,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
