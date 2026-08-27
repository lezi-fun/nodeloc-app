package app.nodeloc.util

/** 官网帖子语言提示条用的语言代码→中文名映射,覆盖常见站内语言;未知代码原样返回。 */
object LanguageNames {
    private val names = mapOf(
        "en" to "英语",
        "zh_CN" to "简体中文",
        "zh_TW" to "繁体中文",
        "ja" to "日语",
        "ko" to "韩语",
        "vi" to "越南语",
        "fr" to "法语",
        "de" to "德语",
        "es" to "西班牙语",
        "ru" to "俄语",
        "ar" to "阿拉伯语",
        "pt" to "葡萄牙语",
        "th" to "泰语",
        "it" to "意大利语",
        "id" to "印尼语",
    )

    fun displayName(code: String): String = names[code] ?: code
}
