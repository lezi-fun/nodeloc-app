package app.nodeloc.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@Serializable
data class LatestDto(
    val users: List<UserDto> = emptyList(),
    @SerialName("topic_list") val topicList: TopicListDto = TopicListDto(),
)

@Serializable
data class TopicListDto(
    val topics: List<TopicDto> = emptyList(),
    @SerialName("more_topics_url") val moreTopicsUrl: String? = null,
    @SerialName("per_page") val perPage: Int? = null,
)

/** GET /c/{id}.json 与 GET /topics/private-messages/{username}.json 共用的顶层容器 */
@Serializable
data class CategoryTopicsResponseDto(@SerialName("topic_list") val topicList: TopicListDto = TopicListDto())

/** 私信收件箱列表一条:与公开话题字段不同(无 category,带对方最新发帖人与未读数) */
@Serializable
data class PmTopicDto(
    val id: Long,
    val title: String,
    @SerialName("posts_count") val postsCount: Int = 0,
    @SerialName("bumped_at") val bumpedAt: String = "",
    val excerpt: String? = null,
    @SerialName("last_poster_username") val lastPosterUsername: String = "",
    val unread: Int = 0,
)

@Serializable
data class PmTopicListDto(val topics: List<PmTopicDto> = emptyList())

@Serializable
data class PmListResponseDto(@SerialName("topic_list") val topicList: PmTopicListDto = PmTopicListDto())

@Serializable
data class UserDto(
    val id: Int,
    val username: String,
    @SerialName("avatar_template") val avatarTemplate: String? = null,
)

/** GET /u/search/users.json 响应,私信收件人实时联想用 */
@Serializable
data class UserSearchDto(val users: List<UserDto> = emptyList())

@Serializable
data class CustomEmojiDto(
    val name: String = "",
    val url: String = "",
    val group: String = "",
    val tonable: Boolean = false,
)

@Serializable
data class AuthProviderDto(
    val name: String = "",
    @SerialName("pretty_name_override") val prettyName: String? = null,
    @SerialName("title_override") val title: String? = null,
    @SerialName("frame_width") val frameWidth: Int? = null,
    @SerialName("frame_height") val frameHeight: Int? = null,
    @SerialName("can_connect") val canConnect: Boolean? = null,
    @SerialName("can_revoke") val canRevoke: Boolean? = null,
)

/** /session/current.json 与登录成功返回的当前用户 */
@Serializable
data class CurrentUserDto(
    val id: Int,
    val username: String,
    @SerialName("avatar_template") val avatarTemplate: String? = null,
    @SerialName("trust_level") val trustLevel: Int = 0,
    @SerialName("unread_notifications") val unreadNotifications: Int = 0,
    @SerialName("unread_high_priority_notifications") val unreadHighPriorityNotifications: Int = 0,
    val admin: Boolean = false,
    val moderator: Boolean = false,
    /** 侧栏"节点"区块要展示哪些分类,由用户在官网侧栏设置里选定,服务端下发 */
    @SerialName("sidebar_category_ids") val sidebarCategoryIds: List<Int> = emptyList(),
    /** 侧栏"标签"区块的自定义标签;为空时前端回退到站点热门标签 */
    @SerialName("sidebar_tags") val sidebarTags: List<String> = emptyList(),
    /** 侧栏"小程序"区块:最近使用过的小程序话题 */
    @SerialName("recent_apps") val recentApps: List<RecentAppDto> = emptyList(),
    /**
     * 是否有权变更帖子所有者,服务端已经算好直接下发(仅 admin,或站点开关允许时的版主/
     * 特定用户组成员才会带这个字段,值恒为 true,字段不存在即代表没有权限)。
     */
    @SerialName("can_change_post_owner") val canChangePostOwner: Boolean = false,
) {
    /** 是否是站务人员(管理员或版主),对应官网 guardian 的 is_staff? */
    val isStaff: Boolean get() = admin || moderator

    /**
     * 与官网 currentUser.canManageTopic 一致:管理员/版主,或信任等级4的领袖用户。
     * 注意不是所有"管理操作"都用这个粒度——锁定编辑/取消隐藏/变更所有者的后端 guardian
     * 只认 is_staff?,不含 tl4;只有"重新渲染"用的是这个更宽的判断,调用处按需选用 [isStaff] 或本属性。
     */
    val canManageTopic: Boolean get() = isStaff || trustLevel == 4
}

