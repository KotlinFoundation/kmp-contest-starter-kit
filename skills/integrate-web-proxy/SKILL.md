---
name: integrate-web-proxy
description: Deploy the Web/functions Cloud Functions AI proxy (OpenAI/Replicate), store API keys in Google Cloud Secret Manager, set CLOUD_FUNCTIONS_URL, and call it from a Ktor client with a Firebase Bearer token. Use when the developer wants live AI/backend calls, to deploy the Cloud Functions, or to wire the app to OpenAI/Replicate securely.
---

# Integrate the web proxy (Cloud Functions AI backend)

OpenAI and Replicate keys must never live in the mobile app. The `Web/functions` backend is a
pre-built Firebase Cloud Functions proxy: the app calls it with a Firebase ID token, the function
reads the real API key from **Google Cloud Secret Manager**, forwards the request, and returns a
uniform envelope. Requires a Blaze-plan Firebase project (`setup-firebase`) and auth
(`enable-auth` / anonymous).

## What's in `Web/functions`

- `index.js` — exports the enabled functions (`replicate*`, `openAi*`); comment a line to disable one.
- `api/openai.js` — `openAiCreateTextCompletion`, `openAiCreateImage` (secret `OPENAI_API_KEY`).
- `api/replicate.js` — `replicateCreatePrediction`, `replicateCreateModelPrediction`,
  `replicateGetPredictionStatus`, `replicateCancelPrediction` (secret `REPLICATE_API_KEY`).
- `utils/validation.js` — `Validation.validateAll(req, res, { requireAuth, requirePostRequest })`.
  With `requireAuth: true` (the default for most endpoints) the request **must** carry
  `Authorization: Bearer <Firebase ID token>`; the function verifies it via `admin.auth().verifyIdToken`.
- `utils/utils.js` — `makeApiRequest(...)` forwards the call and `sendApiResponse(...)` shapes the reply.

### Response envelope

Every function returns the same JSON shape (`utils/utils.js` → `sendApiResponse`):

```json
{ "statusCode": 200, "errorMessage": null, "data": { /* raw provider response */ } }
```

- `statusCode` — HTTP status.
- `errorMessage` — non-null only on error (e.g. `403 "Missing Authorization header..."`).
- `data` — the provider's raw response object on success.

Model your response DTO on this envelope with a nested `data` payload.

## 1. Store API keys in Secret Manager — User Action

1. Get keys: OpenAI (https://platform.openai.com/), Replicate (https://replicate.com/).
2. Enable + open Secret Manager:
   https://console.cloud.google.com/security/secret-manager (select the Firebase project).
3. **Create secret** with the **exact** name `OPENAI_API_KEY`, paste the key. Repeat for
   `REPLICATE_API_KEY`. (Names must match `defineSecret(...)` in `api/*.js`.)
4. Grant the functions' service account access to each secret.

## 2. Deploy — User Action

Install the standard Firebase CLI if needed (`npm install -g firebase-tools`), then from the `Web/`
directory:

```bash
firebase login
firebase deploy --only functions
```

The deploy prints the base URL, shaped
`https://REGION-PROJECT_ID.cloudfunctions.net` (default region `us-central1`). Copy it.

> To disable an endpoint before deploy, comment out its `exports.<fn>` line in
> `Web/functions/index.js`. To allow unauthenticated calls on an endpoint (dev/testing only), set
> `requireAuth: false` in that function's `Validation.validateAll(...)` call.

## 3. Point the app at the URL — User Action / Agent Action

Set the base URL in
`shared/src/commonMain/kotlin/com/kotlinfoundation/kmpstarterkit/util/Constants.kt`:

```kotlin
const val CLOUD_FUNCTIONS_URL = "https://us-central1-your-project-id.cloudfunctions.net"
```

## 4. Test locally first (optional) — Agent Action

From `Web/`:

```bash
firebase emulators:start --only functions
```

Point `CLOUD_FUNCTIONS_URL` at the emulator host while iterating, then switch back to the deployed URL.

## 5. Call it from the app — Agent Action

Follow the `add-api-service` skill to build the Ktor client end-to-end (request/response DTOs, API
service, repository, ViewModel call). Two specifics for this backend:

- **Base URL** = `Constants.CLOUD_FUNCTIONS_URL`; the path is the function name
  (e.g. `/openAiCreateTextCompletion`). Most endpoints are `POST`.
- **Auth header** — attach the current Firebase ID token as
  `Authorization: Bearer <token>` (the same token from the anonymous/social session). Without it,
  auth-gated endpoints return `403` in `errorMessage`.
- **Response** — parse the `{ statusCode, errorMessage, data }` envelope; on non-null `errorMessage`,
  surface it as a failure in the repository's `Result`.

## 6. Validate — Validation

On a real device/emulator with a signed-in (anonymous or social) user, invoke the wired call and
confirm the function returns `data`. This is the phase's validation gate.

## Next

Live remote calls + auth working completes the `integrations` phase → move to `publishing`.
