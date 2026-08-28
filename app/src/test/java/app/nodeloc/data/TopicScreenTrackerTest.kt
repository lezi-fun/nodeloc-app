package app.nodeloc.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TopicScreenTrackerTest {

    @Test
    fun `accumulates elapsed time for visible posts each tick`() {
        val tracker = TopicScreenTracker(topicId = 1)
        tracker.tick(1000, setOf(1, 2))
        tracker.tick(1000, setOf(2, 3))
        val pending = tracker.drainPending()!!
        assertEquals(1000L, pending.timingsMs[1])
        assertEquals(2000L, pending.timingsMs[2])
        assertEquals(1000L, pending.timingsMs[3])
        assertEquals(2000L, pending.topicTimeMs)
    }

    @Test
    fun `does not flush before 60s once the post has already been seen`() {
        val tracker = TopicScreenTracker(topicId = 1)
        tracker.tick(1000, setOf(1)) // 首次看到楼层 1,这一 tick 会立即要求 flush
        tracker.drainPending() // msSinceLastFlush 归零,楼层 1 之后不再是"新楼层"
        repeat(59) { tracker.tick(1000, setOf(1)) }
        assertFalse(tracker.shouldFlush())
        tracker.tick(1000, setOf(1)) // 第 60 次 tick,累计满 60000ms
        assertTrue(tracker.shouldFlush())
    }

    @Test
    fun `flushes immediately when a brand new post becomes visible`() {
        val tracker = TopicScreenTracker(topicId = 1)
        tracker.tick(1000, setOf(1))
        assertTrue(tracker.shouldFlush())
    }

    @Test
    fun `drainPending resets accumulator and clears flush trigger`() {
        val tracker = TopicScreenTracker(topicId = 1)
        tracker.tick(1000, setOf(1))
        tracker.drainPending()
        tracker.tick(1000, setOf(1)) // 楼层 1 已经见过,不再是"新楼层"
        assertFalse(tracker.shouldFlush())
        tracker.drainPending()
        assertNull(tracker.drainPending())
    }

    @Test
    fun `per-post cap stops further accumulation once reached`() {
        val tracker = TopicScreenTracker(topicId = 1)
        // 先用两次 tick 刚好把楼层 1 累计到上限(单次 tick 内的 total 检查是加之前的值,
        // 所以要分两步才能验证"到达上限后不再新增"这件事)
        tracker.tick(TopicScreenTracker.PER_POST_CAP_MS - 1000, setOf(1))
        tracker.tick(1000, setOf(1))
        tracker.drainPending()
        tracker.tick(5000, setOf(1))
        val pending = tracker.drainPending()
        // 楼层 1 已达上限,不再累计新增量,但话题总时长仍照常累计
        assertNull(pending?.timingsMs?.get(1))
        assertEquals(5000L, pending?.topicTimeMs)
    }

    @Test
    fun `restore merges failed batch back for retry`() {
        val tracker = TopicScreenTracker(topicId = 1)
        tracker.tick(1000, setOf(1))
        val failed = tracker.drainPending()!!
        tracker.restore(failed)
        tracker.tick(500, setOf(1))
        val retried = tracker.drainPending()!!
        assertEquals(1500L, retried.timingsMs[1])
        assertEquals(1500L, retried.topicTimeMs)
    }
}
