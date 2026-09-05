plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val appVersion = providers.gradleProperty("appVersion")
    .orElse(providers.environmentVariable("APP_VERSION"))
    .orElse("0.0.1")
    .get()
    .removePrefix("v")
val appVersionCode = providers.gradleProperty("appVersionCode")
    .orElse(providers.environmentVariable("APP_VERSION_CODE"))
    .map { it.toIntOrNull()?.coerceAtLeast(1) ?: 1 }
    .orElse(1)
    .get()

android {
    namespace = "app.nodeloc"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.nodeloc"
        minSdk = 26
        targetSdk = 34
        versionCode = appVersionCode
        versionName = appVersion
    }

    val ksFile: String = System.getenv("NL_KEYSTORE")
        ?: (rootDir.resolve("keystore/release.keystore").absolutePath)

    signingConfigs {
        create("release") {
            storeFile = file(ksFile)
            storePassword = System.getenv("NL_KS_PASS") ?: "android"
            keyAlias = System.getenv("NL_KEY_ALIAS") ?: "androiddebugkey"
            keyPassword = System.getenv("NL_KEY_PASS") ?: "android"
            enableV1Signing = true
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
            isUniversalApk = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    // 楼层操作菜单要跟官网(Font Awesome: flag/wrench/trash-can/link/share/ellipsis 等)图标语义对齐,
    // 基础 material-icons-core 只有 49 个图标不够用,引入 extended 版(BOM 统一管理,不用单独锁版本号)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-svg:2.7.0")
    implementation("io.coil-kt:coil-gif:2.7.0")
    implementation("androidx.browser:browser:1.8.0")
    implementation("org.jsoup:jsoup:1.18.1")
    // 官网 composer 的 Markdown 预览是纯客户端渲染(markdown-it),服务端没有预览端点,
    // 所以本地用 CommonMark 实现同样的规范 + GFM 表格/删除线扩展,渲染结果交给 CookedText。
    implementation("org.commonmark:commonmark:0.22.0")
    implementation("org.commonmark:commonmark-ext-gfm-tables:0.22.0")
    implementation("org.commonmark:commonmark-ext-gfm-strikethrough:0.22.0")
    implementation("org.commonmark:commonmark-ext-autolink:0.22.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    testImplementation("junit:junit:4.13.2")
}
