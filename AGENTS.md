# Koko — KMPStarterKit

> **Koko** is the app name; the project is the KMP contest starter kit (KMPStarterKit).

## Project Overview

Monorepo with three main parts:
- **MobileApp/** — Kotlin Compose Multiplatform mobile app (Android, iOS, JVM Desktop, Web/WASM)
- **Web/** — Firebase Hosting landing page + Cloud Functions backend (Node.js)
- **Documentation/** — Docusaurus documentation site (git submodule, published at kotlinfoundation.org/kmp-contest-starter-kit-documentation)

Tech stack: Kotlin 2.3.20, Compose Multiplatform 1.10.0, AGP 9.2.0, Gradle 9.4.1, Gradle Kotlin DSL
Package: `com.kotlinfoundation.koko`

## Repository Structure

```
MobileApp/
├── androidApp/              # Android Application entry point (com.android.application)
│   ├── src/main/AndroidManifest.xml
│   ├── src/main/kotlin/     # MainActivity (AppActivity) + Application class (AndroidApp)
│   ├── src/main/res/        # Launcher icons, app theme, colors
│   ├── google-services.json # Firebase config (Android)
│   └── build.gradle.kts     # applicationId, versionCode/Name, signing, buildTypes
├── desktopApp/              # JVM Desktop entry point (kotlin("jvm") + Compose Desktop)
│   ├── src/main/kotlin/     # main.kt — application { Window { App() } }
│   └── build.gradle.kts     # compose.desktop.application + nativeDistributions (DMG/MSI/DEB)
├── webApp/                  # Wasm/JS browser entry point (KMP wasmJs + Compose for Web)
│   ├── src/wasmJsMain/kotlin/   # main.kt — ComposeViewport { App() }
│   ├── src/wasmJsMain/resources/ # index.html, styles.css
│   └── build.gradle.kts     # wasmJs { browser() } + webpack/devServer config
├── shared/              # Shared KMP library (com.android.kotlin.multiplatform.library)
│   ├── src/commonMain/      # Shared business logic & UI (Kotlin/Compose), exposes App() + AppInitializer
│   │   └── kotlin/.../
│   │       ├── data/source/local/   # Room 3 @Database + @Entity + @Dao + DatabaseModule (Koin)
│   │       └── presentation/
│   │           ├── navigation/  # Routes.kt, NavigationState.kt, Navigator.kt, AppNavigation.kt
│   │           ├── screens/     # Per-feature folders: <feature>/{*Screen,*UiState,*ViewModel}.kt
│   │           ├── components/  # Shared composables
│   │           └── theme/       # Colors, typography, theme
│   ├── src/androidMain/     # Android platform impls (Firebase, AdMob, DatabaseProvider, etc.)
│   ├── src/iosMain/         # iOS DatabaseProvider + iOS-specific code
│   ├── src/webMain/         # Shared web (wasmJs+js): Platform.web.kt + DatabaseProviderImpl (WebWorkerSQLiteDriver + OPFS) + createSQLiteWorker() expect
│   ├── src/wasmJsMain/      # wasmJs createSQLiteWorker() actual (@JsFun worker factory)
│   ├── src/jsMain/          # js createSQLiteWorker() actual (js("…") worker factory)
│   ├── src/nonWebMain/      # Code shared across non-web platforms (FileManager.nonWeb, etc.)
│   ├── src/jvmMain/         # Desktop DatabaseProvider + Platform.jvm.kt etc.
│   ├── src/commonTest/      # Shared tests (JVM + Android host)
│   ├── src/jvmTest/         # JVM-only tests (e.g. Compose UI tests via runComposeUiTest)
│   ├── src/androidHostTest/ # Robolectric host tests + optional Roborazzi screenshot tooling
│   └── src/commonMain/.../util/StoreScreenshot.kt # @StoreScreenshot annotation + StoreDevice enum
├── designsystem/            # Reusable UI components library (KMP)
│   └── src/jvmMain/         # Desktop entry for component preview (Main.kt)
├── libs/
│   └── subscription/        # Subscription module
│       ├── subscription-api/        # Subscription interface contracts
│       ├── subscription-revenuecat/ # RevenueCat implementation
│       └── subscription-adapty/     # Adapty implementation
├── build-logic/             # Convention plugins for KMP library modules
├── iosApp/                  # Xcode project wrapper
├── distribution/            # Release assets (keystore, what's new, store_screenshots/)
├── scripts/                 # Build/release helper scripts
└── fastlane/                # iOS release automation

.github/                     # GitHub Actions 
├── actions/setup-gradle/    # Composite action: JDK, Gradle cache, KMP/SwiftPM caches
└── workflows/               # pr_checks.yml, publish_android_playstore.yml, publish_ios_appstore.yml, web_build.yml

Web/
├── public/                  # Firebase Hosting (static landing page)
└── functions/               # Cloud Functions (OpenAI, Replicate integrations)

Documentation/               # Docusaurus docs site (git submodule)
├── docs/                    # Markdown documentation pages
├── src/                     # Docusaurus theme/components
└── static/                  # Static assets (images, etc.)
```

### Source Set Hierarchy
```
common  (Room 3 @Database + DAOs live here — runs on all platforms)
├── mobile (Android + iOS)
├── nonWeb (Android + iOS + JVM — sqlite-bundled driver)
├── nonMobile (Web + JVM)
└── web (js + wasmJs — sqlite-web only on wasmJs)
```

### Firebase client SDKs in shared code — ask first, always

There is **no Firebase client SDK for the `wasmJs` target**. The GitLive
[firebase-kotlin-sdk](https://github.com/GitLiveApp/firebase-kotlin-sdk) covers Android, iOS, JVM and JS
but **not** Kotlin/Wasm, so putting Firestore (or any Firebase client SDK) in `commonMain` breaks the web
build. Desktop is never the blocker — the browser target is.

This applies to **any** task that puts Firebase data into shared code: Firestore, realtime database, cloud
storage, cross-device sync, syncing credits/settings/user data, server-authoritative balances.

**Stop and ask the developer which they want. Do not choose for them, do not answer with a
recommendation as if the question were settled, and never drop the web target silently.** Two options:
keep Wasm and put Firestore behind the Cloud Functions backend this kit already ships, or drop the
`wasmJs` target and use GitLive directly. Full guidance + the Wasm-safe architecture:
[`skills/setup-firebase/SKILL.md`](skills/setup-firebase/SKILL.md).

## Build & Run

### Android
```bash
./gradlew :androidApp:assembleDebug
# APK: androidApp/build/outputs/apk/debug/androidApp-debug.apk
# SHA1 for Firebase: ./gradlew :androidApp:signingReport
```

### iOS
**Do NOT run iOS builds/tests for routine validation** — they are slow. Only run when the issue explicitly requires iOS behavior or the user asks.

**iOS SwiftPM linkage (generic — any SwiftPM-backed dependency).** When the shared framework consumes a Swift Package Manager dependency (today: `kmpnotifier-push-firebase` → `firebase-ios-sdk`/FirebaseMessaging; tomorrow: any other SPM-backed lib), Kotlin 2.4 + embed-and-sign needs the committed `iosApp/KotlinMultiplatformLinkedPackage/` (a generated local Swift package that force-links the SPM products) plus `ENABLE_USER_SCRIPT_SANDBOXING = NO` on the app target. It is a **build-once, commit-once** artifact (portable — no app-specific paths), so generated apps inherit it. **Regenerate ONLY when the SwiftPM dependency/product set changes**, via `XCODEPROJ_PATH="$PWD/iosApp/iosApp.xcodeproj" GRADLE_PROJECT_PATH=":shared" ./gradlew :shared:integrateEmbedAndSign :shared:integrateLinkagePackage` (from `MobileApp/`; both env vars are required), then add the matching Swift package version in Xcode (e.g. `firebase-ios-sdk` exact `12.17.0`) and commit the diff. The per-build `embedAndSignAppleFrameworkForXcode` run-script phase is already wired and is NOT something to run manually. Full guide: `Documentation/docs/production/iOS.md`.

### JVM Desktop
- Full app: `./gradlew :desktopApp:run` (or `:desktopApp:packageDistributionForCurrentOS` for native installer)
- Design-system component preview: run `designsystem/src/jvmMain/kotlin/Main.kt` from IDE

### Web (Wasm/JS)
- Dev server: `./gradlew :webApp:wasmJsBrowserDevelopmentRun`
- Production bundle: `./gradlew :webApp:wasmJsBrowserDistribution` (output at `webApp/build/dist/wasmJs/productionExecutable/`)

## Testing & Validation

All Gradle commands run from `MobileApp/`.

### Quality gates (run on every PR via `.github/workflows/pr_checks.yml`)
1. **Lint / formatting**: `./gradlew spotlessCheck` — Spotless + ktlint. Auto-fix locally with `./gradlew spotlessApply`. Rule overrides live in the root `build.gradle.kts` (Compose `@Composable` PascalCase, wildcard imports, max-line-length all disabled).
2. **Unit + UI tests**: `./gradlew :shared:jvmTest :shared:testAndroidHostTest` — both source sets pull from `commonTest`. `jvmTest` also includes `jvmTest/`-only tests (e.g. headless `runComposeUiTest`).
3. **Android debug build**: `./gradlew :androidApp:assembleDebug` — also builds `:shared` transitively.

### Validation guardrails (avoid the build loop)
These three scoped tasks ARE the whole validation. Do not improvise around them:
- **Never run the aggregate tasks `check`, `build`, or `clean build`.** They pull in **every** target — including iOS — so an unrelated iOS cache/link failure fails the whole run, which reads as "broken" and tempts a retry loop. They are also far slower. Run only the three scoped gates above (plus the web/iOS compile checks below when relevant).
- **`assembleDebug` succeeding IS the Android validation.** Do **not** then auto-install + launch via `adb` and poll to "confirm it works" — the launcher Activity is `.AppActivity` (Application class `.AndroidApp`), but guessing/parsing it via adb is fragile and is what spirals into a loop. To see the app actually render, use the `verify-ui` skill (headless PNG) or hand off to the developer to hit Run in the IDE.
- **Run tasks are long-running and never exit**: `:desktopApp:run`, `:webApp:wasmJsBrowserDevelopmentRun`, `:androidApp:installDebug`+launch. Start once (background if you need the shell back); a task that hasn't returned is **running, not hung** — do not kill and re-run.
- **Configuration cache is on** (`org.gradle.configuration-cache=true`). Anything that must run at execution time has to be configuration-cache safe. In particular, **do not add a `commonWebpackConfig { }` block to `webApp/build.gradle.kts`** — it is a Gradle script object reference and cannot be serialized, which fails every `:webApp:wasmJs*` task. Dev-server settings (the AI CORS proxies) live in `webApp/webpack.config.d/*.js` instead.
- **A failed or slow gate is not a retry signal.** Read the error and fix it, or STOP and report to the developer. Never re-run the same command hoping it passes. First build is slow (downloads JBR + Compose) — expected, not a hang.

### Other test paths
- **Android UI/logic** (instrumented): `./gradlew :androidApp:connectedDebugAndroidTest` (device required).
- **Design system only**: just compile with build tasks; no tests yet.
- **iOS tests**: Do NOT run unless explicitly required.

### Test layout
- Unit / Flow / ViewModel tests: `shared/src/commonTest/kotlin/`. Use `kotlinx-coroutines-test` for `runTest` + `StandardTestDispatcher`/`UnconfinedTestDispatcher`. No Turbine — collect `Flow` emissions via `launch { flow.toList(emissions) }` if needed.
- Compose UI tests: `shared/src/commonTest/kotlin/` (multiplatform via `runComposeUiTest`, runs on both JVM and Android host) or `shared/src/jvmTest/kotlin/` for JVM-only ones. Screens expose a **pure `(uiState, onUiEvent)` overload** so they render with no ViewModel/Koin — see `SampleComposeUiTest` for the template, and the **`verify-ui`** skill.
- Screenshot tests (optional, local only — NOT a PR gate, goldens are not committed): **every `@Preview`** under `com.kotlinfoundation.koko` is snapshotted by the parameterized `PreviewScreenshotTest` (`shared/src/androidHostTest/`), which discovers previews via ComposablePreviewScanner. Record with `./gradlew :shared:recordRoborazziAndroidHostTest` → PNGs in `shared/src/androidHostTest/snapshots/`; compare with `./gradlew :shared:verifyRoborazziAndroidHostTest`.
- Tests run on a **JDK 21** JVM (`tasks.withType<Test>` in `shared/build.gradle.kts`) while code compiles against 17. Robolectric loads real dependency bytecode and `filekit` ≥ 0.14 ships Java 21 class files, so a 17 test JVM fails the preview scan with `UnsupportedClassVersionError`. The foojay resolver provisions the JDK automatically.

### `@Preview` annotation
Always use `androidx.compose.ui.tooling.preview.Preview`, NOT `org.jetbrains.compose.ui.tooling.preview.Preview` (the latter is deprecated as of CMP 1.10 and not discovered by ComposablePreviewScanner). The multiplatform-aware AndroidX import comes from `org.jetbrains.compose.ui:ui-tooling-preview` (already wired in `shared` and `designsystem`).

## Agent Working Style — parallelize by default

Most tasks in this repo decompose into independent pieces (screens, models, docs, skills, per-file
edits). Working through them one at a time is the main reason sessions feel slow. If your harness
supports subagents / parallel tool calls (Claude Code, Cursor, Copilot Workspace, …), **fan out
whenever the pieces are independent** — for any task, not just feature builds:

- **Reads are always safe to parallelize.** Product docs, multiple source files, several skills —
  read them concurrently, never one-by-one.
- **Fan out implementation when files are disjoint.** One subagent per screen / model / feature
  folder / doc file. The biggest case is `build-features` (see its step 3), but the same applies to
  refactors, doc sweeps, multi-screen UI passes, test authoring.
- **File-ownership rule** (what makes fan-out safe): a subagent edits **only files it exclusively
  owns** (its feature folder). Shared, single-instance files are edited **only by the orchestrating
  agent**, merging what subagents return: `Routes.kt`, `AppNavigation.kt`, `root/Di.kt`,
  `AppDatabase.kt`, `DatabaseModule.kt`, `composeResources/values/strings.xml`, gradle files.
  Two subagents writing one file concurrently = lost updates.
- **Never parallelize the scaffold scripts** (`generate_screen.sh`, `make_local.sh`,
  `refactor_package.sh`) — they patch shared files at insertion markers; concurrent runs corrupt
  them. Scaffold sequentially (they're fast), then fan out the implementation.
- **One Gradle invocation at a time, run by the orchestrator.** Subagents should not build or test;
  concurrent Gradle runs in one checkout just serialize on the daemon lock. Validate **once** after
  merging all subagent output — `spotlessApply`, the scoped gates, and a single
  `recordRoborazziAndroidHostTest` run (it snapshots every `@Preview` in one pass).
- **Long-running tasks run in the background** — `:desktopApp:run`, the wasm dev server. Don't block
  a session waiting on a task that never exits (see Validation guardrails).
- **Git worktrees** are for genuinely independent workstreams that must not interfere (a risky
  refactor alongside feature work, two large PRs in flight) — each worktree pays a full fresh Gradle
  build, so they are **not** worth it for per-screen fan-out. Default to subagents in one checkout;
  reach for a worktree when isolation matters more than build reuse.

## Architecture & Key Conventions

### Core Principles
- **MVP-first minimalism** — avoid over-engineering
- **No pass-through use cases** — call repositories directly from presentation when no domain orchestration is needed. Only introduce a use case for: aggregation of multiple repos, business rule enforcement, transaction coordination, throttling/debouncing, validation, or cross-cutting concerns
- **Concrete repositories** — no interfaces unless 2+ live implementations exist
- **Domain models are source of truth** — immutable data classes, no serialization annotations, no persistence/UI concerns

### Layer Overview
```
Presentation (screens, ViewModels, components)
    ↓ depends on
Domain (models, exceptions, use cases only when justified)
    ↓ depends on
Data (repositories, API services, local storage, preferences)
```

### Domain Layer
- Location: `shared/src/commonMain/kotlin/com/<org>/<app>/domain/`
- Models: immutable `data class` with `val` properties, sensible defaults, pure domain (no `@Serializable`)
- Allowed domain exceptions: `UnauthenticatedException`, `PurchaseRequiredException`
- Async: `suspend` functions and `Flow`/`StateFlow`
- Mapping: convert DTOs/entities → domain at data layer boundaries

### API Services
- Location: `shared/src/commonMain/kotlin/com/<org>/<app>/data/source/remote/apiservices/`
- DTOs: `data/source/remote/request/` and `data/source/remote/response/`
- Class naming: `*Request`, `*Response` suffixes required
- All DTOs: `@Serializable` + `@SerialName` annotations
- **Return raw data types, NOT Result wrappers** — let repositories handle Result wrapping
- Include `asDomain()` mapping methods on response DTOs
- Transport: Ktor Client with centralized config (JSON, logging, timeouts)
- Testing: Use Ktor `MockEngine`

```kotlin
interface JobApiService {
    suspend fun getJobs(request: GetJobsRequest): GetJobsResponse
}
```

**AI transport (proxy vs direct).** The OpenAI/Replicate services route every call through
`AiTransport` (`data/source/remote/apiservices/ai/AiTransport.kt`) instead of hitting the Ktor client
directly. It picks between two backends and adapts the response so the DTOs + `AiGenerationProvider`s +
`AiApiBaseResponse.handleAsResult` never change:
- **Proxy (production, default):** call `${AppConfiguration.CLOUD_FUNCTIONS_URL}/…` via the Firebase-interceptor
  client; the body already is the `{statusCode, errorMessage, data}` envelope.
- **Direct (prototyping):** call the provider URL (`api.openai.com` / `api.replicate.com`) via a second
  client with **no** Firebase interceptor, sending `Authorization: Bearer <OPENAI_API_KEY|REPLICATE_API_KEY>`
  (from `BuildConfig`/`local.properties`) + Replicate's `Prefer: wait`. The raw provider JSON is re-wrapped
  into a synthetic `AiApiBaseResponse`.
- **Selection:** auto — proxy only when `AppConfiguration.CLOUD_FUNCTIONS_URL` is set, otherwise direct
  (the proxy is useless without a URL, so a blank URL always falls back to a direct provider call).
  `AppConfiguration.USE_AI_PROXY_SERVER` (`Boolean?`; `true`=proxy, `false`=direct, `null`=auto) overrides.
  Prototyping only — direct-mode keys ship in the app
  binary; production keeps the proxy (keys in Secret Manager). `AiTransport` is **provider-agnostic**: each
  service supplies its own `directUrl` + headers + key-readiness, so a new provider needs no `AiTransport`
  change. Text→image is fully Firebase-free; image-editing hosts the input image via a direct upload
  (see **File hosting** below) so the provider can fetch it by URL — no Firebase.

### File hosting

When a flow needs a **public URL** for a local file (today: the reference image handed to an AI
provider), `GenerationRepository.uploadFilesIntoCloud()` uploads the bytes via
**`TemporaryFileUploadApiService`** (`data/source/remote/apiservices/`) — a plain Ktor multipart POST
to **tempfile.org**, an anonymous host that needs **no API key**, so image hosting works with zero
config on every platform. Following the API-service convention it returns the raw
`TemporaryFileUploadResponse` (`response/file/`); the repo maps it with `asDownloadUrl()` (viewer URL
+ `download`) and handles failure. The client is `HttpClientFactory.fileUpload()` (multipart-safe —
no default json content type). No storage library. To swap hosts, change the service URL + response
DTO — the only touch point.

### Repositories
- **Prefer concrete classes** — no interface unless swapping implementations at runtime
- **BackgroundExecutor** for all async operations:

```kotlin
// CORRECT — no redundant try-catch
suspend fun getJobs(page: Int, limit: Int): Result<List<Job>> =
    backgroundExecutor.execute {
        val response = jobApiService.getJobs(GetJobsRequest(page, limit))
        Result.success(response.jobs.map { it.asDomain() })
    }

// INCORRECT — BackgroundExecutor already catches exceptions, logs, returns Result.failure()
suspend fun getJobs(page: Int, limit: Int): Result<List<Job>> =
    backgroundExecutor.execute {
        try { /* ... */ } catch (e: Exception) { Result.failure(e) }  // REDUNDANT
    }
