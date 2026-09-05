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
    private const val KEY_CHECKIN_DATE = "checkin_date"
    private const val KEY_CHECKIN_USER = "checkin_user"
    private const val KEY_NOTIFICATION_LAST_ID = "notification_last_id"

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

    /**
     * 接管 WebView 的会话 cookie(第三方登录走完后调用)。
     * 入参是 CookieManager.getCookie() 的 "k=v; k=v" 格式,只取会话相关的两个。
     */
    fun adoptCookieHeader(cookieHeader: String) {
        cookieHeader.split(';').forEach { part ->
            val idx = part.indexOf('=')
            if (idx <= 0) return@forEach
            val name = part.substring(0, idx).trim()
            val value = part.substring(idx + 1).trim().takeIf { it.isNotBlank() } ?: return@forEach
            when (name) {
                "_t" -> tCookie = value
                "_forum_session" -> sessionCookie = value
            }
        }
        // 会话已换人,旧 CSRF 失效
        csrfToken = null
    }

    fun cachedUserJson(): String? =
        if (::prefs.isInitialized) prefs.getString(KEY_USER, null) else null

    fun cacheUser(json: String) {
        if (!::prefs.isInitialized) return
        prefs.edit().putString(KEY_USER, json).apply()
    }

    fun checkinDate(): String? = if (::prefs.isInitialized) prefs.getString(KEY_CHECKIN_DATE, null) else null

    fun markCheckedIn(userId: Int, date: String) {
        if (!::prefs.isInitialized) return
        prefs.edit().putInt(KEY_CHECKIN_USER, userId).putString(KEY_CHECKIN_DATE, date).apply()
    }

    fun isCheckedIn(userId: Int, date: String): Boolean =
        ::prefs.isInitialized && prefs.getInt(KEY_CHECKIN_USER, -1) == userId && prefs.getString(KEY_CHECKIN_DATE, null) == date

    /** 最近一次已经投递到系统通知栏的站点通知 ID。 */
    fun lastNotifiedNotificationId(): Long =
        if (::prefs.isInitialized) prefs.getLong(KEY_NOTIFICATION_LAST_ID, -1L) else -1L

    fun markNotificationNotified(id: Long) {
        if (!::prefs.isInitialized) return
        prefs.edit().putLong(KEY_NOTIFICATION_LAST_ID, id).apply()
    }

    fun clear() {
        sessionCookie = null
        csrfToken = null
        if (!::prefs.isInitialized) return
        prefs.edit()
            .remove(KEY_T)
            .remove(KEY_USER)
            .remove(KEY_CHECKIN_DATE)
            .remove(KEY_CHECKIN_USER)
            .remove(KEY_NOTIFICATION_LAST_ID)
            .apply()
    }
}
