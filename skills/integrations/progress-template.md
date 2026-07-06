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
- [ ] **[Agent]** Walk through `local.properties` keys, `gradle.properties` `SUBSCRIPTION_PROVIDER`, `Constants.kt` fields
- [ ] **[User]** `MobileApp/local.properties` exists with `sdk.dir` (gitignored — confirm)

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

## 3. Social sign-in (Google / Apple) — `enable-auth`
- [ ] **[Agent]** Confirm `Constants.AUTH_SOCIAL_LOGIN_ENABLED = true`
- [ ] **[User]** Enable Google + Apple providers in Firebase Auth
- [ ] **[User]** Copy Web client ID → `GOOGLE_WEB_CLIENT_ID` in `local.properties`
- [ ] **[User]** Wire iOS `Info.plist` client IDs + add "Sign In with Apple" capability in Xcode

## 4. Web proxy deploy + client wiring — `integrate-web-proxy`
- [ ] **[Agent]** Explain `Web/functions`, `{statusCode, errorMessage, data}` shape, `requireAuth`
- [ ] **[User]** Set Secret Manager `OPENAI_API_KEY` / `REPLICATE_API_KEY`
- [ ] **[User]** `firebase deploy --only functions` from `Web/`
- [ ] **[User]** Paste base URL into `Constants.CLOUD_FUNCTIONS_URL`
- [ ] **[Agent]** Wire a client (Ktor + Firebase Bearer token) per the `add-api-service` pattern

## 5. Validation gate
- [ ] **[Validate]** App authenticates (anonymous or social) AND a live Cloud Function returns data
- [ ] **[Validate]** Run the `run-quality-gates` skill

## Next
- [ ] Proceed to the `publishing` phase
