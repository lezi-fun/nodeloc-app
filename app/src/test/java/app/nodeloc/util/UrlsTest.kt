package app.nodeloc.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlsTest {

    private val base = "https://www.nodeloc.com"

    @Test
    fun `absolute url is returned untouched`() {
        assertEquals("https://cdn.example/a.png", absoluteUrl("https://cdn.example/a.png", base))
        assertEquals("http://cdn.example/a.png", absoluteUrl("http://cdn.example/a.png", base))
    }

    /** 旧实现把 `//cdn/...` 拼成 `https://www.nodeloc.com//cdn/...`,外链图片全挂。 */
    @Test
    fun `protocol relative url gets https scheme`() {
        assertEquals("https://cdn.example/a.png", absoluteUrl("//cdn.example/a.png", base))
    }

    @Test
    fun `site absolute path is prefixed with base`() {
        assertEquals("$base/uploads/a.png", absoluteUrl("/uploads/a.png", base))
    }

    @Test
    fun `site relative path gets a separator`() {
        assertEquals("$base/uploads/a.png", absoluteUrl("uploads/a.png", base))
    }

    @Test
    fun `trailing slash on base does not double up`() {
        assertEquals("$base/uploads/a.png", absoluteUrl("/uploads/a.png", "$base/"))
    }

    @Test
    fun `data uri is passed through`() {
        val uri = "data:image/png;base64,iVBORw0KGgo="
        assertEquals(uri, absoluteUrl(uri, base))
    }

    @Test
    fun `blank and null yield null`() {
        assertNull(absoluteUrl(null, base))
        assertNull(absoluteUrl("", base))
        assertNull(absoluteUrl("   ", base))
    }

    @Test
    fun `avatar template placeholder is already substituted by caller`() {
        assertEquals(
            "$base/user_avatar/www.nodeloc.com/tily/96/1_2.png",
            absoluteUrl("/user_avatar/www.nodeloc.com/tily/96/1_2.png", base),
        )
    }

    @Test
    fun `external http and https urls require confirmation`() {
        assertTrue(isExternalHttpUrl("https://example.com/page"))
        assertTrue(isExternalHttpUrl("http://example.com/page"))
    }

    @Test
    fun `nodeloc hosts are treated as internal`() {
        assertFalse(isExternalHttpUrl("https://www.nodeloc.com/t/topic/1"))
        assertFalse(isExternalHttpUrl("https://nodeloc.com/t/topic/1"))
    }

    @Test
    fun `invalid and non http urls do not count as external web links`() {
        assertFalse(isExternalHttpUrl("mailto:test@example.com"))
        assertFalse(isExternalHttpUrl("not a url"))
        assertFalse(isExternalHttpUrl(null))
    }
}
