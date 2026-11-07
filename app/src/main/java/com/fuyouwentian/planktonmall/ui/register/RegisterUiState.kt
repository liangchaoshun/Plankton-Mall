package com.fuyouwentian.planktonmall.ui.register

data class RegisterUiState(
    val account: String = "",
    val password: String = "",
    val isAccountFmtError: Boolean = false,
    val isPasswordFmtError: Boolean = false,
    val committing: Boolean = false,
    val error: String? = null,
)
