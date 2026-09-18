package com.fuyouwentian.planktonmall.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    @SerialName("_id")
    val id: String,
    val name_zh: String,
    val name_en: String,
    val price: String,
    val home_banner: Boolean,
    val home_display: Boolean,
    val icon_url: String,
    val banner_video_url: String,
    val banner_model_url: String,
    val series_id: String,
    val category_id: String,
    val desc_url: List<String>,
    val banner_url: List<String>,
    val create_time: String,
    val update_time: String,
    val series_name_zh: String,
    val series_name_en: String,
    val category_name_zh: String,
    val category_name_en: String
)
