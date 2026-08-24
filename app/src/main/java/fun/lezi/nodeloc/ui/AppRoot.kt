package fun.lezi.nodeloc.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import fun.lezi.nodeloc.data.model.TopicDto
import fun.lezi.nodeloc.ui.screens.TopicDetailScreen
import fun.lezi.nodeloc.ui.screens.TopicListScreen
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** 极简双屏导航:列表 ⇄ 详情(与设计画布交互一致) */
@Composable
fun AppRoot() {
    var detailJson by rememberSaveable { mutableStateOf<String?>(null) }

    BackHandler(enabled = detailJson != null) { detailJson = null }

    val d = detailJson?.let { runCatching { DetailArgs.fromJson(it) }.getOrNull() }
    if (d == null) {
        TopicListScreen(
            onOpenTopic = { t: TopicDto -> detailJson = DetailArgs.of(t).toJson() }
        )
    } else {
        TopicDetailScreen(args = d, onBack = { detailJson = null })
    }
}

data class DetailArgs(val id: Long, val title: String, val categoryId: Int, val pinned: Boolean) {
    fun toJson(): String =
        """{"id":$id,"title":${JsonPrimitive(title)},"categoryId":$categoryId,"pinned":$pinned}"""

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        fun of(t: TopicDto) = DetailArgs(t.id, t.title, t.categoryId, t.isPinned)
        fun fromJson(s: String): DetailArgs {
            val o: JsonObject = json.parseToJsonElement(s).jsonObject
            return DetailArgs(
                id = o["id"]!!.jsonPrimitive.content.toLong(),
                title = o["title"]?.jsonPrimitive?.content ?: "",
                categoryId = o["categoryId"]?.jsonPrimitive?.content?.toInt() ?: 0,
                pinned = o["pinned"]?.jsonPrimitive?.content == "true",
            )
        }
    }
}