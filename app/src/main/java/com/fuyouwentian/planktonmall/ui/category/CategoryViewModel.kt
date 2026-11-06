package com.fuyouwentian.planktonmall.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.core.error.ErrorHandler
import com.fuyouwentian.planktonmall.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepo: CategoryRepository,
    private val errorHandler: ErrorHandler,
) : ViewModel() {
    // UI 状态
    private val _uiState = MutableStateFlow(CategoryUiState(loading = true))
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    fun fetchData() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            try {
                val categoryData = categoryRepo.getCategories()
                categoryData?.let { _uiState.update { it.copy(categoryData = categoryData) } }
                _uiState.update { it.copy(loading = false) }
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