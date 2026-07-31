---
name: new-screen
description: Scaffold a new app screen (Screen + UiState + ViewModel) and wire it into navigation and DI. Use whenever the user asks for a new screen, page, or view in the mobile app — never hand-create these files.
---

# Add a new screen

Run from `MobileApp/`:

```bash
./scripts/generate_screen.sh YourScreenName
```

The script is idempotent (safe to re-run). It generates
`shared/src/commonMain/kotlin/com/kotlinfoundation/koko/presentation/screens/yourscreenname/`:

- `YourScreenNameScreen.kt` — dual-overload pattern (ViewModel entry point + pure composable for previews/tests)
- `YourScreenNameUiState.kt` — UiState data class + UiEvent sealed interface
- `YourScreenNameViewModel.kt` — the screen's ViewModel (extends `androidx.lifecycle.ViewModel`)

and wires everything end-to-end:

- Route inserted into `presentation/navigation/Routes.kt`
- `entry<YourScreenNameScreenRoute> { … }` stub + imports in `presentation/navigation/AppNavigation.kt`
- `viewModelOf(::YourScreenNameViewModel)` + import in `root/Di.kt`

Insertion points in those three files are marked with
`// Add new … below — generate_screen.sh inserts here.` comments — never remove or move those markers.

## After running

1. Edit the generated `entry<>` block in `AppNavigation.kt` to add the navigation callbacks the screen needs (`navigator.navigate(...)`, `navigator.goBack()`, ...).
2. Implement the UI in the pure-composable overload; keep logic in the ViewModel.
3. Feature folders contain **only** `*Screen.kt`, `*UiState.kt`, `*ViewModel.kt` — never a `*ScreenRoute.kt` (routes live in `Routes.kt`).
4. Verify it with the **`verify-ui`** skill — a headless Compose test for behaviour (the generated dual-overload makes this possible), plus a `@Preview` rendered to a PNG you can actually look at.
5. Validate with the `run-quality-gates` skill.

---

*Phase 1 · First Run building block — part of the [getting-started](../getting-started/SKILL.md) guide.*
