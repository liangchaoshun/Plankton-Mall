package com.fuyouwentian.planktonmall.ui.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.data.local.SyncRecordState
import com.fuyouwentian.planktonmall.data.remote.FriendlyException
import com.fuyouwentian.planktonmall.domain.model.Add2CartProductParams
import com.fuyouwentian.planktonmall.domain.model.FavorRecord
import com.fuyouwentian.planktonmall.domain.model.HistoryRecord
import com.fuyouwentian.planktonmall.domain.model.Product
import com.fuyouwentian.planktonmall.domain.model.ProductDetailRequest
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import com.fuyouwentian.planktonmall.domain.repository.CartRepository
import com.fuyouwentian.planktonmall.domain.repository.FavorRepository
import com.fuyouwentian.planktonmall.domain.repository.HistoryRepository
import com.fuyouwentian.planktonmall.domain.repository.ProductRepository
import com.fuyouwentian.planktonmall.domain.repository.TokenRepository
import com.fuyouwentian.planktonmall.domain.repository.UserRepository
import com.fuyouwentian.planktonmall.util.MongoIdGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val cartRepo: CartRepository,
    private val tokenRepo: TokenRepository,
    private val productRepo: ProductRepository,
    private val historyRepo: HistoryRepository,
    private val favorRepo: FavorRepository,
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
            _uiState.update { it.copy(product = null, loading = true, error = null) }
            try {
                // mock data
                // val product = null
                // val product = MockProducts.ProductDetail
                val localUserProfile = userRepo.getLocalUserProfile()
                val userId = localUserProfile?.id ?: ""
                val product = productRepo.getProductDetail(
                    ProductDetailRequest(
                        userId = userId,
                        goodsId = pid
                    )
                )
                if (id != requestId) return@launch // 已过期
                product?.let { nonNullProduct ->
                    _uiState.update { it.copy(product = nonNullProduct, error = null) }
                    // 登录后存在用户信息时，才加入历史数据表
                    if (tokenRepo.hasToken() && localUserProfile != null) {
                        // 未被物理删除的商品都可加入浏览历史（即使逻辑删除、下架、售罄）
                        val today = LocalDate.now()
                        val historyRecord = HistoryRecord(
                            userId = localUserProfile.id,
                            goodsId = nonNullProduct.id,
                            isValid = nonNullProduct.deleted == 0,
                            goodsNameZh = nonNullProduct.nameZh,
                            goodsNameEn = nonNullProduct.nameEn,
                            goodsPrice = nonNullProduct.price,
                            goodsIcon = nonNullProduct.iconUrl,
                            goodsStock = nonNullProduct.stock,
                            goodsOnSale = nonNullProduct.onSale,
                            // 固定格式 "yyyy-MM-dd"，月和日必须是两位数，否则按日期排序不正确
                            createDate = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                            createTimeMillis = System.currentTimeMillis()
                        )
                        historyRepo.insert(historyRecord)
                    }
                }
            } catch (e: Exception) {
                if (id == requestId) handleError(e)
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    fun add2Cart(product: Product) {
        viewModelScope.launch {
            try {
                if (!tokenRepo.hasToken()) {
                    _uiEvent.emit(UiEvent.ShowToast("请先登录"))
                    return@launch
                }
                val bool = cartRepo.insert(
                    Add2CartProductParams(
                        id = product.id,
                        price = product.price,
                        stock = product.stock,
                        onSale = product.onSale,
                        nameZh = product.nameZh,
                        nameEn = product.nameEn,
                        iconUrl = product.iconUrl
                    )
                )
                if (bool) _uiEvent.emit(UiEvent.ShowToast("加入购物车成功"))
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    // 添加、取消收藏
    fun toggleFavorites() {
        Log.d("Detail", "toggleFavorites")
        viewModelScope.launch {
            try {
                val userLocalProfile = userRepo.getLocalUserProfile()
                if (!tokenRepo.hasToken() || userLocalProfile == null) {
                    _uiEvent.emit(UiEvent.ShowToast("请先登录"))
                    return@launch
                }
                _uiState.value.product?.let { product ->
                    val productId = product.id
                    if (favorRepo.getItemCountByGoodsId(productId) > 0) {
                        // 已存在，则取消收藏
                        if (favorRepo.removeFromFavorites(productId)) {
                            _uiEvent.emit(UiEvent.ShowToast("取消收藏成功"))
                            _uiState.update { it.copy(product = it.product?.copy(isFavorite = false)) }
                        }
                    } else {
                        // 不存在，则加入收藏
                        val req = FavorRecord(
                            id = MongoIdGenerator.generate(),
                            userId = userLocalProfile.id,
                            goodsId = product.id,
                            isValid = product.deleted == 0,
                            goodsNameZh = product.nameZh,
                            goodsNameEn = product.nameEn,
                            goodsIcon = product.iconUrl,
                            goodsPrice = product.price,
                            goodsStock = product.stock,
                            goodsOnSale = product.onSale,
                            syncState = SyncRecordState.PENDING_INSERT
                        )
                        if (favorRepo.add2Favorites(request = req)) {
                            _uiEvent.emit(UiEvent.ShowToast("加入收藏成功"))
                            _uiState.update { it.copy(product = it.product?.copy(isFavorite = true)) }
                        }
                    }
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun immediatelyMadeAnOrder(productId: String) {
        Log.d("Detail", "immediately make an order: $productId")
        _uiState.update { it.copy(isMadeOrder = false) }
        viewModelScope.launch {
            try {
                if (!tokenRepo.hasToken()) {
                    _uiEvent.emit(UiEvent.ShowToast("请先登录"))
                    return@launch
                }
                // TODO 创建订单
                delay(5.seconds)
                _uiState.update { it.copy(isMadeOrder = true) } // 跳转到下单页
            } catch (e: Exception) {
                handleError(e)
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
            else -> e.message ?: "请稍后重试"
        }
        Log.d("HandleError", msg)
        _uiState.update { it.copy(error = msg) }
        _uiEvent.emit(UiEvent.ShowToast(msg))
    }
}
