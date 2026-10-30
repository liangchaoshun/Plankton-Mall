package com.fuyouwentian.planktonmall.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.fuyouwentian.planktonmall.data.local.PrefsKeys
import com.fuyouwentian.planktonmall.data.local.profile.ProfileDao
import com.fuyouwentian.planktonmall.data.local.profile.toDomain
import com.fuyouwentian.planktonmall.data.local.profile.toEntity
import com.fuyouwentian.planktonmall.data.remote.ApiService
import com.fuyouwentian.planktonmall.data.remote.safeApiCall
import com.fuyouwentian.planktonmall.domain.model.LoginRequest
import com.fuyouwentian.planktonmall.domain.model.LoginResponse
import com.fuyouwentian.planktonmall.domain.model.RegisterRequest
import com.fuyouwentian.planktonmall.domain.model.UserProfile
import com.fuyouwentian.planktonmall.domain.repository.TokenRepository
import com.fuyouwentian.planktonmall.domain.repository.UserRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val profileDao: ProfileDao,
    private val tokenRepo: TokenRepository,
    private val dataStore: DataStore<Preferences>
) : UserRepository {
    override suspend fun login(param: LoginRequest): LoginResponse? {
        val response = safeApiCall { api.userLogin(param) }
        response?.let {
            // 缓存到本地
            tokenRepo.saveToken(response.token)
            // 保存当前登录的账号的 userId
            dataStore.edit { prefs ->
                prefs[PrefsKeys.USER_ID] = response.userInfo.id
            }
            profileDao.insertUserProfile(response.userInfo.toEntity())
        }
        return response
    }

    override suspend fun register(param: RegisterRequest): Boolean? {
        return safeApiCall { api.userRegister(param) }
    }

    override suspend fun logout(): Boolean? {
        return safeApiCall { api.userLogout() }
    }

    override suspend fun getCurrentUserId(): String? {
        return dataStore.data
            .catch { e ->
                // DataStore 读取时若发生 IO 异常（如文件损坏），则 emit 一个空 Preferences 给下游，避免崩溃
                if (e is IOException) emit(emptyPreferences()) else throw e
            }
            .map { prefs -> prefs[PrefsKeys.USER_ID] }
            .first()
    }

    override suspend fun getLocalUserProfile(): UserProfile? {
        return profileDao.getUserProfile()?.toDomain()
    }

    override suspend fun getRemoteUserProfile(): UserProfile? {
        val result = safeApiCall { api.userGetProfile() }
        result?.let {
            profileDao.insertUserProfile(result.toEntity()) // 缓存到本地
        }
        return result
    }

    override suspend fun isLoggedIn(): Boolean? {
        if (!tokenRepo.hasToken()) return false
        return safeApiCall { api.userValidate() }
    }

}