```

- Only use try-catch inside `execute{}` for: custom error handling, different exception types, or resource cleanup
- Local operations (mark as seen, save locally): can be synchronous, use Room DAOs
- Server operations: always via BackgroundExecutor

### Presentation Layer
- Location: `shared/src/commonMain/kotlin/com/<org>/<app>/presentation/`
- Organize by feature: `presentation/screens/<feature>/`
- Shared components: `presentation/components/`

**ViewModel pattern:**
```kotlin
class HomeViewModel(
    private val repository: Repository
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        repository.dataFlow,
    ) { data -> HomeUiState(/* map */) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState())

    fun onUiEvent(event: HomeUiEvent) {
        when (event) { /* handle */ }
    }
}
```

**Screen composable dual-overload pattern:**
```kotlin
// Entry point with ViewModel
@Composable
fun HomeScreen(viewModel: HomeViewModel, onNavigate: (dest) -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(uiState = uiState, onUiEvent = viewModel::onUiEvent, onNavigate = onNavigate)
}

// Pure composable for testing/previews
@Composable
fun HomeScreen(uiState: HomeUiState, onUiEvent: (HomeUiEvent) -> Unit, onNavigate: (dest) -> Unit) {
    // UI implementation
}
```

**UI State & Events:**
```kotlin
data class HomeUiState(
    val isLoading: Boolean = false,
    val items: List<ItemUiState> = emptyList(),
    val error: String? = null
)

