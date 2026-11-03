package com.fuyouwentian.planktonmall.core.error

import com.fuyouwentian.planktonmall.core.event.GlobalEventBus
import com.fuyouwentian.planktonmall.core.event.SystemEvent
import com.fuyouwentian.planktonmall.data.remote.FriendlyException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException


@Singleton
class ErrorHandler @Inject constructor(
    private val eventBus: GlobalEventBus
) {
    suspend fun handle(e: Exception): String {
        if (e is CancellationException) throw e
        /**
         * 都一起了：
         * 业务错误（status_code != 20000）
         * HTTP 协议错误（404, 500 等）
         * 网络错误（断网、超时）
         */
        val message = when (e) {
            is FriendlyException -> e.friendlyMessage
            else -> e.message ?: "请稍后重试"
        }
        eventBus.postEvent(SystemEvent.ShowToast(message))
        return message // 返回给调用方决定是否更新 exceptionInfo
    }
}
