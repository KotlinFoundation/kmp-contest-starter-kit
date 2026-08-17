@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.github.gmazzo.buildconfig.BuildConfigExtension
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.roborazzi)
}

kotlin {
    applyDefaultHierarchyTemplate {
        common {
            group("mobile") {
                group("ios")
                // Match by platformType so the new AGP 9 KMP library Android target is included.
                withCompilations { it.target.platformType == KotlinPlatformType.androidJvm }
            }

            group("nonMobile") {
                withJs()
                withWasmJs()
                withJvm()
            }

            group("nonWeb") {
                group("ios")
                withCompilations { it.target.platformType == KotlinPlatformType.androidJvm }
                withJvm()
            }
        }
    }

    android {
        namespace = "com.kotlinfoundation.koko.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources { enable = true }
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
        withHostTest { isIncludeAndroidResources = true }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "Shared"
            isStatic = true
            // Explicit bundle id for crash symbolication; silences the "Cannot infer a bundle ID" link warning.
            binaryOption("bundleId", "com.kotlinfoundation.koko.shared")
        }
    }

    jvm()
    // Browser test tasks need a headless browser (Karma) that isn't available in CI / `check`,
    // and they'd just re-run the same commonTest already covered by jvmTest + testAndroidHostTest.
    // Disable the test run only; the browser() app target is unaffected.
    js { browser { testTask { enabled = false } } }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser { testTask { enabled = false } } }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.designsystem)
            api(libs.compose.runtime)
            api(libs.compose.ui)
            api(libs.koin.core)
            api(libs.kmpnotifier.push.firebase)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material.icons)
            implementation(libs.compose.material3)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.kmpauth.google)
            implementation(libs.kmpauth.apple)
            implementation(libs.kmpauth.firebase)
            implementation(libs.napier)
            implementation(libs.room.runtime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.navigation3)
            implementation(libs.lifecyle.runtime)
            implementation(libs.uuid)
            // Only the permission modules this app actually uses. The umbrella
            // calf-permissions artifact links location, bluetooth, contacts and more,
            // and App Store review then demands a purpose string for each (ITMS-90683)
            // even though nothing calls them. Add a module here when you add a permission.
            implementation(libs.calf.permissions.core)
            implementation(libs.calf.permissions.camera)
            implementation(libs.calf.permissions.gallery)
            implementation(libs.calf.permissions.notifications)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.coil.compose)
            implementation(libs.coil.ktor)
            implementation(libs.kotlinx.datetime)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.nav3.ui)
            implementation(libs.lifecycle.viewmodel.navigation3)
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)

            val subscriptionProvider =
                project.findProperty("SUBSCRIPTION_PROVIDER")?.toString()?.uppercase()
                    ?: "ADAPTY"
            when (subscriptionProvider) {
                "ADAPTY" -> implementation(projects.libs.subscription.subscriptionAdapty)
                "REVENUECAT" -> implementation(projects.libs.subscription.subscriptionRevenuecat)
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compose.uiTest)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }

        // Android host tests run on the JVM via Robolectric. Roborazzi snapshots
        // every @Preview discovered by ComposablePreviewScanner.
        val androidHostTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.robolectric)
                implementation(libs.roborazzi)
                implementation(libs.roborazzi.compose)
                implementation(libs.roborazzi.junit.rule)
                implementation(libs.roborazzi.compose.preview.scanner.support)
                implementation(libs.composable.preview.scanner.android)
                implementation(libs.androidx.uitest.junit4)
                implementation(libs.androidx.uitest.testManifest)
            }
        }

        androidMain.dependencies {
            implementation(libs.androidx.activityCompose)
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.messaging)
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)
            implementation(libs.firebase.config)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
            implementation(libs.google.admob)
            implementation(libs.android.inappreview)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.okhttp)
        }

        val nonWebMain by getting {
            dependencies {
                implementation(libs.sqlite.bundled)
            }
        }

        val webMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
                implementation(libs.sqlite.web)
                implementation(npm("@sqlite.org/sqlite-wasm", "3.50.1-build1"))
                implementation(npm("sqlite-wasm-worker", project.file("sqlite-wasm-worker")))
            }
        }
    }
}

