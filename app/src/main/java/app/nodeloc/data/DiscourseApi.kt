package app.nodeloc.data

import app.nodeloc.data.model.CsrfDto
import app.nodeloc.data.model.CurrentSessionDto
import app.nodeloc.data.model.CurrentUserDto
import app.nodeloc.data.model.LatestDto
import app.nodeloc.data.model.NestedChildrenDto
import app.nodeloc.data.model.NestedTopicDto
import app.nodeloc.data.model.PostReplyHistoryDto
import app.nodeloc.data.model.PostRepliesDto
import app.nodeloc.data.model.PostsChunkDto
import app.nodeloc.data.model.SearchDto
import app.nodeloc.data.model.SessionResponseDto
import app.nodeloc.data.model.SiteDto
import app.nodeloc.data.model.TopicDetailDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

object DiscourseApi {
    const val BASE = "https://www.nodeloc.com"
    private const val HOST = "www.nodeloc.com"

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /** 会话 cookie:长期 _t 由 SessionStore 持久化,临时 _forum_session 仅存内存 */
    private val sessionCookieJar = object : CookieJar {
        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            if (url.host != HOST) return emptyList()
            fun cookie(name: String, value: String?) = value?.takeIf { it.isNotBlank() }?.let {
                Cookie.Builder().name(name).value(it).domain(HOST).path("/").build()
            }
            return listOfNotNull(cookie("_t", SessionStore.tCookie), cookie("_forum_session", SessionStore.sessionCookie))
        }

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            if (url.host != HOST) return
            for (c in cookies) when (c.name) {
                "_t" -> SessionStore.tCookie = c.value.takeIf { it.isNotBlank() }
                "_forum_session" -> SessionStore.sessionCookie = c.value.takeIf { it.isNotBlank() }
            }
        }
    }

    private val client = OkHttpClient.Builder()
        .callTimeout(java.time.Duration.ofSeconds(25))
        .cookieJar(sessionCookieJar)
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
                if (!resp.isSuccessful || body == null) throw httpError(resp.code, body)
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

    // ---------- 会话 ----------

    /** 获取 CSRF token(带缓存);登录/登出后由调用方置空重取 */
    suspend fun fetchCsrf(): String = withContext(Dispatchers.IO) {
        SessionStore.csrfToken?.let { return@withContext it }
        val req = Request.Builder()
            .url(BASE + "/session/csrf")
            .header("X-Requested-With", "XMLHttpRequest")
            .build()
        client.newCall(req).execute().use { resp ->
            val body = resp.body?.string()
            if (!resp.isSuccessful || body == null) throw httpError(resp.code, body)
            json.decodeFromString(CsrfDto.serializer(), body).csrf.also { SessionStore.csrfToken = it }
        }
    }

    /**
     * 密码登录。成功返回当前用户;账号开启 2FA 时抛 [SecondFactorRequiredException]
     * (携带二次提交所需的 second_factor_token);凭据错误抛 [ApiException](服务端文案)。
     */
    suspend fun login(
        login: String,
        password: String,
        secondFactorToken: String? = null,
        totp: String? = null,
    ): CurrentUserDto = withContext(Dispatchers.IO) {
        val csrf = fetchCsrf()
        val form = FormBody.Builder()
            .add("login", login)
            .add("password", password)
            .add("second_factor_method", "1")
        secondFactorToken?.takeIf { it.isNotBlank() }?.let { form.add("second_factor_token", it) }
        totp?.takeIf { it.isNotBlank() }?.let { form.add("second_factor_totp", it) }
        val req = Request.Builder()
            .url(BASE + "/session")
            .header("X-Requested-With", "XMLHttpRequest")
            .header("X-CSRF-Token", csrf)
            .post(form.build())
            .build()
        client.newCall(req).execute().use { resp ->
            val body = resp.body?.string()
            if (!resp.isSuccessful || body == null) throw httpError(resp.code, body)
            val r = json.decodeFromString(SessionResponseDto.serializer(), body)
            when {
                r.secondFactorRequired -> throw SecondFactorRequiredException(r.secondFactorToken ?: "")
                r.error != null -> throw ApiException(0, message = r.error)
                r.user != null -> {
                    // 会话已切换,旧 CSRF 失效
                    SessionStore.csrfToken = null
                    r.user
                }
                else -> throw ApiException(0, message = "登录失败,请稍后再试")
            }
        }
    }

    /** 退出登录并清空本地会话 */
    suspend fun logout(username: String) = withContext(Dispatchers.IO) {
        val csrf = fetchCsrf()
        val req = Request.Builder()
            .url(BASE + "/session/" + username + "/logout")
            .header("X-Requested-With", "XMLHttpRequest")
            .header("X-CSRF-Token", csrf)
            .post(FormBody.Builder().build())
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw httpError(resp.code, resp.body?.string())
            SessionStore.clear()
        }
    }

    /** 当前登录用户;未登录(403/404)返回 null */
    suspend fun currentUser(): CurrentUserDto? = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(BASE + "/session/current.json").build()
        client.newCall(req).execute().use { resp ->
            if (resp.code == 403 || resp.code == 404) return@use null
            val body = resp.body?.string()
            if (!resp.isSuccessful || body == null) throw httpError(resp.code, body)
            json.decodeFromString(CurrentSessionDto.serializer(), body).currentUser
        }
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
                if (!resp.isSuccessful || body == null) throw httpError(resp.code, body)
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
                if (!resp.isSuccessful || body == null) throw httpError(resp.code, body)
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
