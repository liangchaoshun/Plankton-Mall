package com.fuyouwentian.planktonmall.data.repository

import com.fuyouwentian.planktonmall.data.local.profile.ProfileDao
import com.fuyouwentian.planktonmall.data.local.profile.toEntity
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
    private val profileDao: ProfileDao,
    private val tokenRepo: TokenRepository,
) : UserRepository {
    override suspend fun login(param: LoginRequest): LoginResponse {
        val response = safeApiCall { api.userLogin(param) }
        // 缓存到本地
        tokenRepo.saveToken(response.token)
        profileDao.insertUserProfile(response.userInfo.toEntity())
        return response
    }

    override suspend fun register(param: RegisterRequest) {
        api.userRegister(param)
    }

    override suspend fun logout() {
        api.userLogout()
    }

    override suspend fun getUserProfile(id: String): UserProfile {
        val result = safeApiCall { api.userGetProfile(id) }
        profileDao.insertUserProfile(result.toEntity()) // 缓存到本地
        return result
    }
}