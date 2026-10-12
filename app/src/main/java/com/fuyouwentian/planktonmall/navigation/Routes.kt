package com.fuyouwentian.planktonmall.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.fuyouwentian.planktonmall.R

object Routes {
    const val HOME = "home"
    const val CATEGORY = "category"
    const val CARt = "cart"
    const val PROFILE = "profile"
    const val CHECKOUT = "checkout"
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
        route = Routes.CARt,
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