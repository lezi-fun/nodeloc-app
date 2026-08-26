package app.nodeloc.data

/**
 * 带 HTTP 语义的业务异常。[message] 为可直接展示给用户的中文文案；
 * [code] 供界面区分 403（无权限）/404（不存在）等专用状态。
 */
class ApiException(val code: Int, message: String) : RuntimeException(message)

fun httpError(code: Int): ApiException = ApiException(
    code,
    when (code) {
        401 -> "请先登录后再操作"
        403 -> "无权访问该内容"
        404 -> "内容不存在或已被删除"
        429 -> "请求过于频繁，请稍后再试"
        in 500..599 -> "服务暂时不可用($code)"
        else -> "网络错误(HTTP $code)"
    },
)
