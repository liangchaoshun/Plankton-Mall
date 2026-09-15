package com.fuyouwentian.planktonmall

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fuyouwentian.planktonmall.ui.CartScreen
import com.fuyouwentian.planktonmall.ui.CategoryScreen
import com.fuyouwentian.planktonmall.ui.HomeScreen
import com.fuyouwentian.planktonmall.ui.ProfileScreen


@Composable
fun PlanktonMallApp(
    modifier: Modifier = Modifier,
    // viewModel: OrderViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    // 在 Composable 上下文中先取出字符串
    val routes = NAVI_DEST.map { stringResource(it.routeId) }
    val selectedDestination = remember { mutableStateOf(routes[0]) }
    // 用 NavController 的 backStackEntry 驱动选中状态（推荐）
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute: String = backStackEntry?.destination?.route ?: routes[0]
    Scaffold(
        bottomBar = {
            NavigationBar(modifier = Modifier.fillMaxWidth()) {
                NAVI_DEST.forEachIndexed { index, dest ->
                    val route = routes[index]
                    NavigationBarItem(
                        selected = currentRoute == route,
                        onClick = {
                            navController.navigate(route) {
                                // 把栈弹到只剩起始目的地（Home），然后再压入新的目的地
                                // saveState：弹栈时保存 UI 状态（滚动位置、输入内容等）
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
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
                        .padding(dimensionResource(R.dimen.padding_medium))
                )
            }
            composable(route = routes[1]) {
                CategoryScreen(
                    modifier = Modifier.fillMaxSize()
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
