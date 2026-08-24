package app.nodeloc.ui.theme

import androidx.compose.ui.graphics.Color

// 方向 A · 原生 Material(浅色)
val ALight = NodelocColors(
    primary = Color(0xFF009966), onPrimary = Color.White,
    background = Color(0xFFFFFFFF), onBackground = Color(0xFF202124),
    surface = Color(0xFFF8F9FA), onSurface = Color(0xFF202124),
    surfaceVariant = Color(0xFFF1F3F2), onSurfaceVariant = Color(0xFF697077),
    outlineVariant = Color(0xFFECEEF0),
    secondaryContainer = Color(0xFFE2F3EC), onSecondaryContainer = Color(0xFF007A52),
    headerBg = Color(0xFFFFFFFF), hot = Color(0xFFC5221F), adminBadge = Color(0xFFB8860B)
)

// 方向 B · 暖夜阅读(深色)
val BDark = NodelocColors(
    primary = Color(0xFF3ECF82), onPrimary = Color(0xFF0E2418),
    background = Color(0xFF1E1A15), onBackground = Color(0xFFE8E0D2),
    surface = Color(0xFF272119), onSurface = Color(0xFFE8E0D2),
    surfaceVariant = Color(0xFF2A241D), onSurfaceVariant = Color(0xFFA39684),
    outlineVariant = Color(0xFF372F24),
    secondaryContainer = Color(0xFF12331F), onSecondaryContainer = Color(0xFF3ECF82),
    headerBg = Color(0xFF12100D), hot = Color(0xFFFF7A45), adminBadge = Color(0xFFF5BF03)
)
