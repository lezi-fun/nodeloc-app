package app.nodeloc.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

@Serializable
data class PosterRef(val user_id: Int = 0)

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
    val posters: List<PosterRef> = emptyList(),
) {
    val isPinned get() = pinned || pinnedGlobally
}

@Serializable
data class TopicDetailDto(
    val id: Long,
    val title: String,
    val views: Int = 0,
    @SerialName("posts_count") val postsCount: Int = 0,
    @SerialName("category_id") val categoryId: Int = 0,
    @SerialName("post_stream") val postStream: PostStreamDto = PostStreamDto(),
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

@Serializable
data class ReplyToUserDto(
    val id: Int = 0,
    val username: String = "",
    @SerialName("avatar_template") val avatarTemplate: String? = null,
)

@Serializable
data class PostDto(
    val id: Long = 0,
    val username: String,
    @SerialName("avatar_template") val avatarTemplate: String? = null,
    @SerialName("post_number") val postNumber: Int = 0,
    val cooked: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("reply_to_post_number") val replyToPostNumber: Int? = null,
    @SerialName("reply_to_user") val replyToUser: ReplyToUserDto? = null,
    @SerialName("reply_count") val replyCount: Int = 0,
    @SerialName("actions_summary") val actionsSummary: List<ActionSummaryDto> = emptyList(),
    /** 以下三项由 Discourse 逐楼层下发,用于展示身份徽章 */
    val admin: Boolean = false,
    val moderator: Boolean = false,
    val staff: Boolean = false,
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
)
