package app.nodeloc.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import app.nodeloc.data.model.BadgeStyleDto
import app.nodeloc.util.hexColor

/**
 * discourse-custom-badge 插件的称号动效:官网用 CSS background-clip:text 做渐变文字滚动动画。
 * 这里用 Compose TextStyle.brush + 无限循环的渐变位移动画还原,颜色变量(--highlight/--tertiary 等)
 * 取近似色,不追求逐像素还原 CSS,只求视觉相近。未匹配到样式表或没有 text_effect 时走纯色文字。
 */
@Composable
fun BadgeTitleText(text: String, style: BadgeStyleDto?, baseColor: Color, modifier: Modifier = Modifier) {
    val baseTextStyle = LocalTextStyle.current
    val color = style?.customStyle?.textColor?.let { runCatching { hexColor(it) }.getOrNull() } ?: baseColor
    val effect = style?.customStyle?.textEffect

    if (effect == null) {
        Text(text, style = baseTextStyle.copy(color = color), modifier = modifier)
        return
    }

    val transition = rememberInfiniteTransition(label = "badge-title-flow")
    val durationMs = effectDurationMs(effect)
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "badge-title-offset",
    )
    val colors = effectGradientColors(effect, color)
    val brush = movingLinearGradient(colors, offset)
    Text(text, style = baseTextStyle.copy(brush = brush), modifier = modifier)
}

/** 沿颜色列表滚动的线性渐变:offset 每循环一轮,颜色带整体平移一个周期,营造流光效果。 */
private fun movingLinearGradient(colors: List<Color>, offset: Float): Brush {
    val extended = colors + colors // 首尾相接,平移时看不出接缝
    val n = extended.size
    val stops = FloatArray(n) { i -> i.toFloat() / (n - 1) }
    val shifted = stops.map { ((it + offset) % 1f) }
    // 按位置重新排序,保持渐变单调递增,避免 Brush 因 stop 顺序错乱而跳变
    val paired = shifted.zip(extended).sortedBy { it.first }
    return Brush.linearGradient(
        colorStops = paired.map { it.first to it.second }.toTypedArray(),
        start = Offset.Zero,
        end = Offset.Infinite,
    )
}

private fun effectDurationMs(effect: String): Int = when (effect) {
    "shimmer" -> 2800
    "gold-flow" -> 4400
    "silver-flow" -> 5000
    "rainbow-flow" -> 5000
    "aurora-flow" -> 7000
    "fire-flow" -> 3500
    "ocean-flow" -> 6000
    "galaxy-flow" -> 8000
    "holographic-flow" -> 6000
    else -> 4000
}

/** 近似官网 CSS 变量配色(--highlight/--tertiary/--danger 等),取 NodeLoc 亮色主题实测色值。 */
private fun effectGradientColors(effect: String, base: Color): List<Color> = when (effect) {
    "shimmer" -> listOf(base, base, Color.White.copy(alpha = 0.9f), base, base)
    "gold-flow" -> listOf(base, Color(0xFFFFD54F), Color(0xFF00A86B), Color(0xFFFFD54F), base)
    "silver-flow" -> listOf(base, Color(0xFFB0BEC5), Color(0xFFECEFF1), Color(0xFF90A4AE), base)
    "rainbow-flow" -> listOf(
        base, Color(0xFF00A86B), Color(0xFFE53935), Color(0xFFEF5350),
        Color(0xFFFFD54F), Color(0xFF43A047), Color(0xFF7E57C2), base,
    )
    "aurora-flow" -> listOf(base, Color(0xFF00A86B), Color(0xFF26C6DA), Color(0xFF7E57C2), Color(0xFF00A86B), base)
    "fire-flow" -> listOf(base, Color(0xFFE53935), Color(0xFFFFD54F), Color(0xFFD32F2F), base)
    "ocean-flow" -> listOf(base, Color(0xFF00A86B), Color(0xFF7E57C2), Color(0xFF26C6DA), base)
    "galaxy-flow" -> listOf(base, Color(0xFF7E57C2), Color(0xFFE53935), Color(0xFF26C6DA), base)
    "holographic-flow" -> listOf(
        base, Color(0xFF26C6DA), Color(0xFFE53935), Color(0xFF7E57C2), Color(0xFFFFD54F), base,
    )
    else -> listOf(base, base)
}
