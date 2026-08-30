package app.nodeloc.data

import android.content.Context
import app.nodeloc.data.model.CurrentUserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/** 登录态单例:冷启动先用缓存用户恢复,再异步经 /session/current.json 校验 */
object SessionRepo {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private val _currentUser = MutableStateFlow<CurrentUserDto?>(null)
    val currentUser: StateFlow<CurrentUserDto?> = _currentUser.asStateFlow()

    fun init(context: Context) {
        SessionStore.init(context)
        SessionStore.cachedUserJson()?.let { cached ->
            runCatching { json.decodeFromString<CurrentUserDto>(cached) }
                .getOrNull()
                ?.takeIf { SessionStore.tCookie != null }
                ?.let { _currentUser.value = it }
        }
    }

    /** 网络校验当前会话;失败时保留本地状态,避免弱网误登出 */
    suspend fun refresh() {
        val user = runCatching { DiscourseApi.currentUser() }.getOrNull() ?: return
        _currentUser.value = user
        SessionStore.cacheUser(json.encodeToString(CurrentUserDto.serializer(), user))
    }

    suspend fun login(
        login: String,
        password: String,
        secondFactorToken: String? = null,
        totp: String? = null,
    ): CurrentUserDto {
        val user = DiscourseApi.login(login, password, secondFactorToken, totp)
        _currentUser.value = user
        SessionStore.cacheUser(json.encodeToString(CurrentUserDto.serializer(), user))
        return user
    }

    suspend fun loginWithPayload(payload: String): CurrentUserDto {
        val user = DiscourseApi.loginWithPayload(payload)
        _currentUser.value = user
        SessionStore.cacheUser(json.encodeToString(CurrentUserDto.serializer(), user))
        return user
    }

    suspend fun logout() {
        val name = _currentUser.value?.username ?: return
        runCatching { DiscourseApi.logout(name) }
        _currentUser.value = null
    }
}
