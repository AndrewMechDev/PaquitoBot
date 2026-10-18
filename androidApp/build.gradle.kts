import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val clerkPublishableKey = localProperties.getProperty("clerk.publishableKey", "").trim()
val miniMaxApiKey = localProperties.getProperty("minimax.apiKey", "").trim()
require(clerkPublishableKey.startsWith("pk_test_") || clerkPublishableKey.startsWith("pk_live_")) {
    "Falta clerk.publishableKey en local.properties. Agrega la publishable key de Clerk (pk_test_... o pk_live_...)."
}

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}
dependencies {
    implementation(project(":sharedLogic"))

    implementation(libs.google.auth)
    implementation(libs.kotlinx.coroutines.playServices)
    implementation(libs.clerk.android.api)
    implementation(libs.clerk.android.ui)

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.components.resources)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.work.runtime)
    coreLibraryDesugaring(libs.desugar.jdk)

    // Glassmorphism real (blur de fondo) para el Navbar flotante - reemplaza
    // la opacidad simulada sin blur que habia antes.
    implementation(libs.haze)
    implementation(libs.haze.materials)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.androidx.test.runner)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${libs.versions.kotlinx.coroutines.get()}")
}

android {
    namespace = "pe.tecsup.paquitobot"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "pe.tecsup.paquitobot"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "CLERK_PUBLISHABLE_KEY",
            "\"$clerkPublishableKey\""
        )
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            // Solo para prototipos locales: la clave queda dentro del APK debug.
            buildConfigField("String", "MINIMAX_API_KEY", "\"${miniMaxApiKey.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
        }
        release {
            buildConfigField("String", "MINIMAX_API_KEY", "\"\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}
