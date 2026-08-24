package fun.lezi.nodeloc.data

import fun.lezi.nodeloc.data.model.LatestDto
import fun.lezi.nodeloc.data.model.SiteDto
import fun.lezi.nodeloc.data.model.TopicDetailDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request

object DiscourseApi {
    const val BASE = "https://www.nodeloc.com"

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val client = OkHttpClient.Builder()
        .callTimeout(java.time.Duration.ofSeconds(25))
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", "NodeLocAndroid/0.1 (+https://github.com/lezi-fun/nodeloc-app)")
                    .header("Accept", "application/json")
                    .build()
            )
        }
        .build()

    private suspend inline fun <reified T> get(path: String): T =
        withContext(Dispatchers.IO) {
            val req = Request.Builder().url(BASE + path).build()
            client.newCall(req).execute().use { resp ->
                val body = resp.body?.string()
                check(resp.isSuccessful && body != null) { "HTTP " + resp.code }
                json.decodeFromString(body)
            }
        }

    suspend fun latest(page: Int = 0): LatestDto =
        get("/latest.json?no_definitions=true&page=" + page)

    suspend fun topic(id: Long): TopicDetailDto =
        get("/t/" + id + ".json?track_visit=false")

    suspend fun site(): SiteDto =
        get("/site.json")

    companion object {
        val jsonMedia = "application/json; charset=utf-8".toMediaType()
    }
}
