package app.nodeloc.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import app.nodeloc.AuthCallbackHandler
import app.nodeloc.data.SessionRepo
import app.nodeloc.data.model.TopicDto
import app.nodeloc.ui.components.NodeLocDrawer
import app.nodeloc.ui.screens.CreateTopicScreen
import app.nodeloc.ui.screens.LoginScreen
import app.nodeloc.ui.screens.SearchScreen
import app.nodeloc.ui.screens.SettingsScreen
import app.nodeloc.ui.screens.TopicDetailScreen
import app.nodeloc.ui.screens.TopicListScreen
import app.nodeloc.ui.screens.UserProfileScreen
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
    var createTopicOpen by rememberSaveable { mutableStateOf(false) }
    var profileUsername by rememberSaveable { mutableStateOf<String?>(null) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // 注册 OAuth 回调处理
    DisposableEffect(Unit) {
        AuthCallbackHandler.setCallback { payload ->
            scope.launch {
                runCatching { SessionRepo.loginWithPayload(payload) }
                    .onSuccess { loginOpen = false }
                    .onFailure { /* 静默失败，用户可以重试 */ }
            }
        }
        onDispose {
            AuthCallbackHandler.clearCallback()
        }
    }

    BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }
    BackHandler(enabled = profileUsername != null && !drawerState.isOpen) { profileUsername = null }
    BackHandler(enabled = detailJson != null && profileUsername == null && !drawerState.isOpen) { detailJson = null }
    BackHandler(
        enabled = searchOpen && detailJson == null && profileUsername == null && !drawerState.isOpen,
    ) { searchOpen = false }
    BackHandler(
        enabled = loginOpen && detailJson == null && !searchOpen && profileUsername == null && !drawerState.isOpen,
    ) { loginOpen = false }
    BackHandler(
        enabled = createTopicOpen && detailJson == null && !searchOpen && !loginOpen &&
            profileUsername == null && !drawerState.isOpen,
    ) { createTopicOpen = false }
    BackHandler(
        enabled = settingsOpen && detailJson == null && !searchOpen && !loginOpen &&
            !createTopicOpen && profileUsername == null && !drawerState.isOpen,
    ) { settingsOpen = false }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            NodeLocDrawer(
                onClose = { scope.launch { drawerState.close() } },
                onOpenLogin = { loginOpen = true },
                onOpenTopicId = { id -> detailJson = DetailArgs(id, "", 0, false).toJson() },
                onOpenProfile = { username -> profileUsername = username },
                onOpenSettings = { settingsOpen = true },
            )
        },
    ) {
        val d = detailJson?.let { runCatching { DetailArgs.fromJson(it) }.getOrNull() }
        when {
            settingsOpen -> SettingsScreen(onBack = { settingsOpen = false })
            profileUsername != null -> UserProfileScreen(
                username = profileUsername!!,
                onBack = { profileUsername = null },
                onOpenTopic = { topicId -> detailJson = DetailArgs(topicId, "", 0, false).toJson() },
            )
            d != null -> TopicDetailScreen(
                args = d,
                onBack = { detailJson = null },
                onOpenLogin = { loginOpen = true },
                onOpenProfile = { username -> profileUsername = username },
            )
            searchOpen -> SearchScreen(
                onBack = { searchOpen = false },
                onOpenTopic = { t: TopicDto -> detailJson = DetailArgs.of(t).toJson() },
            )
            loginOpen -> LoginScreen(onBack = { loginOpen = false })
            createTopicOpen -> CreateTopicScreen(
                onBack = { createTopicOpen = false },
                onCreated = { topicId ->
                    createTopicOpen = false
                    detailJson = DetailArgs(topicId, "", 0, false).toJson()
                },
            )
            else -> TopicListScreen(
                onOpenDrawer = { scope.launch { drawerState.open() } },
                onOpenSearch = { searchOpen = true },
                onOpenTopic = { t: TopicDto -> detailJson = DetailArgs.of(t).toJson() },
                onOpenLogin = { loginOpen = true },
                onOpenCreateTopic = { createTopicOpen = true },
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
