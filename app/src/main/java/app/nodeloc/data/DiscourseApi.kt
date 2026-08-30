package app.nodeloc.data

import app.nodeloc.data.model.CheckinResponseDto
import app.nodeloc.data.model.CustomEmojiDto
import app.nodeloc.data.model.CsrfDto
import app.nodeloc.data.model.CurrentSessionDto
import app.nodeloc.data.model.CurrentUserDto
import app.nodeloc.data.model.AuthProviderDto
import app.nodeloc.data.model.BadgeStyleDto
import app.nodeloc.data.model.BookmarkCreatedDto
import app.nodeloc.data.model.CreatedPostDto
import app.nodeloc.data.model.PermanentlyDeleteCheckDto
import app.nodeloc.data.model.GifSearchDto
import app.nodeloc.data.model.LatestDto
import app.nodeloc.data.model.LotteryActionResultDto
import app.nodeloc.data.model.LotteryDto
import app.nodeloc.data.model.NestedChildrenDto
import app.nodeloc.data.model.NestedTopicDto
import app.nodeloc.data.model.NestedTopicPageDto
import app.nodeloc.data.model.PostDto
import app.nodeloc.data.model.PostCookedDto
import app.nodeloc.data.model.PostEditResponseDto
import app.nodeloc.data.model.PostPreviewDto
import app.nodeloc.data.model.PostReplyHistoryDto
import app.nodeloc.data.model.PostRepliesDto
import app.nodeloc.data.model.PostsChunkDto
import app.nodeloc.data.model.RewardActionResultDto
import app.nodeloc.data.model.UploadResponseDto
import app.nodeloc.data.model.UserActionDto
import app.nodeloc.data.model.UserActionsResponseDto
import app.nodeloc.data.model.UserProfileDto
import app.nodeloc.data.model.UserProfileResponseDto
import app.nodeloc.data.model.UserSummaryDto
import app.nodeloc.data.model.UserSummaryResponseDto
import app.nodeloc.data.model.SearchDto
import app.nodeloc.data.model.SessionResponseDto
import app.nodeloc.data.model.SiteDto
import app.nodeloc.data.model.TopicDetailDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody

object DiscourseApi {
    const val BASE = "https://www.nodeloc.com"
    private const val HOST = "www.nodeloc.com"
    @Volatile private var cachedBadgeStyles: List<BadgeStyleDto>? = null

    /**
     * coerceInputValues：服务端某些字段(如用户 name)理论上是非空字符串,但实际会返回 JSON null
     * (用户未填写昵称时)。没有这个开关,遇到「类型非空但值是 null」会直接抛异常导致整页崩溃;
     * 开启后 kotlinx.serialization 会自动回退到该字段声明的默认值,而不是让整个反序列化失败。
     */
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

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
        .connectTimeout(java.time.Duration.ofSeconds(15))
        .readTimeout(java.time.Duration.ofSeconds(45))
        .writeTimeout(java.time.Duration.ofSeconds(30))
        .callTimeout(java.time.Duration.ofSeconds(60))
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

    suspend fun customEmojis(): List<CustomEmojiDto> {
        val element = get<kotlinx.serialization.json.JsonElement>("/emojis.json")
        val obj = element.jsonObject
        val result = mutableListOf<CustomEmojiDto>()
        for ((_, groupArray) in obj) {
            val emojis = runCatching { groupArray.jsonArray }.getOrNull() ?: continue
            emojis.mapNotNullTo(result) {
                runCatching { json.decodeFromJsonElement(CustomEmojiDto.serializer(), it) }.getOrNull()
            }
        }
        return result.filter { it.name.isNotBlank() && it.url.isNotBlank() }
    }

    suspend fun authProviders(): List<AuthProviderDto> =
        get("/auth/list-providers")

    suspend fun latest(page: Int = 0): LatestDto =
        get("/latest.json?no_definitions=true&page=" + page)

    /**
     * include_raw=true 是编辑功能的前提:服务端 PostStreamSerializerMixin 只在这个 query
     * 参数为真时才会给每个楼层塞未渲染的 raw 字段(且仍受权限过滤,非本人/非 staff 楼层
     * 服务端本来就不会真的下发内容),没带这个参数时 raw 恒为 null,编辑框只能拿到空文本。
     */
    suspend fun topic(id: Long): TopicDetailDto =
        get("/t/" + id + ".json?track_visit=false&include_raw=true")

    suspend fun nestedTopic(slug: String, id: Long, page: Int = 0, sort: String = "top"): NestedTopicDto =
        get("/n/" + slug + "/" + id + ".json?page=" + page + "&sort=" + sort + "&include_raw=true")

    /** 嵌套话题的后续根楼层分页;响应只含 roots 分页字段,不含 topic/op_post。 */
    suspend fun nestedTopicPage(slug: String, id: Long, page: Int = 0, sort: String = "top"): NestedTopicPageDto =
        get("/n/" + slug + "/" + id + ".json?page=" + page + "&sort=" + sort + "&include_raw=true")

