package app.nodeloc.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.nodeloc.R

/**
 * 官方字标呼吸脉冲加载指示。
 * 纯 Compose 实现(WebView 方案在部分设备上渲染空白,已弃用)。
 */
@Composable
fun LoadingMark(modifier: Modifier = Modifier, height: Dp = 30.dp) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(850, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "alpha",
    )
    Image(
        painter = painterResource(R.drawable.nodeloc_logo),
        contentDescription = null,
        modifier = modifier.then(Modifier.height(height).alpha(alpha)),
    )
}
