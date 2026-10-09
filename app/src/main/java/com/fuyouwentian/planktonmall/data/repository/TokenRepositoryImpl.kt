package com.fuyouwentian.planktonmall.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.fuyouwentian.planktonmall.domain.repository.TokenRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : TokenRepository {
    //
    override suspend fun saveToken(token: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getToken(): String? {
        TODO("Not yet implemented")
    }

    override suspend fun clearToken() {
        TODO("Not yet implemented")
    }

    override suspend fun isLoggedIn(): Boolean {
        TODO("Not yet implemented")
    }
}