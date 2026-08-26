package app.nodeloc.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import app.nodeloc.data.model.TopicDto
import app.nodeloc.ui.components.NodeLocDrawer
import app.nodeloc.ui.screens.LoginScreen
import app.nodeloc.ui.screens.SearchScreen
import app.nodeloc.ui.screens.TopicDetailScreen
import app.nodeloc.ui.screens.TopicListScreen
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun AppRoot() {
    var detailJson by rememberSaveable { mutableStateOf<String?>(null) }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var loginOpen by rememberSaveable { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }
    BackHandler(enabled = detailJson != null && !drawerState.isOpen) { detailJson = null }
    BackHandler(enabled = searchOpen && detailJson == null && !drawerState.isOpen) { searchOpen = false }
    BackHandler(enabled = loginOpen && detailJson == null && !searchOpen && !drawerState.isOpen) { loginOpen = false }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NodeLocDrawer(
                onClose = { scope.launch { drawerState.close() } },
                onOpenLogin = { loginOpen = true },
            )
        },
    ) {
        val d = detailJson?.let { runCatching { DetailArgs.fromJson(it) }.getOrNull() }
        when {
            d != null -> TopicDetailScreen(args = d, onBack = { detailJson = null })
            searchOpen -> SearchScreen(
                onBack = { searchOpen = false },
                onOpenTopic = { t: TopicDto -> detailJson = DetailArgs.of(t).toJson() },
            )
            loginOpen -> LoginScreen(onBack = { loginOpen = false })
            else -> TopicListScreen(
                onOpenDrawer = { scope.launch { drawerState.open() } },
                onOpenSearch = { searchOpen = true },
                onOpenTopic = { t: TopicDto -> detailJson = DetailArgs.of(t).toJson() },
            )
        }
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
