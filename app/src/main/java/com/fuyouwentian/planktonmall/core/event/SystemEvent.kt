package com.fuyouwentian.planktonmall.core.event

sealed class SystemEvent {
    data object ForceLogout : SystemEvent() // 强制登出
    data object NetworkUnavailable : SystemEvent() // 网络断开
    data class ShowToast(val message: String) : SystemEvent() // 全局轻提示
}
