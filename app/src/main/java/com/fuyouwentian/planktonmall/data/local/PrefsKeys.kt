package com.fuyouwentian.planktonmall.data.local

import androidx.datastore.preferences.core.stringPreferencesKey

object PrefsKeys {
    val TOKEN = stringPreferencesKey("auth_token")
    val USER_ID = stringPreferencesKey("user_id")
}