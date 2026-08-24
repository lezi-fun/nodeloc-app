package app.nodeloc.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.PathParser

private data class DrawPath(val color: Color, val d: String)

/** 官方字标自绘动画数据(svg g translate(30,0), stroke 26) */
private val SEQ = listOf(
    DrawPath(Color(0xFF009966), "M0,222 L0,172 A50,50 0 0 1 100,172 L100,222"),
    DrawPath(Color(0xFF009966), "M400,58 L400,222"),
    DrawPath(Color(0xFF009966), "M450,172 L550,172"),
    DrawPath(Color(0xFF009966), "M550,172 A50,50 0 1 0 541,201"),
    DrawPath(Color(0xFF009966), "M150,172 a50,50 0 1,0 100,0 a50,50 0 1,0 -100,0"),
    DrawPath(Color(0xFF009966), "M300,172 a50,50 0 1,0 100,0 a50,50 0 1,0 -100,0"),
    DrawPath(Color(0xFFFF9933), "M600,58 L600,222"),
    DrawPath(Color(0xFFFF9933), "M891,143 A50,50 0 1 0 891,201"),
    DrawPath(Color(0xFFFF9933), "M650,172 a50,50 0 1,0 100,0 a50,50 0 1,0 -100,0"),
)
private const val VB_W = 960f
private const val VB_H = 280f
private const val G_TX = 30f
private const val STROKE_W = 26f

/** 复刻官网加载动画:字标逐笔描绘并循环 */
@Composable
fun DrawLoading(modifier: Modifier = Modifier, size: Dp = 132.dp) {
    val transition = rememberInfiniteTransition(label = "draw")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "progress",
    )
    val paths = remember {
        SEQ.map { (color, d) ->
            val p = Path()
            p.addPath(PathParser().parsePathString(d).toPath(), Offset(G_TX, 0f))
            val m = PathMeasure()
            m.setPath(p, false)
            Triple(color, p, m.length)
        }
    }
    Box(modifier.then(Modifier.size(size).aspectRatio(VB_W / VB_H))) {
        Canvas(Modifier.size(size)) {
            val scale = this.size.width / VB_W
            translate(left = 0f, top = (this.size.height - VB_H * scale) / 2f) {
                val n = paths.size
                paths.forEachIndexed { i, (color, p, len) ->
                    val local = ((t * n) - i).coerceIn(0f, 1f)
                    if (local <= 0f) return@forEachIndexed
                    val eased = 1f - (1f - local) * (1f - local)
                    val dest = Path()
                    val m = PathMeasure()
                    m.setPath(p, false)
                    m.getSegment(0f, len * eased, dest, true)
                    drawPath(dest, color, style = Stroke(STROKE_W * scale, cap = StrokeCap.Round))
                }
            }
        }
    }
}
