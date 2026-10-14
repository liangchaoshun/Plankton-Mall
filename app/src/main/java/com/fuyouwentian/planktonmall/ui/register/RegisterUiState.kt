package com.fuyouwentian.planktonmall.ui.register

data class RegisterUiState(
    val account: String = "",
    val password: String = "",
    val commiting: Boolean = false,
    val error: String? = null,
)
