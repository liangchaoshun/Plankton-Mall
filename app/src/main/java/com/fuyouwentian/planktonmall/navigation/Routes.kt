package com.fuyouwentian.planktonmall.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.fuyouwentian.planktonmall.R

object Routes {
    const val HOME = "home"
    const val CATEGORY = "category"
    const val CART = "cart"
    const val PROFILE = "profile"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DETAIL = "detail/{id}"
    const val PRODUCTS = "products/{q}"
}

data class NavHostDest(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val iconTextId: Int
)

val NAVI_DEST = listOf(
    NavHostDest(
        route = Routes.HOME,
        selectedIcon = Icons.Outlined.Home,
        unselectedIcon = Icons.Outlined.Home,
        iconTextId = R.string.home_label,
    ),
    NavHostDest(
        route = Routes.CATEGORY,
        selectedIcon = Icons.Outlined.Category,
        unselectedIcon = Icons.Outlined.Category,
        iconTextId = R.string.category_label
    ),
    NavHostDest(
        route = Routes.CART,
        selectedIcon = Icons.Outlined.ShoppingCart,
        unselectedIcon = Icons.Outlined.ShoppingCart,
        iconTextId = R.string.cart_label,
    ),
    NavHostDest(
        route = Routes.PROFILE,
        selectedIcon = Icons.Outlined.Person,
        unselectedIcon = Icons.Outlined.Person,
        iconTextId = R.string.profile_label,
    )
)

// 取出底部导航栏的路由字符串
val NAVI_ROUTES = NAVI_DEST.map { it.route }

// 跳转到底部导航栏的一级页面（Home, Category, Cart, Profile）
// ⚠ 无论是“手动点击底部导航栏”还是“从其他页面（编程）跳转到一级页面”都统一使用该方法
fun NavHostController.navigate2Tab(route: String) {
    navigate(route) {
        // 把栈一直弹到 HOME，并保存状态（为了 Tab 切换时的状态恢复）
        // 前提是：NavHost.startDestination 必须是 Routes.HOME
        popUpTo(Routes.HOME) { saveState = true } // 弹栈时保存 UI 状态（滚动位置、输入内容等）
        launchSingleTop = true // 避免重复压栈
        // 若为 true，则编程跳转到普通页面，又跳到以及页面，
        // 再手动点击底部导航栏跳到其他一级页面时，会显示那个普通页面而非一级页面，
        // false 可以避免这种情况
        restoreState = false // 恢复目标状态
    }
}

// 登录/注册成功、退出登录：清空一切，重新开始
fun NavHostController.navigate2HomeClearAll() {
    navigate(Routes.HOME) {
        // 弹到根节点，inclusive = true 表示连根节点（如果根节点不是 Home）也弹出，确保干净
        popUpTo(graph.findStartDestination().id) { inclusive = true }
        launchSingleTop = true
        // ⚠ 不能加 saveState 和 restoreState
    }
}

// 跳转到普通页面（Detail Products Login Register）
// 这种页面不破坏主栈结构，直接压栈即可
fun NavHostController.navigate2Normal(route: String) {
    navigate(route) {
        // 不写 popUpTo，保留历史记录，按返回键可以回退
        launchSingleTop = true
    }
}
