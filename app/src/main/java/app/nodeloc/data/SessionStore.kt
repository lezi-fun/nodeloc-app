package app.nodeloc.data

import android.content.Context
import android.content.SharedPreferences

/**
 * 会话持久化:仅存长期登录 cookie(_t)与最近一次 current_user JSON;
 * _forum_session 等临时 cookie 只存内存,冷启动重新经 /session/csrf 获取。
 */
object SessionStore {
    private const val PREFS = "nodeloc_session"
    private const val KEY_T = "cookie_t"
    private const val KEY_USER = "current_user_json"

    private lateinit var prefs: SharedPreferences

    /** 临时会话 cookie,仅本次进程有效 */
    @Volatile var sessionCookie: String? = null

    /** CSRF token,登录/登出后需重取 */
    @Volatile var csrfToken: String? = null

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    var tCookie: String?
        get() = if (::prefs.isInitialized) prefs.getString(KEY_T, null) else null
        set(value) {
            if (!::prefs.isInitialized) return
            prefs.edit().putString(KEY_T, value).apply()
        }

    fun cachedUserJson(): String? =
        if (::prefs.isInitialized) prefs.getString(KEY_USER, null) else null

    fun cacheUser(json: String) {
        if (!::prefs.isInitialized) return
        prefs.edit().putString(KEY_USER, json).apply()
    }

    fun clear() {
        sessionCookie = null
        csrfToken = null
        if (!::prefs.isInitialized) return
        prefs.edit().remove(KEY_T).remove(KEY_USER).apply()
    }
}
