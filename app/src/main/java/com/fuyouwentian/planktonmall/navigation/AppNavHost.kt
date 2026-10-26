package com.fuyouwentian.planktonmall.navigation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fuyouwentian.planktonmall.domain.model.UiEvent
import com.fuyouwentian.planktonmall.ui.cart.CartScreen
import com.fuyouwentian.planktonmall.ui.category.CategoryScreen
import com.fuyouwentian.planktonmall.ui.detail.DetailScreen
import com.fuyouwentian.planktonmall.ui.home.HomeScreen
import com.fuyouwentian.planktonmall.ui.login.LoginScreen
import com.fuyouwentian.planktonmall.ui.products.ProductsScreen
import com.fuyouwentian.planktonmall.ui.profile.ProfileScreen
import com.fuyouwentian.planktonmall.ui.register.RegisterScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: AppNavHostViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    // 用 NavController 的 backStackEntry 驱动选中状态（推荐）
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute: String = backStackEntry?.destination?.route ?: Routes.HOME
    // 只有当前路由在底部导航的目的地里，才显示底部栏
    val showBottomBar = currentRoute in NAVI_ROUTES
    val cartTotalQuantity by viewModel.cartTotalQuantity.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.tokenUiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                // 强制踢出到登录页，就必须清空栈
                is UiEvent.NavigateToLogin -> {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true // 清空整个回退栈
                        }
                        launchSingleTop = true
                    }
                }

                else -> {}
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                // 自定义底部导航栏布局
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface) // 底部背景色
                        .navigationBarsPadding() // 适配真机三大金刚键/手势条
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp), // 固定高度
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NAVI_DEST.forEach { dest ->
                            val route = dest.route
                            val selected = currentRoute == route
                            val iconVector =
                                if (selected) dest.selectedIcon else dest.unselectedIcon
                            val destColor =
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null // 去掉水波纹
                                    ) {
                                        if (currentRoute != route) {
                                            navController.navigate2Tab(route)
                                        } else {
                                            // 可实现滚动到当前页面最顶端
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                // 选中的背景
                                Box(
                                    modifier = Modifier
                                        .size(width = 60.dp, height = 50.dp)
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.primaryContainer.copy(
                                                0.3f
                                            )
                                            else Color.Transparent
                                        )
                                )

                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = iconVector,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = destColor
                                        )
                                        // 购物车徽章
                                        if (dest.route == Routes.CART && cartTotalQuantity > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .offset(x = 12.dp, y = (-8).dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.Red),
                                            ) {
                                                Text(
                                                    text = if (cartTotalQuantity > 99) "99+" else cartTotalQuantity.toString(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(
                                                        horizontal = 4.dp,
                                                        vertical = 0.dp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = stringResource(dest.iconTextId),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = destColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.PROFILE, // ⚠✨🔥
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(route = Routes.HOME) {
                HomeScreen(
                    modifier = Modifier.fillMaxSize(),
                    onProductClick = { productId -> navController.navigate2Normal("detail/${productId}") },
                    onSearch = { qs ->
                        if (qs.isNotBlank()) navController.navigate2Normal("products/$qs")
                    }
                )
            }
            composable(route = Routes.CATEGORY) {
                CategoryScreen(
                    modifier = Modifier.fillMaxSize(),
                    onSeriesClick = { seriesId -> navController.navigate2Normal("products/$seriesId") }
                )
            }
            composable(route = Routes.CART) {
                CartScreen(
                    modifier = Modifier.fillMaxSize(),
                    onProductClick = { productId -> navController.navigate2Normal("detail/${productId}") },
                    onSettleClick = { navController.navigate2Tab(Routes.PROFILE) },
                    onNavigate2Login = { navController.navigate2Normal(Routes.LOGIN) },
                    onNavigate2Pickup = { navController.navigate2Tab(Routes.HOME) }
                )
            }
            composable(route = Routes.PROFILE) {
                ProfileScreen(
                    modifier = Modifier.fillMaxSize(),
                    onNavigate2Login = { navController.navigate2Normal(Routes.LOGIN) },
                    onLogoutSuccess = { navController.navigate2HomeClearAll() },
                    onProductClick = { productId -> navController.navigate2Normal("detail/${productId}") },
                )
            }
            composable(route = Routes.LOGIN) {
                LoginScreen(
                    modifier = Modifier.fillMaxSize(),
                    onNavigate2Register = { navController.navigate2Normal(Routes.REGISTER) },
                    onNavigate2Home = {
                        /**
                         * 401 保存跳转登录前的路由，登录后重新跳转到保存的路由，存在隐患
                         * 因保存的路由对应页面的获取数据接口，可能对新登录的账号无权限，又返回 401，陷入无限死循环
                         * 所以此处跳转固定的路由
                         */
                        navController.navigate2HomeClearAll()
                    }
                )
            }
            composable(route = Routes.REGISTER) {
                RegisterScreen(
                    modifier = Modifier.fillMaxSize(),
                    onNavigateToLogin = { navController.navigate2Normal(Routes.LOGIN) },
                    onRegisterSuccess = { navController.navigate2HomeClearAll() }
                )
            }
            composable(
                route = Routes.PRODUCTS,
                arguments = listOf(navArgument("q") { type = NavType.StringType })
            ) { backStackEntry ->
                val q = backStackEntry.arguments?.getString("q") ?: ""
                ProductsScreen(
                    q = q,
                    modifier = Modifier.fillMaxSize(),
                    onProductClick = { productId -> navController.navigate2Normal("detail/${productId}") },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                DetailScreen(
                    id = id,
                    onBackClick = { navController.popBackStack() },
                    onCartIconClick = { navController.navigate2Tab(Routes.CART) },
                    onBuyNowClick = { navController.navigate2Tab(Routes.PROFILE) },
                )
            }
        }
    }
}
