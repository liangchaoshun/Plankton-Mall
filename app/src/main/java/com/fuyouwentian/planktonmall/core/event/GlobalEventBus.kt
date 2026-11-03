package com.fuyouwentian.planktonmall.core.event

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalEventBus @Inject constructor() {
    // replay = 0：不重放给新订阅者
    // extraBufferCapacity = 64 + DROP_OLDEST：订阅者短暂缺席时缓冲 64 条，超了丢最旧
    // 注意：emit 是挂起函数，没有订阅者时会在缓冲满后挂起，不会丢
    private val _events = MutableSharedFlow<SystemEvent>(
        replay = 0,
        extraBufferCapacity = 64, // 更大的缓冲
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events = _events.asSharedFlow()

    suspend fun postEvent(event: SystemEvent) {
        _events.emit(event)
    }
}