sealed interface HomeUiEvent {
    data object Refresh : HomeUiEvent
    data class ItemClicked(val id: String) : HomeUiEvent
}
```

### Dependency Injection (Koin)
- Framework: Koin with multiplatform support
- Module definitions live in **`root/Di.kt`**: `dataModule`, `domainModule`, `presentationModule`, and `appModules` (the aggregate). The per-target `platformModule` is the `expect`/`actual` module.
- Platform modules: `androidMain/di/`, `iosMain/di/` using `expect`/`actual`
- Scoping:
  - `singleOf()` → repositories, network clients, databases, app-wide services
  - `factoryOf()` / `viewModelOf()` → ViewModels, validators, screen-scoped services
  - `bind` → only when multiple implementations exist
- Initialization: `root/AppInitializer.kt` is the bootstrap — `startKoin { … modules(appModules) … }` (loaded from `Di.kt`) plus one-time startup side effects (logging, analytics, notifications, billing, ads, anonymous sign-in). Each platform entry point calls `AppInitializer.initialize { }` once.

### App configuration
Per-app compile-time config lives in **`root/AppConfiguration.kt`** (`object AppConfiguration`) — the toggles/values a developer sets when spinning up an app: `PREMIUM_FEATURES_ENABLED`, `CLOUD_FUNCTIONS_URL`, `USE_AI_PROXY_SERVER`, `AUTH_SOCIAL_LOGIN_ENABLED`, legal URLs, `CONTACT_EMAIL`, `APPSTORE_APP_ID`, and the `subscriptionProviderFactory` / `authServiceProviderFactory` selectors. `PREMIUM_FEATURES_ENABLED` controls whether the app has any **premium (paid/gated) features** — not whether the app is free to download. `false` (default) = **no premium features**, everything unlocked/free: no paywall/subscriptions/upgrade UI, billing never initializes, credits off (generation free, credit UI hidden) — gated at `AppInitializer` (skip IAP init), `AccountViewModel` (no Subscriptions row/banner), onboarding (no Get-Premium button), `HomeScreen` (no credit chip), and `GenerationRepository` (no credit spend). `true` = premium features available (gate/limit via subscriptions + credits + paywall); flip it in the monetization phase once store products exist. Distinct from **`util/Constants.kt`** (framework detail — paywall entitlement/placement ids, `CREDIT_PACK_PRODUCT_ID_PREFIX`, DB/prefs file names) and from **`FeatureFlagManager`** (runtime, Firebase Remote Config). `scripts/check_env.sh` reads `AppConfiguration.kt` for the URLs/AI/auth values it verifies.

### Coroutines
- `backgroundScope`: `CoroutineScope(SupervisorJob() + Dispatchers.IO)` for repo/data work
- `ApplicationScope`: for jobs that outlive screens (caches, analytics, sync)
- **Inject dispatchers** for testability — never hardcode `Dispatchers.IO`
- Testing: `StandardTestDispatcher`/`UnconfinedTestDispatcher` + `TestScope`

### Utilities
- Location: `shared/src/commonMain/kotlin/com/<org>/<app>/util/`
- Organize by functional domain: `analytics/`, `extensions/`, `file/`, `logging/`, `Platform.kt`
- Singleton pattern (`object`) for stateful utilities
- Extension functions grouped by type: `StringExtensions.kt`, `FlowExtensions.kt`, `ComposeExtensions.kt`
- `expect`/`actual` for platform abstractions

### Runtime Permissions
- Backed by **Calf** (`calf-permissions`, commonMain — real dialogs on Android/iOS, granted no-ops on desktop/web).
- App-level API: `util/permissions/AppPermissionState.kt`. Never use Calf types directly in screens — go through `AppPermissionState`.
- Per-permission helpers: `rememberNotificationPermissionState()`, `rememberCameraPermissionState()`, `rememberGalleryPermissionState()`, `rememberLocationPermissionState()`, `rememberMicrophonePermissionState()`. Each returns `AppPermissionState { isGranted, shouldShowRationale, request(), openSettings() }`.
- Any other permission: `rememberAppPermissionState(Permission.X)` — same wrapper, one extra import.
- Ask-on-entry pattern: `RequestPermissionOnEntry(state)` (skips if already granted or rationale pending). Used for the notification permission in `HomeScreen`.
- iOS: camera/gallery/location/microphone need their `NS*UsageDescription` keys in `iosApp/iosApp/Info.plist`; notification permission needs none.

**Media capture (camera / gallery).** Use FileKit, not raw platform APIs:
- **Camera photo:** `FileKit.openCameraPicker()` — a suspend `expect`/`actual` in `util/file/FileManager.kt` (mobile actual takes a photo; non-mobile returns `null`, no camera). Call it from a coroutine and feed the resulting `PlatformFile?` to your UiEvent. The demo Home screen wires the `AddOrChooseFileContainer` capture button this way. iOS requires `NSCameraUsageDescription` in `Info.plist` (already added) — **missing the key hard-crashes** the app when the camera opens.
- **Gallery pick:** `rememberFilePickerLauncher(type = FileKitType.Image, …)`. On iOS this uses `PHPickerViewController` (out-of-process) — **no** photo-library usage-description key required.

### Splash Screen
Each platform uses its **native** launch screen — no library.

- **Android** (`androidx.core:core-splashscreen`): theme `Theme.App.Starting` in `androidApp/src/main/res/values/styles.xml`. Background is `@color/windowBackgroundColor` — **the same as the app window**, so the hand-off to `AppTheme` has no color flash. The icon is the **adaptive launcher icon** `@mipmap/ic_launcher`. (The system masks the splash icon to a circle; adaptive icons are built for that, so it stays crisp and uncropped — a full-bleed brand logo would get its corners cut. The native theme also can't reference Compose/`designsystem` resources like `ic_logo`, only `res/` ones.) The brand logo (`ic_logo`) is shown **uncut** by the Compose splash (the onboarding `LogoImage`). Wired via `android:theme="@style/Theme.App.Starting"` on `AppActivity` + `installSplashScreen()` (before `super.onCreate()`) in `App.android.kt`. To hold the splash while the app loads: `installSplashScreen().setKeepOnScreenCondition { !ready }`.
- **iOS** (declarative `UILaunchScreen` in `iosApp/iosApp/Info.plist`, iOS 14+ — no storyboard, no `.xcodeproj` edit). iOS can't reference the designsystem/Compose resources, so the splash logo is **one** file you replace:
  - Logo → `iosApp/iosApp/Assets.xcassets/ic_logo.imageset/ic_logo.png` (single universal image; the brand logo, asset name `ic_logo`).
  - Background → `SplashBackground.colorset` — keep it equal to the app window background (`AppTheme.colors.background` / Android `windowBackgroundColor`, currently `#F9F7FF` light and `#12101A` dark) so the transition is seamless. Give the colorset both a light (universal) and a dark (`luminosity` = `dark`) appearance so neither mode flashes.
  - The plist keys `UIColorName` / `UIImageName` reference those two asset names.

