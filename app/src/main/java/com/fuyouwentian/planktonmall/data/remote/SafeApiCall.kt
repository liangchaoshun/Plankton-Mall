package com.fuyouwentian.planktonmall.data.remote

import retrofit2.HttpException
import java.io.IOException


// 1. 业务异常（HTTP 200，但 status_code != 20000）
class ApiException(val code: String, override val message: String) : Exception(message)
// 2. HTTP 协议异常（404, 500 等）
class HttpExceptionWrapper(val code: Int, override val message: String) : Exception(message)
// 3. 网络连接异常（断网、超时）
class NetworkException(override val message: String) : Exception(message)

// 统一的 API 调用包装器
suspend fun <T> safeApiCall(call: suspend () -> BaseResponse<T>): T {
    return try {
        val response = call() // 这里可能抛出 HttpException 或 IOException

        // 走到这里说明 HTTP 状态码是 200-299
        if (response.status_code == "20000") {
            response.data // 成功，返回真实数据
        } else {
            // HTTP 成功，但业务失败
            throw ApiException(response.status_code, response.message)
        }
    } catch (e: HttpException) {
        // 捕获 401, 404, 500 等 HTTP 协议错误
        val errorMsg = when (e.code()) {
            401 -> "登录已过期，请重新登录"
            404 -> "请求的资源不存在"
            500 -> "服务器开小差了，请稍后再试"
            else -> "网络请求错误: ${e.code()}"
        }
        throw HttpExceptionWrapper(e.code(), errorMsg)
    } catch (e: IOException) {
        // 捕获断网、超时等底层 IO 错误
        throw NetworkException("网络连接失败，请检查网络设置")
    } catch (e: Exception) {
        // 捕获解析错误等其他异常
        throw Exception("数据解析异常: ${e.message}")
    }
}