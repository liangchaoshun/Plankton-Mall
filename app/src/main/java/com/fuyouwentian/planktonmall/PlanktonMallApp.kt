package com.fuyouwentian.planktonmall

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fuyouwentian.planktonmall.ui.cart.CartScreen
import com.fuyouwentian.planktonmall.ui.category.CategoryScreen
import com.fuyouwentian.planktonmall.ui.detail.DetailScreen
import com.fuyouwentian.planktonmall.ui.home.HomeScreen
import com.fuyouwentian.planktonmall.ui.products.ProductsScreen
import com.fuyouwentian.planktonmall.ui.profile.ProfileScreen


@Composable
fun PlanktonMallApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // 在 Composable 上下文中先取出字符串
    val routes = NAVI_DEST.map { stringResource(it.routeId) }
    // 用 NavController 的 backStackEntry 驱动选中状态（推荐）
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute: String = backStackEntry?.destination?.route ?: routes[0]
    // 只有当前路由在底部导航的目的地里，才显示底部栏
    val showBottomBar = currentRoute in routes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(modifier = Modifier.fillMaxWidth()) {
                    NAVI_DEST.forEachIndexed { index, dest ->
                        val route = routes[index]
                        NavigationBarItem(
                            selected = currentRoute == route,
                            onClick = {
                                Log.d(
                                    "BottomNav",
                                    "navigate to: $route, currentRoute: $currentRoute"
                                )
                                // 注意：从 CategoryScreen 页面中点击系列，进入 ProductsScreen 页面，
                                //      后续再点击底部导航菜单，页面显示的是 ProductsScreen 而不是 CategoryScreen
                                navController.navigate(route) {
                                    // 把栈弹到只剩起始目的地（Home），然后再压入新的目的地
                                    // saveState：弹栈时保存 UI 状态（滚动位置、输入内容等）
                                    popUpTo(routes[0]) { saveState = true }
                                    launchSingleTop = true // 避免栈顶重复：目标目的地已经在栈顶，就复用，不再压入新的实例
                                    restoreState = true // 恢复之前保存的状态，和 saveState 配套
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentRoute == route) dest.selectedIcon else dest.unselectedIcon,
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(dest.iconTextId)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = routes[0],
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(route = routes[0]) {
                HomeScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_medium)),
                    onProductClick = { productId -> navController.navigate("detail/${productId}") },
                    onSearch = { qs ->
                        if (qs.isNotBlank()) navController.navigate("products/$qs")
                    }
                )
            }
            composable(route = routes[1]) {
                CategoryScreen(
                    modifier = Modifier.fillMaxSize(),
                    onSeriesClick = { seriesId -> navController.navigate("products/$seriesId") }
                )
            }
            composable(route = routes[2]) {
                CartScreen(
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(route = routes[3]) {
                ProfileScreen(
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(
                route = "products/{q}",
                arguments = listOf(navArgument("q") { type = NavType.StringType })
            ) { backStackEntry ->
                val q = backStackEntry.arguments?.getString("q") ?: ""
                ProductsScreen(
                    q = q,
                    modifier = Modifier.fillMaxSize(),
                    onProductClick = { productId -> navController.navigate("detail/${productId}") },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = "detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                DetailScreen(
                    id = id,
                    modifier = Modifier.fillMaxSize(),
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

data class NavHostDest(
    @StringRes val routeId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val iconTextId: Int
)

val NAVI_DEST = listOf(
    NavHostDest(
        routeId = R.string.home_route,
        selectedIcon = Icons.Outlined.Home,
        unselectedIcon = Icons.Outlined.Home,
        iconTextId = R.string.home_label,
    ),
    NavHostDest(
        routeId = R.string.category_route,
        selectedIcon = Icons.Outlined.Category,
        unselectedIcon = Icons.Outlined.Category,
        iconTextId = R.string.category_label
    ),
    NavHostDest(
        routeId = R.string.cart_route,
        selectedIcon = Icons.Outlined.ShoppingCart,
        unselectedIcon = Icons.Outlined.ShoppingCart,
        iconTextId = R.string.cart_label,
    ),
    NavHostDest(
        routeId = R.string.profile_route,
        selectedIcon = Icons.Outlined.Person,
        unselectedIcon = Icons.Outlined.Person,
        iconTextId = R.string.profile_label,
    )
)
