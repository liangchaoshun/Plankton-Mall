package com.fuyouwentian.planktonmall.data.repository

import com.fuyouwentian.planktonmall.data.remote.ApiService
import com.fuyouwentian.planktonmall.data.remote.safeApiCall
import com.fuyouwentian.planktonmall.domain.model.LoginRequest
import com.fuyouwentian.planktonmall.domain.model.LoginResponse
import com.fuyouwentian.planktonmall.domain.model.RegisterRequest
import com.fuyouwentian.planktonmall.domain.model.UserProfile
import com.fuyouwentian.planktonmall.domain.repository.TokenRepository
import com.fuyouwentian.planktonmall.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val tokenRepo: TokenRepository
) : UserRepository {
    override suspend fun login(param: LoginRequest): LoginResponse {
        return safeApiCall { api.userLogin(param) }
    }

    override suspend fun register(param: RegisterRequest) {
        api.userRegister(param)
    }

    override suspend fun logout() {
        api.userLogout()
    }

    override suspend fun getUserProfile(id: String): UserProfile {
        return safeApiCall { api.userGetProfile(id) }
    }

    override suspend fun updateUserProfile() {
        TODO("Not yet implemented")
    }
}