compose.resources {
    packageOfResClass = "com.kotlinfoundation.koko.generated.resources"
}

// iOS unit tests can't link: the Firebase (SwiftPM) dependency is wired into the app framework
// but not the test binary's linker search path (`ld: framework 'FirebaseCore' not found`). The
// same commonTest already runs on JVM + Android host (the PR gate), so disable the iOS test
// link + run tasks. The iOS app framework (linkDebugFramework*) is unaffected.
tasks.matching {
    it.name.startsWith("linkDebugTestIos") || Regex("^ios\\w*Test$").matches(it.name)
}.configureEach { enabled = false }

// Amazon Appstore SDK ships legacy bytecode that fails Robolectric's verifier when
// its broadcast receiver is registered during test setup. RevenueCat pulls it in
// transitively; we don't need it for screenshot testing — strip it from the host-
// test classpath so Robolectric never tries to load `ResponseReceiver`.
configurations.matching { it.name.contains("ndroidHostTest", ignoreCase = false) }.configureEach {
    exclude(group = "com.amazon.device", module = "amazon-appstore-sdk")
    exclude(group = "com.revenuecat.purchases", module = "purchases-store-amazon")
}

roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/androidHostTest/snapshots"))
}

// Propagate `-PgenerateStoreScreenshots=true` to the test JVM as a system property so
// StoreScreenshotGeneratorTest's parameter list opens up. Without the flag the test
// is a no-op, keeping PR-check runs fast.
tasks.withType<Test>().configureEach {
    systemProperty(
        "generateStoreScreenshots",
        providers.gradleProperty("generateStoreScreenshots").orElse("false").get(),
    )

    // Run tests on JDK 21 while everything still COMPILES against 17 (jvmToolchain(17)).
    // Robolectric loads real dependency bytecode, and filekit >= 0.14 ships Java 21 class
    // files (major 65). On a 17 test JVM the preview scanner dies with
    // `UnsupportedClassVersionError: io/github/vinceglb/filekit/PlatformFile`, which takes
    // out the whole Roborazzi run. The foojay resolver in settings.gradle.kts provisions
    // the JDK automatically, so no manual install is needed.
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        },
    )
}

