package app.nodeloc.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * 带 HTTP 语义的业务异常。[message] 为可直接展示给用户的中文文案(优先采用服务端返回的
 * errors 文案,与网页错误页一致);[code] 供界面区分 403/404 等专用状态;
 * [errorType] 对应 Discourse 的 error_type(如 invalid_access/not_found)。
 */
class ApiException(val code: Int, val errorType: String? = null, message: String) : RuntimeException(message)

/** 登录时服务端要求两步验证(TOTP),[secondFactorToken] 为二次提交所需临时凭据 */
class SecondFactorRequiredException(val secondFactorToken: String) :
    RuntimeException("该账号已开启两步验证")

private val errorJson = Json { ignoreUnknownKeys = true }

private fun errorElementMessage(element: JsonElement): String? = when (element) {
    is JsonPrimitive -> element.contentOrNull
    else -> element.jsonObject["message"]?.jsonPrimitive?.contentOrNull
        ?: element.jsonObject["description"]?.jsonPrimitive?.contentOrNull
}

/** 由 HTTP 状态码与响应体构造语义化异常,body 形如 {"errors":[…],"error_type":"…"} */
fun httpError(code: Int, body: String? = null): ApiException {
    var serverMessage: String? = null
    var errorType: String? = null
    if (!body.isNullOrBlank()) {
        runCatching {
            val obj = errorJson.parseToJsonElement(body).jsonObject
            errorType = obj["error_type"]?.jsonPrimitive?.contentOrNull
            serverMessage = obj["errors"]?.jsonArray?.firstOrNull()?.let { errorElementMessage(it) }
                ?: obj["error"]?.jsonPrimitive?.contentOrNull
                ?: obj["message"]?.jsonPrimitive?.contentOrNull
        }
    }
    return ApiException(
        code,
        errorType,
        when (code) {
            401 -> "请先登录后再操作"
            403 -> serverMessage ?: "您没有访问该资源的能力"
            404 -> serverMessage ?: "找不到请求的 URL 或资源。"
            429 -> "请求过于频繁，请稍后再试"
            in 500..599 -> "服务暂时不可用($code)"
            else -> serverMessage ?: "网络错误(HTTP $code)"
        },
    )
}
