package com.fuyouwentian.planktonmall.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    val status_code: String,
    val message: String,
    val data: T? = null
)