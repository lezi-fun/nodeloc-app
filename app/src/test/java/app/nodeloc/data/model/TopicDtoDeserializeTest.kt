package app.nodeloc.data.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class TopicDtoDeserializeTest {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun `deserializes topic with object array tags`() {
        val raw = """
            {"id":1,"title":"t","tags":[{"id":31,"name":"求助","slug":"31-tag"}]}
        """.trimIndent()
        val topic = json.decodeFromString<TopicDto>(raw)
        assertEquals(1, topic.tags.size)
        assertEquals("求助", topic.tags[0].name)
    }

    @Test
    fun `deserializes topic with string array tags`() {
        val raw = """
            {"id":1,"title":"t","tags":["vps","aff"]}
        """.trimIndent()
        val topic = json.decodeFromString<TopicDto>(raw)
        assertEquals(2, topic.tags.size)
        assertEquals("vps", topic.tags[0].name)
    }

    @Test
    fun `deserializes latest json topic list array`() {
        val raw = """
            {"topics":[
              {"id":1,"title":"a","tags":[]},
              {"id":2,"title":"b","tags":[{"id":5,"name":"AI","slug":"ai"}]}
            ]}
        """.trimIndent()
        val list = json.decodeFromString<TopicListDto>(raw)
        assertEquals(2, list.topics.size)
    }
}
