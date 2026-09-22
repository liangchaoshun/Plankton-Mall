package com.fuyouwentian.planktonmall.data.repository

import com.fuyouwentian.planktonmall.data.model.Category
import com.fuyouwentian.planktonmall.data.model.CategoryData
import com.fuyouwentian.planktonmall.data.remote.ApiService
import com.fuyouwentian.planktonmall.data.remote.safeApiCall
import com.fuyouwentian.planktonmall.domain.repository.ICategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val apiService: ApiService, // Hilt 会自动提供
) : ICategoryRepository {
    override suspend fun getCategories(): CategoryData<Category> {
        // 🌟 直接用 safeApiCall 包起来，它会返回 ProductsData，或者抛出异常
        return safeApiCall { apiService.getCategories() }
    }
}
