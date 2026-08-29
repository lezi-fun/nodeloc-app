package app.nodeloc.data

import app.nodeloc.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

@Serializable
private data class GithubReleaseDto(
    @SerialName("tag_name") val tagName: String = "",
    val name: String? = null,
    @SerialName("html_url") val htmlUrl: String = "",
    val draft: Boolean = false,
    val prerelease: Boolean = false,
)

data class AppRelease(
    val tagName: String,
    val name: String,
    val htmlUrl: String,
)

/** 每次进程启动后台检查 GitHub 上手动发布的 v* release，不参与 NodeLoc 登录请求。 */
object GithubReleaseChecker {
    private const val ReleasesUrl = "https://api.github.com/repos/lezi-fun/nodeloc-app/releases?per_page=20"
    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .callTimeout(15, TimeUnit.SECONDS)
        .build()
    private val _latestRelease = MutableStateFlow<AppRelease?>(null)
    val latestRelease: StateFlow<AppRelease?> = _latestRelease.asStateFlow()

    suspend fun check() = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(ReleasesUrl)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "NodeLocAndroid/${BuildConfig.VERSION_NAME}")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use
                val releases = json.decodeFromString<List<GithubReleaseDto>>(response.body?.string().orEmpty())
                val latest = releases
                    .asSequence()
                    .filter { it.tagName.startsWith("v") && !it.draft && !it.prerelease && it.htmlUrl.isNotBlank() }
                    .mapNotNull { release ->
                        val version = release.tagName.removePrefix("v")
                        if (isNewer(version, BuildConfig.VERSION_NAME)) {
                            AppRelease(release.tagName, release.name.orEmpty(), release.htmlUrl)
                        } else null
                    }
                    .maxWithOrNull { left, right ->
                        compareVersions(
                            versionParts(left.tagName.removePrefix("v")),
                            versionParts(right.tagName.removePrefix("v")),
                        )
                    }
                _latestRelease.value = latest
            }
        }
    }

    private fun isNewer(candidate: String, current: String): Boolean =
        compareVersions(versionParts(candidate), versionParts(current)) > 0

    private fun versionParts(version: String): List<Int> =
        version.substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }.take(4).let {
            it + List((4 - it.size).coerceAtLeast(0)) { 0 }
        }

    private fun compareVersions(left: List<Int>, right: List<Int>): Int =
        (0 until maxOf(left.size, right.size)).firstNotNullOfOrNull { index ->
            (left.getOrElse(index) { 0 } - right.getOrElse(index) { 0 }).takeIf { it != 0 }
        } ?: 0
}
