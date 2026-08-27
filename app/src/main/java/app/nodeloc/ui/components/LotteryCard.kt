package app.nodeloc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.nodeloc.data.DiscourseApi
import app.nodeloc.data.SiteRepo
import app.nodeloc.data.model.LotteryDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private val CardGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF3B1F6E), Color(0xFF1C0A4A), Color(0xFF0E0528)),
)
private val StatusOpenColor = Color(0xFF009900)
private val StatusDrawnColor = Color(0xFF009966)
private val OnCardText = Color.White
private val OnCardTextMuted = Color(0xFFBBAFDA)

/**
 * discourse-lottery 抽奖卡片:与 cooked 正文平行渲染(数据来自 post.lottery,不在 HTML 里)。
 * 覆盖参与购票(含"随缘")、发起者开奖/结束、已开奖中奖名单展示。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LotteryCard(lottery: LotteryDto, modifier: Modifier = Modifier) {
    var state by remember(lottery.id) { mutableStateOf(lottery) }
    var buyOpen by remember(lottery.id) { mutableStateOf(false) }
    var quantity by remember(lottery.id) { mutableIntStateOf(state.minTicketsPerUser.coerceAtLeast(1)) }
    var busy by remember(lottery.id) { mutableStateOf(false) }
    var errorMsg by remember(lottery.id) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun refresh() {
        scope.launch {
            runCatching { DiscourseApi.lottery(state.id) }.onSuccess { state = it }
        }
    }

    Surface(shape = RoundedCornerShape(11.dp), color = Color(0xFF1C0A4A), modifier = modifier.fillMaxWidth()) {
        Column(Modifier.background(CardGradient).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    state.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnCardText,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                StatusBadge(state.status)
            }
            Spacer(Modifier.height(10.dp))

            if (state.isOpen) Countdown(state.drawAt)

            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                MetaLine("开奖时间：" + formatDateTime(state.drawAt))
                MetaLine(state.participantsCount.toString() + " / " + state.maxParticipantsDisplay + " 参与者")
                MetaLine("已售奖券：" + state.ticketsCount)
                if (state.userTickets > 0) MetaLine("您的奖券：" + state.userTickets)
            }

            if (state.minParticipants > 0 || state.maxTicketsPerUser < 100_000) {
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (state.minParticipants > 0) ConditionChip("至少 " + state.minParticipants + " 人参与")
                    ConditionChip("每人最多 " + state.maxTicketsPerUser + " 张券")
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("奖品", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = OnCardTextMuted)
            Spacer(Modifier.height(4.dp))
            state.levels.forEach { level ->
                Text(
                    level.name + "  " + level.prize + " × " + level.quantity,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnCardText,
                    modifier = Modifier.padding(vertical = 1.dp),
                )
            }

            if (state.isDrawn && state.winners.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("中奖名单", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = OnCardTextMuted)
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.winners.forEach { w ->
                        Surface(shape = RoundedCornerShape(999.dp), color = Color.White.copy(alpha = 0.1f)) {
                            Row(Modifier.padding(horizontal = 9.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                Avatar(w.username, SiteRepo.avatarUrl(w.avatarTemplate, 48), 20.dp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    w.levelName + " — " + w.prize,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnCardText,
                                )
                            }
                        }
                    }
                }
            } else if (state.participants.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("参与人数", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = OnCardTextMuted)
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.participants.forEach { p ->
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (p.isRandom) Color(0xFFFFD54F).copy(alpha = 0.18f) else Color.White.copy(alpha = 0.1f),
                        ) {
                            Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Avatar(p.username, SiteRepo.avatarUrl(p.avatarTemplate, 48), 18.dp)
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    "+" + p.tickets,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (p.isRandom) Color(0xFFFFD54F) else OnCardText,
                                )
                            }
                        }
                    }
                }
            }

            errorMsg?.let { msg ->
                Spacer(Modifier.height(10.dp))
                Text(msg, style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF8A80))
            }

            if (state.isOpen) {
                Spacer(Modifier.height(14.dp))
                if (buyOpen) {
                    BuyPopover(
                        quantity = quantity,
                        min = state.minTicketsPerUser.coerceAtLeast(1),
                        max = (state.maxTicketsPerUser - state.userTickets).coerceAtLeast(state.minTicketsPerUser.coerceAtLeast(1)),
                        busy = busy,
                        onQuantityChange = { quantity = it },
                        onConfirm = {
                            busy = true; errorMsg = null
                            scope.launch {
                                runCatching { DiscourseApi.lotteryParticipate(state.id, quantity, random = false) }
                                    .onSuccess { r -> if (r.success) { buyOpen = false; refresh() } else errorMsg = r.message ?: "参与失败" }
                                    .onFailure { errorMsg = it.message ?: "参与失败" }
                                busy = false
                            }
                        },
                        onRandom = {
                            busy = true; errorMsg = null
                            scope.launch {
                                runCatching { DiscourseApi.lotteryParticipate(state.id, quantity, random = true) }
                                    .onSuccess { r -> if (r.success) { buyOpen = false; refresh() } else errorMsg = r.message ?: "参与失败" }
                                    .onFailure { errorMsg = it.message ?: "参与失败" }
                                busy = false
                            }
                        },
                        onCancel = { buyOpen = false; errorMsg = null },
                    )
                } else {
                    Surface(
                        onClick = { buyOpen = true },
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0xFF00A86B),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(Modifier.padding(vertical = 11.dp), contentAlignment = Alignment.Center) {
                            Text(
                                if (state.isParticipating) "追加抽奖券" else "参与抽奖",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }
                }

                if (state.canDraw == true || state.canClose == true) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (state.canDraw == true) {
                            OutlinedButton(
                                enabled = !busy,
                                onClick = {
                                    busy = true; errorMsg = null
                                    scope.launch {
                                        runCatching { DiscourseApi.lotteryDraw(state.id) }
                                            .onSuccess { r -> if (r.success) refresh() else errorMsg = r.message ?: "开奖失败" }
                                            .onFailure { errorMsg = it.message ?: "开奖失败" }
                                        busy = false
                                    }
                                },
                            ) { Text("立即开奖", color = OnCardText) }
                        }
                        if (state.canClose == true) {
                            TextButton(
                                enabled = !busy,
                                onClick = {
                                    busy = true; errorMsg = null
                                    scope.launch {
                                        runCatching { DiscourseApi.lotteryClose(state.id) }
                                            .onSuccess { r -> if (r.success) refresh() else errorMsg = r.message ?: "结束失败" }
                                            .onFailure { errorMsg = it.message ?: "结束失败" }
                                        busy = false
                                    }
                                },
                            ) { Text("结束抽奖", color = OnCardTextMuted) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BuyPopover(
    quantity: Int,
    min: Int,
    max: Int,
    busy: Boolean,
    onQuantityChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onRandom: () -> Unit,
    onCancel: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.08f), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (quantity > min) onQuantityChange(quantity - 1) }, enabled = quantity > min) {
                    Text("−", style = MaterialTheme.typography.titleLarge, color = OnCardText)
                }
                Text(
                    quantity.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnCardText,
                    modifier = Modifier.width(48.dp),
                )
                IconButton(onClick = { if (quantity < max) onQuantityChange(quantity + 1) }, enabled = quantity < max) {
                    Icon(Icons.Filled.Add, "增加", tint = OnCardText)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    onClick = onConfirm,
                    enabled = !busy,
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFF00A86B),
                    modifier = Modifier.weight(1f),
                ) {
                    Box(Modifier.padding(vertical = 9.dp), contentAlignment = Alignment.Center) {
                        if (busy) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        else Text("确认购买", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                OutlinedButton(onClick = onCancel, enabled = !busy) { Text("取消", color = OnCardText) }
                OutlinedButton(onClick = onRandom, enabled = !busy) { Text("随缘", color = Color(0xFFFFD54F)) }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (text, bg, fg) = when (status) {
        "open" -> Triple("开放中", StatusOpenColor, Color.White)
        "drawn" -> Triple("已开奖", StatusDrawnColor, Color.White)
        "failed" -> Triple("开奖失败", Color.Transparent, Color.White.copy(alpha = 0.7f))
        "closed" -> Triple("已结束", Color.Transparent, Color.White.copy(alpha = 0.7f))
        else -> Triple(status, Color.Transparent, Color.White.copy(alpha = 0.7f))
    }
    Surface(shape = RoundedCornerShape(999.dp), color = bg) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = if (bg == Color.Transparent) 0.dp else 9.dp, vertical = if (bg == Color.Transparent) 0.dp else 3.dp),
        )
    }
}

@Composable
private fun MetaLine(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = OnCardTextMuted)
}

@Composable
private fun ConditionChip(text: String) {
    Surface(shape = RoundedCornerShape(999.dp), color = Color.White.copy(alpha = 0.08f)) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = OnCardTextMuted,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
        )
    }
}

/** 每秒刷新的倒计时:天/时/分/秒四段,与官网 lottery-countdown 布局一致 */
@Composable
private fun Countdown(drawAtIso: String) {
    var now by remember { mutableStateOf(OffsetDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = OffsetDateTime.now()
        }
    }
    val drawAt = remember(drawAtIso) { runCatching { OffsetDateTime.parse(drawAtIso) }.getOrNull() }
    if (drawAt == null) return
    val remaining = Duration.between(now, drawAt)
    if (remaining.isNegative) return
    val days = remaining.toDays()
    val hours = remaining.toHours() % 24
    val minutes = remaining.toMinutes() % 60
    val seconds = remaining.seconds % 60
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        CountdownSegment(days.toString(), "天")
        ColonSep()
        CountdownSegment(hours.toString().padStart(2, '0'), "时")
        ColonSep()
        CountdownSegment(minutes.toString().padStart(2, '0'), "分")
        ColonSep()
        CountdownSegment(seconds.toString().padStart(2, '0'), "秒")
    }
}

@Composable
private fun ColonSep() {
    Text(":", style = MaterialTheme.typography.titleMedium, color = OnCardTextMuted)
}

@Composable
private fun CountdownSegment(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.1f)) {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnCardText,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnCardTextMuted)
    }
}

private fun formatDateTime(iso: String): String = runCatching {
    OffsetDateTime.parse(iso).format(DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss"))
}.getOrDefault(iso)