/** 侧栏"小程序"条目,对应 discourse_apps 插件的最近使用记录 */
@Serializable
data class RecentAppDto(
    val id: Int = 0,
    val slug: String = "",
    val name: String = "",
    @SerialName("logo_url") val logoUrl: String? = null,
    val url: String = "",
)

@Serializable
data class CurrentSessionDto(@SerialName("current_user") val currentUser: CurrentUserDto? = null)

/** POST /session 的响应:成功含 user;失败含 error;需 2FA 时含 second_factor_* 字段 */
@Serializable
data class SessionResponseDto(
    val user: CurrentUserDto? = null,
    val error: String? = null,
    @SerialName("second_factor_required") val secondFactorRequired: Boolean = false,
    @SerialName("second_factor_token") val secondFactorToken: String? = null,
    @SerialName("second_factor_method") val secondFactorMethod: Int? = null,
)

/** POST /posts/preview.json 响应。 */
@Serializable
data class PostPreviewDto(val cooked: String = "")

/** POST /uploads.json 响应;短链接优先用于 Markdown 插入。 */
@Serializable
data class UploadResponseDto(
    val url: String? = null,
    @SerialName("short_url") val shortUrl: String? = null,
    @SerialName("original_filename") val originalFilename: String? = null,
    val error: String? = null,
)

/** POST /checkin 响应;签到插件返回当天日期和获得的能量。 */
@Serializable
data class CheckinResponseDto(
    val success: Boolean = false,
    val points: Int = 0,
    @SerialName("user_date") val userDate: String? = null,
    val timezone: String? = null,
    val message: String? = null,
)

@Serializable
data class CsrfDto(val csrf: String = "")

@Serializable
data class PosterRef(val user_id: Int = 0)

/**
 * 话题标签。NodeLoc 定制端下发对象数组 [{id,name,slug}],
 * 标准 Discourse 为字符串数组,序列化器对两者兼容。
 */
@Serializable
data class TagDto(val id: Int = 0, val name: String = "", val slug: String = "")

/**
 * 注意:不要在 TagDto 类声明上加 @Serializable(with = TagDtoSerializer::class)。
 * 那样会让 TagDto.serializer() 反过来返回 TagDtoSerializer 自己,而这里的父类构造函数
 * 又需要调用 TagDto.serializer() 取内部序列化器,形成循环初始化,运行时会在类加载阶段
 * 直接抛 NullPointerException(JVM 处理静态初始化循环依赖的经典陷阱)。
 * 正确做法是只在使用处(见 TopicDto.tags/TopicDetailDto.tags 字段)标注 @Serializable(with=...)。
 */
object TagDtoSerializer : JsonTransformingSerializer<TagDto>(TagDto.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        if (element is JsonPrimitive) buildJsonObject { put("name", element.jsonPrimitive) } else element
}

/** List<TagDto> 属性专用:逐元素套用 [TagDtoSerializer] 的兼容规则,用在 tags 字段上。 */
object TagDtoListSerializer : KSerializer<List<TagDto>> {
    private val delegate = ListSerializer(TagDtoSerializer)
    override val descriptor: SerialDescriptor = delegate.descriptor
    override fun serialize(encoder: Encoder, value: List<TagDto>) = delegate.serialize(encoder, value)
    override fun deserialize(decoder: Decoder): List<TagDto> = delegate.deserialize(decoder)
}

