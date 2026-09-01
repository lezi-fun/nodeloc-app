package app.nodeloc.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import app.nodeloc.ui.theme.LocalNodelocColors

/*
 * 尺寸与配色照 common/components/composer-toggle-switch.scss:
 *   --toggle-switch-width: 52px; --toggle-switch-height: 26px
 *   __slider  { background: var(--primary-low); border-radius: var(--d-border-radius) }
 *   ::before  { background: var(--tertiary-low);
 *               width:  calc(height - var(--space-half))  = 26 - 2 = 24px
 *               height: calc(height - var(--space-1))     = 26 - 4 = 22px
 *               top: var(--space-half) = 2px
 *               --markdown → translateX(0.125rem) = 2px
 *               --rte      → translateX(width - height) = 26px }
 *   图标宽 calc(height - var(--space-half)) = 24px,左右各内缩 2px,
 *   常态 var(--primary-high),选中侧 var(--primary)
 * (foundation/base.scss: --space: 4px,故 --space-half = 2px、--space-1 = 4px)
 */
private val TrackWidth = 52.dp
private val TrackHeight = 26.dp
private val KnobWidth = 24.dp
private val KnobHeight = 22.dp
private val EdgeInset = 2.dp
private val SegmentWidth = 24.dp

/**
 * 编辑器的 Markdown / 渲染视图切换开关,复刻官网 composer 工具栏最左侧的
 * ComposerToggleSwitch(components/composer/toggle-switch.gjs):一条灰底滑轨,
 * 选中侧盖一块浅色滑块,左格是 Font Awesome 的 fab-markdown 标记,右格是字母 A。
 *
 * 官网这个开关切的是 Markdown 编辑器与富文本编辑器;本 app 没有富文本编辑器,
 * 右格对应渲染预览 —— 交互语义不同,但形态与官网一致。
 *
 * @param preview true 表示当前在渲染视图(滑块在右)
 */
@Composable
fun ComposerToggleSwitch(preview: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val nc = LocalNodelocColors.current
    // --markdown → 2px,--rte → width - height = 26px
    val knobOffset by animateDpAsState(
        targetValue = if (preview) TrackWidth - TrackHeight else EdgeInset,
        label = "composerToggleKnob",
    )
    Box(
        modifier
            .size(TrackWidth, TrackHeight)
            .clip(RoundedCornerShape(4.dp))
            .background(nc.outlineVariant)
            .clickable(onClick = onToggle),
    ) {
        // ::before 滑块,官网用 --tertiary-low(强调色的浅色调)
        Box(
            Modifier
                .offset(x = knobOffset, y = EdgeInset)
                .size(KnobWidth, KnobHeight)
                .clip(RoundedCornerShape(4.dp))
                .background(nc.primary.copy(alpha = 0.18f)),
        )
        // 左格:fab-markdown
        Box(
            Modifier.size(SegmentWidth, TrackHeight).offset(x = EdgeInset),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                MarkdownMark,
                contentDescription = "Markdown 源码",
                tint = if (preview) nc.onSurfaceVariant else nc.onBackground,
                modifier = Modifier.size(14.dp),
            )
        }
        // 右格:字母 A
        Box(
            Modifier
                .size(SegmentWidth, TrackHeight)
                .offset(x = TrackWidth - SegmentWidth - EdgeInset),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "A",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (preview) nc.onBackground else nc.onSurfaceVariant,
            )
        }
    }
}

/** Font Awesome free brands "markdown"(fab-markdown),官网左格用的就是它 */
private val MarkdownMark: ImageVector by lazy {
    ImageVector.Builder(
        name = "FabMarkdown",
        defaultWidth = 20.dp,
        defaultHeight = 16.dp,
        viewportWidth = 640f,
        viewportHeight = 512f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            // M593.8 59.1H46.2C20.7 59.1 0 79.8 0 105.2v301.5c0 25.5 20.7 46.2 46.2 46.2h547.7
            // c25.5 0 46.2-20.7 46.1-46.1V105.2c0-25.4-20.7-46.1-46.2-46.1z
            moveTo(593.8f, 59.1f)
            horizontalLineTo(46.2f)
            curveTo(20.7f, 59.1f, 0f, 79.8f, 0f, 105.2f)
            verticalLineToRelative(301.5f)
            curveToRelative(0f, 25.5f, 20.7f, 46.2f, 46.2f, 46.2f)
            horizontalLineToRelative(547.7f)
            curveToRelative(25.5f, 0f, 46.2f, -20.7f, 46.1f, -46.1f)
            verticalLineTo(105.2f)
            curveToRelative(0f, -25.4f, -20.7f, -46.1f, -46.2f, -46.1f)
            close()
            // M338.5 360.6H277v-120l-61.5 76.9-61.5-76.9v120H92.3V151.4h61.5l61.5 76.9 61.5-76.9h61.5v209.2z
            moveTo(338.5f, 360.6f)
            horizontalLineTo(277f)
            verticalLineToRelative(-120f)
            lineToRelative(-61.5f, 76.9f)
            lineToRelative(-61.5f, -76.9f)
            verticalLineToRelative(120f)
            horizontalLineTo(92.3f)
            verticalLineTo(151.4f)
            horizontalLineToRelative(61.5f)
            lineToRelative(61.5f, 76.9f)
            lineToRelative(61.5f, -76.9f)
            horizontalLineToRelative(61.5f)
            verticalLineToRelative(209.2f)
            close()
            // m135.3 3.1L381.5 256H443V151.4h61.5V256H566z
            moveToRelative(135.3f, 3.1f)
            lineTo(381.5f, 256f)
            horizontalLineTo(443f)
            verticalLineTo(151.4f)
            horizontalLineToRelative(61.5f)
            verticalLineTo(256f)
            horizontalLineTo(566f)
            close()
        }
    }.build()
}
