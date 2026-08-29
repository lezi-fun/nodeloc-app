package app.nodeloc.data.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 复现"部分用户详情页整页崩溃"的 bug:未填写昵称的用户,服务端 /u/{username}.json
 * 返回 "name": null(字段存在但值为 null),而 UserProfileDto.name 是非空 String 类型。
 * 没有 Json { coerceInputValues = true } 时,kotlinx.serialization 会直接抛
 * SerializationException 而不是回退到默认值,导致整个反序列化失败、页面报错。
 */
class UserProfileDtoDeserializeTest {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    @Test
    fun `name null falls back to empty string instead of crashing`() {
        val raw = """
            {"user":{"id":1,"username":"xiaohuihuil","name":null,"avatar_template":"/letter_avatar_proxy/v4/letter/x/abc/{size}.png"}}
        """.trimIndent()
        val response = json.decodeFromString<UserProfileResponseDto>(raw)
        assertEquals("xiaohuihuil", response.user.username)
        assertEquals("", response.user.name)
    }
}