@Serializable
data class TopicDto(
    val id: Long,
    val title: String,
    val slug: String = "topic",
    @SerialName("posts_count") val postsCount: Int = 0,
    @SerialName("like_count") val likeCount: Int = 0,
    val views: Int = 0,
    @SerialName("category_id") val categoryId: Int = 0,
    val pinned: Boolean = false,
    @SerialName("pinned_globally") val pinnedGlobally: Boolean = false,
    @SerialName("bumped_at") val bumpedAt: String = "",
    /** 该话题需要指定信任等级才能阅读正文 */
    @SerialName("has_read_permission_restriction") val hasReadPermissionRestriction: Boolean = false,
    @SerialName("read_permission_trust_level") val readPermissionTrustLevel: Int? = null,
    val posters: List<PosterRef> = emptyList(),
    @Serializable(with = TagDtoListSerializer::class) val tags: List<TagDto> = emptyList(),
) {
    val isPinned get() = pinned || pinnedGlobally
}

@Serializable
data class TopicDetailDto(
    val id: Long,
    val title: String,
    val slug: String = "topic",
    val views: Int = 0,
    @SerialName("posts_count") val postsCount: Int = 0,
    @SerialName("category_id") val categoryId: Int = 0,
    @SerialName("is_nested_view") val isNestedView: Boolean = false,
    @SerialName("post_stream") val postStream: PostStreamDto = PostStreamDto(),
    @Serializable(with = TagDtoListSerializer::class) val tags: List<TagDto> = emptyList(),
)

@Serializable
data class NestedTopicDto(
    val topic: TopicDetailDto,
    @SerialName("op_post") val opPost: PostDto,
    val roots: List<PostDto> = emptyList(),
    val page: Int = 0,
    val sort: String = "top",
    @SerialName("effective_sort") val effectiveSort: String = sort,
    @SerialName("has_more_roots") val hasMoreRoots: Boolean = false,
)

/** 嵌套话题继续加载根楼层时的分页响应;服务端不会重复返回 topic/op_post。 */
@Serializable
data class NestedTopicPageDto(
    val roots: List<PostDto> = emptyList(),
    @SerialName("has_more_roots") val hasMoreRoots: Boolean = false,
    val page: Int = 0,
    val sort: String = "top",
    @SerialName("effective_sort") val effectiveSort: String = sort,
)

@Serializable
data class NestedChildrenDto(
    val children: List<PostDto> = emptyList(),
    val page: Int = 0,
    @SerialName("has_more") val hasMore: Boolean = false,
)

@Serializable
data class PostStreamDto(
    val posts: List<PostDto> = emptyList(),
    /** 全部楼层 id(Discourse 分块拉取依据) */
    val stream: List<Long> = emptyList(),
)

/** /t/{id}/posts.json?post_ids[]=… 的响应 */
@Serializable
data class PostsChunkDto(
    @SerialName("post_stream") val postStream: PostStreamDto = PostStreamDto(),
    val users: List<UserDto> = emptyList(),
)

@Serializable
data class PostRepliesDto(
    @SerialName("post_replies") val posts: List<PostDto> = emptyList(),
)

@Serializable
data class PostReplyHistoryDto(
    @SerialName("post_reply_histories") val posts: List<PostDto> = emptyList(),
)

/**
 * 楼层可执行操作汇总(GET /t/{id}.json 的 posts[].actions_summary):每个 id 对应
 * /site.json 里 post_action_types 的一种操作(2=赞、3=偏离话题、4=不当言论...)。
 * [canAct] 为真表示当前登录用户当下可以对这条楼层执行该操作——比如本人帖子的
 * "赞"(id=2)没有 can_act,因为不能给自己点赞;未登录或已经点过赞时同样没有 can_act。
 * [count] 仅点赞(id=2)才有意义,是当前的赞数。
 */
@Serializable
data class ActionSummaryDto(
    val id: Int = 0,
    val count: Int = 0,
    @SerialName("can_act") val canAct: Boolean = false,
)

