package com.fuyouwentian.planktonmall.ui.detail


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.data.remote.FriendlyException
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import com.fuyouwentian.planktonmall.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DetailViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {
    // UI 状态
    private val _uiState = MutableStateFlow(DetailUiState(loading = true))
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // UI 事件（用于 Toast、导航等一次性动作）
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    // 防止竞态：间隔太短“下拉刷新”
    private var requestId = 0

    // 保存最后一次请求参数，供 loadMore 使用
    private var lastRequest: String = ""


    // 初始化加载/刷新
    fun fetchProductDetail(pid: String) {
        lastRequest = pid
        val id = ++requestId
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            try {
                // mock data
                //val product = null
                //val product = MockProducts.ProductDetail
                val product = productRepository.getProduct(pid)
                if (id != requestId) return@launch // 已过期
                _uiState.update { it.copy(product = product, loading = false) }
            } catch (e: Exception) {
                if (id == requestId) handleError(e)
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    private suspend fun handleError(e: Exception) {
        if (e is CancellationException) throw e // 协程取消时会抛出
        val msg = when (e) {
            /**
             * 都一起了：
             * 业务错误（status_code != 20000）
             * HTTP 协议错误（404, 500 等）
             * 网络错误（断网、超时）
             */
            is FriendlyException -> e.friendlyMessage
            else -> "未知错误：${e.message ?: "请稍后重试"}"
        }
        _uiEvent.emit(UiEvent.ShowToast(msg))
    }
}
