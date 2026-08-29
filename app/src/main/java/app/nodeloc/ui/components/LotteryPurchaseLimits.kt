package app.nodeloc.ui.components

import app.nodeloc.data.model.LotteryDto

/**
 * 与 discourse-lottery 官网一致的购票边界。userTickets 已计入当前用户已有奖券，
 * maxTicketsPerUser 为 0 时表示不设上限；这里仍限制单次输入最多 100 张。
 */
data class LotteryPurchaseLimits(val min: Int, val max: Int) {
    val canPurchase: Boolean get() = max >= min
}

fun lotteryPurchaseLimits(lottery: LotteryDto): LotteryPurchaseLimits {
    val min = maxOf(1, lottery.minTicketsPerUser - lottery.userTickets)
    val max = if (lottery.maxTicketsPerUser > 0) {
        maxOf(0, lottery.maxTicketsPerUser - lottery.userTickets)
    } else {
        100
    }
    return LotteryPurchaseLimits(min, max)
}
