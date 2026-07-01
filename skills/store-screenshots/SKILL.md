---
name: store-screenshots
description: Generate App Store / Play Store screenshots at storefront pixel sizes from @StoreScreenshot previews. Use when the user asks for store screenshots, marketing screenshots, or screenshots for app submission.
---

# Generate store screenshots

Run from `MobileApp/`:

```bash
./scripts/generate_store_screenshots.sh
```

Output lands at `distribution/store_screenshots/<locale>/<device>/*.png` — pure screen captures at storefront pixel dimensions, upload-ready. No Fastlane / ImageMagick / Ruby needed.

## Adding a new storefront screenshot

Drop a `@Preview @StoreScreenshot @Composable` function next to the screen it previews (`HomeScreen.kt`, etc.). The body should call the screen the same way the running app does — wrapped in `AppTheme`:

```kotlin
@Preview
@StoreScreenshot(locale = "en", tag = "01-home")
@Composable
private fun HomeStoreScreenshot_iPhone_en() {
    AppTheme {
        HomeScreen(uiState = HomeUiState(creditBalance = 12), onUiEvent = {})
    }
}
```

Rules:

- Leave `device =` unset unless the user explicitly asks for a specific device (the default is `StoreDevice.IPHONE_6_5`).
- `@StoreScreenshot` previews are excluded from regression screenshot tests and only render when the script sets `-PgenerateStoreScreenshots=true` — don't try to run them via the normal test tasks.
- The annotation + `StoreDevice` enum live at `shared/src/commonMain/.../util/StoreScreenshot.kt`.

---

*Phase 3 · Publication — part of the [publishing](../publishing/SKILL.md) guide.*