### Local Preferences (DataStore)
- `UserPreferences` interface (suspend API) in `data/source/preferences/`, backed by **DataStore Preferences** (`androidx.datastore:datastore-preferences-core`).
- Platform `PreferencesDataStoreProviderImpl`s mirror the `DatabaseProvider` pattern: file-based on Android/iOS/JVM (`PreferenceDataStoreFactory.createWithPath`), `WebLocalStorage` on js/wasmJs. Registered in each `platformModule`.
- The `DataStore<Preferences>` instance is deliberately NOT a Koin binding (generic types erase to `DataStore` — a second DataStore would collide). `UserPreferencesImpl` gets it via the provider inside its `single { }` block in `dataModule`.
- Pin DataStore to 1.3.0-alpha09+ — earlier versions have no js/wasmJs artifacts.

### Navigation
- **Jetpack Navigation 3** (KMP build: `org.jetbrains.androidx.navigation3:navigation3-ui:1.1.0`). Owns the back stack as plain state; one `NavDisplay` renders entries.
- All navigation code lives in `shared/src/commonMain/kotlin/com/<org>/<app>/presentation/navigation/`:
  - `Routes.kt` — every destination as a `@Serializable sealed interface ScreenRoute : NavKey` plus `data object`/`data class` subtypes. `TopLevelScreenRoute : ScreenRoute` marks bottom-nav tabs.
  - `NavigationState.kt` — swap-style state: `currentBackstack` plus per-tab `topLevelBackStacks` storage. Persisted with `rememberSerializable(SnapshotStateListSerializer())` (sealed hierarchy enables polymorphic save/restore).
  - `Navigator.kt` — public API: `navigate`, `add`, `set`, `replace`, `switchToTab`, `goBack`. `navigate(route)` dispatches to `switchToTab` for `TopLevelScreenRoute`s and `add` otherwise.
  - `AppNavigation.kt` — sets up `NavDisplay`, the bottom nav bar, and the `EntryProviderScope<ScreenRoute>.screens(navigator)` extension where each `entry<XScreenRoute> { … }` block calls the feature `*Screen` composable directly.
