package com.fuyouwentian.planktonmall.core.auth

import com.fuyouwentian.planktonmall.core.event.GlobalEventBus
import com.fuyouwentian.planktonmall.core.event.SystemEvent
import com.fuyouwentian.planktonmall.domain.repository.TokenRepository
import com.fuyouwentian.planktonmall.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    private val userRepo: UserRepository,
    private val tokenRepo: TokenRepository,
    private val eventBus: GlobalEventBus
) {
    suspend fun getCurrentUserId(showToast: Boolean = true): String? {
        val hasToken = tokenRepo.hasToken()
        val userId = userRepo.getCurrentUserId()
        return if (hasToken && userId != null) {
            userId
        } else {
            if (showToast) {
                eventBus.postEvent(SystemEvent.ShowToast("请先登录"))
            }
            null
        }
    }
}