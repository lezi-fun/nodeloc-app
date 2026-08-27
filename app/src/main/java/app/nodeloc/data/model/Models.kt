package app.nodeloc.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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

@Serializable
data class UserDto(
    val id: Int,
    val username: String,
    @SerialName("avatar_template") val avatarTemplate: String? = null,
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

@Serializable
data class CsrfDto(val csrf: String = "")

@Serializable
data class PosterRef(val user_id: Int = 0)

/**
 * 话题标签。NodeLoc 定制端下发对象数组 [{id,name,slug}],
 * 标准 Discourse 为字符串数组,序列化器对两者兼容。
 */
@Serializable(with = TagDtoSerializer::class)
data class TagDto(val id: Int = 0, val name: String = "", val slug: String = "")

object TagDtoSerializer : JsonTransformingSerializer<TagDto>(TagDto.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        if (element is JsonPrimitive) buildJsonObject { put("name", element.jsonPrimitive) } else element
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
    val tags: List<TagDto> = emptyList(),
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
    val tags: List<TagDto> = emptyList(),
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

@Serializable
data class ActionSummaryDto(val id: Int = 0, val count: Int = 0)

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

@Serializable
data class ReplyToUserDto(
    val id: Int = 0,
    val username: String = "",
    @SerialName("avatar_template") val avatarTemplate: String? = null,
)

@Serializable
data class PostDto(
    val id: Long = 0,
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
data class SiteDto(val categories: List<CategoryDto> = emptyList())

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
