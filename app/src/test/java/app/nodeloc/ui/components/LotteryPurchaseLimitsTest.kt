package app.nodeloc.ui.components

import app.nodeloc.data.model.LotteryDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LotteryPurchaseLimitsTest {
    @Test
    fun `minimum includes tickets already owned`() {
        val limits = lotteryPurchaseLimits(
            LotteryDto(minTicketsPerUser = 3, maxTicketsPerUser = 10, userTickets = 1),
        )
        assertEquals(2, limits.min)
        assertEquals(9, limits.max)
        assertTrue(limits.canPurchase)
    }

    @Test
    fun `maximum is remaining quota`() {
        val limits = lotteryPurchaseLimits(
            LotteryDto(minTicketsPerUser = 1, maxTicketsPerUser = 5, userTickets = 4),
        )
        assertEquals(1, limits.min)
        assertEquals(1, limits.max)
    }

    @Test
    fun `zero maximum means unbounded single purchase limit`() {
        val limits = lotteryPurchaseLimits(
            LotteryDto(minTicketsPerUser = 1, maxTicketsPerUser = 0, userTickets = 99),
        )
        assertEquals(1, limits.min)
        assertEquals(100, limits.max)
        assertTrue(limits.canPurchase)
    }

    @Test
    fun `cannot purchase when remaining quota is below required minimum`() {
        val limits = lotteryPurchaseLimits(
            LotteryDto(minTicketsPerUser = 3, maxTicketsPerUser = 4, userTickets = 3),
        )
        assertEquals(1, limits.min)
        assertEquals(1, limits.max)
        assertTrue(limits.canPurchase)

        val impossible = lotteryPurchaseLimits(
            LotteryDto(minTicketsPerUser = 3, maxTicketsPerUser = 2, userTickets = 0),
        )
        assertEquals(3, impossible.min)
        assertEquals(2, impossible.max)
        assertFalse(impossible.canPurchase)
    }
}
