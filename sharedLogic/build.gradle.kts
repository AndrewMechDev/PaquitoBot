import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

kotlin {
    // --- Targets iOS deshabilitados (alcance actual: Android-only) ---
    // Auditoría 2026-08-06: el repositorio es actualmente solo Android.
    // Estos targets se conservan comentados porque (a) la skill/convención del
    // repo es multiplatform-ready y (b) si el alcance vuelve a incluir iOS en
    // el futuro, basta descomentar este bloque y re-correr gradle sync.
    //
    // listOf(
    //     iosArm64(),
    //     iosSimulatorArm64()
    // ).forEach { iosTarget ->
    //     iosTarget.binaries.framework {
    //         baseName = "SharedLogic"
    //         isStatic = true
    //     }
    // }

    android {
       namespace = "pe.tecsup.paquitobot.sharedLogic"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()

       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }

    sourceSets {
        commonMain.dependencies {
            // put your Multiplatform dependencies here
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}