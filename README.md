# Koko — KMPStarterKit

**Koko** is the KMP contest starter kit — a Kotlin Multiplatform + Compose Multiplatform boilerplate for building Android and iOS apps with a shared codebase.

## Getting Started

We have built the KMP Contest Starter Kit (nicknamed Koko) as the fastest way to transform your idea from a production-grade app on Google Play and the App Store. If you are looking to get started with KMP, we recommend that you run through the "KMP Quickstart" at https://kotlinlang.org/docs/multiplatform/quickstart.html and the "Create Your First Compose Multiplatform app" at https://kotlinlang.org/docs/multiplatform/compose-multiplatform-create-first-app.html - and then come back once you have completed both of those. 

To get started with Koko, open this project in Android Studio and type `Proceed with koko-getting-started skill` into the agent's chat panel.

Documentation: https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/ 

> **Cloning on Windows?** This repo uses git symlinks (`CLAUDE.md`/`GEMINI.md` → `AGENTS.md`,
> `.claude/skills` → `skills/`). On Windows, clone with symlinks enabled or they become plain text
> files and AI-agent skill discovery silently breaks: enable **Developer Mode** (Settings → For
> developers), then `git clone -c core.symlinks=true <repo-url>`. Also note `Documentation/` is a
> submodule — use `git clone --recurse-submodules` if you want the docs site locally (optional).

## Project Structure

```
├── MobileApp/         # Compose Multiplatform mobile app (Android, iOS, Web, Desktop)
├── Web/               # Firebase Hosting landing page + Cloud Functions backend
├── Documentation/     # Docusaurus documentation site (git submodule) — kotlinfoundation.org/kmp-contest-starter-kit-documentation
├── AiGuidelines/      # AI-assisted development guidelines & agent prompts
├── skills/            # Agent-agnostic skills (SKILL.md format) for coding agents
├── .github/           # GitHub Actions workflows + composite actions (PR checks, publish, web build)
└── AGENTS.md          # Primary AI-agent context file (CLAUDE.md symlinks here)
```

## Features

### Core
- **[Authentication](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/features/auth)** — Google & Apple sign-in via Firebase Authentication
- **[In-App Purchases & Subscriptions](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/features/inapp-purchases-subscription)** — Adapty (default) and RevenueCat integration (switchable via the `SUBSCRIPTION_PROVIDER` gradle property)
- **[Push Notifications](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/features/notifications)** — Firebase Cloud Messaging for Android & iOS
- **[In-App Review](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/features/inapp-review)** — Native app rating prompts
- **[Feature Flags / Remote Config](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/features/feature-flag)** — Runtime feature toggling via Firebase Remote Config
- **[Firebase Integration](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/features/firebase-integration)** — Analytics, Crashlytics, Messaging, Remote Config

