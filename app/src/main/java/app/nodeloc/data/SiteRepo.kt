package app.nodeloc.data

import app.nodeloc.data.model.CategoryDto
import app.nodeloc.data.model.PostActionTypeDto
import app.nodeloc.data.model.TagDto
import app.nodeloc.util.absoluteUrl
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

/** 站点元数据(分类色、热门标签、举报操作类型等),进程内缓存 */
object SiteRepo {
    @Volatile private var categories: Map<Int, CategoryDto>? = null
    @Volatile private var topTags: List<TagDto>? = null
    @Volatile private var postActionTypes: List<PostActionTypeDto>? = null
    @Volatile private var popularApps: List<app.nodeloc.data.model.RecentAppDto>? = null

    private suspend fun loadSite() {
        if (categories != null && topTags != null && postActionTypes != null) return
        val site = runCatching { DiscourseApi.site() }.getOrNull() ?: return
        categories = site.categories.associateBy { it.id }
        topTags = site.topTags
        postActionTypes = site.postActionTypes
        popularApps = site.popularApps
    }

    suspend fun categories(): Map<Int, CategoryDto> {
        loadSite()
        return categories ?: emptyMap()
    }

    suspend fun category(id: Int): CategoryDto? = categories()[id]

    /** 侧栏"标签"区块回退展示用的站点热门标签(用户没有自定义 sidebar_tags 时) */
    suspend fun topTags(): List<TagDto> {
        loadSite()
        return topTags ?: emptyList()
    }

    suspend fun popularApps(): List<app.nodeloc.data.model.RecentAppDto> {
        loadSite()
        return popularApps ?: emptyList()
    }

    suspend fun postActionTypes(): List<PostActionTypeDto> {
        loadSite()
        return postActionTypes ?: emptyList()
    }

    /**
     * `avatar_template` 形如 `/user_avatar/www.nodeloc.com/xx/{size}/123_2.png`,
     * 也可能是 CDN 上的协议相对地址,交给 [absoluteUrl] 统一归一化。
     */
    fun avatarUrl(template: String?, size: Int = 96): String? =
        absoluteUrl(template?.replace("{size}", size.toString()), DiscourseApi.BASE)

    /**
     * 动图头像:官网帖子流头像用 _2.gif(真动画),其余位置用 _2.png(单帧静态)。
     * 仅当模板以 _2.png 结尾时替换,其余形态原样返回。
     */
    fun animatedAvatarUrl(template: String?, size: Int = 96): String? =
        avatarUrl(template, size)?.let { if (it.endsWith("_2.png")) it.dropLast(4) + ".gif" else it }

    /** ISO8601 → 「x 分钟前」 */
    fun relativeTime(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        return runCatching {
            val t = OffsetDateTime.parse(iso)
            val m = ChronoUnit.MINUTES.between(t, OffsetDateTime.now(t.offset))
            when {
                m < 1 -> "刚刚"
                m < 60 -> m.toString() + " 分钟前"
                m < 1440 -> (ChronoUnit.HOURS.between(t, OffsetDateTime.now(t.offset))).toString() + " 小时前"
                m < 43200 -> (ChronoUnit.DAYS.between(t, OffsetDateTime.now(t.offset))).toString() + " 天前"
                else -> ((ChronoUnit.DAYS.between(t, OffsetDateTime.now(t.offset))) / 30).toString() + " 个月前"
            }
        }.getOrDefault("")
    }
}
