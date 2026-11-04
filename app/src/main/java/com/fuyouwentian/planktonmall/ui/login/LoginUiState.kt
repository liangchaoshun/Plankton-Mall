package com.fuyouwentian.planktonmall.ui.login

data class LoginUiState(
    val account: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val committing: Boolean = false,
    val error: String? = null,
)
