package app.nodeloc.ui.components

import android.graphics.Path
import android.graphics.PathMeasure
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.PathParser
import kotlinx.coroutines.isActive
import kotlin.math.min

private const val LogoWidth = 960f
private const val LogoHeight = 280f
private const val StrokeWidth = 26f
private const val DrawDurationMs = 400L
private const val FinalDelayMs = 3_200L

private data class DrawPath(val path: Path, val color: Color, val delayMs: Long)

/**
 * NodeLoc 官网字标的原生 Compose 渲染版本。
 *
 * 资源本身保留在 assets/loading.svg 作为官方来源，但不再把 CSS 动画交给
 * WebView 解释，避免不同 Android System WebView 对 pathLength 和 keyframes 的差异。
 */
@Composable
fun LoadingMark(modifier: Modifier = Modifier, height: Dp = 64.dp) {
    var elapsedMs by remember { mutableLongStateOf(0L) }
    val paths = remember {
        listOf(
            svgPath("M0,222 L0,172 A50,50 0 0 1 100,172 L100,222", 0xFF009966, 0),
            svgPath("M150,172 a50,50 0 1,0 100,0 a50,50 0 1,0 -100,0", 0xFF009966, 400),
            svgPath("M300,172 a50,50 0 1,0 100,0 a50,50 0 1,0 -100,0", 0xFF009966, 800),
            svgPath("M400,58 L400,222", 0xFF009966, 1_200),
            svgPath("M450,172 L550,172", 0xFF009966, 1_600),
            svgPath("M550,172 A50,50 0 1 0 541,201", 0xFF009966, 2_000),
            svgPath("M600,58 L600,222", 0xFFFF9933, 2_400),
            svgPath("M650,172 a50,50 0 1,0 100,0 a50,50 0 1,0 -100,0", 0xFFFF9933, 2_800),
            svgPath("M891,143 A50,50 0 1 0 891,201", 0xFFFF9933, 3_200),
        )
    }

    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (isActive) {
            val now = withFrameNanos { it }
            elapsedMs = ((now - start) / 1_000_000L).coerceAtMost(FinalDelayMs + DrawDurationMs)
            if (elapsedMs >= FinalDelayMs + DrawDurationMs) break
        }
    }

    Canvas(
        modifier = modifier
            .height(height)
            .aspectRatio(LogoWidth / LogoHeight)
            .semantics { contentDescription = "加载中" },
    ) {
        val scale = min(size.width / LogoWidth, size.height / LogoHeight)
        val left = (size.width - LogoWidth * scale) / 2f
        val top = (size.height - LogoHeight * scale) / 2f

        withTransform({
            translate(left, top)
            scale(scale, scale)
            translate(30f, 0f)
        }) {
            paths.forEach { spec ->
                val progress = ((elapsedMs - spec.delayMs).toFloat() / DrawDurationMs).coerceIn(0f, 1f)
                if (progress <= 0f) return@forEach
                val measure = PathMeasure(spec.path, false)
                val segment = Path()
                measure.getSegment(0f, measure.length * progress, segment, true)
                drawPath(
                    segment.asComposePath(),
                    color = spec.color,
                    style = Stroke(
                        width = StrokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }
        }
    }
}

private fun svgPath(data: String, color: Long, delayMs: Long): DrawPath =
    DrawPath(
        path = requireNotNull(PathParser.createPathFromPathData(data)) { "Invalid NodeLoc logo path" },
        color = Color(color),
        delayMs = delayMs,
    )
