package com.fuyouwentian.planktonmall.common

object Constants {
    val OBJECT_ID_REGEX = Regex("^[0-9a-fA-F]{24}$")
    val ACCOUNT_REGEX = Regex("^[a-zA-Z][a-zA-Z0-9]*$")
    val PASSWORD_REGEX = Regex("^[a-zA-Z][a-zA-Z0-9!@#$%^&*./\\-_]*$")
}