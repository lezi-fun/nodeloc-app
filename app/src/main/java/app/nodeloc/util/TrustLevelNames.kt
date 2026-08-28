package app.nodeloc.util

/**
 * NodeLoc 官网信任等级(trust_level 0~4)对应的会员称号,与站内默认徽章"白银会员"/"黄金会员"/
 * "钻石会员"/"王者会员"一致(0 级为"青铜会员",官网没有对应的默认徽章,但用户等级体系里就是最低档)。
 * 注意:这只是官网默认称号,用户若被授予自定义称号(title 字段)应优先显示那个,
 * 此映射仅用于没有 title 时的兜底展示(如抽屉里的当前登录用户,接口未返回 title)。
 */
object TrustLevelNames {
    private val names = listOf("青铜会员", "白银会员", "黄金会员", "钻石会员", "王者会员")

    fun displayName(trustLevel: Int): String = names.getOrElse(trustLevel) { "青铜会员" }
}
