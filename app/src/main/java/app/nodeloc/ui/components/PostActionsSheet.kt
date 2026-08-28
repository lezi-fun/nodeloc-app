package app.nodeloc.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import app.nodeloc.data.DiscourseApi
import app.nodeloc.data.SiteRepo
import app.nodeloc.data.model.PostDto
import app.nodeloc.ui.theme.LocalNodelocColors
import kotlinx.coroutines.launch

/**
 * 举报菜单的一个选项:[id]/[requireMessage] 来自站点 post_action_types(叫什么、要不要补充说明),
 * 但"是否展示"完全看该楼层 [PostDto.actionsSummary] 里对应 id 是否有 can_act=true —— 这才是
 * 服务端逐楼层算出来的真实权限(例如已被举报过的帖子、系统帖等会少几项),不是全站固定不变的。
 */
private data class FlagReason(val id: Int, val label: String, val requireMessage: Boolean)

/** /site.json 的 post_action_types 里,"赞"这个操作类型的 id,服务端固定为 2,举报菜单要排除它 */
private const val LikeActionTypeId = 2

/**
 * 楼层"更多操作"底部弹层。除"复制链接"外,每一项是否展示都由服务端逐楼层下发的权限位驱动:
 * - 收藏/取消收藏:任何人可点,未登录时服务端会返回"请先登录"提示(不在前端假设登录状态)
 * - 举报:[canFlag] 取自 [PostDto.actionsSummary] 是否还有除"赞"以外的 can_act=true 项,
 *   具体原因列表再用这些 id 去 [SiteRepo.postActionTypes] 联表拿名称/是否需要留言——
 *   同一篇帖子在不同人眼里可用的举报原因可能不同(如已被举报过、系统帖等)
 * - 编辑:[PostDto.canEdit] 为真时可用(本人帖子,或版主/管理员)
 * - 删除/恢复:[PostDto.canDelete]/[PostDto.canRecover] 为真时可用(本人或版主/管理员;
 *   版主能删除普通用户看不到的更多帖子,这里完全跟随服务端字段,不额外判断角色)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostActionsSheet(
    post: PostDto,
    topicSlug: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onUpdated: (PostDto) -> Unit,
    onNeedsReload: () -> Unit,
) {
    val nc = LocalNodelocColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var busy by remember(post.id) { mutableStateOf(false) }
    var errorMsg by remember(post.id) { mutableStateOf<String?>(null) }
    var confirmDelete by remember(post.id) { mutableStateOf(false) }
    var flagSheetOpen by remember(post.id) { mutableStateOf(false) }
    var flagReasons by remember(post.id) { mutableStateOf<List<FlagReason>>(emptyList()) }
    val canFlag = post.actionsSummary.any { it.id != LikeActionTypeId && it.canAct }

    fun toggleBookmark() {
        if (busy) return
        busy = true
        scope.launch {
            runCatching {
                if (post.bookmarked) {
                    post.bookmarkId?.let { DiscourseApi.deleteBookmark(it) }
                    onUpdated(post.copy(bookmarked = false, bookmarkId = null))
                } else {
                    val id = DiscourseApi.bookmarkPost(post.id)
                    onUpdated(post.copy(bookmarked = true, bookmarkId = id))
                }
            }.onFailure { errorMsg = it.message ?: "操作失败，请稍后再试" }
            busy = false
        }
    }

    fun deletePost() {
        if (busy) return
        busy = true
        scope.launch {
            runCatching { DiscourseApi.deletePost(post.id) }
                .onSuccess { onNeedsReload() }
                .onFailure { errorMsg = it.message ?: "删除失败，请稍后再试" }
            busy = false
        }
    }

    fun recoverPost() {
        if (busy) return
        busy = true
        scope.launch {
            runCatching { DiscourseApi.recoverPost(post.id) }
                .onSuccess { onNeedsReload() }
                .onFailure { errorMsg = it.message ?: "恢复失败，请稍后再试" }
            busy = false
        }
    }

    LaunchedEffect(post.id) {
        if (!canFlag) return@LaunchedEffect
        val actionableIds = post.actionsSummary.filter { it.id != LikeActionTypeId && it.canAct }.map { it.id }.toSet()
        val types = runCatching { SiteRepo.postActionTypes() }.getOrDefault(emptyList())
        flagReasons = types
            .filter { it.id in actionableIds && it.isFlag }
            .map { FlagReason(it.id, it.name, it.requireMessage) }
    }

    fun flag(reasonId: Int, message: String?) {
        if (busy) return
        busy = true
        scope.launch {
            runCatching { DiscourseApi.flagPost(post.id, reasonId, message) }
                .onSuccess {
                    flagSheetOpen = false
                    onDismiss()
                    Toast.makeText(context, "已提交举报，管理员会尽快处理", Toast.LENGTH_SHORT).show()
                }
                .onFailure { errorMsg = it.message ?: "举报失败，请稍后再试" }
            busy = false
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = nc.surface) {
        Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            errorMsg?.let { msg ->
                Text(msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            }
            ActionRow(
                icon = Icons.Filled.Link,
                label = "复制链接",
                onClick = {
                    clipboard.setText(AnnotatedString(DiscourseApi.BASE + "/t/" + topicSlug + "/" + post.topicId + "/" + post.postNumber))
                    onDismiss()
                },
            )
            ActionRow(
                icon = if (post.bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                label = if (post.bookmarked) "取消收藏" else "收藏此帖子",
                enabled = !busy,
                onClick = ::toggleBookmark,
            )
            if (post.canEdit) {
                ActionRow(icon = Icons.Filled.Edit, label = "编辑", onClick = { onDismiss(); onEdit() })
            }
            if (canFlag) {
                ActionRow(icon = Icons.Filled.Flag, label = "举报", enabled = flagReasons.isNotEmpty(), onClick = { flagSheetOpen = true })
            }
            if (post.canRecover) {
                ActionRow(icon = Icons.Filled.Restore, label = "恢复此帖子", enabled = !busy, onClick = ::recoverPost)
            } else if (post.canDelete) {
                ActionRow(icon = Icons.Filled.Delete, label = "删除", tint = MaterialTheme.colorScheme.error, enabled = !busy,
                    onClick = { confirmDelete = true })
            }
        }
    }

    if (flagSheetOpen) {
        FlagReasonSheet(
            reasons = flagReasons,
            busy = busy,
            onDismiss = { flagSheetOpen = false },
            onPick = ::flag,
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("删除此帖子？") },
            text = { Text("删除后帖子会被隐藏，管理员/版主在一定期限内仍可恢复。") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; deletePost() }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("取消") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlagReasonSheet(
    reasons: List<FlagReason>,
    busy: Boolean,
    onDismiss: () -> Unit,
    onPick: (Int, String?) -> Unit,
) {
    val nc = LocalNodelocColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var pendingReason by remember { mutableStateOf<FlagReason?>(null) }
    var message by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = nc.surface) {
        val reason = pendingReason
        if (reason == null) {
            Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text(
                    "举报原因",
                    style = MaterialTheme.typography.titleSmall,
                    color = nc.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                )
                reasons.forEach { r ->
                    ActionRow(
                        icon = Icons.Filled.Flag,
                        label = r.label,
                        enabled = !busy,
                        onClick = { if (r.requireMessage) pendingReason = r else onPick(r.id, null) },
                    )
                }
            }
        } else {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(reason.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = nc.onBackground)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = { Text("请补充说明…") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { pendingReason = null; message = "" }) { Text("取消") }
                    TextButton(enabled = !busy && message.isNotBlank(), onClick = { onPick(reason.id, message) }) {
                        Text("提交举报", color = nc.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean = true,
    tint: androidx.compose.ui.graphics.Color = LocalNodelocColors.current.onSurfaceVariant,
    onClick: () -> Unit,
) {
    val nc = LocalNodelocColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.height(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, color = if (enabled) nc.onBackground else nc.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}
