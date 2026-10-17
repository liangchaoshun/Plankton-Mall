package com.fuyouwentian.planktonmall.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Series(
    @SerialName("_id")
    val id: String,
    @SerialName("name_zh")
    val nameZh: String,
    @SerialName("name_en")
    val nameEn: String,
    val desc: String,
    val no: Int,
    @SerialName("icon_url")
    val iconUrl: String,
    @SerialName("category_id")
    val categoryId: String,
    @SerialName("create_time")
    val createTime: String,
    @SerialName("update_time")
    val updateTime: String
)
