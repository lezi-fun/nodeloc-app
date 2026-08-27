package app.nodeloc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.nodeloc.data.DiscourseApi
import app.nodeloc.data.model.RewardDto
import app.nodeloc.ui.theme.LocalNodelocColors
import kotlinx.coroutines.launch

/**
 * discourse-reward"打赏这个帖主"弹窗。官网默认金额来自站点设置(未暴露给客户端接口),
 * 这里固定用官网观察到的默认值 10;数量步进用 -/+ 而非任意输入,与官网一致。
 */
@Composable
fun RewardDialog(targetUsername: String, onDismiss: () -> Unit, onRewarded: (RewardDto) -> Unit, postId: Long) {
    val nc = LocalNodelocColors.current
    val scope = rememberCoroutineScope()
    var amount by remember { mutableIntStateOf(10) }
    var note by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!submitting) onDismiss() },
        title = { Text("打赏 @$targetUsername", color = nc.onBackground) },
        text = {
            androidx.compose.foundation.layout.Column {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("说点什么", color = nc.onSurfaceVariant) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = nc.outlineVariant,
                        focusedBorderColor = nc.primary,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 12.dp))
                Row(
                    Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { if (amount > 1) amount-- }, enabled = amount > 1) {
                        Text("−", style = MaterialTheme.typography.titleLarge, color = nc.onBackground)
                    }
                    Text(
                        amount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = nc.onBackground,
                        modifier = Modifier.width(48.dp),
                    )
                    IconButton(onClick = { amount++ }) {
                        Text("+", style = MaterialTheme.typography.titleLarge, color = nc.onBackground)
                    }
                }
                errorMsg?.let { msg ->
                    Text(msg, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            if (submitting) {
                CircularProgressIndicator(color = nc.primary, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            } else {
                TextButton(onClick = {
                    submitting = true
                    errorMsg = null
                    scope.launch {
                        runCatching { DiscourseApi.giveReward(postId, amount, note) }
                            .onSuccess { r -> if (r.success && r.reward != null) onRewarded(r.reward) else errorMsg = r.message ?: "打赏失败" }
                            .onFailure { errorMsg = it.message ?: "打赏失败" }
                        submitting = false
                    }
                }) { Text("打赏这个帖主", color = nc.primary) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !submitting) { Text("取消", color = nc.onSurfaceVariant) }
        },
    )
}
