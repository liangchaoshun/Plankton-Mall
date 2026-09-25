package com.fuyouwentian.planktonmall.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    val status_code: String,
    val data: T, // 🔥⭐ 跟后端约定，正常数据返回为 null 时，给默认值
    val message: String
)