package com.fuyouwentian.planktonmall.ui.login

data class LoginUiState(
    val account: String = "",
    val password: String = "",
    val rsaId: String = "",
    val loading: Boolean = false,
    val error: String? = null,
)