- `LocalNavigator: Navigator` composition local for navigation access from any screen.
- Top-level (tab) entries use `noAnimationMetadata` so tab switches are instant; pushes use the default forward animation.
- ViewModels are scoped per `NavEntry` via `rememberViewModelStoreNavEntryDecorator` (no separate "navigator-scoped" helper).
- Feature folders under `presentation/screens/<feature>/` contain **only** `*Screen.kt`, `*UiState.kt`, `*ViewModel.kt` — never a `*ScreenRoute.kt`.

### Agent Skills
Reusable, agent-agnostic skills live in **`skills/<name>/SKILL.md`** (open Agent Skills format; `.claude/skills` symlinks there for Claude Code). Read the matching skill before doing the task — they encode the project-specific steps (commands, file paths, console URLs) so you need neither the docs nor any external CLI. Full index + descriptions: [`skills/README.md`](skills/README.md).

Two layers:
- **Guides** — one per phase of the developer journey. Each is an ordered checklist whose steps are tagged **Agent Action** / **User Action** / **Validation**; stop at each **User Action** and wait for the developer. Copy the guide's `progress-template.md` to track progress.
- **Task skills** — one job each, usable standalone; the guides call them by name.

**Progress tracking (read these before resuming any work):** progress files live at the **git repository root**, are committed, and are how a new session learns what's already done.

> **Where exactly is "the repo root"?** It is the folder that **contains** `MobileApp/`, `Web/`, and
> `Documentation/` (the git root) — **not** inside `MobileApp/`. This matters because the developer
> usually opens `MobileApp/` in Android Studio (the "MobileApp Window"), so from that window the progress
> files are **one level up**: read/write them at `../PROGRESS_*.md`. Never create them inside `MobileApp/`
> — that scatters duplicates and later sessions look in the wrong place. If you can't find them, look at
> the git root before assuming they don't exist (`git rev-parse --show-toplevel`).

| File | Tracks | Written by |
|---|---|---|
| `PROGRESS_FEATURES.md` | **What's actually built** — each model/screen from the PRD, plus the branded onboarding/paywall | `build-features` |
| `PROGRESS_P1_GETTING_STARTED.md` … `PROGRESS_P5_GROWTH.md` | The *guide's* steps for each phase | the phase guides |

**Resume rule:** before building, read the relevant progress file and continue from the **first unchecked item**. Never redo checked work; never re-derive a plan while unchecked items remain. Tick items off as you finish them, not at the end. If you finish a chunk (e.g. onboarding screens) and the guide has more unchecked items, **keep going through the guide** — do not stop and ask "what next"; the next step is the next unchecked item.

**Starting from an idea** ("build me a habit tracker"): run **`skills/new-app/`** first — it interviews the developer, writes `AiGuidelines/project/{prd,user_flow,ui_ux}.md`, picks the app name/id, and records deferred decisions as `TODO(<phase>)` markers in `root/AppConfiguration.kt`. It then hands off to Phase 1. Phase 1 needs **no Firebase, no subscription provider, no store account** (mock provider + direct-mode AI cover it).

**Developer journey (walk the guides in order):**

| Phase | Guide | Goal |
|---|---|---|
| 1 · First Run | `skills/getting-started/` | App running locally on your own device, rebranded, driven purely locally (screen, Room, preference, network call, permission) — no cloud |
| 2 · Integrations | `skills/integrations/` | Firebase + auth + web-proxy backend wired; real remote calls work |
| 3 · Publication | `skills/publishing/` | Icons, release signing (keys in CI not the app), store listings, first build in review |
| 4 · Monetization | `skills/monetization/` | Subscriptions + credit-pack IAPs + paywall + ads |
| 5 · Growth | `skills/growth/` | Analytics/Crashlytics/RemoteConfig, push, onboarding, virality loops |

