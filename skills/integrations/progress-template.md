# Phase 2 Progress — Integrations

> [!NOTE]
> **Setup Instruction:** Copy this template file to the root of your repository and rename it to **`PROGRESS_P2_INTEGRATIONS.md`**.
> Use this file to tick items as you go. Full instructions are in the `integrations` skill.
>
> **Role Labels:**
> - **[User]:** Developer does it in a browser/console.
> - **[Agent]:** AI/developer can execute directly (edit code, write configurations).
> - **[Validate]:** A verification gate.
>
> *STOP RULE:* Stop at any unchecked **[User]** item and wait for confirmation before proceeding.

## 1. Key catalog — `configure-environment`
- [ ] **[Agent]** Walk through `local.properties` keys, `gradle.properties` `SUBSCRIPTION_PROVIDER`, `AppConfiguration.kt` fields
- [ ] **[User]** `MobileApp/local.properties` exists with `sdk.dir` (copy from `local.properties.example`; gitignored — confirm)
- [ ] **[Agent]** Run `./scripts/check_env.sh --phase integrations` to see which keys this phase needs

## 2. Firebase project + apps + anonymous auth — `setup-firebase`
- [ ] **[Agent]** Read `applicationId` / iOS bundle id; run `./gradlew :androidApp:signingReport` for the SHA-1
- [ ] **[User]** Create Firebase project at console.firebase.google.com
- [ ] **[User]** Register Android app (package = applicationId, add SHA-1)
- [ ] **[User]** Register iOS app (bundle id)
- [ ] **[User]** Place `google-services.json` → `MobileApp/androidApp/`
- [ ] **[User]** Place `GoogleService-Info.plist` → `MobileApp/iosApp/iosApp/`
- [ ] **[User]** Enable Anonymous sign-in
- [ ] **[User]** Upgrade to Blaze plan (needed for Cloud Functions)
- [ ] **[Validate]** `./gradlew :androidApp:assembleDebug` succeeds with real `google-services.json`

## 3. Authentication — anonymous first, social opt-in — `enable-auth`
- [ ] **[Agent]** Anonymous auth already works (enabled in step 2, `AUTH_SOCIAL_LOGIN_ENABLED = false`) — nothing needed for the easy path
- [ ] **[Agent]** Ask the developer: "do you also want Google / Apple sign-in?" (skip the rest if no)
- [ ] **[User]** *(opt-in)* Set `AUTH_SOCIAL_LOGIN_ENABLED = true`; enable Google + Apple providers in Firebase Auth (re-download both config files)
- [ ] **[User]** *(opt-in)* Copy Web client ID → `GOOGLE_WEB_CLIENT_ID` in `local.properties`
- [ ] **[User]** *(opt-in)* Fill iOS `Info.plist` client IDs + add "Sign In with Apple" capability in Xcode (+ Apple `.p8`/Service ID for Android web flow)

## 4. Subscriptions — OPTIONAL commercial step (opt-in) — `setup-subscriptions`
- [ ] **[Agent]** Ask: "are you setting up subscriptions/monetization now?" (skip if no — the app still works)
- [ ] **[User]** *(opt-in)* Copy Adapty/RevenueCat public SDK keys → `SUBSCRIPTION_PROVIDER_ANDROID_API_KEY` / `_IOS_API_KEY` in `local.properties`

## 5. Web proxy deploy + client wiring — `integrate-web-proxy`
- [ ] **[Agent]** Explain `Web/functions`, `{statusCode, errorMessage, data}` shape, `requireAuth`
- [ ] **[User]** Set Secret Manager `OPENAI_API_KEY` / `REPLICATE_API_KEY`
- [ ] **[User]** `firebase deploy --only functions` from `Web/`
- [ ] **[User]** Paste base URL into `AppConfiguration.CLOUD_FUNCTIONS_URL` → existing AI flow now uses the proxy (no code change; the proxy client attaches the Firebase token). Hand-wire only for a NEW endpoint.
- [ ] **[Agent]** *(prototyping alt)* Skip the proxy: set `OPENAI_API_KEY`/`REPLICATE_API_KEY` in `local.properties`, leave `CLOUD_FUNCTIONS_URL` blank → direct provider calls (key on device — not for production)

## 6. Validation gate
- [ ] **[Validate]** `./scripts/check_env.sh --phase integrations` — clean for the anonymous-only path once real Firebase config is in place
- [ ] **[Validate]** App authenticates (anonymous or social) AND a live Cloud Function returns data
- [ ] **[Validate]** Run the `run-quality-gates` skill

## Next
- [ ] Proceed to the `publishing` phase
