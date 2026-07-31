@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

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
        browser {
            commonWebpackConfig {
                outputFileName = "webApp.js"
                devServer =
                    (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                        static(project.projectDir.path)
                        // Local-dev CORS bypass for DIRECT-mode AI: the browser can't call
                        // api.replicate.com / api.openai.com (no Access-Control-Allow-Origin), so the app
                        // (on web) calls same-origin /replicate & /openai and the dev server proxies them
                        // to the provider with the key forwarded server-side. See baseUrl in
                        // ReplicateApiService / OpenAiApiService.
                        // Deployed web still needs the Cloud Functions proxy (CLOUD_FUNCTIONS_URL).
                        proxy =
                            (proxy ?: mutableListOf()).apply {
                                add(
                                    KotlinWebpackConfig.DevServer.Proxy(
                                        context = mutableListOf("/replicate"),
                                        target = "https://api.replicate.com",
                                        pathRewrite = mutableMapOf("^/replicate" to ""),
                                        changeOrigin = true,
                                        secure = false,
                                    ),
                                )
                                add(
                                    KotlinWebpackConfig.DevServer.Proxy(
                                        context = mutableListOf("/openai"),
                                        target = "https://api.openai.com",
                                        pathRewrite = mutableMapOf("^/openai" to ""),
                                        changeOrigin = true,
                                        secure = false,
                                    ),
                                )
                                // Replicate's output-image CDN also sends no CORS headers, so the
                                // generated image is fetched through the proxy too (see FileManager.web.kt).
                                add(
                                    KotlinWebpackConfig.DevServer.Proxy(
                                        // Non-nested prefix: "/replicate" would greedily match this path too.
                                        context = mutableListOf("/rdelivery"),
                                        target = "https://replicate.delivery",
                                        pathRewrite = mutableMapOf("^/rdelivery" to ""),
                                        changeOrigin = true,
                                        secure = false,
                                    ),
                                )
                            }
                    }
            }
        }
        binaries.executable()
    }

    sourceSets {
        webMain.dependencies {
            implementation(projects.shared)
        }
    }
}
