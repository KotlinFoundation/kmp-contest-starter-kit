@file:OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import extensions.kotlinMultiplatform
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

pluginManager.apply("org.jetbrains.kotlin.multiplatform")
pluginManager.apply("com.android.kotlin.multiplatform.library")

private val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
private val androidCompileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
private val androidMinSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()


kotlinMultiplatform {

    targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach {
        compileSdk = androidCompileSdk
        minSdk = androidMinSdk
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
        androidResources { enable = true }
    }

    applyDefaultHierarchyTemplate {
        common {
            group("mobile") {
                group("ios")
                withCompilations { it.target.platformType == KotlinPlatformType.androidJvm }
            }

            group("nonMobile") {
                withJs()
                withWasmJs()
                withJvm()
            }
        }
    }

    iosArm64()
    iosSimulatorArm64()

    jvm()
    wasmJs {
        browser()
    }
    js(IR) {
        nodejs()
        browser()
        binaries.library()
    }

    sourceSets {
        commonMain.dependencies { }
    }
}
