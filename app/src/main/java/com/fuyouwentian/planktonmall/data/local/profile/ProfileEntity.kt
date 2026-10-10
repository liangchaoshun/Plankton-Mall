package com.fuyouwentian.planktonmall.data.local.profile

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey
    val id: String,
    val account: String,
    val nickname: String? = null,
    val avatar: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val bio: String? = null,
    val birthday: String? = null
)