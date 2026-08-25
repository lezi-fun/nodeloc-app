package app.nodeloc.ui.theme

import androidx.compose.ui.graphics.Color

// 官网默认浅色主题
val ALight = NodelocColors(
    primary = Color(0xFF009966), onPrimary = Color.White,
    background = Color(0xFFEDEAE3), onBackground = Color(0xFF222222),
    surface = Color(0xFFFFFFFF), onSurface = Color(0xFF222222),
    surfaceVariant = Color(0xFFF8F8F8), onSurfaceVariant = Color(0xFF646464),
    outlineVariant = Color(0xFFE2DED5),
    secondaryContainer = Color(0xFFE2F3EC), onSecondaryContainer = Color(0xFF007A52),
    headerBg = Color(0xFFFFFFFF), hot = Color(0xFFC5221F), adminBadge = Color(0xFFB8860B)
)

// NodeLoc Classic 官网默认深色主题
val BDark = NodelocColors(
    primary = Color(0xFF118A53), onPrimary = Color.White,
    background = Color(0xFF1E1A15), onBackground = Color(0xFFD5D5D5),
    surface = Color(0xFF231D16), onSurface = Color(0xFFD5D5D5),
    surfaceVariant = Color(0xFF2A241D), onSurfaceVariant = Color(0xFFA39684),
    outlineVariant = Color(0xFF372F24),
    secondaryContainer = Color(0xFF0E2E22), onSecondaryContainer = Color(0xFF62FEDF),
    headerBg = Color(0xFF12100D), hot = Color(0xFFE45735), adminBadge = Color(0xFFF5BF03)
)
