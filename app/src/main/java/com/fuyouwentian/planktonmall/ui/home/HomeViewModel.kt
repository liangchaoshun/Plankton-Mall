package com.fuyouwentian.planktonmall.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.data.model.HomeProductsRequest
import com.fuyouwentian.planktonmall.data.model.UiEvent
import com.fuyouwentian.planktonmall.data.remote.ApiException
import com.fuyouwentian.planktonmall.data.remote.HttpExceptionWrapper
import com.fuyouwentian.planktonmall.data.remote.NetworkException
import com.fuyouwentian.planktonmall.domain.repository.IProductRepository
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
class HomeViewModel @Inject constructor(
    private val productRepository: IProductRepository
) : ViewModel() {
    // UI 状态
    private val _uiState = MutableStateFlow(HomeUiState(loading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // UI 事件（用于 Toast、导航等一次性动作）
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    init {
        initHandler()
    }

    /*fun initHandler() {
        _uiState.value = HomeUiState(
            products = MockProducts.allProducts,
            loading = false
        )
    }*/

    fun initHandler() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(loading = true)
            try {
                val productsData = productRepository.getHomeProducts(HomeProductsRequest())
                _uiState.value = HomeUiState(productsData = productsData, loading = false)
            } catch (e: ApiException) {
                // 业务错误（status_code != 20000）
                _uiState.value = HomeUiState(loading = false)
                _uiEvent.emit(UiEvent.ShowToast(e.message))
            } catch (e: HttpExceptionWrapper) {
                // HTTP 协议错误（404, 500 等）
                _uiState.value = HomeUiState(loading = false)
                _uiEvent.emit(UiEvent.ShowToast(e.message))
            } catch (e: NetworkException) {
                // 网络错误（断网、超时）
                _uiState.value = HomeUiState(loading = false)
                _uiEvent.emit(UiEvent.ShowToast(e.message))
            } catch (e: Exception) {
                // 其他未知错误
                _uiState.value = HomeUiState(loading = false)
                _uiEvent.emit(UiEvent.ShowToast("未知错误: ${e.message ?: "请稍后重试"}"))
            }
        }
    }
}
