package com.kotlinfoundation.koko.util

/**
 * Marks a `@Preview`-annotated composable as a storefront asset.
 *
 * The screenshot pipeline `StoreScreenshotGeneratorTest` (in `androidHostTest`)
 * filters for previews carrying this annotation, configures the surrounding canvas to
 * the device's exact pixel dimensions, and writes the captured PNG to
 * `distribution/store_screenshots/<locale>/<device>/`. The captured image is the
 * screen content as rendered — no marketing chrome, no headlines — so the preview
 * body should look exactly like what you want uploaded to the store.
 *
 * Example:
 * ```
 * @androidx.compose.ui.tooling.preview.Preview
 * @StoreScreenshot(device = StoreDevice.IPHONE_6_5, locale = "en", tag = "01-home")
 * @Composable
 * private fun HomeStoreScreenshot_iPhone_en() {
 *     AppTheme {
 *         HomeScreen(uiState = HomeUiState.Stub)
 *     }
 * }
 * ```
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class StoreScreenshot(
    val device: StoreDevice = StoreDevice.IPHONE_6_5,
    val locale: String = "en",
    val tag: String = "",
)

/**
 * Storefront device dimensions and density.
 *
 * `dpi` matters: it sets the density Compose uses to translate dp → px. With the
 * default mdpi (160), every dp becomes one pixel, so a 1320 px canvas is treated
 * as a tablet-wide 1320 dp surface and components render zoomed-out. Matching the
 * real device's dpi keeps text and components at the same visual scale your app
 * shows on the actual phone (e.g. iPhone 16 Pro Max ≈ 460 dpi → 1320 px / 2.875 ≈
 * 459 dp wide, which lines up with the real ~440 dp iPhone width).
 *
 * App Store and Play Store accept multiple display sizes; these constants match
 * the required ones at the time of writing. Add new entries as specs change.
 */
enum class StoreDevice(
    val widthPx: Int,
    val heightPx: Int,
    val dpi: Int,
) {
    // Apple App Store
    IPHONE_6_5(widthPx = 1284, heightPx = 2778, dpi = 458), // iPhone 14 Pro Max
    IPHONE_6_9(widthPx = 1320, heightPx = 2868, dpi = 460), // iPhone 16 Pro Max
    IPAD_13(widthPx = 2064, heightPx = 2752, dpi = 264), // iPad Pro 13"

    // Google Play Store
    PIXEL_PHONE(widthPx = 1080, heightPx = 1920, dpi = 480), // xxhdpi phones (Pixel 6/7)
    ANDROID_TABLET_10(widthPx = 1920, heightPx = 1200, dpi = 240), // hdpi 10" tablet
}
