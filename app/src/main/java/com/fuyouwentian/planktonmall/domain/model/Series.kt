package com.fuyouwentian.planktonmall.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Series(
    @SerialName("_id")
    val id: String,
    val name_zh: String,
    val name_en: String,
    val desc: String,
    val no: Int,
    val icon_url: String,
    val category_id: String,
    val create_time: String,
    val update_time: String
)
