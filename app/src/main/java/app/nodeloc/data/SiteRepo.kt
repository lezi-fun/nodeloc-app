package app.nodeloc.data

import app.nodeloc.data.model.CategoryDto
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

/** 站点元数据(分类色等),进程内缓存 */
object SiteRepo {
    @Volatile private var categories: Map<Int, CategoryDto>? = null

    suspend fun categories(): Map<Int, CategoryDto> {
        categories?.let { return it }
        return runCatching { DiscourseApi.site().categories.associateBy { it.id } }
            .getOrNull()?.also { categories = it } ?: emptyMap()
    }

    suspend fun category(id: Int): CategoryDto? = categories()[id]

    fun avatarUrl(template: String?, size: Int = 96): String? {
        if (template.isNullOrBlank()) return null
        val path = if (template.startsWith("http")) template.replace("{size}", size.toString())
                   else DiscourseApi.BASE + template.replace("{size}", size.toString())
        return path
    }

    /** ISO8601 → 「x 分钟前」 */
    fun relativeTime(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        return runCatching {
            val t = OffsetDateTime.parse(iso)
            val m = ChronoUnit.MINUTES.between(t, OffsetDateTime.now(t.offset))
            when {
                m < 1 -> "刚刚"
                m < 60 -> m + " 分钟前"
                m < 1440 -> ChronoUnit.HOURS.between(t, OffsetDateTime.now(t.offset)) + " 小时前"
                m < 43200 -> ChronoUnit.DAYS.between(t, OffsetDateTime.now(t.offset)) + " 天前"
                else -> ChronoUnit.DAYS.between(t, OffsetDateTime.now(t.offset)) / 30 + " 个月前"
            }
        }.getOrDefault("")
    }
}
