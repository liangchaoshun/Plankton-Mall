package com.fuyouwentian.planktonmall.ui.components.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.core.auth.AuthManager
import com.fuyouwentian.planktonmall.core.error.ErrorHandler
import com.fuyouwentian.planktonmall.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryContentViewModel @Inject constructor(
    private val historyRepo: HistoryRepository,
    private val errorHandler: ErrorHandler,
    private val authManager: AuthManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryContentUiState(loading = false))
    val uiState: StateFlow<HistoryContentUiState> = _uiState.asStateFlow()

    fun initContent() {
        _uiState.update {
            it.copy(
                loading = true,
                histories = sortedMapOf(),
                error = null
            )
        }
        loadBrowsingHistory()
    }

    fun loadBrowsingHistory() {
        viewModelScope.launch {
            try {
                val userId = authManager.getCurrentUserId() ?: return@launch
                val recordsMap = historyRepo.getItems(userId)
                    .groupBy { it.createDate }
                    .mapValues { (_, records) ->
                        // 列表中，按 createTimeMillis 字段倒序
                        records.sortedByDescending { it.createTimeMillis }
                    }
                    // 转换成有序（按 key 倒序） map
                    .toSortedMap(compareByDescending { it })
                _uiState.update { it.copy(histories = recordsMap) }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    private suspend fun handleError(e: Exception) {
        val message = errorHandler.handle(e)
        _uiState.update { it.copy(error = message) }
    }
}
