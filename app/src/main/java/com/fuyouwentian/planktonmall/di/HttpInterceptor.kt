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
        /** 401 */
        if (response.code == 401) {
            Log.d("HttpInterceptor", "401 Unauthorized")
            scope.launch { tokenRepo.onUnauthorized() }
        }

        return response
    }
}