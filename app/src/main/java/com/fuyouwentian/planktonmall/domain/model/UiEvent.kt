package com.fuyouwentian.planktonmall.domain.model

sealed class UiEvent {
    // 为何 ShowToast 不使用 object >> https://chat.deepseek.com/share/0e4k6fra0acquk242x
    data class ShowToast(val message: String) : UiEvent()
}
