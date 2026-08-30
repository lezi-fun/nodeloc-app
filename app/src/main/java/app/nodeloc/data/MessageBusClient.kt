package app.nodeloc.data

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.util.UUID

/**
 * MessageBus 客户端
 *
 * 实现 Discourse MessageBus 长轮询机制，用于接收服务端实时推送的消息。
 *
 * 使用示例：
 * ```
 * val messageBus = MessageBusClient(scope)
 *
 * // 订阅频道
 * messageBus.subscribe("/latest")
 * messageBus.subscribe("/topic/12345")
 *
 * // 开始轮询
 * messageBus.start()
 *
 * // 停止轮询
 * messageBus.stop()
 * ```
 */
class MessageBusClient(
    private val scope: CoroutineScope,
    private val config: MessageBusConfig = MessageBusConfig(),
) {
    companion object {
        private const val TAG = "MessageBusClient"
    }

    /** 客户端唯一 ID (UUID 格式，无连字符) */
    private val clientId = UUID.randomUUID().toString().replace("-", "")

    /** 当前订阅的频道 */
    private val channels = mutableMapOf<String, ChannelState>()

    /** 序列号，每次轮询递增 */
    private var sequence = 0

    /** 轮询任务 */
    private var pollingJob: Job? = null

    /** 是否正在轮询 */
    val isPolling: Boolean
        get() = pollingJob?.isActive == true

    /** 消息回调 */
    private val messageCallbacks = mutableMapOf<String, (MessageBusMessage) -> Unit>()

    /**
     * 订阅频道
     * @param channel 频道名称，如 "/latest", "/topic/12345"
     * @param lastMessageId 最后接收的消息 ID，-1 表示不接收历史消息
     * @param callback 收到消息时的回调
     */
    fun subscribe(channel: String, lastMessageId: Long = -1, callback: ((MessageBusMessage) -> Unit)? = null) {
        synchronized(channels) {
            channels[channel] = ChannelState(channel, lastMessageId)
            if (callback != null) {
                messageCallbacks[channel] = callback
            }
        }
        Log.d(TAG, "Subscribed to $channel (lastId=$lastMessageId)")
    }

    /**
     * 取消订阅频道
     */
    fun unsubscribe(channel: String) {
        synchronized(channels) {
            channels.remove(channel)
            messageCallbacks.remove(channel)
        }
        Log.d(TAG, "Unsubscribed from $channel")
    }

    /**
     * 更新频道的消息回调
     */
    fun setChannelCallback(channel: String, callback: (MessageBusMessage) -> Unit) {
        synchronized(channels) {
            if (channels.containsKey(channel)) {
                messageCallbacks[channel] = callback
            }
        }
    }

    /**
     * 开始长轮询
     */
    fun start() {
        if (isPolling) {
            Log.w(TAG, "Already polling")
            return
        }

        Log.i(TAG, "Starting MessageBus polling (clientId=$clientId)")
        pollingJob = scope.launch {
            var errorCount = 0

            while (isActive) {
                try {
                    poll()
                    errorCount = 0 // 重置错误计数

                    // 短暂延迟后继续下一次轮询
                    delay(config.minPollInterval)

                } catch (e: MessageBusRateLimitException) {
                    Log.w(TAG, "Rate limited, waiting 10 seconds")
                    delay(10_000)

                } catch (e: Exception) {
                    errorCount++
                    val backoff = minOf(5_000L * errorCount, 60_000L)
                    Log.e(TAG, "Poll error (retry in ${backoff}ms)", e)
                    delay(backoff)
                }
            }
        }
    }

    /**
     * 停止长轮询
     */
    fun stop() {
        Log.i(TAG, "Stopping MessageBus polling")
        pollingJob?.cancel()
        pollingJob = null
    }

    /**
     * 执行一次轮询
     */
    private suspend fun poll() {
        val params = buildPollParams()

        // 如果没有订阅任何频道，跳过轮询
        if (params.isEmpty()) {
            delay(1000)
            return
        }

        val messages = DiscourseApi.messageBusPoll(clientId, params)

        // 更新每个频道的 lastMessageId 并分发消息
        messages.forEach { msg ->
            synchronized(channels) {
                channels[msg.channel]?.lastMessageId = msg.messageId
            }
            handleMessage(msg)
        }

        // 递增序列号
        sequence++
    }

    /**
     * 构建轮询参数
     * 格式: /latest=123&/delete=456&__seq=1
     */
    private fun buildPollParams(): String {
        val channelParams = synchronized(channels) {
            channels.values.map { state ->
                val encodedChannel = URLEncoder.encode(state.channel, "UTF-8")
                "$encodedChannel=${state.lastMessageId}"
            }
        }

        if (channelParams.isEmpty()) {
            return ""
        }

        return (channelParams + "__seq=$sequence").joinToString("&")
    }

    /**
     * 处理收到的消息
     */
    private fun handleMessage(msg: MessageBusMessage) {
        Log.d(TAG, "Received message on ${msg.channel} (id=${msg.messageId})")

        // 调用频道特定的回调
        synchronized(channels) {
            messageCallbacks[msg.channel]?.invoke(msg)
        }

        // 通用消息处理（可扩展）
        when {
            msg.channel == "/latest" -> handleLatestUpdate(msg)
            msg.channel.startsWith("/topic/") -> handleTopicUpdate(msg)
            msg.channel == "/refresh_client" -> handleRefreshClient(msg)
            msg.channel.startsWith("/private-messages/") -> handlePrivateMessage(msg)
        }
    }

    /**
     * 处理 /latest 频道消息（首页新话题）
     */
    private fun handleLatestUpdate(msg: MessageBusMessage) {
        // TODO: 通知首页刷新话题列表
        Log.d(TAG, "Latest update: ${msg.data}")
    }

    /**
     * 处理 /topic/{id} 频道消息（话题更新）
     */
    private fun handleTopicUpdate(msg: MessageBusMessage) {
        // TODO: 通知话题页刷新内容
        Log.d(TAG, "Topic update: ${msg.data}")
    }

    /**
     * 处理 /refresh_client 频道消息（强制刷新）
     */
    private fun handleRefreshClient(msg: MessageBusMessage) {
        // TODO: 提示用户刷新应用
        Log.w(TAG, "Client refresh requested: ${msg.data}")
    }

    /**
     * 处理私信频道消息
     */
    private fun handlePrivateMessage(msg: MessageBusMessage) {
        // TODO: 显示新私信通知
        Log.d(TAG, "Private message update: ${msg.data}")
    }
}
