package com.fuyouwentian.planktonmall.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    @SerialName("_id")
    val id: String,
    val name_zh: String,
    val name_en: String,
    val desc: String,
    val no: Int,
    val series_data: List<Series>,
    val create_time: String,
    val update_time: String
)

@Serializable
data class CategoryData<T>(
    val data: List<T>,
    val total: Int
)