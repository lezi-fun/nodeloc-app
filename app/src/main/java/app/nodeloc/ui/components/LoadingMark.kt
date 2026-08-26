package app.nodeloc.ui.components

import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
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

/** 官方 SVG 描边宽度 */
private const val StrokeWidth = 26f
private const val DrawDurationMs = 400L
private const val FinalDelayMs = 3_200L
/** 官方 SVG 的 <g transform="translate(30,0)"> */
private const val OfficialGroupOffsetX = 30f

private class StrokeSpec(
    val path: Path,
    val measure: PathMeasure,
    val color: Color,
    val delayMs: Long,
) {
    val length: Float = measure.length
}

/**
 * NodeLoc 官网字标的原生 Compose 渲染版本。
 *
 * 官方 assets/loading.svg 依赖 CSS keyframes 驱动 stroke-dashoffset，
 * 不同 WebView 表现不一致，这里用 PathMeasure 做等价的路径裁剪绘制；
 * 绘制区域取全部笔画（含描边）的实际包围盒并以此居中，
 * 因此不受官方 <g transform> 平移或 viewBox 留白影响，始终视觉居中。
 */
@Composable
fun LoadingMark(modifier: Modifier = Modifier, height: Dp = 44.dp) {
    var elapsedMs by remember { mutableLongStateOf(0L) }
    val (specs, bounds) = remember {
        fun spec(data: String, color: Long, delayMs: Long): StrokeSpec {
            val path =
                requireNotNull(PathParser.createPathFromPathData(data)) { "Invalid NodeLoc logo path" }
            path.offset(OfficialGroupOffsetX, 0f)
            return StrokeSpec(path, PathMeasure(path, false), Color(color), delayMs)
        }
        // 路径数据逐条对应 assets/loading.svg，延迟对应其 animation-delay
        val list = listOf(
            spec("M0,222 L0,172 A50,50 0 0 1 100,172 L100,222", 0xFF009966, 0),
            spec("M150,172 a50,50 0 1,0 100,0 a50,50 0 1,0 -100,0", 0xFF009966, 400),
            spec("M300,172 a50,50 0 1,0 100,0 a50,50 0 1,0 -100,0", 0xFF009966, 800),
            spec("M400,58 L400,222", 0xFF009966, 1_200),
            spec("M450,172 L550,172", 0xFF009966, 1_600),
            spec("M550,172 A50,50 0 1 0 541,201", 0xFF009966, 2_000),
            spec("M600,58 L600,222", 0xFFFF9933, 2_400),
            spec("M650,172 a50,50 0 1,0 100,0 a50,50 0 1,0 -100,0", 0xFFFF9933, 2_800),
            spec("M891,143 A50,50 0 1 0 891,201", 0xFFFF9933, 3_200),
        )
        val ink = RectF()
        val tmp = RectF()
        list.forEach { spec ->
            spec.path.computeBounds(tmp, true)
            ink.union(tmp)
        }
        // 外扩半描边宽度（圆头笔画半径），再留 1px 抗锯齿余量
        ink.inset(-StrokeWidth / 2f - 1f, -StrokeWidth / 2f - 1f)
        list to ink
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
            .aspectRatio(bounds.width() / bounds.height())
            .semantics { contentDescription = "加载中" },
    ) {
        val scale = min(size.width / bounds.width(), size.height / bounds.height())
        withTransform({
            translate(
                size.width / 2f - scale * bounds.centerX(),
                size.height / 2f - scale * bounds.centerY(),
            )
            scale(scale, scale)
        }) {
            val segment = Path()
            specs.forEach { spec ->
                val progress = ((elapsedMs - spec.delayMs).toFloat() / DrawDurationMs).coerceIn(0f, 1f)
                if (progress <= 0f) return@forEach
                spec.measure.getSegment(0f, spec.length * progress, segment, true)
                drawPath(
                    segment.asComposePath(),
                    color = spec.color,
                    style = Stroke(
                        width = StrokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
                segment.rewind()
            }
        }
    }
}