/** discourse-reactions:某一种表情反应的汇总计数,id 是表情键名(heart/+1/laughing/...) */
@Serializable
data class ReactionSummaryDto(val id: String = "", val type: String = "emoji", val count: Int = 0)

/** discourse-reactions:当前用户已选择的反应;can_undo 为 false 时(如管理员锁定)不能取消 */
@Serializable
data class CurrentUserReactionDto(
    val id: String = "",
    val type: String = "emoji",
    @SerialName("can_undo") val canUndo: Boolean = true,
)

@Serializable
data class LotteryLevelDto(
    val id: Int = 0,
    val name: String = "",
    val prize: String = "",
    val quantity: Int = 0,
)

@Serializable
data class LotteryParticipantDto(
    val username: String = "",
    @SerialName("avatar_template") val avatarTemplate: String? = null,
    val tickets: Int = 0,
    @SerialName("is_random") val isRandom: Boolean = false,
)

@Serializable
data class LotteryWinnerDto(
    val username: String = "",
    @SerialName("avatar_template") val avatarTemplate: String? = null,
    @SerialName("level_name") val levelName: String = "",
    val prize: String = "",
)

/** discourse-lottery 插件:与帖子 cooked 正文平行的抽奖组件状态 */
@Serializable
data class LotteryDto(
    val id: Long = 0,
    val title: String = "",
    @SerialName("min_participants") val minParticipants: Int = 0,
    @SerialName("max_participants") val maxParticipants: Int = 0,
    @SerialName("max_tickets_per_user") val maxTicketsPerUser: Int = 1,
    @SerialName("min_tickets_per_user") val minTicketsPerUser: Int = 1,
    @SerialName("draw_at") val drawAt: String = "",
    /** open / drawn(已开奖) / failed(流抽,未达最低参与门槛) / closed(发起者手动关闭) */
    val status: String = "open",
    val levels: List<LotteryLevelDto> = emptyList(),
    @SerialName("tickets_count") val ticketsCount: Int = 0,
    @SerialName("participants_count") val participantsCount: Int = 0,
    @SerialName("user_tickets") val userTickets: Int = 0,
    @SerialName("is_participating") val isParticipating: Boolean = false,
    @SerialName("can_draw") val canDraw: Boolean? = null,
    @SerialName("can_manage") val canManage: Boolean? = null,
    @SerialName("can_close") val canClose: Boolean? = null,
    val participants: List<LotteryParticipantDto> = emptyList(),
    val winners: List<LotteryWinnerDto> = emptyList(),
) {
    val isOpen get() = status == "open"
    val isDrawn get() = status == "drawn"
    val maxParticipantsDisplay get() = if (maxParticipants >= 1_000_000) "∞" else maxParticipants.toString()
}

/** discourse-reward:一条打赏记录(能量,即站内积分) */
@Serializable
data class RewardDto(
    val id: Long = 0,
    val amount: Int = 0,
    val username: String = "",
    @SerialName("avatar_template") val avatarTemplate: String? = null,
    val note: String? = null,
    @SerialName("is_system_reward") val isSystemReward: Boolean = false,
    @SerialName("is_deduct") val isDeduct: Boolean = false,
)

/** POST /reward/give 等接口的响应:成功时带回新增的这一条打赏记录 */
@Serializable
data class RewardActionResultDto(
    val success: Boolean = false,
    val reward: RewardDto? = null,
    val message: String? = null,
)

/** POST /lottery/{id}/participate、/draw、/close 的统一响应形态 */
@Serializable
data class LotteryActionResultDto(
    val success: Boolean = false,
    val message: String? = null,
)

/** GET /gifs/search.json 单个搜索结果的 webp 媒体信息 */
@Serializable
data class GifMediaDto(val url: String = "", val dims: List<Int> = emptyList())

@Serializable
data class GifMediaFormatsDto(val webp: GifMediaDto = GifMediaDto())

@Serializable
data class GifResultDto(
    val id: String = "",
    val title: String = "",
    @SerialName("media_formats") val mediaFormats: GifMediaFormatsDto = GifMediaFormatsDto(),
)

