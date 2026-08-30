package app.nodeloc.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * MessageBus 消息体
 */
@Serializable
data class MessageBusMessage(
    /** 频道名称，如 "/latest", "/topic/12345" */
    val channel: String,

    /** 消息 ID，用于追踪已接收的消息 */
    @SerialName("message_id") val messageId: Long,

    /** 消息数据，具体结构取决于频道类型 */
    val data: JsonElement,
)

/**
 * 频道订阅状态
 */
data class ChannelState(
    /** 频道名称 */
    val channel: String,

    /** 最后接收的消息 ID，-1 表示首次订阅 */
    var lastMessageId: Long = -1,
)

/**
 * MessageBus 配置
 */
data class MessageBusConfig(
    /** 长轮询超时时间（毫秒），默认 25 秒 */
    val callbackInterval: Long = 25_000,

    /** 最大轮询间隔（毫秒），默认 3 分钟 */
    val maxPollInterval: Long = 180_000,

    /** 最小轮询间隔（毫秒），默认 100ms */
    val minPollInterval: Long = 100,

    /** 是否启用长轮询 */
    val enableLongPolling: Boolean = true,
)
