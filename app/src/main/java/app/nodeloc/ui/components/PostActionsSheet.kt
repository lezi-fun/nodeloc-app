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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.runtime.collectAsState
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
import app.nodeloc.data.SessionRepo
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
 * - Wiki化/取消:[PostDto.canWiki] 为真时可用——不是站务独占,信任等级4或站点开放
 *   self_wiki_allowed_groups 的用户也能对自己的帖子这样做,所以单独用这个字段而不是 canManage
 * - 删除/恢复:[PostDto.canDelete]/[PostDto.canRecover] 为真时可用(本人或版主/管理员;
 *   版主能删除普通用户看不到的更多帖子,这里完全跟随服务端字段,不额外判断角色)
 * - 管理操作(锁定编辑/取消隐藏/重新渲染/变更所有者):这几项不是逐楼层下发的字段,而是
 *   站务身份本身决定的,和该楼层是不是本人发的无关。但粒度不完全一样(已核对 discourse
 *   后端 guardian 定义):锁定编辑/取消隐藏只认 [CurrentUserDto.isStaff];重新渲染额外
 *   放开给信任等级4([CurrentUserDto.canManageTopic]);变更所有者由服务端在
 *   /session/current.json 里算好直接下发([CurrentUserDto.canChangePostOwner])。
 *   永久删除单独用 [PostDto.canPermanentlyDelete] 判断(服务端只在"已删除+当前用户是
 *   管理员+站点开启该功能"三者都满足时才下发这个字段)。
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
    var confirmPermanentDelete by remember(post.id) { mutableStateOf(false) }
    var changeOwnerOpen by remember(post.id) { mutableStateOf(false) }
    var flagSheetOpen by remember(post.id) { mutableStateOf(false) }
    var flagReasons by remember(post.id) { mutableStateOf<List<FlagReason>>(emptyList()) }
    val canFlag = post.actionsSummary.any { it.id != LikeActionTypeId && it.canAct }
    val me by SessionRepo.currentUser.collectAsState()
    // 这几项管理操作的后端权限粒度不完全一样(已核对 discourse 的 guardian 定义):
    // 锁定编辑/取消隐藏只认 is_staff?(admin/moderator);重新渲染额外放开给信任等级4;
    // 变更所有者由服务端在 /session/current.json 里直接算好下发,不是简单的角色判断。
    val isStaff = me?.isStaff == true
    val canRebake = me?.canManageTopic == true
    val canChangeOwner = me?.canChangePostOwner == true

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

    fun toggleLocked() {
        if (busy) return
        busy = true
        scope.launch {
            runCatching { DiscourseApi.setPostLocked(post.id, !post.locked) }
                .onSuccess { onUpdated(post.copy(locked = !post.locked)) }
                .onFailure { errorMsg = it.message ?: "操作失败，请稍后再试" }
            busy = false
        }
    }

    fun toggleWiki() {
        if (busy) return
        busy = true
        scope.launch {
            runCatching { DiscourseApi.setPostWiki(post.id, !post.wiki) }
                .onSuccess { onUpdated(post.copy(wiki = !post.wiki)) }
                .onFailure { errorMsg = it.message ?: "操作失败，请稍后再试" }
            busy = false
        }
    }

    fun rebake() {
        if (busy) return
        busy = true
        scope.launch {
            runCatching { DiscourseApi.rebakePost(post.id) }
                .onSuccess { onNeedsReload(); onDismiss() }
                .onFailure { errorMsg = it.message ?: "重新渲染失败，请稍后再试" }
            busy = false
        }
    }

    fun unhide() {
        if (busy) return
        busy = true
        scope.launch {
            runCatching { DiscourseApi.unhidePost(post.id) }
                .onSuccess { onUpdated(post.copy(hidden = false)) }
                .onFailure { errorMsg = it.message ?: "取消隐藏失败，请稍后再试" }
            busy = false
        }
    }

    fun permanentlyDelete() {
        if (busy) return
        busy = true
        scope.launch {
            runCatching { DiscourseApi.deletePost(post.id, forceDestroy = true) }
                .onSuccess { onNeedsReload() }
                .onFailure { errorMsg = it.message ?: "永久删除失败，请稍后再试" }
            busy = false
        }
    }

    /** 永久删除前必须先让服务端确认(deleted_check),不能光靠前端猜权限就直接删 */
    fun requestPermanentDelete() {
        if (busy) return
        busy = true
        scope.launch {
            runCatching { DiscourseApi.permanentlyDeleteCheck(post.id) }
                .onSuccess { check ->
                    if (check.canPermanentlyDelete) confirmPermanentDelete = true
                    else errorMsg = check.reason ?: "当前无法永久删除此帖子"
                }
                .onFailure { errorMsg = it.message ?: "操作失败，请稍后再试" }
            busy = false
        }
    }

    fun changeOwner(newUsername: String) {
        if (busy || newUsername.isBlank()) return
        busy = true
        scope.launch {
            runCatching { DiscourseApi.changePostOwner(post.topicId, listOf(post.id), newUsername.trim()) }
                .onSuccess { changeOwnerOpen = false; onNeedsReload() }
                .onFailure { errorMsg = it.message ?: "变更所有者失败，请稍后再试" }
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
                icon = Icons.Filled.ContentCopy,
                label = "复制 Markdown",
                onClick = {
                    clipboard.setText(AnnotatedString(post.raw.orEmpty()))
                    Toast.makeText(context, "已复制 Markdown 内容", Toast.LENGTH_SHORT).show()
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
            if (post.canWiki) {
                ActionRow(
                    icon = Icons.Filled.MenuBook,
                    label = if (post.wiki) "取消 Wiki" else "设为 Wiki",
                    enabled = !busy,
                    onClick = ::toggleWiki,
                )
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
            if (isStaff) {
                ActionRow(
                    icon = if (post.locked) Icons.Filled.LockOpen else Icons.Filled.Lock,
                    label = if (post.locked) "解除锁定" else "锁定编辑",
                    enabled = !busy,
                    onClick = ::toggleLocked,
                )
            }
            if (canRebake) {
                ActionRow(icon = Icons.Filled.Autorenew, label = "重新渲染", enabled = !busy, onClick = ::rebake)
            }
            if (isStaff && post.hidden) {
                ActionRow(icon = Icons.Filled.Visibility, label = "取消隐藏", enabled = !busy, onClick = ::unhide)
            }
            if (canChangeOwner) {
                ActionRow(icon = Icons.Filled.Person, label = "变更所有者", enabled = !busy, onClick = { changeOwnerOpen = true })
            }
            if (post.canPermanentlyDelete) {
                ActionRow(
                    icon = Icons.Filled.DeleteForever,
                    label = "永久删除",
                    tint = MaterialTheme.colorScheme.error,
                    enabled = !busy,
                    onClick = ::requestPermanentDelete,
                )
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

    if (confirmPermanentDelete) {
        AlertDialog(
            onDismissRequest = { confirmPermanentDelete = false },
            title = { Text("永久删除此帖子？") },
            text = { Text("此操作不可撤销，帖子内容将从数据库彻底清除，无法再恢复。") },
            confirmButton = {
                TextButton(onClick = { confirmPermanentDelete = false; permanentlyDelete() }) {
                    Text("永久删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmPermanentDelete = false }) { Text("取消") } },
        )
    }

    if (changeOwnerOpen) {
        ChangeOwnerDialog(
            busy = busy,
            errorMsg = errorMsg,
            onDismiss = { changeOwnerOpen = false },
            onConfirm = ::changeOwner,
        )
    }
}

@Composable
private fun ChangeOwnerDialog(
    busy: Boolean,
    errorMsg: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var username by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("变更所有者") },
        text = {
            Column {
                Text("把这条帖子的作者变更为其他用户,常用于处理违规注册的马甲号。", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = { Text("新所有者的用户名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                errorMsg?.let { msg ->
                    Spacer(Modifier.height(6.dp))
                    Text(msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            TextButton(enabled = !busy && username.isNotBlank(), onClick = { onConfirm(username) }) { Text("确认变更") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
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
