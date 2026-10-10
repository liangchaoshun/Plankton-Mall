package com.fuyouwentian.planktonmall.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.fuyouwentian.planktonmall.data.local.PrefsKeys
import com.fuyouwentian.planktonmall.domain.repository.TokenRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : TokenRepository {
    override suspend fun saveToken(token: String) {
        dataStore.edit { prefs ->
            prefs[PrefsKeys.TOKEN] = token
        }
    }

    override suspend fun getToken(): String? {
        return dataStore.data
            .catch { e ->
                // DataStore 读取时若发生 IO 异常（如文件损坏），回退到空数据，避免崩溃
                if (e is IOException) emit(emptyPreferences()) else throw e
            }
            .map { prefs -> prefs[PrefsKeys.TOKEN] }
            .first()
    }

    override suspend fun clearToken() {
        dataStore.edit { prefs ->
            prefs.remove(PrefsKeys.TOKEN)
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return !getToken().isNullOrBlank()
    }
}