---
name: capture-app-screens
description: Render the app's own screens to plain PNGs at storefront pixel sizes (1284×2778 etc.) from @Preview @StoreScreenshot composables — the real UI and nothing else, no headlines, no device frames, no styled backgrounds. Use when the developer wants captures of their actual screens, plain store-listing images ready to upload, or the base artwork a designed marketing image gets built on top of. NOT for designed marketing screenshots with marketing copy, brand panels, or phone mockups — this skill has no design step and cannot produce those. Also NOT for a quick visual check of a screen during development — use verify-ui for that.
---

# Capture app screens at storefront sizes

Renders your real Compose screens to PNGs sized for the App Store / Play Store. What comes out is the
UI itself and nothing else — no marketing copy, no gradient background, no phone bezel.

## Is this the right skill?

| The developer wants | Answer |
|---|---|
| PNGs of the real screens, at store pixel sizes | **this skill** |
| Just to *see* how a screen looks while building | `verify-ui` — same renderer, normal size, no storefront pixel math |
| Headlines, brand panel, device frames, styled backgrounds | **not this skill** — see below |

**"Make my App Store screenshots" is ambiguous. Ask which they mean before running anything.**

Plain captures of the real UI are legitimate store assets and plenty of shipped apps use exactly
that. But many developers picture the polished marketing images — a headline above a tilted phone on
a colored background. This kit has no design step and cannot produce those; it only produces the
screen artwork that such an image would be composed from. If that is what they want, say so plainly
and let them bring their own design tool, then hand it these PNGs as the input.

## Run it

From `MobileApp/`:

```bash
./scripts/generate_store_screenshots.sh
```

Output lands at `distribution/store_screenshots/<locale>/<device>/*.png`. No Fastlane / ImageMagick /
Ruby needed.

## Adding a screen to the set

Drop a `@Preview @StoreScreenshot @Composable` function next to the screen it captures
(`HomeScreen.kt`, etc.). The body should call the screen the same way the running app does, wrapped in
`AppTheme`:

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

- Leave `device =` unset unless the developer asks for a specific one (default `StoreDevice.IPHONE_6_5`).
- `@StoreScreenshot` previews are excluded from regression screenshot tests and only render when the
  script passes `-PgenerateStoreScreenshots=true` — they won't appear via the normal test tasks.
- The annotation and the `StoreDevice` enum live at `shared/src/commonMain/.../util/StoreScreenshot.kt`.
- The annotation, script, and output directory keep the `store_screenshots` naming; only this skill is
  named for what it produces.

---

*Phase 3 · Publication — part of the [publishing](../publishing/SKILL.md) guide.*
