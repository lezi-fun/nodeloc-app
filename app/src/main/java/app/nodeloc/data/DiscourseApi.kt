package app.nodeloc.data

import app.nodeloc.data.model.CsrfDto
import app.nodeloc.data.model.CurrentSessionDto
import app.nodeloc.data.model.CurrentUserDto
import app.nodeloc.data.model.CreatedPostDto
import app.nodeloc.data.model.GifSearchDto
import app.nodeloc.data.model.LatestDto
import app.nodeloc.data.model.LotteryActionResultDto
import app.nodeloc.data.model.LotteryDto
import app.nodeloc.data.model.NestedChildrenDto
import app.nodeloc.data.model.NestedTopicDto
import app.nodeloc.data.model.PostDto
import app.nodeloc.data.model.PostReplyHistoryDto
import app.nodeloc.data.model.PostRepliesDto
import app.nodeloc.data.model.PostsChunkDto
import app.nodeloc.data.model.RewardActionResultDto
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

    /** 带 CSRF 的写请求统一入口。_forum_session 被服务端定期轮换会使缓存的 CSRF 失效:
     *  遇 403 时清空缓存重取 token 自动重试一次。返回 (HTTP 状态码, 响应体)。 */
    private suspend fun writeRequest(
        path: String,
        build: Request.Builder.(csrf: String) -> Request.Builder,
    ): Pair<Int, String?> =
        withContext(Dispatchers.IO) {
            fun send(csrf: String): Pair<Int, String?> {
                val req = Request.Builder()
                    .url(BASE + path)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .header("X-CSRF-Token", csrf)
                    .build(csrf)
                    .build()
                return client.newCall(req).execute().use { resp -> resp.code to resp.body?.string() }
            }
            var result = send(fetchCsrf())
            if (result.first == 403) {
                SessionStore.csrfToken = null
                result = send(fetchCsrf())
            }
            result
        }

    private suspend fun postForm(path: String, form: FormBody): Pair<Int, String?> =
        writeRequest(path) { post(form) }

    private suspend fun putForm(path: String, form: FormBody = FormBody.Builder().build()): Pair<Int, String?> =
        writeRequest(path) { put(form) }

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
        val form = FormBody.Builder()
            .add("login", login)
            .add("password", password)
            .add("second_factor_method", "1")
        secondFactorToken?.takeIf { it.isNotBlank() }?.let { form.add("second_factor_token", it) }
        totp?.takeIf { it.isNotBlank() }?.let { form.add("second_factor_totp", it) }
        val (code, body) = postForm("/session", form.build())
        if (code !in 200..299 || body == null) throw httpError(code, body)
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

    /** 退出登录并清空本地会话 */
    suspend fun logout(username: String) {
        val (code, body) = postForm("/session/" + username + "/logout", FormBody.Builder().build())
        if (code !in 200..299) throw httpError(code, body)
        SessionStore.clear()
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

    /** discourse-lottery:购票参与。random=true 时服务端随机决定票数("随缘"按钮),quantity 仍需传但会被忽略。 */
    suspend fun lotteryParticipate(lotteryId: Long, quantity: Int, random: Boolean): LotteryActionResultDto {
        val form = FormBody.Builder()
            .add("quantity", quantity.toString())
            .add("random", random.toString())
            .build()
        val (code, body) = postForm("/lottery/$lotteryId/participate", form)
        if (code !in 200..299 || body == null) throw httpError(code, body)
        return json.decodeFromString(LotteryActionResultDto.serializer(), body)
    }

    /** discourse-lottery:发起者开奖(需 can_draw) */
    suspend fun lotteryDraw(lotteryId: Long): LotteryActionResultDto {
        val (code, body) = postForm("/lottery/$lotteryId/draw", FormBody.Builder().build())
        if (code !in 200..299 || body == null) throw httpError(code, body)
        return json.decodeFromString(LotteryActionResultDto.serializer(), body)
    }

    /** discourse-lottery:发起者手动结束抽奖(需 can_close) */
    suspend fun lotteryClose(lotteryId: Long): LotteryActionResultDto {
        val (code, body) = postForm("/lottery/$lotteryId/close", FormBody.Builder().build())
        if (code !in 200..299 || body == null) throw httpError(code, body)
        return json.decodeFromString(LotteryActionResultDto.serializer(), body)
    }

    /** discourse-lottery:拉取单个抽奖最新状态,用于操作后刷新卡片 */
    suspend fun lottery(lotteryId: Long): LotteryDto = get("/lottery/$lotteryId")

    /**
     * discourse-reactions:对某楼层切换一种表情反应。同一 reaction 再点一次是取消;
     * 选择另一种 reaction 会先取消当前反应再套用新的(服务端行为,不是本地模拟)。
     * 响应体是该楼层完整的最新 PostDto,直接替换本地缓存的这条帖子即可同步 UI。
     */
    suspend fun toggleReaction(postId: Long, reaction: String): PostDto {
        val encoded = java.net.URLEncoder.encode(reaction, "UTF-8")
        val (code, body) = putForm("/discourse-reactions/posts/$postId/custom-reactions/$encoded/toggle.json")
        if (code !in 200..299 || body == null) throw httpError(code, body)
        return json.decodeFromString(body)
    }

    /** discourse-reward:给某楼层打赏能量(站内积分)。amount 为正整数,note 可留空。 */
    suspend fun giveReward(postId: Long, amount: Int, note: String = ""): RewardActionResultDto {
        val form = FormBody.Builder()
            .add("post_id", postId.toString())
            .add("amount", amount.toString())
            .add("note", note)
            .build()
        val (code, body) = postForm("/reward/give", form)
        if (code !in 200..299 || body == null) throw httpError(code, body)
        return json.decodeFromString(body)
    }

    /** KLIPY GIF 搜索(经站点后端代理);pos 传上一页返回的 next 游标,首页传 null */
    suspend fun gifSearch(query: String, pos: String? = null): GifSearchDto =
        withContext(Dispatchers.IO) {
            val url = (BASE + "/gifs/search.json").toHttpUrl()
                .newBuilder()
                .addQueryParameter("q", query)
                .apply { pos?.let { addQueryParameter("pos", it) } }
                .build()
            val req = Request.Builder().url(url).build()
            client.newCall(req).execute().use { resp ->
                val body = resp.body?.string()
                if (!resp.isSuccessful || body == null) throw httpError(resp.code, body)
                json.decodeFromString(body)
            }
        }

    /**
     * 在话题下发布回复(顶层)。需要登录态;失败时抛 [ApiException],
     * message 为服务端文案(如频率限制、无权限)。
     */
    suspend fun createPost(topicId: Long, raw: String) {
        val form = FormBody.Builder()
            .add("topic_id", topicId.toString())
            .add("raw", raw)
            .build()
        val (code, body) = postForm("/posts", form)
        if (code !in 200..299 || body == null) throw httpError(code, body)
        // 发帖成功后旧 CSRF 已消费,置空待下次重取
        SessionStore.csrfToken = null
    }

    /** 发布新话题(POST /posts 同一端点,带 title+category 即视为创建新话题)。返回新话题 id,用于跳转详情页。 */
    suspend fun createTopic(title: String, raw: String, categoryId: Int): Long {
        val form = FormBody.Builder()
            .add("title", title)
            .add("raw", raw)
            .add("category", categoryId.toString())
            .build()
        val (code, body) = postForm("/posts", form)
        if (code !in 200..299 || body == null) throw httpError(code, body)
        SessionStore.csrfToken = null
        return json.decodeFromString(CreatedPostDto.serializer(), body).topicId
    }

}