### Data & Networking
- **[Network](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/features/network)** — Ktor HTTP client with centralized config, JSON serialization, logging
- **[Local Storage](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/features/local-storage)** — Room database for offline persistence
- **[User Preferences](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/features/user-preferences)** — Jetpack DataStore Preferences on all targets (file-based on Android/iOS/JVM, `WebLocalStorage` on web)
- **Runtime Permissions** — [Calf](https://github.com/MohamedRejeb/Calf)-backed `AppPermissionState` API with ready-made helpers for notification, camera, gallery, location, and microphone permissions
- **Splash Screen** — native launch screen on both platforms (no library): Android `core-splashscreen` theme + iOS declarative `UILaunchScreen`. Rebrand by editing the color/icon (see the *Splash Screen* section in `AGENTS.md`/`CLAUDE.md`)

### UI & Development
- **[UI Components](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/features/ui-components)** — Pre-built design system with reusable Compose components
- **[Screen Generator](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/features/screen-generator)** — Bash script to scaffold new screens with boilerplate
- **[Logging](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/features/logging)** — Structured logging via Napier

### Quality & Testing
- **Spotless + ktlint** — Run `./gradlew spotlessApply` to auto-format Kotlin source and Gradle KTS. Enforced on every PR.
- **Unit & Compose UI tests** — `kotlinx-coroutines-test` for `Flow` / ViewModel tests; `runComposeUiTest` for headless UI tests on JVM. Run via `./gradlew :shared:jvmTest :shared:testAndroidHostTest`.
- **Screenshot tests (optional, local)** — Roborazzi + ComposablePreviewScanner can snapshot every `@Preview` under `com.kotlinfoundation.koko.*`. Record baselines with `./gradlew :shared:recordRoborazziAndroidHostTest`, then compare with `./gradlew :shared:verifyRoborazziAndroidHostTest`. Goldens are not committed and verification is not part of PR checks.
- **Storefront screenshot generator** — `./scripts/generate_store_screenshots.sh` renders every `@Preview @StoreScreenshot` composable at App Store / Play Store pixel sizes, ready to upload. The capture is the screen as it renders — no marketing chrome, device frames, or headlines added. No Fastlane / ImageMagick required.

### Monetization & Growth
- **[Google AdMob Ads](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/features/admob-ads)** — Banner, interstitial, and rewarded ads
- **[Flexible Credit System](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/features/credits-system)** — Local credit system with renewable credits
- **No-premium mode** — the default (`AppConfiguration.PREMIUM_FEATURES_ENABLED = false`): no paywall, no subscriptions, credits off, all features free. Flip it to `true` when you add premium features (the app can still be free to download)

### AI & Backend
- **[AI Integration](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/features/ai-integration)** — OpenAI, Replicate, and DALL-E via Firebase Cloud Functions (secure API key handling)
- **[App Landing Page](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/features/app-landing-page)** — Pre-built landing page template deployable to Firebase Hosting

### DevOps
- **[GitHub CI/CD Actions](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/features/github-ci-cd)** — `pr_checks.yml` runs format/test/screenshot/build gates on every PR; release workflows publish to Play Store and App Store from tag pushes. Workflows live at the repo root in `.github/workflows/`.
- **[Fastlane](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/production/fastlane)** — Pre-configured lanes for Play Store & App Store publishing
- **[Scripts](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/features/scripts)** — Helper scripts for package/app-ID refactor, version bumps, keystore generation, module creation, ASO metadata, store-screenshot rendering

## Contributing

We welcome contributions from the community! If you'd like to help improve the Koko template, please review our [Contributing Guidelines](CONTRIBUTING.md) for information on our workflow, code style, and how to submit a Pull Request.

### Production
- **[Pre-Publishing Checklist](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/production/pre-publishing-checklist)** — Step-by-step checklist for app icons, API keys, signing, and store setup
- **[Android Production](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/production/android)** — Android keystore and Play Store publishing
- **[iOS Production](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation/production/iOS)** — App Store publishing and certificate setup

For more details, visit the [full documentation](https://kotlinfoundation.org/kmp-contest-starter-kit-documentation).

## Tech Stack

- **Language**: Kotlin 2.3.20
- **UI**: Compose Multiplatform 1.10.0
- **Platforms**: Android, iOS, Web (WASM), JVM Desktop
- **DI**: Koin 4.2.0-beta2
- **Networking**: Ktor 3.3.1
- **Database**: Room 3.0.0-alpha01 (KMP — Android, iOS, JVM, wasmJs via OPFS)
- **Preferences**: DataStore 1.3.0-alpha09 (all targets, incl. js/wasmJs)
- **Permissions**: Calf 0.12.0
- **Auth**: Firebase Authentication
- **Subscriptions**: Adapty (default) / RevenueCat (switchable)
- **Quality**: Spotless 8.4.0 + ktlint 1.7.1, Roborazzi 1.60.0 + ComposablePreviewScanner 0.9.0 (screenshot tests)


| Project | Details |
|---------|---------|
| **MobileApp** | See [MobileApp/README.md](MobileApp/README.md) for setup, build commands, and architecture |
| **Web** | Firebase Hosting static site + Node.js Cloud Functions |

> **New here? Follow the developer journey.** The [`skills/`](skills/README.md) folder is a phase-by-phase path from a cloned template to a shipped, earning app: **getting-started** (run it locally) → **integrations** → **publishing** → **monetization** → **growth**. Each guide is a checklist you can follow with an AI agent *or* by hand — real commands, paths, and console steps, no external docs needed.

### Prerequisites

- JDK 17+
- Android SDK (for mobile app)
- Xcode (for iOS builds)
- Optional: [KDoctor](https://github.com/Kotlin/kdoctor) to verify environment

> **iOS note:** the shared framework links some native SDKs (e.g. Firebase, via KMPNotifier) through Swift Package Manager. The required Kotlin↔SwiftPM linkage package (`MobileApp/iosApp/KotlinMultiplatformLinkedPackage/`) ships committed, so iOS builds — and apps generated from KMPStarterKit — work out of the box. You only regenerate it when you add or change a SwiftPM-backed dependency. See [iOS production docs › SwiftPM Dependencies & the Linkage Package](Documentation/docs/production/iOS.md).

### Cloning with Documentation Submodule

The `Documentation/` directory is a git submodule. To initialize it when cloning:

```bash
git clone --recurse-submodules <repo-url>
# Or if already cloned:
git submodule update --init
```

## AI-Assisted Development

This project is set up to be AI-ready out of the box — coding agents (Claude Code, Codex, Cursor, Aider, etc.) get the same context the team uses:

- **`AGENTS.md`** — Primary context file following the vendor-neutral [agents.md](https://agents.md) convention, auto-read by Codex, Gemini CLI, Cursor, and others (`CLAUDE.md` is a symlink to it, so Claude Code reads the same file)
- **`AiGuidelines/tech/`** — Architecture patterns & coding conventions
- **`AiGuidelines/agents/`** — Specialized role prompts (product designer, UI/UX, paywall, onboarding, etc.)
- **`AiGuidelines/creative/`** — Animation patterns & easter egg inspiration
- **`AiGuidelines/project/`** — Product requirements & user flow documentation
- **`skills/`** — A phase-by-phase **developer journey** as agent-agnostic skills (open `SKILL.md` format): five guides — getting-started → integrations → publishing → monetization → growth — plus the one-job task skills they compose (run the app, new screen/model, Firebase, auth, signing, subscriptions, ads, notifications, …). Each is followable by an AI agent or by hand. Index: [`skills/README.md`](skills/README.md). Claude Code discovers them via the `.claude/skills` symlink; other agents via the Skills section in `AGENTS.md` (Gemini/Cursor/Copilot pointer files included)
- **Build & test workflows** — All quality gates an agent needs are documented in `AGENTS.md` (Spotless, JVM/Android tests, debug build) and enforced in `.github/workflows/pr_checks.yml`
- **Scaffolding scripts** — `MobileApp/scripts/generate_screen.sh` and `MobileApp/scripts/make_local.sh` keep agent-generated code consistent with project conventions
- **Environment config** — copy `MobileApp/local.properties.example` → `local.properties`; `MobileApp/scripts/check_env.sh --phase <phase>` reports which required service keys are still placeholders so the agent can ask for them (the build otherwise defaults them and stays green)

## License

Released under the [MIT License](LICENSE) — Copyright (c) 2026 KotlinFoundation.
