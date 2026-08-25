package app.nodeloc.util

import androidx.compose.ui.graphics.Color

/**
 * Discourse 的 `category.color` 是不带 `#` 的裸十六进制，长度并不保证是 6：
 * nodeloc.com 线上就存在 `"FFF"`（分类 59「影视分享」）这类三位简写。
 *
 * `android.graphics.Color.parseColor` 只接受 `#RRGGBB` / `#AARRGGBB`，
 * 遇到三位简写会直接抛 IllegalArgumentException，把整个列表项打崩。
 * 这里做一次容错归一化，任何无法识别的输入都退回站点主色。
 */
object HexColor {

    /** 解析失败时的兜底颜色(Discourse 默认分类色 #0088CC)。 */
    const val FallbackArgb: Int = 0xFF0088CC.toInt()

    /**
     * 支持的写法:`RGB` / `RGBA` / `RRGGBB` / `AARRGGBB`,可带或不带前导 `#`。
     * 返回 ARGB 整数;不合法输入返回 [FallbackArgb]。
     */
    fun toArgb(raw: String?): Int {
        val hex = raw?.trim()?.removePrefix("#")?.uppercase() ?: return FallbackArgb
        if (hex.isEmpty() || !hex.all { it in '0'..'9' || it in 'A'..'F' }) return FallbackArgb

        val rrggbbaa = when (hex.length) {
            // FFF → FFFFFF,FFFA → FFFFFFAA(每位翻倍,与 CSS 简写一致)
            3, 4 -> buildString(hex.length * 2) { hex.forEach { append(it).append(it) } }
            6, 8 -> hex
            else -> return FallbackArgb
        }

        return runCatching {
            when (rrggbbaa.length) {
                6 -> 0xFF000000.toInt() or rrggbbaa.toInt(16)
                // Discourse 只发 RRGGBB,但 8 位按 CSS 的 RRGGBBAA 处理更符合直觉
                else -> {
                    val rgb = rrggbbaa.substring(0, 6).toInt(16)
                    val alpha = rrggbbaa.substring(6, 8).toInt(16)
                    (alpha shl 24) or rgb
                }
            }
        }.getOrDefault(FallbackArgb)
    }
}

/** [HexColor.toArgb] 的 Compose 包装。 */
fun hexColor(raw: String?): Color = Color(HexColor.toArgb(raw))
