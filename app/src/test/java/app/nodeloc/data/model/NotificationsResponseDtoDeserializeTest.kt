package app.nodeloc.data.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class NotificationsResponseDtoDeserializeTest {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Test
    fun decodesNotificationFieldsUsedByTheUi() {
        val payload = """
            {
              "notifications": [{
                "id": 42,
                "notification_type": 2,
                "read": false,
                "created_at": "2026-09-05T08:00:00.000Z",
                "topic_id": 123,
                "acting_user_avatar_template": "/user_avatar/www.nodeloc.com/alice/{size}/1_2.png",
                "data": {
                  "display_username": "alice",
                  "topic_title": "测试话题"
                }
              }],
              "total_rows_notifications": 1,
              "seen_notification_id": 40
            }
        """.trimIndent()

        val response = json.decodeFromString<NotificationsResponseDto>(payload)
        val notification = response.notifications.single()

        assertEquals(1, response.totalRows)
        assertEquals(42L, notification.id)
        assertEquals(2, notification.notificationType)
        assertFalse(notification.read)
        assertEquals(123L, notification.topicId)
        assertEquals("alice", notification.data.displayUsername)
        assertEquals("测试话题", notification.data.topicTitle)
    }
}
