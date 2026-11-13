plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.androidx.room)
}

room {
    // 设置导出“数据库模式”路径
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = "com.fuyouwentian.planktonmall"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.fuyouwentian.planktonmall"
        minSdk = 26
        targetSdk = 37
        // 🔥设置 app 版本号 >> https://chat.deepseek.com/share/yirr0ezizh4jqamy0q
        versionCode = 2 // ⚠✨🔥 内部版本号，整数，不显示给用户，用于判断更新，新版发布时，数字必须比上一个版本大
        versionName = "1.0.1" // 版本名称，字符串，用户能查看到的版本号（如："1.0.0" 、"2.1.5-beta" 等）

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    buildToolsVersion = "37.0.0"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler) // 注意：这里引用的是在 toml 里定义的 hilt-compiler
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging) // 日志拦截器
    implementation(libs.converter.kotlinx.serialization) // 转换器
    implementation(libs.kotlinx.serialization.json) // 序列化库
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)   // 注意这里用 ksp() 而不是 kapt()
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Test

}