package com.fuyouwentian.planktonmall.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.domain.repository.IProductRepository
import com.fuyouwentian.planktonmall.mock.MockProducts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val productRepository: IProductRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(loading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        initHandler()
    }

    /*private fun initHandler() {
        _uiState.value = HomeUiState(
            products = MockProducts.allProducts,
            loading = false
        )
    }*/

    private fun initHandler() {
        viewModelScope.launch {
            val products = try {
                productRepository.getProducts()
            } catch (e: Exception) {
                e.printStackTrace() // 打印堆栈信息
                Log.e("HomeViewModel", "请求失败: ${e.message}", e) // 打印日志
                emptyList()
            }
            _uiState.value = HomeUiState(
                products = products,
                loading = false
            )
        }
    }
}