package com.fuyouwentian.planktonmall.di

import android.util.Log
import com.fuyouwentian.planktonmall.domain.repository.TokenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HttpInterceptor @Inject constructor(
    private val tokenRepo: TokenRepository,
    @ApplicationScope private val scope: CoroutineScope, // 自定义 Hilt Qualifier: QualifierModule
) : Interceptor {
    // 拦截器：加 token、处理 401（比如跳登录）
    override fun intercept(chain: Interceptor.Chain): Response {
        /** 头部带上 token */
        // runBlocking，必须先拿到 token 才发请求
        val token = runBlocking { tokenRepo.getToken() }
        val request = chain
            .request()
            .newBuilder()
            .apply {
                token?.takeIf { it.isNotBlank() }?.let {
                    addHeader("Authorization", "Bearer $it")
                }
            }
            .build()

        val response = chain.proceed(request)
        /**
         * 拦截 401
         * 只跳转，不提示
         * 提示文案，safeApiCall 中将异常传到 *ViewModel 再到 Compose UI 弹出提示
         */
        if (response.code == 401) {
            scope.launch { tokenRepo.onUnauthorized() }
        }
        // 只有 200-299 且有 body 才尝试解析
        if (response.code in 200..299) {
            // peekBody 不会消耗原流
            val bodyString = response.peekBody(100).string() // 100 字节数足够了
            // Log.d("HttpInterceptor", "Response Body: $bodyString")
            if (bodyString.contains("\"status_code\":\"40001\"")) {
                scope.launch { tokenRepo.onUnauthorized() }
            }
        }

        return response
    }
}