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

> **Prototyping shortcut — Direct AI mode (no Firebase).** To try AI generation *before* deploying this
> proxy: put an `OPENAI_API_KEY` / `REPLICATE_API_KEY` in `local.properties` and leave
> `AppConfiguration.CLOUD_FUNCTIONS_URL` blank. The app's `AiTransport` then calls the provider **directly** from
> the device (auto-detected; `AppConfiguration.USE_AI_PROXY_SERVER` overrides — `true`=proxy, `false`=direct).
> **Not for production** — the key is
> compiled into the app binary. Deploy this proxy and clear those keys before shipping. Note: text→image
> is fully Firebase-free; image-*editing* still uploads the reference image via `KMPStorage`/Firebase
> Storage. Direct calls return the provider's raw JSON (no `{statusCode,errorMessage,data}` envelope) —
> `AiTransport` adapts it, so the DTOs and generation providers are unchanged.

## What's in `Web/functions`

- `index.js` — exports the enabled functions (`replicate*`, `openAi*`), gated by `AI_PROVIDERS`
  (see "Deploy a single provider" below). Runs on the **Node 22** runtime (`functions/package.json`).
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

This envelope is already modeled as `AiApiBaseResponse<T>` (with `handleAsResult`) — reuse it; the
provider payload is the nested `data: T`. (In direct mode the provider returns `T` at the top level and
`AiTransport` re-wraps it into the same `AiApiBaseResponse<T>`, so nothing downstream changes.)

## Prerequisites

- **Blaze plan** — Cloud Functions require the Firebase project to be on the **Blaze
  (pay-as-you-go)** plan (`setup-firebase` step 2: Project Overview → Usage and billing → Modify
  plan → Blaze). Deploys fail on the free Spark plan. Generous free tier — set a budget alert.
- **Default resource location** — a brand-new project needs one set before the first functions
  deploy (`setup-firebase`: Storage → Get Started), or the deploy fails with `generateUploadUrl`.
- **Firebase CLI** on your PATH. If a command below fails with `command not found: firebase`:

  - macOS / Linux, no Node required (fastest): `curl -sL https://firebase.tools | bash`
  - If you already use Node.js / npm: `npm install -g firebase-tools`

  The functions target the **Node 22** runtime; if you deploy from a machine with your own Node,
  install an active LTS (Node 20 or 22) to avoid old-runtime and modern-engine crashes.

## 1. Store API keys in Secret Manager — User Action

1. Get keys: OpenAI (https://platform.openai.com/), Replicate (https://replicate.com/).
2. **Enable the Secret Manager API** for the Firebase project (Cloud Functions can't read secrets
   until it's on) — open the API-library page and click **Enable**:
   `https://console.cloud.google.com/apis/library/secretmanager.googleapis.com?project=YOUR_PROJECT_ID`
   (replace `YOUR_PROJECT_ID`). Then open the Secret Manager console:
   https://console.cloud.google.com/security/secret-manager (select the Firebase project).
3. **Create secret** with the **exact** name `REPLICATE_API_KEY` (the default provider), paste the
   key. Add `OPENAI_API_KEY` too only if you enable OpenAI. (Names must match `defineSecret(...)` in
   `api/*.js`.) Create the secrets **before** deploying — the deploy binds them, and creating them
   first avoids interactive prompts. Only create the secret(s) for the provider(s) you enable
   (see "Which providers deploy" under step 2).
4. Grant the functions' service account access to each secret.

## 2. Deploy — User Action

From the `Web/` directory:

```bash
firebase login
firebase deploy --only functions
```

The deploy prints the base URL, shaped
`https://REGION-PROJECT_ID.cloudfunctions.net` (default region `us-central1`). Copy it.

> **Which providers deploy** — by default (no `.env`) **only Replicate** is deployed, so you only
> need `REPLICATE_API_KEY` and the first deploy just works. To use OpenAI instead, or both, copy
> `Web/functions/.env.example` to `Web/functions/.env` and set `AI_PROVIDERS` (e.g.
> `AI_PROVIDERS=openai` or `AI_PROVIDERS=replicate,openai`). Only the listed providers' endpoints
> deploy, so only **their** secrets need to exist in Secret Manager. To allow unauthenticated calls
> on an endpoint (dev/testing only), set `requireAuth: false` in that function's
> `Validation.validateAll(...)` call.

> **First deploy on a brand-new project fails?** Two common one-time causes: the Secret Manager
> API isn't enabled (step 1), or the project has no default Cloud Storage bucket yet — see the
> Storage-bucket step in `setup-firebase`. Enable both in the console, then re-run the deploy.

## 3. Point the app at the URL — User Action / Agent Action

Set the base URL in
`shared/src/commonMain/kotlin/com/kotlinfoundation/koko/root/AppConfiguration.kt`:

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

The OpenAI/Replicate calls are **already wired** — `OpenAiApiService` / `ReplicateApiService` route
through `AiTransport`, and the proxy `HttpClient` (`HttpClientFactory.default`) attaches the Firebase ID
token to every request automatically. So once `CLOUD_FUNCTIONS_URL` is set (unless `USE_AI_PROXY_SERVER`
forces direct), the existing generation flow uses the proxy with **no code change** — a set proxy URL
takes precedence over any direct key.

To add a **new** endpoint:
- **AI provider call** — add a method to the AI service that calls
  `aiTransport.execute(method, proxyUrl = "${AppConfiguration.CLOUD_FUNCTIONS_URL}/<functionName>", direct = directSpec("<providerUrl>"), proxyQueryParams = …, body = …)`.
  `AiTransport` picks proxy vs direct and adapts the response — the DTOs and `handleAsResult` don't change.
- **Other backend call** — follow `add-api-service` (Ktor service + DTOs + repository `Result`).

Specifics for this backend:
- **Base URL** = `AppConfiguration.CLOUD_FUNCTIONS_URL`; the path is the function **name**
  (e.g. `/openAiCreateTextCompletion`). Most endpoints are `POST`; dynamic values go as **query params**
  (the functions read `req.query`, not path segments).
- **Auth** — the proxy client attaches `Authorization: Bearer <Firebase ID token>` automatically; you
  don't set it per call. Auth-gated endpoints return `403` in `errorMessage` without a signed-in user.
- **Response** — the `{ statusCode, errorMessage, data }` envelope is parsed by `AiApiBaseResponse` /
  `handleAsResult`; a non-null `errorMessage` becomes a `Result` failure.

## 6. Validate — Validation

On a real device/emulator with a signed-in (anonymous or social) user, invoke the wired call and
confirm the function returns `data`. This is the phase's validation gate.

## Next

Live remote calls + auth working completes the `integrations` phase → move to `publishing`.

Adding Firestore-backed endpoints on top of this backend → `sync-data-firebase`. It reuses the same
functions + ID-token shape, and starts with the `wasmJs` trade-off question the developer must answer.
