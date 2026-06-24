---
name: run-quality-gates
description: Run the same lint, test, screenshot, and build checks as CI. Use before declaring any code change done, before commits/PRs, or when the user asks to validate/verify changes.
---

# Run quality gates

All commands run from `MobileApp/`. Same gates as `.github/workflows/pr_checks.yml`, in order:

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

- Do NOT run iOS builds/tests for routine validation — they are slow. Only when the change is iOS-specific or the user asks.
- Web check when web code changed: `./gradlew :shared:compileKotlinWasmJs :shared:compileKotlinJs`.
- Quick iOS-code compile check without a full build: `./gradlew :shared:compileAppleMainKotlinMetadata`.
