package fun.lezi.nodeloc.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class NodelocColors(
    val primary: Color, val onPrimary: Color,
    val background: Color, val onBackground: Color,
    val surface: Color, val onSurface: Color,
    val surfaceVariant: Color, val onSurfaceVariant: Color,
    val outlineVariant: Color,
    val secondaryContainer: Color, val onSecondaryContainer: Color,
    val headerBg: Color, val hot: Color, val adminBadge: Color
)

val LocalNodelocColors = staticCompositionLocalOf { ALight }

/** 跟随系统:浅色=A 方向,深色=B 方向 */
@Composable
fun NodelocTheme(content: @Composable () -> Unit) {
    val nc = if (isSystemInDarkTheme()) BDark else ALight
    val m3 = if (isSystemInDarkTheme()) {
        darkColorScheme(
            primary = nc.primary, onPrimary = nc.onPrimary,
            background = nc.background, onBackground = nc.onBackground,
            surface = nc.surface, onSurface = nc.onSurface,
            surfaceVariant = nc.surfaceVariant, onSurfaceVariant = nc.onSurfaceVariant,
            outlineVariant = nc.outlineVariant,
            secondaryContainer = nc.secondaryContainer, onSecondaryContainer = nc.onSecondaryContainer,
        )
    } else {
        lightColorScheme(
            primary = nc.primary, onPrimary = nc.onPrimary,
            background = nc.background, onBackground = nc.onBackground,
            surface = nc.surface, onSurface = nc.onSurface,
            surfaceVariant = nc.surfaceVariant, onSurfaceVariant = nc.onSurfaceVariant,
            outlineVariant = nc.outlineVariant,
            secondaryContainer = nc.secondaryContainer, onSecondaryContainer = nc.onSecondaryContainer,
        )
    }
    CompositionLocalProvider(LocalNodelocColors provides nc) {
        MaterialTheme(colorScheme = m3, typography = NodelocTypography, shapes = NodelocShapes, content = content)
    }
}

val NodelocShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(18.dp),
)
