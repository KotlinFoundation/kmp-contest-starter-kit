@file:OptIn(
    com.github.takahirom.roborazzi.ExperimentalRoborazziApi::class,
    com.github.takahirom.roborazzi.InternalRoborazziApi::class,
)

package com.kotlinfoundation.koko.screenshot

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.takahirom.roborazzi.RoborazziComposeOptions
import com.github.takahirom.roborazzi.RoborazziComposePreviewTestCategory
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.composeTestRule
import com.github.takahirom.roborazzi.previewDevice
import com.kotlinfoundation.koko.util.StoreScreenshot
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.getAnnotation
import java.util.Locale

/**
 * Renders every `@Preview @StoreScreenshot` composable at storefront pixel dimensions
 * and writes the captured PNG under `distribution/store_screenshots/<locale>/<device>/`.
 *
 * Gated behind the `-PgenerateStoreScreenshots=true` Gradle property — without it the
 * parameter list is empty so the test is a no-op on regular CI runs. Invoke via
 * `./scripts/generate_store_screenshots.sh`, which sets the flag and runs Roborazzi
 * in record mode.
 *
 * Add a new screenshot by writing a `@Preview @StoreScreenshot @Composable` function
 * anywhere under package name (typically next to the screen it previews).
 * The captured PNG is the screen content as rendered — no marketing chrome added.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = android.app.Application::class, manifest = Config.NONE, sdk = [34])
class StoreScreenshotGeneratorTest(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Category(RoborazziComposePreviewTestCategory::class)
    @Test
    fun render() {
        val annotation = requireNotNull(preview.getAnnotation<StoreScreenshot>())

        Locale.setDefault(Locale.forLanguageTag(annotation.locale))

        val name =
            buildString {
                if (annotation.tag.isNotBlank()) {
                    append(annotation.tag)
                    append('_')
                }
                append(preview.methodName)
            }
        val filePath =
            "../distribution/store_screenshots/${annotation.locale}/${annotation.device.name.lowercase()}/$name.png"

        // Pair `previewDevice` with `composeTestRule` so Roborazzi resizes the rule's
        // activity surface to the storefront dimensions before rendering — without
        // this pairing the activity window stays at its default ~320×470dp and
        // anything bigger gets clipped out of the captured bitmap.
        @Suppress("UNCHECKED_CAST")
        val ruleForOptions =
            composeRule as
                AndroidComposeTestRule<ActivityScenarioRule<out ComponentActivity>, *>
        val composeOptions =
            RoborazziComposeOptions {
                composeTestRule(ruleForOptions)
                previewDevice(
                    "spec:width=${annotation.device.widthPx}px," +
                        "height=${annotation.device.heightPx}px," +
                        "dpi=${annotation.device.dpi}",
                )
            }
        preview.captureRoboImage(
            filePath = filePath,
            roborazziComposeOptions = composeOptions,
        )
    }

    companion object {
        private const val PACKAGE_ROOT = "com.kotlinfoundation.koko"
        private const val GENERATE_FLAG = "generateStoreScreenshots"

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun previews(): List<ComposablePreview<AndroidPreviewInfo>> {
            // No-op unless explicitly invoked via `./scripts/generate_store_screenshots.sh`
            // (which sets `-PgenerateStoreScreenshots=true`, propagated to this JVM as a
            // system property by `tasks.withType<Test>().configureEach { systemProperty(...) }`
            // in `shared/build.gradle.kts`).
            if (System.getProperty(GENERATE_FLAG) != "true") return emptyList()

            return AndroidComposablePreviewScanner()
                .scanPackageTrees(PACKAGE_ROOT)
                .includePrivatePreviews()
                .includeAnnotationInfoForAllOf(StoreScreenshot::class.java)
                .getPreviews()
                .filter { it.getAnnotation<StoreScreenshot>() != null }
        }
    }
}