/** GET /posts/{id}/cooked.json 响应:切换原文/译文后的正文 HTML */
@Serializable
data class PostCookedDto(val cooked: String = "")

/** PUT /posts/{id} 响应的顶层容器 */
@Serializable
data class PostEditResponseDto(val post: PostDto)

/** GET /gifs/search.json 响应:next 为空字符串或缺省表示没有下一页 */
@Serializable
data class GifSearchDto(
    val results: List<GifResultDto> = emptyList(),
    val next: String? = null,
)

/** POST /posts 成功创建新话题时的响应,只取跳转详情页需要的字段 */
@Serializable
data class CreatedPostDto(
    @SerialName("topic_id") val topicId: Long = 0,
)

/** POST /bookmarks.json 成功响应,取书签 id 供取消收藏时使用 */
@Serializable
data class BookmarkCreatedDto(val id: Long = 0)

/** GET /posts/{id}/permanently_delete_check.json 响应:永久删除前的服务端权限确认 */
@Serializable
data class PermanentlyDeleteCheckDto(
    @SerialName("can_permanently_delete") val canPermanentlyDelete: Boolean = false,
    val reason: String? = null,
)

@Serializable
data class ReplyToUserDto(
    val id: Int = 0,
    val username: String = "",
    @SerialName("avatar_template") val avatarTemplate: String? = null,
)

@Serializable
data class PostDto(
    val id: Long = 0,
    @SerialName("topic_id") val topicId: Long = 0,
    val username: String = "",
    @SerialName("avatar_template") val avatarTemplate: String? = null,
    @SerialName("post_number") val postNumber: Int = 0,
    val cooked: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("reply_to_post_number") val replyToPostNumber: Int? = null,
    @SerialName("reply_to_user") val replyToUser: ReplyToUserDto? = null,
    @SerialName("reply_count") val replyCount: Int = 0,
    @SerialName("direct_reply_count") val directReplyCount: Int = 0,
    @SerialName("total_descendant_count") val totalDescendantCount: Int = 0,
    val children: List<PostDto>? = null,
    @SerialName("actions_summary") val actionsSummary: List<ActionSummaryDto> = emptyList(),
    /** 以下三项由 Discourse 逐楼层下发,用于展示身份徽章 */
    val admin: Boolean = false,
    val moderator: Boolean = false,
    val staff: Boolean = false,
    /** discourse-lottery 插件:该楼层若挂了抽奖组件则非空,与 cooked 正文平行渲染 */
    val lottery: LotteryDto? = null,
    /** discourse-reactions:该楼层已收到的各类表情反应汇总(id 为 heart/+1/laughing 等表情键名) */
    val reactions: List<ReactionSummaryDto> = emptyList(),
    /** discourse-reactions:当前登录用户对该楼层已选择的反应,未反应时为 null */
    @SerialName("current_user_reaction") val currentUserReaction: CurrentUserReactionDto? = null,
    /** discourse-reward:该楼层已收到的打赏记录,最新的排在最前(与官网一致) */
    val rewards: List<RewardDto> = emptyList(),
    /** discourse-custom-badge:发帖时该用户显示的称号,可为空 */
    @SerialName("user_title") val userTitle: String? = null,
    /** discourse 内容本地化:该楼层原文语言,如 en/vi/ja;原文本就是站点默认语言时为 null */
    val locale: String? = null,
    /** true 表示当前 cooked 是自动翻译结果(非楼主原文语言),需展示"查看原文"提示条 */
    @SerialName("is_localized") val isLocalized: Boolean = false,
    /** 未渲染的 Markdown 源码;仅本人帖子且已登录时非空,用于填充编辑框 */
    val raw: String? = null,
    /** 当前用户是否有权编辑此楼层 */
    @SerialName("can_edit") val canEdit: Boolean = false,
    /** 是否为当前登录用户本人所发 */
    val yours: Boolean = false,
    /** 当前用户是否有权删除此楼层(含管理员/版主删除他人帖子的情形) */
    @SerialName("can_delete") val canDelete: Boolean = false,
    /** 楼层已被删除且当前用户有权恢复 */
    @SerialName("can_recover") val canRecover: Boolean = false,
    /** 是否已被当前用户加入书签 */
    val bookmarked: Boolean = false,
    /** 书签记录 id,取消书签(DELETE /bookmarks/{id}.json)时需要 */
    @SerialName("bookmark_id") val bookmarkId: Long? = null,
    /** 是否已被锁定编辑(仅管理员/版主可锁定/解锁,锁定后作者本人也不能再编辑) */
    val locked: Boolean = false,
    /** 是否已被设为 wiki 帖(任何受信任用户都能编辑正文) */
    val wiki: Boolean = false,
    /** 当前用户是否有权将此楼层设为/取消 wiki(本人楼主或管理员/版主) */
    @SerialName("can_wiki") val canWiki: Boolean = false,
    /** 帖子是否因被举报等原因被隐藏 */
    val hidden: Boolean = false,
    /** 非空表示楼层已被(软)删除 */
    @SerialName("deleted_at") val deletedAt: String? = null,
    /**
     * 仅当"已删除 + 当前用户是管理员 + 站点开启 can_permanently_delete 设置"三者都满足时,
     * 服务端才会下发这个字段(值恒为 true,不存在即代表不能永久删)。真正执行前仍需再调
     * permanently_delete_check 接口二次确认,这里只用来控制按钮是否展示。
     */
    @SerialName("can_permanently_delete") val canPermanentlyDelete: Boolean = false,
) {
    /** 徽章文案:管理员 → ADMIN,版主 → MOD,其余职员 → STAFF,普通用户无。 */
    val staffBadge: String?
        get() = when {
            admin -> "ADMIN"
            moderator -> "MOD"
            staff -> "STAFF"
            else -> null
        }
}

