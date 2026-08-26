---
name: generate-app-icons
description: Generate the iOS AppIcon.appiconset and Android adaptive launcher mipmaps from one source logo. Use when the user wants to set/replace the app icon, launcher icon, or app store icon before publishing.
---

# Generate app icons

Produce all platform icon assets from **one square source logo** (PNG, at least
1024×1024, no transparency for the App Store 1024 marketing icon). This is part of
the **publishing** phase — do it once per rebrand.

## Source logo

Ask the user for a single square PNG. Everything below is derived from it. If they
only have an SVG, rasterize it to a 1024×1024 PNG first.

## Android — adaptive launcher (`androidApp/src/main/res/`)

Android uses an **adaptive icon**: a foreground layer + a background color, composited
and masked by the launcher. The starter kit already has the wiring — you replace the
raster foregrounds and the background color:

```
androidApp/src/main/res/
├── mipmap-mdpi/     ic_launcher.webp, ic_launcher_foreground.webp, ic_launcher_round.webp
├── mipmap-hdpi/     …same three…
├── mipmap-xhdpi/    …
├── mipmap-xxhdpi/   …
├── mipmap-xxxhdpi/  …
├── mipmap-anydpi-v26/  ic_launcher.xml, ic_launcher_round.xml   (adaptive descriptors — leave as-is)
├── drawable/        ic_launcher_monochrome.xml   (themed-icon layer — redraw per rebrand)
└── values/colors.xml   → <color name="ic_launcher_background">…</color>
```

Foreground `ic_launcher_foreground.webp` sizes per density bucket (foreground is drawn
into a 108dp × 108dp canvas; only the centre 72dp × 72dp "safe zone" is guaranteed
visible after masking, so keep the logo well inside):

| Density bucket | Foreground px |
|----------------|---------------|
| mdpi (1×)      | 108 × 108     |
| hdpi (1.5×)    | 162 × 162     |
| xhdpi (2×)     | 216 × 216     |
| xxhdpi (3×)    | 324 × 324     |
| xxxhdpi (4×)   | 432 × 432     |

The legacy `ic_launcher.webp` / `ic_launcher_round.webp` fallbacks are 48/72/96/144/192
px for the same buckets.

**Easiest path (recommended):** Android Studio → right-click `androidApp/src/main/res/`
→ **New > Image Asset** → *Launcher Icons (Adaptive and Legacy)* → pick the source PNG →
Finish. It regenerates every density + WebP conversion automatically. Then set the
adaptive background in `androidApp/src/main/res/values/colors.xml`
(`ic_launcher_background`).

The adaptive-icon descriptors in `mipmap-anydpi-v26/` and the `<color>` names are already
correct — only the pixels and the background color change.

**Themed icons (Android 13+).** Each descriptor also declares a `<monochrome>` layer
pointing at `drawable/ic_launcher_monochrome.xml`. That is a single-path vector of the
logo silhouette — the system ignores its color and tints it to the user's wallpaper
palette, so it must be one flat shape with no gradient. Redraw it when the logo changes;
Android Studio's Image Asset wizard can emit it from the same source under the
*Monochrome* layer tab. Dropping the layer costs you the themed icon on Android 13+ and
trips the `MonochromeLauncherIcon` lint check.

## iOS — `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/`

The appiconset ships a full flat file set (`Contents.json` maps each filename to a
`size`/`scale`/`idiom`). Regenerate the whole set from the 1024 source and drop the
files in, keeping the **same filenames** so `Contents.json` still resolves. Required
sizes (rendered px = size × scale):

| Purpose                | pt    | scales      | px files                 |
|------------------------|-------|-------------|--------------------------|
| iPhone notification    | 20    | 2×, 3×      | 40, 60                   |
| iPhone settings        | 29    | 2×, 3×      | 58, 87                   |
| iPhone spotlight       | 40    | 2×, 3×      | 80, 120                  |
| iPhone app             | 60    | 2×, 3×      | 120, 180                 |
| iPad notification      | 20    | 1×, 2×      | 20, 40                   |
| iPad settings          | 29    | 1×, 2×      | 29, 58                   |
| iPad spotlight         | 40    | 1×, 2×      | 40, 80                   |
| iPad app               | 76    | 1×, 2×      | 76, 152                  |
| iPad Pro app           | 83.5  | 2×          | 167                      |
| App Store marketing    | 1024  | 1×          | 1024 (no alpha)          |

**Easiest path:** upload the 1024 PNG to a generator like
[appicon.co](https://www.appicon.co/) (select iPhone + iPad), download the produced
`AppIcon.appiconset`, and replace the folder contents in
`iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/` (keep the generated `Contents.json`).

## Splash logo (separate from the launcher icon)

Publishing branding also touches the native splash screens (see `AGENTS.md` → *Splash
Screen*). The splash uses the **brand logo shown uncut**, not the masked launcher icon:

- iOS splash logo → `iosApp/iosApp/Assets.xcassets/ic_logo.imageset/` — **three files**,
  `ic_logo.png` (1x), `ic_logo@2x.png`, `ic_logo@3x.png`. Splash background →
  `SplashBackground.colorset` (keep equal to the app window background
  `windowBackgroundColor`, currently `#F0EDE5`, so the hand-off has no flash).

  **Size matters here, and getting it wrong is the usual mistake.** A launch image is drawn
  centred at its **point** size, never scaled to fit the screen. The imageset ships at
  **120 pt** (120 / 240 / 360 px), which is roughly a third of the width of a 390 pt iPhone.
  Dropping a 512 px logo into the 1x slot makes it 512 pt — wider than any iPhone — so it
  overflows and is clipped. Do not copy the full-size source logo (or
  `designsystem/.../ic_logo.webp`) straight in.

  Regenerate all three from the source, keeping transparency so the logo sits on the splash
  background in both light and dark mode:

  ```bash
  cd iosApp/iosApp/Assets.xcassets/ic_logo.imageset
  # from an SVG:
  rsvg-convert -w 120 -h 120 -o ic_logo.png    logo.svg
  rsvg-convert -w 240 -h 240 -o ic_logo@2x.png logo.svg
  rsvg-convert -w 360 -h 360 -o ic_logo@3x.png logo.svg
  # or from a large PNG:
  sips -Z 120 logo.png --out ic_logo.png
  sips -Z 240 logo.png --out ic_logo@2x.png
  sips -Z 360 logo.png --out ic_logo@3x.png
  ```

  Leave `Contents.json` alone — it maps the three filenames to the `1x` / `2x` / `3x` slots,
  which is what keeps pixels from being read as points.
- Android splash background → `windowBackgroundColor` in
  `androidApp/src/main/res/values/colors.xml` (the native splash icon is the masked
  adaptive launcher icon; the uncut brand logo is drawn by the Compose onboarding
  `LogoImage`).

## Validate

- Android: `./gradlew :androidApp:assembleDebug` (from `MobileApp/`), install, confirm the
  launcher icon renders crisp and uncropped in the app drawer.
- iOS: open the workspace in Xcode, check the target's **General → App Icons** shows a full
  set with no missing-slot warnings.
- iOS splash: confirm `ic_logo.png` is around 120 px, not the full-size source
  (`magick identify iosApp/iosApp/Assets.xcassets/ic_logo.imageset/*.png`), then launch and
  check the logo sits centred at a sane size instead of filling or overflowing the screen.
