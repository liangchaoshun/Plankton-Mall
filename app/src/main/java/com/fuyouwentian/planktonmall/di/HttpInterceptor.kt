package com.fuyouwentian.planktonmall.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.fuyouwentian.planktonmall.core.event.GlobalEventBus
import com.fuyouwentian.planktonmall.core.event.SystemEvent
import com.fuyouwentian.planktonmall.data.local.PrefsKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HttpInterceptor @Inject constructor(
    private val eventBus: GlobalEventBus,
    private val dataStore: DataStore<Preferences>,
    @ApplicationScope private val scope: CoroutineScope, // 自定义 Hilt Qualifier: QualifierModule
) : Interceptor {
    private fun handleUnauthorized() {
        scope.launch {
            val token = dataStore.data
                .catch { e ->
                    if (e is IOException) emit(emptyPreferences()) else throw e
                }
                .first()[PrefsKeys.TOKEN]
            if (token != null) {
                dataStore.edit {
                    it.remove(PrefsKeys.TOKEN)
                    it.remove(PrefsKeys.USER_ID)
                }
                // 跳转 UI，广播一个全局事件
                eventBus.postEvent(SystemEvent.ForceLogout)
            }
        }
    }

    // 拦截器：加 token、处理 401（比如跳登录）
    override fun intercept(chain: Interceptor.Chain): Response {
        /** 头部带上 token userId */
        // runBlocking，必须先拿到 token 和 userId 才发请求
        val (userId, token) = runBlocking {
            try {
                val prefs = dataStore.data.catch { e ->
                    if (e is IOException) emit(emptyPreferences()) else throw e
                }.first()
                prefs[PrefsKeys.USER_ID] to prefs[PrefsKeys.TOKEN]
            } catch (e: Exception) {
                null to null
            }
        }

        val request = chain
            .request()
            .newBuilder()
            .apply {
                token?.takeIf { it.isNotBlank() }?.let {
                    addHeader("Authorization", "Bearer $it")
                }
                userId?.takeIf { it.isNotBlank() }?.let {
                    addHeader("user-id", userId)
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
            handleUnauthorized()
        }
        // 只有 200-299 且有 body 才尝试解析
        if (response.code in 200..299) {
            // peekBody 不会消耗原流
            val bodyString = response.peekBody(100).string() // 100 字节数足够了
            // Log.d("HttpInterceptor", "Response Body: $bodyString")
            if (bodyString.contains("\"status_code\":\"40001\"")) {
                handleUnauthorized()
            }
        }

        return response
    }
}