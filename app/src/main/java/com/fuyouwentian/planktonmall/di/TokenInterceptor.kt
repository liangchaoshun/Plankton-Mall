package com.fuyouwentian.planktonmall.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.fuyouwentian.planktonmall.data.local.PrefsKeys   // 假设你定义在这
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenInterceptor @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            dataStore.data.first()[PrefsKeys.TOKEN] // TODO 这是啥？
        }
        val request = chain
            .request()
            .newBuilder()
            .apply {
                token?.takeIf { it.isNotBlank() }?.let {
                    addHeader("Authorization", "Bearer $it")
                }
            }
            .build()
        return chain.proceed(request)
    }
}