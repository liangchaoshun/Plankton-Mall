pluginManagement {
    repositories {
        // 插件相关仓库，使用阿里云镜像
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/gradle-plugin")

        // 原有的仓库
        google()
        mavenCentral()
        gradlePluginPortal() // 保留原生的 Gradle 插件门户作为备用
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 依赖相关仓库，使用阿里云镜像
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/central")

        google()
        mavenCentral()
    }
}

rootProject.name = "PlanktonMall"
include(":app")
