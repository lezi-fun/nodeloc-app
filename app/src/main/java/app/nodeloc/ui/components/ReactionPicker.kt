package app.nodeloc.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.nodeloc.data.DiscourseApi
import app.nodeloc.data.model.PostDto
import app.nodeloc.ui.theme.LocalNodelocColors
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

/** 官网 discourse-reactions 的 7 种可选表情,顺序与站点长按菜单一致;默认赞对应 heart。 */
val AvailableReactions = listOf("heart", "+1", "laughing", "open_mouth", "clap", "confetti_ball", "hugs")

private fun emojiUrl(name: String) = DiscourseApi.BASE + "/images/emoji/unicode/$name.png?v=15"

/**
 * 帖子操作栏的反应按钮:单击切换默认反应(heart),长按弹出 7 种表情选单单选。
 * [post] 是最新楼层状态,反应结果由 [onUpdated] 回传新的 PostDto 供调用方替换本地列表项。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReactionButton(post: PostDto, canReact: Boolean, onUpdated: (PostDto) -> Unit, modifier: Modifier = Modifier) {
    val nc = LocalNodelocColors.current
    val scope = rememberCoroutineScope()
    var menuOpen by remember(post.id) { mutableStateOf(false) }
    var busy by remember(post.id) { mutableStateOf(false) }

    fun toggle(reaction: String) {
        if (busy || !canReact) return
        busy = true
        scope.launch {
            runCatching { DiscourseApi.toggleReaction(post.id, reaction) }
                .onSuccess { onUpdated(it) }
            busy = false
        }
    }

    val current = post.currentUserReaction?.id
    Row(
        modifier
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = canReact && !busy,
                onClick = { toggle(current ?: "heart") },
                onLongClick = { if (canReact) menuOpen = true },
            )
            .padding(horizontal = 2.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = emojiUrl(current ?: "heart"),
            contentDescription = current ?: "点赞",
            modifier = Modifier.size(15.dp),
        )
        val total = post.reactions.sumOf { it.count }
        if (total > 0) {
            Spacer(Modifier.size(4.dp))
            Text(total.toString(), style = MaterialTheme.typography.labelMedium, color = nc.onSurfaceVariant)
        }
    }

    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
        Row(
            Modifier.background(nc.surface, RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            AvailableReactions.forEach { reaction ->
                DropdownMenuItem(
                    text = { AsyncImage(model = emojiUrl(reaction), contentDescription = reaction, modifier = Modifier.size(22.dp)) },
                    onClick = { menuOpen = false; toggle(reaction) },
                    modifier = Modifier.size(36.dp),
                    contentPadding = PaddingValues(4.dp),
                )
            }
        }
    }
}