tasks.register("generateStoreScreenshots") {
    group = "koko"
    description = "Render every @Preview @StoreScreenshot into distribution/store_screenshots/."
    dependsOn("recordRoborazziAndroidHostTest")
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspJvm", libs.room.compiler)
    add("kspWasmJs", libs.room.compiler)
    add("kspJs", libs.room.compiler)

    // Compose UI tooling — runtime-only artifact for inspecting the composition tree at runtime.
    // Per the JetBrains KMP-library AGP 9 skill, `androidRuntimeClasspath` keeps it off compile.
    "androidRuntimeClasspath"(libs.compose.uiTooling)
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

buildConfig {
    // BuildConfig configuration here.
    // https://github.com/gmazzo/gradle-buildconfig-plugin#usage-in-kts
    packageName("com.kotlinfoundation.koko.common")
    // AGP 9's `com.android.kotlin.multiplatform.library` plugin does NOT generate BuildConfig the way the
    // old `com.android.application` plugin did. Replicate the `DEBUG` flag here so commonMain code that
    // depended on `BuildConfig.DEBUG` keeps working. Defaults to `false` (production-safe); the consuming
    // app can override via `-PisDebugBuild=true`.
    buildConfigField(
        "DEBUG",
        (project.findProperty("isDebugBuild") as String?)?.toBoolean() ?: false,
    )
    buildConfigField(
        "GOOGLE_WEB_CLIENT_ID",
        getRequiredProperty(key = "GOOGLE_WEB_CLIENT_ID", defaultValue = "testValue"),
    )

    // Firebase web config for KMPAuth on Desktop/Web only — those targets have no native Firebase SDK,
    // so the auth backend runs on a REST engine that needs this config. Leave EMPTY for mobile (Android/iOS
    // read google-services.json / GoogleService-Info.plist). From the Firebase console → a Web app.
    buildConfigField("FIREBASE_API_KEY", getRequiredProperty(key = "FIREBASE_API_KEY", defaultValue = ""))
    buildConfigField("FIREBASE_PROJECT_ID", getRequiredProperty(key = "FIREBASE_PROJECT_ID", defaultValue = ""))
    buildConfigField("FIREBASE_APPLICATION_ID", getRequiredProperty(key = "FIREBASE_APPLICATION_ID", defaultValue = ""))

    // Direct-AI (no-Firebase) provider keys. Empty by default → the app uses the Cloud Functions proxy.
    // Setting one (with a blank CLOUD_FUNCTIONS_URL) makes AiTransport call the provider directly.
    // Prototyping only — the key ships in the app binary; production should keep the proxy.
    buildConfigField("OPENAI_API_KEY", getRequiredProperty(key = "OPENAI_API_KEY", defaultValue = ""))
    buildConfigField("REPLICATE_API_KEY", getRequiredProperty(key = "REPLICATE_API_KEY", defaultValue = ""))

    // Adapty or RevenueCat Api key
    buildConfigField(
        "SUBSCRIPTION_PROVIDER_ANDROID_API_KEY",
        getRequiredProperty(
            key = "SUBSCRIPTION_PROVIDER_ANDROID_API_KEY",
            defaultValue = "testValue",
        ),
    )
    buildConfigField(
        "SUBSCRIPTION_PROVIDER_IOS_API_KEY",
        getRequiredProperty(key = "SUBSCRIPTION_PROVIDER_IOS_API_KEY", defaultValue = "testValue"),
    )

    setupAdmobAdsIds()
}

private fun BuildConfigExtension.setupAdmobAdsIds() {
    // Android Admob ids
    buildConfigField(
        name = "ADMOB_APP_ID_ANDROID",
        value = getRequiredProperty(key = "ADMOB_APP_ID_ANDROID", defaultValue = ""),
    )
    buildConfigField(
        name = "ADMOB_BANNER_AD_ID_ANDROID",
        value = getRequiredProperty(key = "ADMOB_BANNER_AD_ID_ANDROID", defaultValue = ""),
    )
    buildConfigField(
        name = "ADMOB_INTERSTITIAL_AD_ID_ANDROID",
        value = getRequiredProperty(key = "ADMOB_INTERSTITIAL_AD_ID_ANDROID", defaultValue = ""),
    )
    buildConfigField(
        name = "ADMOB_REWARDED_AD_ID_ANDROID",
        value = getRequiredProperty(key = "ADMOB_REWARDED_AD_ID_ANDROID", defaultValue = ""),
    )

    // ios Admob ids
    buildConfigField(
        name = "ADMOB_BANNER_AD_ID_IOS",
        value = getRequiredProperty(key = "ADMOB_BANNER_AD_ID_IOS", defaultValue = ""),
    )
    buildConfigField(
        name = "ADMOB_INTERSTITIAL_AD_ID_IOS",
        value = getRequiredProperty(key = "ADMOB_INTERSTITIAL_AD_ID_IOS", defaultValue = ""),
    )
    buildConfigField(
        name = "ADMOB_REWARDED_AD_ID_IOS",
        value = getRequiredProperty(key = "ADMOB_REWARDED_AD_ID_IOS", defaultValue = ""),
    )
}

fun getRequiredProperty(
    key: String,
    defaultValue: String? = null,
    errorMessage: String = "Make sure you added `$key` in local.properties",
): String {
    val propertyValue: String? = gradleLocalProperties(rootDir, providers).getProperty(key)
    if (propertyValue.isNullOrEmpty() && defaultValue == null) {
        throw IllegalArgumentException(errorMessage)
    }
    return propertyValue ?: defaultValue ?: ""
}
