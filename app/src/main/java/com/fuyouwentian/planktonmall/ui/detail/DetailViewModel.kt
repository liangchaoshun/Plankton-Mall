package com.fuyouwentian.planktonmall.ui.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.core.auth.AuthManager
import com.fuyouwentian.planktonmall.core.error.ErrorHandler
import com.fuyouwentian.planktonmall.core.event.GlobalEventBus
import com.fuyouwentian.planktonmall.core.event.SystemEvent
import com.fuyouwentian.planktonmall.data.local.SyncRecordState
import com.fuyouwentian.planktonmall.domain.model.Add2CartProductParams
import com.fuyouwentian.planktonmall.domain.model.FavorRecord
import com.fuyouwentian.planktonmall.domain.model.HistoryRecord
import com.fuyouwentian.planktonmall.domain.model.OrderRecord
import com.fuyouwentian.planktonmall.domain.model.Product
import com.fuyouwentian.planktonmall.domain.repository.CartRepository
import com.fuyouwentian.planktonmall.domain.repository.FavorRepository
import com.fuyouwentian.planktonmall.domain.repository.HistoryRepository
import com.fuyouwentian.planktonmall.domain.repository.OrderRepository
import com.fuyouwentian.planktonmall.domain.repository.ProductRepository
import com.fuyouwentian.planktonmall.domain.repository.TokenRepository
import com.fuyouwentian.planktonmall.domain.repository.UserRepository
import com.fuyouwentian.planktonmall.util.MongoIdGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val cartRepo: CartRepository,
    private val tokenRepo: TokenRepository,
    private val productRepo: ProductRepository,
    private val historyRepo: HistoryRepository,
    private val favorRepo: FavorRepository,
    private val orderRepo: OrderRepository,
    private val eventBus: GlobalEventBus,
    private val errorHandler: ErrorHandler,
    private val authManager: AuthManager
) : ViewModel() {
    // UI 状态
    private val _uiState = MutableStateFlow(
        DetailUiState(
            loading = true,
            committingOrder = false,
            isMakeOrderDone = false
        )
    )
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

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
                val product = productRepo.getProductDetail(pid)
                if (id != requestId) return@launch // 已过期
                product?.let { nonNullProduct ->
                    _uiState.update { it.copy(product = nonNullProduct, error = null) }
                    // 登录后存在用户信息时，才加入历史数据表
                    val userId = userRepo.getCurrentUserId()
                    if (tokenRepo.hasToken() && userId != null) {
                        // 未被物理删除的商品都可加入浏览历史（即使逻辑删除、下架、售罄）
                        val today = LocalDate.now()
                        val historyRecord = HistoryRecord(
                            userId = userId,
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
                        historyRepo.upsert(historyRecord)
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
                val userId = authManager.getCurrentUserId() ?: return@launch
                val bool = cartRepo.upsert(
                    userId = userId,
                    params = Add2CartProductParams(
                        id = product.id,
                        price = product.price,
                        stock = product.stock,
                        onSale = product.onSale,
                        nameZh = product.nameZh,
                        nameEn = product.nameEn,
                        iconUrl = product.iconUrl
                    )
                )
                if (bool) eventBus.postEvent(SystemEvent.ShowToast("加入购物车成功"))
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
                val userId = authManager.getCurrentUserId() ?: return@launch
                _uiState.value.product?.let { product ->
                    val productId = product.id
                    if (favorRepo.isExist(userId = userId, goodsId = productId)) {
                        // 已存在，则取消收藏
                        if (favorRepo.deleteRecordByIndices(userId = userId, goodsId = productId)) {
                            eventBus.postEvent(SystemEvent.ShowToast("取消收藏成功"))
                            _uiState.update { it.copy(product = it.product?.copy(isFavorite = false)) }
                        }
                    } else {
                        // 不存在，则加入收藏
                        val req = FavorRecord(
                            id = MongoIdGenerator.generate(),
                            userId = userId,
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
                            eventBus.postEvent(SystemEvent.ShowToast("加入收藏成功"))
                            _uiState.update { it.copy(product = it.product?.copy(isFavorite = true)) }
                        }
                    }
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun immediatelyMakeAnOrder() {
        _uiState.value.product?.let { product ->
            _uiState.update { it.copy(committingOrder = true, isMakeOrderDone = false) }
            viewModelScope.launch {
                try {
                    val userId = authManager.getCurrentUserId()
                    if (userId == null) {
                        _uiState.update { it.copy(committingOrder = false) }
                        return@launch
                    }
                    val receiverInfo = userRepo.getCurrentReceiverInfo()
                    val params = OrderRecord(
                        id = MongoIdGenerator.generate(),
                        userId = userId,
                        goodsId = product.id,
                        goodsNameZh = product.nameZh,
                        goodsNameEn = product.nameEn,
                        goodsIcon = product.iconUrl,
                        goodsPrice = product.price,
                        quantity = 1,
                        actualPay = product.price,
                        receiverName = receiverInfo.name,
                        receiverPhone = receiverInfo.phone,
                        receiverAddress = receiverInfo.address,
                        orderStatus = 1, // 1：待付款，2：待发货，3：待收货，4：已完成，5：已取消，6：已关闭
                        paymentStatus = 0, // 0：未支付，1：已支付，2：部分退款，3：全额退款
                        afterSalesStatus = 0, // 0：无售后，1：退款/退货中，2：售后完成
                        paymentTime = "",
                        deleted = 0,
                        syncState = SyncRecordState.PENDING_INSERT
                    )
                    if (orderRepo.makeOrder(listOf(params))) {
                        _uiState.update { it.copy(isMakeOrderDone = true) }
                    }
                } catch (e: Exception) {
                    handleError(e)
                } finally {
                    _uiState.update { it.copy(committingOrder = false) }
                }
            }
        }
    }

    private suspend fun handleError(e: Exception) {
        val message = errorHandler.handle(e)
        _uiState.update { it.copy(error = message) }
    }
}
