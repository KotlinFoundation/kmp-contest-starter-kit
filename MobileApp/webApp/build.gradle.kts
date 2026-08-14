@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
}

kotlin {

    js {
        browser()
        binaries.executable()
    }

    wasmJs {
        outputModuleName.set("webApp")
        // Dev-server settings (static dir + the AI CORS proxies) live in webpack.config.d/ —
        // a commonWebpackConfig {} block is a Gradle script object reference and cannot be
        // serialized by the configuration cache.
        browser()
        binaries.executable()
    }

    sourceSets {
        webMain.dependencies {
            implementation(projects.shared)
        }
    }
}
