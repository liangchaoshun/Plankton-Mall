package com.fuyouwentian.planktonmall.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Kotlin 序列化，默认会忽略掉数据类中没有的 JSON 字段
@Serializable
data class LoginRequest(
    val account: String,
    val password: String,
)
@Serializable
data class LoginResponse(
    @SerialName("user_info")
    val userInfo: UserProfile,
    val token: String,
)

@Serializable
data class RegisterRequest(
    val account: String,
    val password: String,
    val nickname: String,
    val avatar: String? = null
)

@Serializable
data class UserProfile(
    @SerialName("_id")
    val id: String,
    val account: String,
    val password: String,
    val nickname: String? = null,
    val avatar: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val bio: String? = null,
    val birthday: String? = null
)
