---
name: save-preferences
description: Store a small typed flag/setting locally via the UserPreferences (DataStore) API. Use when the user wants to persist a simple key/value preference — a toggle, a counter, "onboarding shown", theme mode, etc. (structured data belongs in Room via new-local-model).
---

# Save a preference (DataStore)

`UserPreferences` is a `suspend` key/value API backed by DataStore Preferences — file-based on
Android/iOS/JVM, `localStorage` on web. Use it for small primitive flags and counters only. Structured
data goes to Room (use the `new-local-model` skill).

Files live in `shared/src/commonMain/kotlin/com/kotlinfoundation/koko/data/source/preferences/`:
- `UserPreferences.kt` — the interface + `Keys` companion (add your key here).
- `UserPreferencesImpl.kt` — DataStore implementation (usually no edits needed — string/int/long/boolean are all supported).
- `PreferencesDataStoreProvider.kt` — platform storage location.

## 1. Add a key

In `UserPreferences.kt`, add a constant to the `Keys` companion:

```kotlin
companion object Keys {
    const val KEY_IS_ONBOARD_SHOWN = "KEY_IS_ONBOARD_SHOWN"
    const val KEY_DARK_MODE = "KEY_DARK_MODE"   // your new key
}
```

## 2. Read / write it (from a coroutine)

```kotlin
userPreferences.putBoolean(UserPreferences.Keys.KEY_DARK_MODE, true)
val dark = userPreferences.getBoolean(UserPreferences.Keys.KEY_DARK_MODE, defaultValue = false)
```

Available getters/setters: `getString/putString`, `getInt/putInt`, `getLong/putLong`,
`getBoolean/putBoolean`, plus `remove(key)` and `clear()`. All are `suspend` — call from a
`ViewModel`/repository coroutine scope.

## Injecting it

`UserPreferences` is already a Koin singleton (bound in `root/Di.kt` `dataModule`) — inject it into your
ViewModel or repository constructor and Koin provides it. Note the `DataStore<Preferences>` instance
itself is deliberately **not** a Koin binding (generic types collide); `UserPreferencesImpl` gets it via
`PreferencesDataStoreProvider`. You don't touch that wiring — just inject `UserPreferences`.

Validate with the `run-quality-gates` skill.
