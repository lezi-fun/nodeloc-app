package app.nodeloc.data

import app.nodeloc.data.model.LatestDto
import app.nodeloc.data.model.NestedChildrenDto
import app.nodeloc.data.model.NestedTopicDto
import app.nodeloc.data.model.PostReplyHistoryDto
import app.nodeloc.data.model.PostRepliesDto
import app.nodeloc.data.model.PostsChunkDto
import app.nodeloc.data.model.SiteDto
import app.nodeloc.data.model.TopicDetailDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request

object DiscourseApi {
    const val BASE = "https://www.nodeloc.com"

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val client = OkHttpClient.Builder()
        .callTimeout(java.time.Duration.ofSeconds(25))
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", "NodeLocAndroid/0.1 (+https://github.com/lezi-fun/nodeloc-app)")
                    .header("Accept", "application/json")
                    .build()
            )
        }
        .build()

    private suspend inline fun <reified T> get(path: String): T =
        withContext(Dispatchers.IO) {
            val req = Request.Builder().url(BASE + path).build()
            client.newCall(req).execute().use { resp ->
                val body = resp.body?.string()
                if (!resp.isSuccessful || body == null) throw httpError(resp.code)
                json.decodeFromString(body)
            }
        }

    suspend fun latest(page: Int = 0): LatestDto =
        get("/latest.json?no_definitions=true&page=" + page)

    suspend fun topic(id: Long): TopicDetailDto =
        get("/t/" + id + ".json?track_visit=false")

    suspend fun nestedTopic(slug: String, id: Long, page: Int = 0, sort: String = "top"): NestedTopicDto =
        get("/n/" + slug + "/" + id + ".json?page=" + page + "&sort=" + sort)

    suspend fun nestedChildren(
        slug: String,
        id: Long,
        parentPostNumber: Int,
        depth: Int,
        page: Int = 0,
        sort: String = "top",
    ): NestedChildrenDto = get(
        "/n/" + slug + "/" + id + "/children/" + parentPostNumber +
            ".json?page=" + page + "&sort=" + sort + "&depth=" + depth.coerceAtLeast(1),
    )

    suspend fun hasActiveSession(): Boolean = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(BASE + "/session/current.json").build()
        client.newCall(req).execute().use { it.isSuccessful }
    }

    suspend fun site(): SiteDto =
        get("/site.json")

    /** 全站搜索,query 会正确 URL 编码 */
    suspend fun search(query: String): SearchDto =
        withContext(Dispatchers.IO) {
            val url = (BASE + "/search.json").toHttpUrl()
                .newBuilder()
                .addQueryParameter("q", query)
                .build()
            val req = Request.Builder().url(url).build()
            client.newCall(req).execute().use { resp ->
                val body = resp.body?.string()
                if (!resp.isSuccessful || body == null) throw httpError(resp.code)
                json.decodeFromString(body)
            }
        }

    /** 按楼层 id 分块拉取(每块最多 20 条,Discourse 硬限制) */
    suspend fun posts(topicId: Long, ids: List<Long>): PostsChunkDto =
        withContext(Dispatchers.IO) {
            val url = ("https://www.nodeloc.com/t/" + topicId + "/posts.json").toHttpUrl()
                .newBuilder()
                .apply { ids.take(20).forEach { addQueryParameter("post_ids[]", it.toString()) } }
                .addQueryParameter("include_suggested", "false")
                .build()
            val req = Request.Builder().url(url).build()
            client.newCall(req).execute().use { resp ->
                val body = resp.body?.string()
                if (!resp.isSuccessful || body == null) throw httpError(resp.code)
                json.decodeFromString(body)
            }
        }

    /** Discourse 楼层下方的直接回复，after 为上次已加载回复的楼层号。 */
    suspend fun replies(postId: Long, after: Int = 1): PostRepliesDto =
        get("/posts/" + postId + "/replies?after=" + after.coerceAtLeast(1))

    /** 当前楼层的回复来源链，用于展开“回复 @用户”上方预览。 */
    suspend fun replyHistory(postId: Long): PostReplyHistoryDto =
        get("/posts/" + postId + "/reply-history")

}