@Serializable
data class SiteDto(
    val categories: List<CategoryDto> = emptyList(),
    /** 站点热门标签,侧栏"标签"区块在用户没有自定义标签时的回退展示 */
    @SerialName("top_tags") val topTags: List<TagDto> = emptyList(),
    /** 全站可用的举报/操作类型定义,举报菜单的文案和"是否需要补充说明"都从这里取,不写死 */
    @SerialName("post_action_types") val postActionTypes: List<PostActionTypeDto> = emptyList(),
    @SerialName("popular_apps") val popularApps: List<RecentAppDto> = emptyList(),
    @SerialName("apps_browse_url") val appsBrowseUrl: String? = null,
)

/** /site.json 的 post_action_types 一条:定义一种可对楼层执行的操作(赞/举报的各细分原因) */
@Serializable
data class PostActionTypeDto(
    val id: Int = 0,
    val name: String = "",
    @SerialName("is_flag") val isFlag: Boolean = false,
    @SerialName("require_message") val requireMessage: Boolean = false,
    @SerialName("applies_to") val appliesTo: List<String> = emptyList(),
)

@Serializable
data class CategoryDto(
    val id: Int,
    val name: String = "",
    val color: String = "0088CC",
    val slug: String = "",
    /** 侧栏展示顺序,与官网一致 */
    val position: Int = 0,
    /** 分类自定义图标(站点为多数节点上传了 svg/png logo) */
    @SerialName("uploaded_logo") val uploadedLogo: UploadedLogoDto? = null,
    /** 非空表示这是某个顶级节点下的子节点 */
    @SerialName("parent_category_id") val parentCategoryId: Int? = null,
    @SerialName("topic_count") val topicCount: Int = 0,
)

@Serializable
data class UploadedLogoDto(
    val url: String = "",
    val width: Int = 0,
    val height: Int = 0,
)

