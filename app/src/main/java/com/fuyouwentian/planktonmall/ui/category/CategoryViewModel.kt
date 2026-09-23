package com.fuyouwentian.planktonmall.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.data.model.UiEvent
import com.fuyouwentian.planktonmall.data.remote.ApiException
import com.fuyouwentian.planktonmall.data.remote.HttpExceptionWrapper
import com.fuyouwentian.planktonmall.data.remote.NetworkException
import com.fuyouwentian.planktonmall.domain.repository.ICategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: ICategoryRepository
) : ViewModel() {
    // UI 状态
    private val _uiState = MutableStateFlow(CategoryUiState(loading = true))
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    // UI 事件（用于 Toast、导航等一次性动作）
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    init {
        initHandler()
    }

    private fun initHandler() {
        viewModelScope.launch {
            _uiState.value = CategoryUiState(loading = true)
            try {
                val categoryData = categoryRepository.getCategories()
                _uiState.value = CategoryUiState(categoryData = categoryData, loading = false)
            } catch (e: ApiException) {
                // 业务错误（status_code != 20000）
                _uiState.value = CategoryUiState(loading = false)
                _uiEvent.emit(UiEvent.ShowToast(e.message))
            } catch (e: HttpExceptionWrapper) {
                // HTTP 协议错误（404, 500 等）
                _uiState.value = CategoryUiState(loading = false)
                _uiEvent.emit(UiEvent.ShowToast(e.message))
            } catch (e: NetworkException) {
                // 网络错误（断网、超时）
                _uiState.value = CategoryUiState(loading = false)
                _uiEvent.emit(UiEvent.ShowToast(e.message))
            } catch (e: Exception) {
                // 其他未知错误
                _uiState.value = CategoryUiState(loading = false)
                _uiEvent.emit(UiEvent.ShowToast("未知错误: ${e.message ?: "请稍后重试"}"))
            }
        }
    }
}
