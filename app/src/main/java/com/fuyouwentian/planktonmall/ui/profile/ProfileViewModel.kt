package com.fuyouwentian.planktonmall.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.data.local.AppDatabase
import com.fuyouwentian.planktonmall.data.remote.FriendlyException
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import com.fuyouwentian.planktonmall.domain.repository.CartRepository
import com.fuyouwentian.planktonmall.domain.repository.HistoryRepository
import com.fuyouwentian.planktonmall.domain.repository.TokenRepository
import com.fuyouwentian.planktonmall.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val tokenRepo: TokenRepository,
    private val appDatabase: AppDatabase,
    private val cartRepo: CartRepository,
    private val historyRepo: HistoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState(loading = false))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    fun loadOrders() {
        Log.d("ProfileViewModel", "loadOrders")
        viewModelScope.launch {
            try {
                // TODO
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun loadBrowsingHistory() {
        Log.d("ProfileViewModel", "loadBrowsingHistory")
        viewModelScope.launch {
            try {
                val recordsMap = historyRepo.getAllItems()
                    .groupBy { it.createDate }
                    .mapValues { (_, records) ->
                        // 列表中，按 createTimeMillis 字段倒排
                        records.sortedByDescending { it.createTimeMillis }
                    }
                    .toSortedMap(compareByDescending { it })
                _uiState.update { it.copy(histories = recordsMap) }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun loadFavorites() {
        Log.d("ProfileViewModel", "loadFavorites")
        viewModelScope.launch {
            try {
                // TODO
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun initScreen() {
        _uiState.update {
            it.copy(
                orders = emptyList(),
                histories = sortedMapOf(),
                favors = emptyList(),
                profile = null,
                hasToken = true,
                loading = false,
                error = null
            )
        }
        loadUserInfo()
    }

    private fun loadUserInfo() {
        Log.d("ProfileViewModel", "loadUserInfo")
        viewModelScope.launch {
            try {
                val hasToken = tokenRepo.hasToken()
                _uiState.update { it.copy(hasToken = hasToken) }
                if (!hasToken) return@launch // 跳出
                userRepo.getLocalUserProfile()?.let { localProfile ->
                    Log.d("ProfileViewModel", "localProfile: $localProfile")
                    _uiState.update { it.copy(profile = localProfile) }
                }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    private suspend fun handleError(e: Exception) {
        if (e is CancellationException) throw e // 协程取消时抛出
        val msg = when (e) {
            /**
             * 都一起了：
             * 业务错误（status_code != 20000）
             * HTTP 协议错误（404, 500 等）
             * 网络错误（断网、超时）
             */
            is FriendlyException -> e.friendlyMessage
            else -> e.message ?: "请稍后重试"
        }
        Log.d("HandleError", msg)
        _uiState.update { it.copy(error = msg) }
        _uiEvent.emit(UiEvent.ShowToast(msg))
    }
}
