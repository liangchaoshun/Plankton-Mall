package com.fuyouwentian.planktonmall.data.local.profile

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.fuyouwentian.planktonmall.domain.model.ReceiverInfo
import com.fuyouwentian.planktonmall.domain.model.ReceiverInfoConverter

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey
    val id: String,
    val account: String,
    val password: String? = "",
    val nickname: String? = "",
    val avatar: String? = "",
    val email: String? = "",
    val phone: String? = "",
    @TypeConverters(ReceiverInfoConverter::class)
    val receiverInfo: List<ReceiverInfo>? = emptyList(),
    val bio: String? = "",
    val birthday: String? = ""
)
