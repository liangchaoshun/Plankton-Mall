package com.fuyouwentian.planktonmall.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    val status_code: String,
    val data: T,
    val message: String
)