**Task skills** (grouped by phase):
- **P1** `new-app`, `build-features`, `run-the-app`, `refactor-package`, `new-screen`, `new-local-model`, `add-api-service`, `save-preferences`, `add-permission`, `new-module`
- **P2** `configure-environment`, `setup-firebase`, `enable-auth`, `integrate-web-proxy`, `sync-data-firebase`
- **P3** `generate-app-icons`, `bump-version`, `setup-signing`, `capture-app-screens`, `setup-appstore-connect`, `setup-google-play`, `publish-release`
- **P4** `design-paywall`, `setup-subscriptions`, `enable-credits`, `enable-ads`
- **P5** `setup-analytics`, `enable-notifications`, `design-onboarding`, `add-virality-loop`
- **Cross-phase** `verify-ui` (behaviour via headless Compose tests + appearance via a rendered PNG), `run-quality-gates`, `sync-template` (pull template updates into a derived app)

### Screen Generation
**Whenever the user asks for a new screen, run this from `MobileApp/`** instead of hand-creating files:

```bash
./scripts/generate_screen.sh YourScreenName
```

It generates `*Screen.kt`, `*UiState.kt`, `*ViewModel.kt` under `presentation/screens/yourscreenname/` **and** wires the screen up end-to-end:
- Inserts the route into `presentation/navigation/Routes.kt`
- Inserts an `entry<YourScreenNameScreenRoute> { … }` stub + imports into `presentation/navigation/AppNavigation.kt`
- Inserts `viewModelOf(::YourScreenNameViewModel)` + import into `root/Di.kt`

The script is idempotent (safe to re-run). Insertion points in the three target files are marked with `// Add new … below — generate_screen.sh inserts here.` comments — leave those alone. After running, edit the generated `entry<>` block in `AppNavigation.kt` to add navigation callbacks the screen needs.

### Local Storage / Room 3
- All Room code is in **`commonMain`**: `data/source/local/{AppDatabase,DatabaseModule}.kt` + `entity/`, `dao/`. Use `androidx.room3.*` imports (Room 2.x `androidx.room.*` is incompatible).
- DAO functions must be `suspend` or return `Flow<T>` — no blocking calls in Room 3.
- Each platform supplies its own `DatabaseProvider` actual that picks the driver:
  - Android / iOS / JVM (`androidMain`, `iosMain`, `jvmMain`) → `BundledSQLiteDriver`
  - wasmJs + js → `WebWorkerSQLiteDriver` backed by [`shared/sqlite-wasm-worker/worker.js`](shared/sqlite-wasm-worker/worker.js) (OPFS persistence). The `DatabaseProviderImpl` class lives once in **`webMain`**; the only per-target difference is `createSQLiteWorker()` — an `internal expect fun` with a `@JsFun` actual (wasmJs) and a `js("…")` actual (js). The `webDatabaseModule` Koin wiring is also a single `webMain` `val` (no expect/actual).
- Repositories inject DAOs directly and convert with extension-function mappers (`Entity.toModel()` / `Model.toEntity()`); there is **no** `LocalDataSource` abstraction or `EntityMapper` interface.

**Whenever the user asks for a new locally-stored model**, run this from `MobileApp/`:
```bash
./scripts/make_local.sh ModelName
```
It scaffolds the domain model, `@Entity` (with mappers) and `@Dao` in `commonMain`, registers the entity in `@Database(entities = [...])`, adds the abstract DAO accessor on `AppDatabase`, and registers a Koin singleton in `DatabaseModule.kt`. Idempotent — safe to re-run. Insertion points are marked `// Add new ... — make_local.sh inserts here.` — leave those alone. After scaffolding, edit the generated entity to add real columns + update the mappers, and bump `@Database(version = ...)` + add a `Migration` if you've already shipped.

