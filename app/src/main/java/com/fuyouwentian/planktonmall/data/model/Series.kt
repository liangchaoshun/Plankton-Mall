package com.fuyouwentian.planktonmall.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Series(
    @SerialName("_id")
    val id: Long
)
