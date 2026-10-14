import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
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

    // Almacenamiento cifrado de flags locales (onboarding, Canvas).
    implementation(libs.androidx.security.crypto)

    // Glassmorphism real (blur de fondo) para el Navbar flotante - reemplaza
    // la opacidad simulada sin blur que habia antes.
    implementation(libs.haze)
    implementation(libs.haze.materials)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
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

        buildConfigField(
            "String",
            "CLERK_PUBLISHABLE_KEY",
            "\"${localProperties.getProperty("clerk.publishableKey", "")}\""
        )
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}