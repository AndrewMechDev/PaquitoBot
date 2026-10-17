import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    // Version fija = kotlin del catalogo (libs.versions.toml no declara este plugin aun).
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.10"
}

kotlin {
    // --- Targets iOS reactivados (2026-08-06): se suma un colaborador dedicado
    // a iOS al equipo. Genera un framework estatico "SharedLogic.framework"
    // que el proyecto Xcode en iosApp/ consume directo (sin CocoaPods).
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SharedLogic"
            isStatic = true
        }
    }

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
            // JSON academico local (LocalJsonAcademicRepository). Sin cliente HTTP.
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${libs.versions.kotlinx.coroutines.get()}")
            // Lee el academic.json real de androidApp desde el test (SystemFileSystem).
            implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.7.0")
        }
    }
}
