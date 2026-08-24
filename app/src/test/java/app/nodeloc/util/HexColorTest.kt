package app.nodeloc.util

import org.junit.Assert.assertEquals
import org.junit.Test

class HexColorTest {

    @Test
    fun `six digit hex keeps channels and gains opaque alpha`() {
        assertEquals(0xFF2CB2B5.toInt(), HexColor.toArgb("2cb2b5"))
        assertEquals(0xFFF7941D.toInt(), HexColor.toArgb("F7941D"))
    }

    @Test
    fun `leading hash is accepted`() {
        assertEquals(0xFF0088CC.toInt(), HexColor.toArgb("#0088CC"))
    }

    /** 线上分类 59「影视分享」的 color 就是 "FFF",旧实现在这里直接崩。 */
    @Test
    fun `three digit shorthand expands per channel`() {
        assertEquals(0xFFFFFFFF.toInt(), HexColor.toArgb("FFF"))
        assertEquals(0xFF00AA33.toInt(), HexColor.toArgb("0A3"))
    }

    @Test
    fun `four digit shorthand carries alpha`() {
        assertEquals(0x88FFFFFF.toInt(), HexColor.toArgb("FFF8"))
    }

    @Test
    fun `eight digit hex is read as RRGGBBAA`() {
        assertEquals(0x800088CC.toInt(), HexColor.toArgb("0088CC80"))
    }

    @Test
    fun `malformed input falls back instead of throwing`() {
        val bad = listOf(null, "", "   ", "GGG", "12345", "#xyzxyz", "0088CC0", "rebeccapurple")
        bad.forEach { assertEquals("input=$it", HexColor.FallbackArgb, HexColor.toArgb(it)) }
    }

    @Test
    fun `surrounding whitespace is tolerated`() {
        assertEquals(0xFF549447.toInt(), HexColor.toArgb("  549447 "))
    }
}
