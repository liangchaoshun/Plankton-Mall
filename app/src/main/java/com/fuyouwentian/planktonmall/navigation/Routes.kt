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

// 底部导航栏对应一级页面（HOME, CATEGORY, CART, PROFILE）
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

/**
 * 用途：手动点击底部导航栏（Home, Category, Cart, Profile）
 * 逻辑：popUpTo(start) { saveState = true } + restoreState = true。实现 Tab 之间的丝滑切换，保存和恢复状态。
 */
fun NavHostController.navigate2Tab(route: String) {
    navigate(route) {
        // 把栈一直弹到 HOME，并保存状态（为了 Tab 切换时的状态恢复）
        popUpTo(graph.findStartDestination().id) { saveState = true } // 弹栈时保存 UI 状态（滚动位置、输入内容等）
        launchSingleTop = true // 避免重复压栈
        // 若为 true，则编程跳转到普通页面，又跳到一级页面，
        // 再手动点击底部导航栏跳到其他一级页面时，会显示那个普通页面而非一级页面，
        // restoreState = false 可以避免这种情况
        restoreState = true // 恢复目标状态
    }
}

/**
 * 用途：彻底重置（登录成功、退出登录等）
 * 逻辑：popUpTo(start) { inclusive = true } + launchSingleTop。连根拔起，清空一切，重新开始
 * 为什么必须用 inclusive = true？
 * 因为若不把 HOME 彻底销毁，之前登录状态下首页加载过的数据（比如 ViewModel 里的用户头像、推荐商品）可能还残留在 NavBackStackEntry 中，
 * 只有彻底 inclusive = true 销毁重来，才能确保前一个账号的所有痕迹被抹除
 */
fun NavHostController.navigate2HomeClearAll() {
    navigate(Routes.HOME) {
        // inclusive = true 表示连起点（如果根节点不是 Home）也弹出，彻底销毁，然后重新创建一个全新的 HOME 压入栈底
        popUpTo(graph.findStartDestination().id) { inclusive = true }
        launchSingleTop = true
        // ⚠ 不能加 saveState 和 restoreState
    }
}

// 同 navigate2HomeClearAll
fun NavHostController.navigate2Login() {
    navigate(Routes.LOGIN) {
        popUpTo(graph.findStartDestination().id) { inclusive = true } // 清空整个回退栈
        launchSingleTop = true
    }
}

/**
 * 用途：进入普通页面（Detail Products Login Register）
 * 逻辑：这种页面不破坏主栈结构，直接压栈，保留历史
 */
fun NavHostController.navigate2Normal(route: String) {
    navigate(route) {
        // 不写 popUpTo，保留历史记录，按返回键可以回退
        launchSingleTop = true
    }
}


/**
 * 用途：代码逻辑触发（编程式跳转），从普通页面（如详情页、列表页等）跳转到底部 Tab 页面
 * 逻辑：popUpTo(start)（不加 inclusive，保留 Tab 底层）。清空中间的普通页栈
 */
fun NavHostController.navigateFromNormal2Tab(route: String) {
    // 先弹出一个层级，解决从详情页（普通也）跳转到 Tab 页后，路由锁死崩溃 bug
    popBackStack() // 弹出当前所在的详情页 (DETAIL)，回到它的上一级
    /*
    // 另外，也可以做成：不管嵌套多深，都清空，只留下 Tab 页的路由
    while (currentBackStackEntry?.destination?.route !in NAVI_ROUTES) {
        if (!popBackStack()) break // popBackStack 弹出一个页面就返回 true。若弹栈失败，则停止循环
    }*/

    // 再跳转到目标 Tab
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
