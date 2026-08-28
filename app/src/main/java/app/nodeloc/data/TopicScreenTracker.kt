package app.nodeloc.data

/**
 * 对齐官网 screen-track 服务(discourse-hdodxd1p.digested.js 里的 Dbe 类)的阅读进度追踪逻辑。
 * 纯状态机,不依赖 Compose/协程,方便单测;真正的每秒 tick 与网络发送由调用方(UI 层)驱动。
 *
 * 官网规则:
 * - 每秒 tick 一次,对当前可见楼层各累加 1000ms
 * - 距上次滚动超过 180s 判定为挂机,期间不再累加(这里简化为由调用方在挂机时不再调用 [tick])
 * - 累计 60s,或本次可见楼层里出现了"从未上报过"的新楼层,就应该 flush 一次
 * - flush 后调用方负责把 [drainPending] 取出的增量真正发到服务端
 * - 单楼层累计上限 3600s,超过后不再对该楼层计入新的时长(防御性上限,与官网一致)
 */
class TopicScreenTracker(private val topicId: Long) {
    private val totalMsByPost = mutableMapOf<Int, Long>()
    private val pendingMsByPost = mutableMapOf<Int, Long>()
    private var pendingTopicTimeMs = 0L
    private var msSinceLastFlush = 0L
    /** 本次 tick 才第一次出现的楼层(对应官网 !this._totalTimings.get(e) 的那一刻),用于立即触发 flush。 */
    private var freshlySeenInLastTick: Set<Int> = emptySet()

    companion object {
        const val FLUSH_INTERVAL_MS = 60_000L
        const val PER_POST_CAP_MS = 3_600_000L
    }

    /** 每次 tick 传入本次经过的毫秒数(通常是 1000)与当前可见楼层号集合。 */
    fun tick(elapsedMs: Long, visiblePostNumbers: Set<Int>) {
        msSinceLastFlush += elapsedMs
        pendingTopicTimeMs += elapsedMs
        val freshlySeen = mutableSetOf<Int>()
        for (postNumber in visiblePostNumbers) {
            val total = totalMsByPost.getOrDefault(postNumber, 0L)
            if (total == 0L) freshlySeen += postNumber
            if (total >= PER_POST_CAP_MS) continue
            totalMsByPost[postNumber] = total + elapsedMs
            pendingMsByPost[postNumber] = pendingMsByPost.getOrDefault(postNumber, 0L) + elapsedMs
        }
        freshlySeenInLastTick = freshlySeen
    }

    /** 是否已经到了该 flush 的时机:累计满 60s,或刚刚在最近一次 tick 里第一次看到某个楼层。 */
    fun shouldFlush(): Boolean = msSinceLastFlush >= FLUSH_INTERVAL_MS || freshlySeenInLastTick.isNotEmpty()

    /**
     * 取出待上报的增量并清零;调用方随后应该把返回值发出去,发送失败时可用 [restore] 放回重试。
     * 即使所有可见楼层都已达单楼层上限、没有产生楼层增量,只要话题总停留时长有累计也应该上报
     * (与官网一致:flush 不依赖楼层数据是否非空,只要 topicTime>0 就会发送)。
     */
    fun drainPending(): PendingTimings? {
        if (pendingMsByPost.isEmpty() && pendingTopicTimeMs <= 0L) return null
        val snapshot = PendingTimings(pendingMsByPost.toMap(), pendingTopicTimeMs)
        pendingMsByPost.clear()
        pendingTopicTimeMs = 0L
        msSinceLastFlush = 0L
        return snapshot
    }

    /** 发送失败时把取出的增量放回队列,与下一批合并后重试(官网失败退避重试同一批数据)。 */
    fun restore(pending: PendingTimings) {
        pending.timingsMs.forEach { (postNumber, ms) ->
            pendingMsByPost[postNumber] = pendingMsByPost.getOrDefault(postNumber, 0L) + ms
        }
        pendingTopicTimeMs += pending.topicTimeMs
    }

    data class PendingTimings(val timingsMs: Map<Int, Long>, val topicTimeMs: Long)
}
