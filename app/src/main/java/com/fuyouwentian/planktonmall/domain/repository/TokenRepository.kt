package com.fuyouwentian.planktonmall.domain.repository;

import com.fuyouwentian.planktonmall.domain.model.UiEvent
import kotlinx.coroutines.flow.SharedFlow

interface TokenRepository {
    val uiEvent: SharedFlow<UiEvent>
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun hasToken(): Boolean
    suspend fun clearToken()
    suspend fun onUnauthorized()
}