package com.fuyouwentian.planktonmall.data.local.profile

import com.fuyouwentian.planktonmall.domain.model.UserProfile

/**
 * Mapper 映射函数
 */

// Entity -> Domain
fun ProfileEntity.toDomain(): UserProfile = UserProfile(
    id = id,
    account = account,
    nickname = nickname,
    password = "", // 本地不存密码，返回空字符串
    avatar = avatar,
    email = email,
    phone = phone,
    address = address,
    bio = bio,
    birthday = birthday
)

// Domain -> Entity
fun UserProfile.toEntity(): ProfileEntity = ProfileEntity(
    id = id,
    account = account,
    nickname = nickname,
    avatar = avatar,
    email = email,
    phone = phone,
    address = address,
    bio = bio,
    birthday = birthday
)
