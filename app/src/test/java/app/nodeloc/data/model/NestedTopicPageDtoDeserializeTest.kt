package app.nodeloc.data.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class NestedTopicPageDtoDeserializeTest {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Test
    fun `pagination response without topic and op post decodes`() {
        val raw = """
            {
              "roots": [],
              "has_more_roots": false,
              "page": 2,
              "suggested_topics": [],
              "related_topics": []
            }
        """.trimIndent()

        val response = json.decodeFromString<NestedTopicPageDto>(raw)

        assertEquals(2, response.page)
        assertFalse(response.hasMoreRoots)
        assertEquals(emptyList<PostDto>(), response.roots)
    }
}
