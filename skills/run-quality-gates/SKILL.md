---
name: run-quality-gates
description: Run the same lint, test, screenshot, and build checks as CI. Use before declaring any code change done, before commits/PRs, or when the user asks to validate/verify changes.
---

# Run quality gates

All commands run from `MobileApp/`. Same gates as `.github/workflows/pr_checks.yml`, in order.

## 0. Environment keys (agent-visible signal)

The build defaults missing keys to `"testValue"`/empty and stays **green**, so a missing cloud key
never fails a gate. Run this first so you can SEE what's still a placeholder:

```bash
# Pass the current phase; omit --phase to check every phase.
./scripts/check_env.sh --phase <getting-started|integrations|publishing|monetization|growth>
```

It's non-breaking (exit 0). If it prints any `⚠️` line for a key **required by the current phase**,
**STOP and ask the developer** to supply that key before declaring the gates green — the compile
gates below will pass regardless.

```bash
# 1. Formatting / lint (auto-fix first, then verify)
./gradlew spotlessApply
./gradlew spotlessCheck

# 2. Unit + UI tests (commonTest runs in both suites)
./gradlew :shared:jvmTest :shared:testAndroidHostTest

# 3. Android debug build (also compiles :shared transitively)
./gradlew :androidApp:assembleDebug
```

Rules:

- **Changed UI?** These gates prove it compiles and behaves — they do **not** prove it *looks* right (screenshot tests aren't a PR gate). Use the **`verify-ui`** skill to render the screen to a PNG and check it.
- Do NOT run iOS builds/tests for routine validation — they are slow. Only when the change is iOS-specific or the user asks.
- Web check when web code changed: `./gradlew :shared:compileKotlinWasmJs :shared:compileKotlinJs`.
- Quick iOS-code compile check without a full build: `./gradlew :shared:compileAppleMainKotlinMetadata`.

## Do NOT (this is what causes a build loop)

The three scoped tasks above ARE the whole validation. Improvising around them is what spirals.

- **Never run `check`, `build`, or `clean build`.** They aggregate **every** target — including iOS — so an unrelated iOS cache/link failure fails the whole run and reads as "broken", tempting a retry. They're also far slower. Use the scoped gates, nothing wider.
- **Don't launch the app to "verify" a code change.** `:androidApp:assembleDebug` compiling green IS the Android gate. Do **not** auto-install + launch via `adb` and poll for the process — the launcher Activity is `.AppActivity` (Application class `.AndroidApp`), but detecting it via adb is fragile and is exactly what loops. To watch it render, use **`verify-ui`** (headless PNG) or ask the developer to hit Run in the IDE.
- **Run tasks never return** (`:desktopApp:run`, `:webApp:wasmJsBrowserDevelopmentRun`, `installDebug`+launch). A command that hasn't exited is **running, not hung** — start it once (in the background if you need the shell) and move on; do not kill and re-run.
- **A failed or slow command is not a retry signal.** Read the error → fix it, or STOP and report. Never re-run the same command hoping for a different result. The **first** build is slow (downloads JBR + Compose) — expected, not a hang.

---

*Cross-phase — every guide's validation steps call this skill.*