### Package / App-ID Refactor
**To rename the app's package / `applicationId` / iOS bundle ID + display name**, run this from `MobileApp/` (it's a bash script — the old `./gradlew refactorPackage` task was removed):
```bash
./scripts/refactor_package.sh --app-id com.example.newapp --app-name NewApp            # full: Kotlin packages + IDs + name
./scripts/refactor_package.sh --app-id com.example.newapp --app-name NewApp --skip-package-rename   # IDs + name only
```
Named flags `--app-id` / `--app-name` are both required (positional `<id> <name>` still accepted as a fallback). It rewrites Kotlin packages/dirs, Gradle files (including the custom task-group label, derived from the package's last segment), Firebase/Google-services configs, iOS `Info.plist`/`project.pbxproj`, the repo-root GitHub publish workflows, the helper scripts, and package references in docs/guidelines (READMEs, `AGENTS.md`, `skills/`, `AiGuidelines/`) — both the dotted id and the slashed `com/x/y` path. `--skip-package-rename` (default off — packages ARE renamed) updates IDs + name only. The values being replaced are **auto-detected** from the project (old id ← androidApp `namespace`; old name ← settings `rootProject.name`), so re-refactoring an already-renamed project just works — `--old-app-id` / `--old-app-name` only override detection. Edits in place + irreversible — it prompts for confirmation unless `-y` is passed. Idempotent. Requires `perl`.

### Syncing a derived app with the template
The rename above normally destroys the merge base with the template repo. **To pull template updates into an app created from this kit**, use `MobileApp/scripts/sync_template.sh` — it keeps a `template-base` branch of template snapshots *rendered as your app* (refactor applied to the incoming tree), so `git merge` sees only real feature changes. Works with "Use this template" repos (unrelated history) via `--bootstrap`. Records the synced commit in `.template-version`; release notes for each sync live in `CHANGELOG.md`. Full flow + conflict-resolution rules: the **`sync-template`** skill. Template maintainers: every feature PR adds a `CHANGELOG.md` entry.

### Store Screenshot Generation
**To produce App Store / Play Store screenshots at storefront pixel sizes**, run this from `MobileApp/`:

```bash
./scripts/generate_store_screenshots.sh
```

Output lands at `distribution/store_screenshots/<locale>/<device>/*.png` — pure screen captures at the storefront pixel dimensions, ready to upload. No Fastlane / ImageMagick / Ruby toolchain needed.

To add a new storefront screenshot, drop a `@Preview @StoreScreenshot @Composable` function next to the screen it previews (`HomeScreen.kt`, `GalleryScreen.kt`, etc.). The preview body should call the screen the same way the running app does — wrapped in `AppTheme`. Example:
```kotlin
@Preview
@StoreScreenshot(device = StoreDevice.IPHONE_6_5, locale = "en", tag = "01-home")
@Composable
private fun HomeStoreScreenshot_iPhone_en() {
    AppTheme {
        HomeScreen(uiState = HomeUiState(creditBalance = 12), onUiEvent = {})
    }
}
```

`@StoreScreenshot`-tagged previews are excluded from the regression screenshot test (they're storefront assets, rendered at huge pixel sizes — different concerns) and only run when the `-PgenerateStoreScreenshots=true` Gradle property is set, which the script handles for you.

**Default device.** Unless the user explicitly requests a different device, leave the annotation with the enum default (`StoreDevice.IPHONE_6_5`). It's already the default value on the `@StoreScreenshot` annotation, so the cleanest form is to omit the `device =` argument entirely.

### Authentication

Auth is the **[KMPAuth](https://github.com/mirzemehdi/KMPAuth) `KMPAuth` facade** used directly — **no
in-repo auth module**, cross-platform including desktop/web (Firebase backend). `AppInitializer` calls
`KMPAuth.initialize { google(...) }`; `UserRepository` wraps the facade (maps `KMPAuthUser` → `User`);
`AuthUIHelperButtons` renders social sign-in via `rememberGoogleAuthState` / `rememberAppleAuthState`.
Auth conditions surface as KMPAuth's typed `KMPAuthUserCollisionException` /
`KMPAuthRecentLoginRequiredException`, not domain exceptions.

### Subscription Provider

Two interchangeable billing backends live under `libs/subscription/` behind the
`subscription-api` contracts: **Adapty (default)** and **RevenueCat**.

- **One switch, one place.** The provider is chosen by the `SUBSCRIPTION_PROVIDER`
  gradle property in `gradle.properties` (`ADAPTY` default, or `REVENUECAT`). That
  property both (a) selects which module `shared/build.gradle.kts` puts on the classpath
  and (b) drives `AppConfiguration.subscriptionProviderFactory`, which delegates to
  `activeSubscriptionProviderFactory` — a single symbol each provider module exposes in
  package `com.kotlinfoundation.koko.subscription.config`. Exactly one provider module is ever
  linked, so `AppConfiguration` never names a concrete provider. **Do not hardcode a provider in
  `AppConfiguration`.**
- Switching providers = change the gradle property only (plus the provider's API keys in
  `local.properties`). Both `ADAPTY` and `REVENUECAT` builds must compile.
- **Mock provider (zero-config demo).** When the active-platform subscription SDK key is still a
  placeholder (`isSubscriptionMockActive()` in `root/Di.kt` checks `BuildConfig.SUBSCRIPTION_PROVIDER_*_API_KEY
  == "testValue"`), DI swaps in `MockSubscriptionProvider` (in `subscription-api`, next to
  `NoOpSubscriptionProvider`) instead of the linked real provider. It returns demo packages and a
  "purchase" simulates success (persisted via `UserPreferences`), so the whole paywall → purchase →
  unlock → cancel flow works on every platform with **no keys**. The paywall shows a red demo warning
  `AppDialog` on each open (`SubscriptionProvider.isMockProvider` → `PaywallUiState.isMock`), buys skip the sign-in gate, and the
  Subscriptions screen's "manage here" link cancels in-app via `cancelMockSubscription()` (an interface
  method — default no-op, only the mock implements it). Client-only fake (no receipt); auto-reverts to
  the real provider the moment a key is set. The mock stays dependency-free — its ids/persistence/clock
  are injected by `Di.kt`.

### Paywall Layer

Location: `shared/src/commonMain/kotlin/com/kotlinfoundation/koko/presentation/screens/paywall/`

Three-piece architecture that keeps Compose screens display-only:

- **`PaywallScreen.kt`** — thin router. Owns the success-view overlay and error dialog, then dispatches to `SubscriptionPaywallScreen` or `CreditPackPaywallScreen` based on `PaywallUiState.mode`. Does **not** wrap in `ScreenWithToolbar` — each child screen owns its own toolbar so we don't double-pad horizontally.
- **`PaywallViewModel.kt`** — lifecycle only: fetch packages, default-selection, buy / restore / success / error. Delegates all formatting to the mapper.
- **`PaywallUiStateMapper.kt`** — pure, stateless. Public entry points: `map(rawPackages, selectedId, mode) → MappedPaywall` and `pickDefaultSelection(rawPackages, mode) → PurchasePackageId?`. Both subscription and credit-pack flows share the same `MappedPaywall { packages, ctaText, aboveCtaText, belowCtaText }` shape. Inject via `singleOf(::PaywallUiStateMapper)` (already wired in `presentationModule`). Unit-test it without Koin/coroutines.
- **`PaywallUiState.kt`** — `PaywallUiState`, `PaywallPackageUiState`, `PaywallUiEvent`, `PaywallMode { SUBSCRIPTION, CREDIT_PACK }`. Footer copy uses position-named fields (`aboveCtaText` / `belowCtaText`) — clearer than `reassuranceText` / `disclosureText`.
- **`PaywallPreviewData.kt`** — `internal object` with `subscriptionState(trialAvailable)`, `paidIntroSubscriptionState()`, `creditPackState()`, and package builders. Display fixtures only — they bypass the mapper so `@Preview` doesn't depend on string resources or billing logic.
- **`subscription/SubscriptionPaywallScreen.kt`** / **`creditpack/CreditPackPaywallScreen.kt`** — each owns its own `ScreenWithToolbar`. Read pre-built `UiText` fields off the state and render. No formatting logic in the screens.

**Strings.** All paywall copy lives in `composeResources/values/strings.xml` under three prefixes:
- `paywall_*` for shared chrome (toolbar / footer / badges).
- `paywall_sub_*` for subscription flow (plan titles, subtitles, reassurance / disclosure templates, CTA).
- `paywall_cp_*` for credit-pack flow (title, subtitle, per-credit unit, CTA).

Period units are **plurals** (`paywall_unit_day`, `paywall_unit_day_count`, etc.) — bare form for noun suffixes (`/week`, `your first month`) and count form for durations (`3 months`, `7 days`).

**UiText extensions** (in `designsystem/util/UiText.kt`):
- `UiText.of(StringResource, vararg args)` — primitive format args (`%1$d`, `%1$s` with strings/numbers).
- `UiText.of(PluralStringResource, count, vararg args)` — quantity-aware plurals.
- `UiText.ofComposed(StringResource, vararg UiText args)` — when a template's args are themselves translated (e.g. `"Save N% on your first <unit>"`).

**Adding a new placement** (e.g. a new credit-pack-like flow): add a constant in `Constants.PAYWALL_PLACEMENT_*`, an entry in `PaywallMode`, a branch in the mapper's `map(...)` / `pickDefaultSelection(...)`, and route to the right child screen from `PaywallScreen`. Strings get a new prefix; reuse the existing `paywall_*` chrome.

## Key Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Kotlin | 2.4.0 | Language (bumped from 2.3.20 — required by KMPNotifier 2.0) |
| Compose Multiplatform | 1.10.3 | UI framework |
| Android Gradle Plugin | 9.2.0 | Android build tooling |
| Gradle | 9.4.1 | Build system |
| compileSdk | 37 | Android compile SDK (KMPNotifier 2.0 Android artifacts require API 37+; `targetSdk` unchanged) |
| Koin | 4.2.1 | Dependency injection |
| Ktor | 3.5.0 | HTTP client |
| KMPNotifier | 2.0.1 | Notifications (split modules: `kmpnotifier-local` + `kmpnotifier-push-firebase`; new `KMPNotifier` API). iOS Firebase via SwiftPM, deployment target 16.0+. |
| KMPAuth | 3.0.5 | Auth facade (Google/Apple/anonymous, Firebase backend). Uses GitLive firebase 3.0.0-alpha01; iOS floor firebase-ios-sdk 12.17.0. |
| Room | 3.0.0-alpha06 | Local database (KMP — `androidx.room3:*`, plugin id `androidx.room3`) |
| SQLite | 2.7.0-alpha06 | `sqlite-bundled` (native) + `sqlite-web` (wasmJs OPFS). **Keep Room and SQLite on the same alpha train** — they're coupled (Room's generated code targets a specific `androidx.sqlite` API surface), so bump both together, never one alone. |
| Navigation 3 | 1.1.1 | Navigation (`org.jetbrains.androidx.navigation3` KMP) |
| Lifecycle ViewModel Navigation 3 | 2.10.0 | Per-NavEntry ViewModel scoping |
| Firebase BOM | 34.17.0 | Analytics, Messaging, Crashlytics, RemoteConfig |
| Adapty | 3.17.0 | In-app purchases — **default provider** (`adapty-kmp`). Selected via the `SUBSCRIPTION_PROVIDER` gradle property. |
| RevenueCat | 3.0.6 | In-app purchases — alternate provider (`purchases-kmp` 3.x — bundles purchases-hybrid-common internally; no iOS pod needed). Set `SUBSCRIPTION_PROVIDER=REVENUECAT` to use. |
| Coil | 3.5.0 | Image loading |
| DataStore | 1.3.0-alpha09 | Preferences storage (KMP — pin to 1.3.0-alpha09+, first version with js/wasmJs targets) |
| Calf | 0.12.0 | Runtime permissions (`calf-permissions`, all targets) |
| Napier | — | Logging |
| Spotless | 8.6.0 | Formatting + ktlint runner (root `build.gradle.kts`) |
| ktlint | 1.8.0 | Kotlin linter (driven by Spotless) |
| Roborazzi | 1.64.0 | Screenshot recording / verification |
| Robolectric | 4.16.1 | Android-on-JVM test runtime for screenshot tests |
| ComposablePreviewScanner | 0.9.0 | `@Preview` discovery for screenshot pipeline |

## Code Style

- Kotlin idiomatic style; follow Compose best practices
- Common code in `commonMain`; platform code in platform-specific source sets
- Prefer small, previewable composables in `designsystem`
- Document public APIs with KDoc
- Use design system components (`AppButton`, `AppCard`, etc.) from `designsystem` module
- **`composeResources/values/strings.xml` is NOT Android `aapt`** — it's parsed as plain XML, so apostrophes do **not** need backslash escaping. Write `couldn't`, never `couldn\'t`. Existing copy uses the typographic apostrophe `'` (U+2019); match it. Only true XML entities need escaping (`&amp;`, `&lt;`). Format args stay `%1$s` / `%1$d`.

## Product Knowledge

### Starting a new project — fill the product guidelines FIRST

In a fresh KMPStarterKit app these `AiGuidelines/project/` files ship **empty or as fill-in templates**.
Before building features, fill them in for your product — everything downstream reads them (the
onboarding/paywall designers, the UI/screen builders, and the self-improve loop all ground their work
in these files). Recommended order:

1. **`project/prd.md`** — what the app is, who it's for, scope, the core value/transformation.
2. **`project/user_flow.md`** — the primary flows and screen sequence.
3. **`project/onboarding.md`** — onboarding pattern, the goal-capture question, first-taste moment
   (use `AiGuidelines/agents/onboarding_designer.md` to author it).
4. **`project/paywall.md`** — primary model, offer architecture, prices, trial framing
   (use `AiGuidelines/agents/paywall_designer.md`).
5. **`project/virality_loops.md`** — multi-surface prompts, share/referral, win-back.
6. **`project/ui_ux.md`** — visual direction and design-system specifics.

Templated files contain `TAILOR PER APP` markers — search for them and replace with real product
decisions. A feature built before these are filled will inherit placeholder strategy, so do this first.

When asked about product behavior, UX, feature scope, or acceptance criteria, consult:
- **PRIMARY**: `AiGuidelines/project/prd.md` — canonical requirements, scope, constraints
- **Supporting**: `AiGuidelines/project/user_flow.md`, `onboarding.md`, `ui_ux.md`, `paywall.md`
- Prefer facts from PRD over assumptions. If detail is missing, ask or state assumptions clearly.
- If PRD and user_flow conflict: PRD wins for scope/data rules, user_flow wins for sequence/UX.

## Detailed Reference Guides

### Technical Guides (`AiGuidelines/tech/`)
In-depth patterns with code examples for each architectural layer:
- `api_services.md` — Ktor client, DTO conventions, testing with MockEngine
- `repository.md` — BackgroundExecutor patterns, correct/incorrect examples
- `domain.md` — Model design, exceptions, mapping, boundaries
- `presentation_layer.md` — ViewModel, screen architecture, component design
- `dependency_injection.md` — Koin module organization, scoping, platform modules
- `coroutines.md` — Scopes, dispatchers, structured concurrency, testing
- `utility_organization.md` — Extension functions, platform abstraction, singleton patterns

### Agent Role Prompts (`AiGuidelines/agents/`)
Specialized system prompts for when acting in specific roles:
- `product_designer.md` — Create development-ready PRDs from raw ideas
- `uiux_strategy.md` — Plan screen structure, flows, and creative design direction
- `uiux_screen_builder.md` — Build Compose Multiplatform UI screens from markdown specs
- `onboarding_designer.md` — Design emotionally engaging onboarding flows
- `paywall_designer.md` — Create high-converting paywall variations for A/B testing
- `user_flow_architect.md` — Transform PRDs into structured, development-ready user flows

### Creative Reference (`AiGuidelines/creative/`)
- `animation_patterns.md` — Playful UI animation patterns (transitions, buttons, lists, gestures, easter eggs)
- `easter_eggs.md` — Interactive surprises, mini-games, device-based interactions

### Documentation Site (`Documentation/`)
Docusaurus-based docs (git submodule, published at kotlinfoundation.org/kmp-contest-starter-kit-documentation):
- Getting started, architecture overview, feature guides
- Production: pre-publishing checklist, Fastlane lanes, Android/iOS publishing
- Feature docs: auth, subscriptions, notifications, credits, ads, AI integration, etc.

### UI/UX Development
When implementing screens:
- **Onboarding**: Use `AiGuidelines/project/onboarding.md` for content/flow
- **Paywall**: Use `AiGuidelines/project/paywall.md` for content/flow
- **Generic screens**: Reference `AiGuidelines/agents/uiux_screen_builder.md` for implementation patterns
- **Overall UX strategy**: Use `AiGuidelines/agents/uiux_strategy.md` for high-level design direction

## Prerequisites

- JDK 17+
- Android SDK path in `local.properties` (`sdk.dir=/path/to/sdk`)
- For iOS: Xcode + optionally KMM plugin
- Optional: Run KDoctor to verify environment
- First run downloads Compose and JetBrains JDK; builds take longer initially
