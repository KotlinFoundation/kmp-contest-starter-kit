@file:OptIn(ExperimentalRoborazziApi::class)

package com.kotlinfoundation.koko.screenshot

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.InternalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziComposeOptions
import com.github.takahirom.roborazzi.RoborazziComposePreviewTestCategory
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.composeTestRule
import com.github.takahirom.roborazzi.previewDevice
import com.kotlinfoundation.koko.data.source.featureflag.FeatureFlagManager
import com.kotlinfoundation.koko.data.source.featureflag.NoImplFeatureFlagManager
import com.kotlinfoundation.koko.util.AppUtil
import com.kotlinfoundation.koko.util.StoreScreenshot
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.getAnnotation

/**
 * Regression screenshot tests — snapshots every `@Preview` under [PACKAGE_ROOT]
 * EXCEPT those tagged with [StoreScreenshot] (those are storefront assets handled
 * by [StoreScreenshotGeneratorTest]).
 *
 * - Local refresh: `./gradlew :shared:recordRoborazziAndroidHostTest`.
 * - PR check: `./gradlew :shared:verifyRoborazziAndroidHostTest`.
 *
 * Goldens land under `shared/src/androidHostTest/snapshots/`. The `@Preview` import
 * MUST be `androidx.compose.ui.tooling.preview.Preview` — the deprecated JetBrains
 * one is not discovered.
 */
@OptIn(InternalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = android.app.Application::class, manifest = Config.NONE, sdk = [34])
class PreviewScreenshotTest(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {
    @get:Rule
    val composeRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> =
        createAndroidComposeRule()

    @Category(RoborazziComposePreviewTestCategory::class)
    @Test
    fun snapshot() {
        @Suppress("UNCHECKED_CAST")
        val ruleForOptions =
            composeRule as
                AndroidComposeTestRule<ActivityScenarioRule<out ComponentActivity>, *>

        // Pair `previewDevice` with `composeTestRule` so Roborazzi sizes the activity window before
        // rendering. Without a device spec Robolectric falls back to its legacy default screen
        // (~320x470dp — a 2010-era mdpi phone), which renders every screen cramped and clips
        // anything taller off the captured bitmap. A `@Preview(device = ...)` alone does NOT fix
        // that: Robolectric never reads the annotation unless it's applied here.
        val deviceSpec = preview.previewInfo.device.takeIf { it.isNotBlank() } ?: DEFAULT_DEVICE_SPEC
        val composeOptions =
            RoborazziComposeOptions {
                composeTestRule(ruleForOptions)
                previewDevice(deviceSpec)
            }
        preview.captureRoboImage(
            filePath = "src/androidHostTest/snapshots/${preview.declaringClass}_${preview.methodName}.png",
            roborazziComposeOptions = composeOptions,
        )
    }

    companion object {
        private const val PACKAGE_ROOT = "com.kotlinfoundation.koko"
        private const val GENERATE_STOREFRONT_FLAG = "generateStoreScreenshots"

        /**
         * Screen size used when a `@Preview` doesn't name a device — a typical modern phone
         * (roughly Pixel 5). Set `@Preview(device = "spec:width=...")` on an individual preview to
         * render it at a different size, e.g. a tablet.
         */
        private const val DEFAULT_DEVICE_SPEC = "spec:width=411dp,height=891dp,dpi=420"

        /**
         * A few composables reach into Koin via `koinInject<T>()` rather than taking the dependency
         * as a parameter — the AdMob banner rendered by `HomeScreen` injects [FeatureFlagManager],
         * and `HelpAndSupportScreen` injects [AppUtil]. Nothing else in the test suite starts Koin,
         * so without this those previews fail with `KoinApplication has not been started`.
         *
         * Start one minimal container for the class with preview-safe stand-ins, and stop it after —
         * the lifecycle Koin's testing docs ask for. Deliberately NOT `KoinTestRule`: that starts and
         * stops per test method, which for a parameterized preview run means restarting a two-binding
         * container ~50 times for no benefit. Reach for `koin-test` if a future test needs
         * `declareMock` / `KoinTest.inject()`.
         *
         * Bind only what composables actually inject — this is not the app graph.
         */
        @BeforeClass
        @JvmStatic
        fun startKoinForPreviews() {
            if (GlobalContext.getOrNull() != null) return
            startKoin {
                modules(
                    module {
                        single<FeatureFlagManager> { NoImplFeatureFlagManager }
                        single<AppUtil> { PreviewAppUtil }
                    },
                )
            }
        }

        @AfterClass
        @JvmStatic
        fun stopKoinAfterPreviews() {
            if (GlobalContext.getOrNull() != null) stopKoin()
        }

        private object PreviewAppUtil : AppUtil {
            override fun getAppName(): String = "Koko"

            override fun shareApp() = Unit

            override fun openFeedbackMail() = Unit

            override fun getAppVersionInfo(): String = "1.0.0"
        }

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun previews(): List<ComposablePreview<AndroidPreviewInfo>> {
            val isRoborazziTask =
                System.getProperty("roborazzi.test.record") == "true" ||
                    System.getProperty("roborazzi.test.verify") == "true" ||
                    System.getProperty("roborazzi.test.compare") == "true"
            if (!isRoborazziTask) return emptyList()

            // When the storefront generator runs (`./scripts/generate_store_screenshots.sh`),
            // skip the regression set so only @StoreScreenshot-tagged previews render.
            if (System.getProperty(GENERATE_STOREFRONT_FLAG) == "true") return emptyList()

            return AndroidComposablePreviewScanner()
                .scanPackageTrees(PACKAGE_ROOT)
                .includePrivatePreviews()
                .includeAnnotationInfoForAllOf(StoreScreenshot::class.java)
                .getPreviews()
                .filter { it.getAnnotation<StoreScreenshot>() == null }
        }
    }
}