/** /search.json 的响应,topics 结构与列表接口一致 */
@Serializable
data class SearchDto(
    val topics: List<TopicDto> = emptyList(),
    val posts: List<SearchPostDto> = emptyList(),
    val users: List<UserDto> = emptyList(),
    val categories: List<CategoryDto> = emptyList(),
)

/** 搜索命中帖子的匹配片段,按 topic_id 关联到话题行展示 */
@Serializable
data class SearchPostDto(
    val id: Long = 0,
    @SerialName("topic_id") val topicId: Long = 0,
    val username: String = "",
    @SerialName("avatar_template") val avatarTemplate: String? = null,
    val blurb: String = "",
    @SerialName("post_number") val postNumber: Int = 0,
    @SerialName("created_at") val createdAt: String = "",
)

/** GET /u/{username}.json 响应的顶层容器,只取 user 字段 */
@Serializable
data class UserProfileResponseDto(val user: UserProfileDto)

/** 用户主页展示所需字段 */
@Serializable
data class UserProfileDto(
    val id: Long = 0,
    val username: String = "",
    val name: String = "",
    @SerialName("avatar_template") val avatarTemplate: String? = null,
    @SerialName("animated_avatar") val animatedAvatar: String? = null,
    val title: String? = null,
    @SerialName("flair_name") val flairName: String? = null,
    @SerialName("flair_url") val flairUrl: String? = null,
    @SerialName("flair_bg_color") val flairBgColor: String? = null,
    @SerialName("flair_color") val flairColor: String? = null,
    @SerialName("trust_level") val trustLevel: Int = 0,
    val admin: Boolean = false,
    val moderator: Boolean = false,
    @SerialName("bio_excerpt") val bioExcerpt: String? = null,
    @SerialName("bio_cooked") val bioCooked: String? = null,
    val website: String? = null,
    val location: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("last_seen_at") val lastSeenAt: String? = null,
    @SerialName("badge_count") val badgeCount: Int = 0,
    @SerialName("gamification_score") val gamificationScore: Long = 0,
    @SerialName("total_followers") val totalFollowers: Int = 0,
    @SerialName("total_following") val totalFollowing: Int = 0,
)

/** GET /u/{username}/summary.json 响应的顶层容器 */
@Serializable
data class UserSummaryResponseDto(@SerialName("user_summary") val userSummary: UserSummaryDto)

@Serializable
data class UserSummaryDto(
    @SerialName("likes_given") val likesGiven: Int = 0,
    @SerialName("likes_received") val likesReceived: Int = 0,
    @SerialName("topic_count") val topicCount: Int = 0,
    @SerialName("post_count") val postCount: Int = 0,
    @SerialName("days_visited") val daysVisited: Int = 0,
    @SerialName("posts_read_count") val postsReadCount: Int = 0,
    @SerialName("solved_count") val solvedCount: Int = 0,
)

/** discourse_custom_badge 插件:全站称号(badge title)的文字颜色与动画特效配置表 */
@Serializable
data class BadgeStyleDto(
    val id: Long = 0,
    val name: String = "",
    val icon: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("custom_style") val customStyle: BadgeCustomStyleDto = BadgeCustomStyleDto(),
)

@Serializable
data class BadgeCustomStyleDto(
    @SerialName("text_color") val textColor: String? = null,
    @SerialName("text_effect") val textEffect: String? = null,
)

/** GET /user_actions.json 响应的顶层容器 */
@Serializable
data class UserActionsResponseDto(@SerialName("user_actions") val userActions: List<UserActionDto> = emptyList())

/** 用户主页"帖子"标签页的一条记录:action_type 4=回复 5=新话题(Discourse 标准枚举) */
@Serializable
data class UserActionDto(
    val excerpt: String? = null,
    @SerialName("action_type") val actionType: Int = 0,
    @SerialName("created_at") val createdAt: String = "",
    val title: String = "",
    @SerialName("topic_id") val topicId: Long = 0,
    @SerialName("post_number") val postNumber: Int = 0,
    @SerialName("category_id") val categoryId: Int = 0,
)
