---
name: verify-ui
description: Verify a screen or component actually works — behaviour via headless Compose UI tests, and appearance by rendering it to a PNG and looking at it. Use after building or changing any UI, or when asked to check/verify/confirm a screen renders and behaves correctly.
---

# Verify UI — fast and accurate

Two tools, two questions. Use both when you changed how something **looks**; the first alone is
enough when you only changed **behaviour**.

| Question | Tool | Cost |
|---|---|---|
| Does it *behave*? (state renders, clicks emit events) | `runComposeUiTest` | **~2s warm** |
| Does it *look* right? (layout, spacing, theme) | Roborazzi → PNG you **read** | ~25s |

Do **not** try to verify through the Web/wasm build. Compose renders to a `<canvas>`, so browser
automation sees no DOM text and the app takes ~25s to paint before anything is queryable. It is
strictly slower and less accurate than both tools above. All commands run from `MobileApp/`.

## 1. Behaviour — `runComposeUiTest`

Every screen has a **pure overload** taking `uiState` + `onUiEvent` instead of a ViewModel, so tests
render it with no Koin, no ViewModel, no device. Queries run against Compose's semantics tree — the
same data a screen reader uses — so you match real text, not pixels.

Copy the shape in
[`SampleComposeUiTest`](../../MobileApp/shared/src/jvmTest/kotlin/com/kotlinfoundation/koko/example/SampleComposeUiTest.kt):

```kotlin
@OptIn(ExperimentalTestApi::class)
class MyScreenTest {
    @Test
    fun `renders state and emits the event`() = runComposeUiTest {
        val events = mutableListOf<MyUiEvent>()
        setContent {
            AppTheme { MyScreen(uiState = MyUiState(count = 42), onUiEvent = { events += it }) }
        }

        onNodeWithText("42").assertExists()      // real rendered text
        onNodeWithText("42").performClick()
        assertTrue(events.any { it is MyUiEvent.OnClick })
    }
}
```

- Tests live in `shared/src/commonTest/kotlin/…` (runs on JVM **and** Android host) or
  `shared/src/jvmTest/kotlin/…` (JVM only).
- Run: `./gradlew :shared:jvmTest` — or one class:
  `./gradlew :shared:jvmTest --tests '*MyScreenTest*'`.
- Assert on **text/semantics**, never coordinates.

> If a screen has no pure overload, add one (ViewModel overload collects state and delegates to it) —
> that is the pattern the whole codebase uses, and it is what makes the screen testable at all.

## 2. Appearance — Roborazzi, then **look at the PNG**

**Any `@Preview` is automatically a screenshot test** — no test code to write. Add the preview, record,
then *read the image*:

```bash
./gradlew :shared:recordRoborazziAndroidHostTest        # renders every @Preview
```

PNGs land in `shared/src/androidHostTest/snapshots/<Class>_<method>.png` (gitignored — goldens are
local). **Open the PNG with your image-reading tool and confirm it looks right.** This is the step
that catches what semantics cannot: overlap, clipping, wrong spacing, unreadable contrast.

```kotlin
@Preview
@Composable
private fun MyScreenPreview() {
    AppTheme { MyScreen(uiState = MyUiState(count = 42), onUiEvent = {}) }
}
```

To catch regressions against previously recorded goldens:

```bash
./gradlew :shared:verifyRoborazziAndroidHostTest        # fails on visual diff
```

Rules:
- Import **`androidx.compose.ui.tooling.preview.Preview`**. The JetBrains one is not discovered.
- `@StoreScreenshot`-tagged previews are storefront assets and are excluded here — see
  `store-screenshots`.
- Previews are scanned under package `com.kotlinfoundation.koko`.

## 3. Finish

Run the **`run-quality-gates`** skill (`spotlessApply`/`spotlessCheck`, tests, Android debug build)
before declaring the change done.

## Notes

- Tests run on a **JDK 21** JVM while the code compiles against 17 (`shared/build.gradle.kts`).
  Robolectric loads real dependency bytecode and `filekit` ≥ 0.14 ships Java 21 class files; on a 17
  test JVM the preview scanner dies with `UnsupportedClassVersionError` and records nothing. Gradle
  provisions the JDK automatically via the foojay resolver — don't "fix" this by lowering it back.
- Screenshot tests are **not** a PR gate (goldens aren't committed), so run them locally when
  appearance matters.
