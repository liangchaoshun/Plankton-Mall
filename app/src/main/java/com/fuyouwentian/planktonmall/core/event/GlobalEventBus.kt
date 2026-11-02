package com.fuyouwentian.planktonmall.core.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalEventBus @Inject constructor() {
    // 缓冲和重放容量为 1
    private val _events = MutableSharedFlow<SystemEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun postEvent(event: SystemEvent) {
        // 使用 tryEmit 因为 SharedFlow 默认没有缓冲，防止阻塞
        _events.tryEmit(event) // 不阻塞，发完即走
    }
}