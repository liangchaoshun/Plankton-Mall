package com.fuyouwentian.planktonmall.di

import com.fuyouwentian.planktonmall.data.repository.ProductRepositoryImpl
import com.fuyouwentian.planktonmall.domain.repository.IProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 编译期：
 *   Hilt 扫描所有 @Module，收集"如何创建依赖"的信息
 *   RepositoryModule 告诉 Hilt：
 *     "有人要 IProductRepository 时，给他 ProductRepositoryImpl"
 *         ↓
 * 运行期：
 *   ViewModel 声明需要 IProductRepository
 *         ↓
 *   Hilt 自动：
 *     1. 查 RepositoryModule → 找到用 ProductRepositoryImpl
 *     2. 查 ProductRepositoryImpl 的构造函数 → 需要 ApiService + ProductDao
 *     3. 查 NetworkModule → 提供 ApiService
 *     4. 查 DatabaseModule → 提供 ProductDao // 没用到
 *     5. 构造 ProductRepositoryImpl
 *     6. 注入到 ViewModel
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    // @Singleton // ProductRepositoryImpl 加了 @Singleton 注解，此处无需再添加
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl
    ): IProductRepository
}