    suspend fun nestedChildren(
        slug: String,
        id: Long,
        parentPostNumber: Int,
        depth: Int,
        page: Int = 0,
        sort: String = "top",
    ): NestedChildrenDto = get(
        "/n/" + slug + "/" + id + "/children/" + parentPostNumber +
            ".json?page=" + page + "&sort=" + sort + "&depth=" + depth.coerceAtLeast(1) + "&include_raw=true",
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

    private suspend fun deleteRequest(path: String): Pair<Int, String?> =
        writeRequest(path) { delete() }

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

    /** NodeLoc discourse-checkin 插件:nonce 在客户端生成,服务端只校验本次请求一致性。 */
    suspend fun checkIn(): CheckinResponseDto {
        val nonce = buildString {
            repeat(2) {
                append(java.util.UUID.randomUUID().toString().replace("-", ""))
            }
        }.take(26)
        val form = FormBody.Builder()
            .add("nonce", nonce)
            .add("timestamp", System.currentTimeMillis().toString())
            .build()
        val (code, body) = writeRequest("/checkin") {
            header("X-Discourse-Checkin", "true")
                .header("X-Checkin-Nonce", nonce)
                .post(form)
        }
        if (code !in 200..299 || body == null) throw httpError(code, body)
        return json.decodeFromString(CheckinResponseDto.serializer(), body)
    }

    /** 使用 Discourse 的预览端点把 Markdown 转成 cooked HTML。 */
    suspend fun previewPost(raw: String): PostPreviewDto {
        val form = FormBody.Builder().add("raw", raw).build()
        val (code, body) = postForm("/posts/preview.json", form)
        if (code !in 200..299 || body == null) throw httpError(code, body)
        return json.decodeFromString(body)
    }

    /** 上传编辑器附件,与 Discourse Composer 使用同一 multipart 字段 file。 */
    suspend fun uploadAttachment(file: java.io.File, mimeType: String): UploadResponseDto = withContext(Dispatchers.IO) {
        val csrf = fetchCsrf()
        val requestBody = file.asRequestBody(mimeType.toMediaType())
        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, requestBody)
            .addFormDataPart("upload_type", "composer")
            .build()
        val request = Request.Builder()
            .url(BASE + "/uploads.json?client_id=nodeloc-android-composer")
            .header("X-Requested-With", "XMLHttpRequest")
            .header("X-CSRF-Token", csrf)
            .post(multipart)
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string()
            if (!response.isSuccessful || body == null) throw httpError(response.code, body)
            json.decodeFromString(UploadResponseDto.serializer(), body)
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
                .addQueryParameter("include_raw", "true")
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

    /** 内容本地化"查看原文":与官网一致,每次调用在原文/译文之间切换,不是幂等的单向操作。 */
    suspend fun postOriginalCooked(postId: Long): String = get<PostCookedDto>("/posts/$postId/cooked.json").cooked

    /**
     * 编辑楼层正文。[originalRaw] 是编辑前拿到的原文,服务端用它做冲突检测(官网字段名 original_text)。
     * 成功返回楼层最新完整 PostDto,直接替换本地缓存即可同步 UI。
     */
    suspend fun editPost(postId: Long, topicId: Long, raw: String, originalRaw: String): PostDto {
        val form = FormBody.Builder()
            .add("post[raw]", raw)
            .add("post[topic_id]", topicId.toString())
            .add("post[original_text]", originalRaw)
            .add("post[edit_reason]", "")
            .build()
        val (code, body) = putForm("/posts/$postId", form)
        if (code !in 200..299 || body == null) throw httpError(code, body)
        return json.decodeFromString<PostEditResponseDto>(body).post
    }

    /** 删除楼层(软删除,管理员/版主删除他人帖子也走这个接口)。[forceDestroy] 为真时是永久删除。 */
    suspend fun deletePost(postId: Long, forceDestroy: Boolean = false) {
        val path = if (forceDestroy) "/posts/$postId?force_destroy=true" else "/posts/$postId"
        val (code, body) = deleteRequest(path)
        if (code !in 200..299) throw httpError(code, body)
    }

    /** 恢复已删除的楼层。 */
    suspend fun recoverPost(postId: Long) {
        val (code, body) = putForm("/posts/$postId/recover")
        if (code !in 200..299) throw httpError(code, body)
    }

    /** 永久删除前的服务端确认:返回是否真的可以永久删除,不可以时带原因文案。 */
    suspend fun permanentlyDeleteCheck(postId: Long): PermanentlyDeleteCheckDto =
        get("/posts/$postId/permanently_delete_check.json")

    /**
     * 楼层级布尔字段的通用更新入口,官网锁定编辑(locked)/Wiki化(wiki)都走这个模式:
     * PUT /posts/{id}/{field} { field: value }。仅管理员/版主可调,楼层字段名与请求体键名一致。
     */
    private suspend fun updatePostField(postId: Long, field: String, value: Boolean) {
        val form = FormBody.Builder().add(field, value.toString()).build()
        val (code, body) = putForm("/posts/$postId/$field", form)
        if (code !in 200..299) throw httpError(code, body)
    }

    suspend fun setPostLocked(postId: Long, locked: Boolean) = updatePostField(postId, "locked", locked)

    suspend fun setPostWiki(postId: Long, wiki: Boolean) = updatePostField(postId, "wiki", wiki)

    /** 重新用最新 Markdown 规则渲染楼层 HTML(修 bug 或升级插件后用来批量刷正文渲染结果)。 */
    suspend fun rebakePost(postId: Long) {
        val (code, body) = putForm("/posts/$postId/rebake")
        if (code !in 200..299) throw httpError(code, body)
    }

    /** 取消隐藏(该楼层因被举报等原因被系统自动隐藏后,管理员/版主人工恢复展示)。 */
    suspend fun unhidePost(postId: Long) {
        val (code, body) = putForm("/posts/$postId/unhide")
        if (code !in 200..299) throw httpError(code, body)
    }

    /** 变更楼层所有者(把帖子过户给另一个用户,常用于处理违规注册的马甲号)。 */
    suspend fun changePostOwner(topicId: Long, postIds: List<Long>, newUsername: String) {
        val form = FormBody.Builder().apply {
            postIds.forEach { add("post_ids[]", it.toString()) }
            add("username", newUsername)
        }.build()
        val (code, body) = postForm("/t/$topicId/change-owner", form)
        if (code !in 200..299) throw httpError(code, body)
    }

    /** 收藏楼层,返回书签 id(取消收藏需要用它调 [deleteBookmark])。 */
    suspend fun bookmarkPost(postId: Long): Long {
        val form = FormBody.Builder()
            .add("bookmarkable_id", postId.toString())
            .add("bookmarkable_type", "Post")
            .add("auto_delete_preference", "3")
            .build()
        val (code, body) = postForm("/bookmarks.json", form)
        if (code !in 200..299 || body == null) throw httpError(code, body)
        return json.decodeFromString<BookmarkCreatedDto>(body).id
    }

    suspend fun deleteBookmark(bookmarkId: Long) {
        val (code, body) = deleteRequest("/bookmarks/$bookmarkId.json")
        if (code !in 200..299) throw httpError(code, body)
    }

    /**
     * 举报楼层。[postActionTypeId] 取自 /site.json 的 post_action_types
     * (如 3=偏离话题、4=不当言论、8=垃圾信息、10=非法);[message] 用于"其他内容"等需要补充说明的类型。
     */
    suspend fun flagPost(postId: Long, postActionTypeId: Int, message: String? = null) {
        val form = FormBody.Builder()
            .add("id", postId.toString())
            .add("post_action_type_id", postActionTypeId.toString())
            .apply { message?.takeIf { it.isNotBlank() }?.let { add("message", it) } }
            .build()
        val (code, body) = postForm("/post_actions", form)
        if (code !in 200..299) throw httpError(code, body)
    }

    /**
     * 阅读进度上报,对应官网 screen-track 服务的 POST /topics/timings。
     * [timings] 是本次要上报的楼层号→本次新增停留毫秒数增量(不是累计值);
     * [topicTimeMs] 是整个话题自打开以来的累计停留毫秒数。
     * 用官网同款的静默/后台请求头,避免这类高频后台请求触发登录跳转或弹错误提示。
     */
    suspend fun postTopicTimings(topicId: Long, timings: Map<Int, Long>, topicTimeMs: Long) {
        val form = FormBody.Builder().apply {
            timings.forEach { (postNumber, ms) -> add("timings[$postNumber]", ms.toString()) }
            add("topic_time", topicTimeMs.toString())
            add("topic_id", topicId.toString())
        }.build()
        val (code, _) = writeRequest("/topics/timings") {
            header("X-Silence-Logger", "true").header("Discourse-Background", "true").post(form)
        }
        if (code !in 200..299) throw httpError(code, null)
    }

    suspend fun userProfile(username: String): UserProfileDto = get<UserProfileResponseDto>("/u/$username.json").user

    suspend fun userSummary(username: String): UserSummaryDto =
        get<UserSummaryResponseDto>("/u/$username/summary.json").userSummary

    /** action types 4=回复 5=新话题(Discourse 标准枚举),用户主页"帖子"标签页用 */
    suspend fun userActions(username: String, filter: String = "4,5"): List<UserActionDto> =
        get<UserActionsResponseDto>("/user_actions.json?username=$username&filter=$filter").userActions

    /**
     * discourse_custom_badge:全站称号动效样式表,公开只读接口。进程内缓存一份即可,
     * 站点管理员改配置的频率极低,不需要每次都重新拉取。
     */
    suspend fun badgeStyles(): List<BadgeStyleDto> {
        cachedBadgeStyles?.let { return it }
        return runCatching { get<List<BadgeStyleDto>>("/discourse_custom_badge/badge-styles/list.json") }
            .getOrDefault(emptyList())
            .also { cachedBadgeStyles = it }
    }

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
