package com.fuyouwentian.planktonmall.ui.cart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuyouwentian.planktonmall.core.auth.AuthManager
import com.fuyouwentian.planktonmall.core.error.ErrorHandler
import com.fuyouwentian.planktonmall.core.event.GlobalEventBus
import com.fuyouwentian.planktonmall.core.event.SystemEvent
import com.fuyouwentian.planktonmall.data.local.SyncRecordState
import com.fuyouwentian.planktonmall.domain.model.CartProduct
import com.fuyouwentian.planktonmall.domain.model.FavorRecord
import com.fuyouwentian.planktonmall.domain.model.OrderRecord
import com.fuyouwentian.planktonmall.domain.repository.CartRepository
import com.fuyouwentian.planktonmall.domain.repository.FavorRepository
import com.fuyouwentian.planktonmall.domain.repository.OrderRepository
import com.fuyouwentian.planktonmall.domain.repository.UserRepository
import com.fuyouwentian.planktonmall.util.MongoIdGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepo: CartRepository,
    private val userRepo: UserRepository,
    private val favorRepo: FavorRepository,
    private val orderRepo: OrderRepository,
    private val eventBus: GlobalEventBus,
    private val errorHandler: ErrorHandler,
    private val authManager: AuthManager
) : ViewModel() {
    // UI 状态
    private val _uiState = MutableStateFlow(CartUiState(loading = true))
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    // 订阅 Room cart data Flow
    @OptIn(ExperimentalCoroutinesApi::class)
    val cartRecords: StateFlow<List<CartProduct>> = userRepo.getCurrentUserIdFlow()
        .flatMapLatest { userId -> // 实验性 API
            if (userId != null) {
                cartRepo.getRecordsFlow(userId)
                    .map { list -> list.sortedByDescending { it.createTime } }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // UI 事件
    private var currentJob: Job? = null

    // 防止竞态：fetchProductsData
    private var requestId = 0

    // 🌟 关于防抖说明：https://chat.deepseek.com/share/xhacrastm18ql8rii0
    // 选用防抖设置每个条目的选中（checked）、数量（quantity）做同步更新到服务器上，还是有点小问题
    // 问题主要在于网络的信号强弱带来的延迟性，导致本地可能已经变了但是上一次的请求延迟了返回结果将当前条目设置为已同步！
    private val syncTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        viewModelScope.launch {
            val userId = userRepo.getCurrentUserId()
            syncTrigger
                .debounce(400.milliseconds)
                .collect {
                    try {
                        cartRepo.syncLocalUpdate2Server(userId)
                    } catch (e: Exception) {
                        handleError(e)
                    }
                }
        }
    }

    // 初始化加载/下拉刷新
    fun fetchCartData() {
        val id = ++requestId
        currentJob?.cancel() // 重置
        currentJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    loading = true,
                    allSelected = false,
                    error = null
                )
            }
            try {
                val userId = authManager.getCurrentUserId(false)
                if (userId == null) {
                    _uiState.update { it.copy(hasToken = false) }
                    return@launch
                }
                cartRepo.fetchData() // 请求，成功后写入 Room cart
                if (id != requestId) return@launch // 已过期
            } catch (e: Exception) {
                if (id == requestId) handleError(e)
            } finally {
                if (id == requestId) _uiState.update { it.copy(loading = false) }
            }
        }
    }

    private suspend fun safeUpdateAndSync(updateLocal: suspend () -> Unit) {
        try {
            updateLocal() // 先更新本地
            syncTrigger.tryEmit(Unit) // 再同步到服务器
        } catch (e: Exception) {
            handleError(e)
        }
    }

    // 切换选中某项
    fun toggleSelect(id: String, bool: Boolean) {
        viewModelScope.launch {
            safeUpdateAndSync { cartRepo.updateMultiChecked(ids = listOf(id), checked = bool) }
        }
    }

    // 切换全选/全不选
    fun toggleSelectAll() {
        val target = !_uiState.value.allSelected // 只有两种可能：全选和全不选
        _uiState.update { state -> state.copy(allSelected = target) }
        viewModelScope.launch {
            safeUpdateAndSync {
                cartRepo.updateMultiChecked(
                    ids = cartRecords.value.filter { it.isValid && it.goodsStock > 0 && it.goodsOnSale == 1 }
                        .map { it.id },
                    checked = target
                )
            }
        }
    }

    // 更新数量
    fun changeQuantity(id: String, value: Int) {
        if (value !in 1 until 1000) return
        // 取旧值用于失败回滚
        val legacyValue = cartRecords.value.firstOrNull { it.id == id }?.quantity ?: return
        if (legacyValue == value) return

        viewModelScope.launch {
            safeUpdateAndSync { cartRepo.updateQuantity(id, value) }
        }
    }

    fun deleteCartRecords(ids: List<String>) {
        Log.d("CartViewModel", "deleteCartRecords: $ids")
        viewModelScope.launch {
            try {
                if (cartRepo.delete(ids)) eventBus.postEvent(SystemEvent.ShowToast("删除成功"))
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun add2Favorites(product: CartProduct) {
        viewModelScope.launch {
            try {
                val userId = authManager.getCurrentUserId() ?: return@launch
                if (favorRepo.isExist(userId = userId, goodsId = product.goodsId)) {
                    // 已存在，则只提示
                    eventBus.postEvent(SystemEvent.ShowToast("加入收藏成功"))
                } else {
                    // 不存在，则加入收藏
                    val req = FavorRecord(
                        id = MongoIdGenerator.generate(),
                        userId = userId,
                        goodsId = product.goodsId,
                        isValid = product.isValid,
                        goodsNameZh = product.goodsNameZh,
                        goodsNameEn = product.goodsNameEn,
                        goodsIcon = product.goodsIcon,
                        goodsPrice = product.goodsPrice,
                        goodsStock = product.goodsStock,
                        goodsOnSale = product.goodsOnSale,
                        syncState = SyncRecordState.PENDING_INSERT
                    )
                    if (favorRepo.add2Favorites(request = req)) {
                        eventBus.postEvent(SystemEvent.ShowToast("加入收藏成功"))
                    }
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun syncData2Server(id: String = "") {
        // Log.d("Cart", "[syncData2Server] id: $id")
        viewModelScope.launch {
            try {
                val userId = userRepo.getCurrentUserId()
                cartRepo.syncLocalUpdate2Server(userId) // 全量（checked、quantity）提交的
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    // TODO 结算成功后，从购物车中删除
    fun makeOrder() {
        val records =
            cartRecords.value.filter { it.isValid && it.goodsStock > 0 && it.goodsOnSale == 1 && it.checked }
        _uiState.update { it.copy(committingOrder = true, isMakeOrderDone = false) }
        viewModelScope.launch {
            val userId = authManager.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(committingOrder = false, isMakeOrderDone = false) }
                return@launch
            }
            try {
                val receiverInfo = userRepo.getCurrentReceiverInfo()
                val params = records.map { record ->
                    val price = record.goodsPrice.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    val actualPay = (price * record.quantity.toBigDecimal())
                        .setScale(2, RoundingMode.HALF_UP)
                        .toPlainString()
                    OrderRecord(
                        id = MongoIdGenerator.generate(),
                        userId = userId,
                        goodsId = record.goodsId,
                        goodsNameZh = record.goodsNameZh,
                        goodsNameEn = record.goodsNameEn,
                        goodsIcon = record.goodsIcon,
                        goodsPrice = record.goodsPrice,
                        quantity = record.quantity,
                        actualPay = actualPay,
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
                }
                orderRepo.makeOrder(params)
                _uiState.update { it.copy(isMakeOrderDone = true) }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _uiState.update { it.copy(committingOrder = false) }
            }
        }
    }

    private suspend fun handleError(e: Exception) {
        val message = errorHandler.handle(e)
        _uiState.update { it.copy(error = message) }
    }
}
