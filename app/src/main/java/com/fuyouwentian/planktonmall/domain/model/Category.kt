package com.fuyouwentian.planktonmall.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    @SerialName("_id")
    val id: String,
    @SerialName("name_zh")
    val nameZh: String,
    @SerialName("name_en")
    val nameEn: String,
    val desc: String,
    val no: Int,
    @SerialName("series_data")
    val seriesData: List<Series>,
    @SerialName("create_time")
    val createTime: String,
    @SerialName("update_time")
    val updateTime: String
)

@Serializable
data class CategoryData<T>(
    val data: List<T>,
    val total: Int
)