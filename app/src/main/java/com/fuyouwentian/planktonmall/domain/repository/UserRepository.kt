package com.fuyouwentian.planktonmall.domain.repository

import com.fuyouwentian.planktonmall.domain.model.LoginRequest
import com.fuyouwentian.planktonmall.domain.model.LoginResponse
import com.fuyouwentian.planktonmall.domain.model.RegisterRequest
import com.fuyouwentian.planktonmall.domain.model.UserProfile

interface UserRepository {
    suspend fun login(param: LoginRequest): LoginResponse?
    suspend fun register(param: RegisterRequest): Boolean?
    suspend fun logout(): Boolean?
    suspend fun getLocalUserProfile(): UserProfile?
    suspend fun getRemoteUserProfile(): UserProfile?
    suspend fun isLoggedIn(): Boolean?
}