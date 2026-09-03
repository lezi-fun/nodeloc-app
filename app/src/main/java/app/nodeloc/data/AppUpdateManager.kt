package app.nodeloc.data

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * GitHub Release 信息
 */
@Serializable
data class GitHubRelease(
    val tag_name: String,
    val name: String,
    val body: String? = null,
    val html_url: String,
    val assets: List<GitHubAsset> = emptyList(),
)

/**
 * GitHub Release Asset
 */
@Serializable
data class GitHubAsset(
    val name: String,
    val browser_download_url: String,
)

/**
 * 应用更新状态管理
 */
object AppUpdateManager {
    private const val TAG = "AppUpdateManager"
    private const val GITHUB_REPO = "lezi-fun/nodeloc-app"
    private const val GITHUB_API_URL = "https://api.github.com/repos/$GITHUB_REPO/releases/latest"

    /** 是否显示更新对话框 */
    var showUpdateDialog by mutableStateOf(false)
        private set

    /** 新版本号 */
    var newVersion by mutableStateOf("")
        private set

    /** 当前版本号 */
    var currentVersion by mutableStateOf("")
        private set

    /** 更新消息内容 */
    var updateMessage by mutableStateOf("")
        private set

    /** GitHub Release URL */
    var releaseUrl by mutableStateOf("")
        private set

    /** APK 下载 URL */
    var apkDownloadUrl by mutableStateOf("")
        private set

    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient()

    /**
     * 正式版本号的形状:v1、v1.2、v1.2.3 都算。
     *
     * CI 对每次提交都会发一个 `build-<短哈希>` 的 Release,这类 tag 不是版本号。
     * 原实现直接拿 tag 去比:`"build-f411917".split(".")` → `toIntOrNull() ?: 0` → `[0]`,
     * 与 `[0,3,2]` 比较时在第二段判定为更旧,于是静默地不提示 —— 即使正式版已发布。
     * 这里显式过滤,只认版本号形状的 tag。
     */
    private val versionTag = Regex("""^v?\d+(\.\d+)*$""")

    /**
     * 检查 GitHub Release 更新
     * @param currentVersionName 当前应用版本号（如 "1.0.0"）
     */
    fun checkForUpdates(currentVersionName: String, scope: CoroutineScope) {
        this.currentVersion = currentVersionName

        scope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(GITHUB_API_URL)
                    .header("Accept", "application/vnd.github.v3+json")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.w(TAG, "Failed to check updates: ${response.code}")
                    return@launch
                }

                val body = response.body?.string() ?: return@launch
                val release = json.decodeFromString<GitHubRelease>(body)

                // 只认版本号形状的 tag;build-<hash> 这类自动构建直接跳过
                if (!versionTag.matches(release.tag_name.trim())) {
                    Log.w(TAG, "Latest release tag is not a version: ${release.tag_name}")
                    return@launch
                }

                // 提取版本号（去掉 'v' 前缀）
                val latestVersion = release.tag_name.trim().removePrefix("v")

                // 查找 universal APK
                val universalApk = release.assets.find { asset ->
                    asset.name.contains("universal", ignoreCase = true) &&
                    asset.name.endsWith(".apk", ignoreCase = true)
                }

                if (universalApk == null) {
                    Log.w(TAG, "No universal APK found in release")
                    return@launch
                }

                // 比较版本号
                if (isNewerVersion(latestVersion, currentVersionName)) {
                    newVersion = latestVersion
                    releaseUrl = release.html_url
                    apkDownloadUrl = universalApk.browser_download_url
                    updateMessage = buildUpdateMessage(release)
                    showUpdateDialog = true
                    Log.i(TAG, "New version available: $latestVersion (current: $currentVersionName)")
                } else {
                    Log.d(TAG, "Already on latest version: $currentVersionName")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error checking for updates", e)
            }
        }
    }

    /**
     * 构建更新消息
     */
    private fun buildUpdateMessage(release: GitHubRelease): String {
        val changelog = release.body?.take(200) ?: "查看完整更新日志请访问 GitHub"
        return "发现新版本 ${release.tag_name}\n\n$changelog"
    }

    /**
     * 比较版本号
     * @return true 如果 newVer > currentVer
     */
    private fun isNewerVersion(newVer: String, currentVer: String): Boolean {
        try {
            val newParts = newVer.split(".").map { it.toIntOrNull() ?: 0 }
            val currentParts = currentVer.split(".").map { it.toIntOrNull() ?: 0 }

            val maxLength = maxOf(newParts.size, currentParts.size)
            for (i in 0 until maxLength) {
                val newPart = newParts.getOrNull(i) ?: 0
                val currentPart = currentParts.getOrNull(i) ?: 0

                if (newPart > currentPart) return true
                if (newPart < currentPart) return false
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error comparing versions", e)
            return false
        }
    }

    /**
     * 关闭更新对话框
     */
    fun dismissDialog() {
        showUpdateDialog = false
    }

    /**
     * 重置状态
     */
    fun reset() {
        showUpdateDialog = false
        newVersion = ""
        updateMessage = ""
        releaseUrl = ""
        apkDownloadUrl = ""
    }
}
