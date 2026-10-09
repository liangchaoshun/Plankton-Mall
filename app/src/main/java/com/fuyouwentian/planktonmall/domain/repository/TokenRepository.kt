package com.fuyouwentian.planktonmall.domain.repository;

interface TokenRepository {
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun clearToken()
    suspend fun isLoggedIn(): Boolean
}
