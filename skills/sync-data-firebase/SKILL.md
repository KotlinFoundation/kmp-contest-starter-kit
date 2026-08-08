---
name: sync-data-firebase
description: Sync app data to Firebase — Firestore documents, cross-device sync, server-authoritative state (credit balances, settings, user profiles, saved items). Use when the developer wants to store data in Firestore, sync data across devices, back up local data to the cloud, make a balance or entitlement server-verified, or asks "how do I sync X with Firebase". Starts with a required question about the wasmJs target, because no Firebase client SDK supports Kotlin/Wasm.
---

# Sync app data to Firebase

The kit stores data locally by default — Room for structured data, DataStore for preferences. This
skill covers moving some of it to Firebase so it survives reinstalls, follows the user across devices,
or becomes server-authoritative.

Firebase setup (project, config files, anonymous auth) must already be done — that's `setup-firebase`.

## 0. Ask the developer first — this step is not optional

> [!IMPORTANT]
> There is **no Firebase client SDK for the `wasmJs` target**. The GitLive
> [firebase-kotlin-sdk](https://github.com/GitLiveApp/firebase-kotlin-sdk) covers Android, iOS, JVM and
> JS but **not** Kotlin/Wasm, so a Firestore call in `commonMain` breaks the web build. Desktop is never
> the blocker — the browser target is.
>
> **Put the question to the developer and wait for their answer.** Do not choose for them, do not
> answer with a recommendation as if it were settled, and never drop a target silently. The web build
> is a deliverable they chose; trading it away is theirs to decide.

Present both options:

| | **Keep Wasm** (present first) | **Drop Wasm** |
|---|---|---|
| How | Firestore lives behind the Cloud Functions backend this kit already ships. Client speaks plain REST. | GitLive SDK in `commonMain`, client talks to Firestore directly. |
| Targets | All five keep working. | No browser build. Android, iOS, Desktop, JS only. |
| Cost | More backend code: functions, security rules, DTOs, an API service. | Shorter path. Realtime listeners and offline persistence come free. |
| Reversible | Yes. | Removing/re-adding the `wasmJs` target is a real chunk of work. |

The rest of this skill covers the **Keep Wasm** path. For Drop Wasm, check the [GitHub releases page](https://github.com/GitLiveApp/firebase-kotlin-sdk/releases) explicitly 
to take the latest stable GitLive version — don't pin an old one — and remove the `wasmJs` target from `shared`, `designsystem`, and `webApp`. Always use **at least version `2.6.0`** of the Gitlive Firebase Kotlin SDK.


## 1. Ask what the sync is actually for

The answer decides how much you build. These are different products:

- **Backup / restore.** Server mirrors local state so a reinstall recovers it. Client stays the
  authority. Cheapest — no rule logic moves server-side.
- **Cross-device continuity.** Same state on phone and tablet. **Anonymous auth gives a per-install
  uid**, so sync alone achieves nothing here — the user needs a real account first (`enable-auth`) and
  an anon→permanent account link. Say this out loud before building; it's the most common wrong
  assumption.
- **Server-authoritative / anti-cheat.** The server holds the value and validates every mutation. This
  is the expensive one: any business rules that currently run in Kotlin have to be ported to the
  functions or duplicated.
- **Server-verified purchases.** Entitlement or balance grants move off the client onto a store
  webhook. Usually the highest-value piece and independent of the other three.

## 2. Architecture (Wasm-safe)

```
KMP app (Android, iOS, Desktop, Wasm, JS)
   │  HTTPS + Firebase ID token
   ▼
HTTPS Cloud Functions  ──►  Firebase Admin SDK  ──►  Cloud Firestore
```

Rules:

- Every Firestore read/write goes through a Cloud Function. The client never touches Firestore.
- Functions use the Firebase Admin SDK (already a dependency in `Web/functions/package.json`), not the
  Firestore REST API.
- Functions verify the caller's ID token before any database operation —
  `Web/functions/utils/validation.js` → `admin.auth().verifyIdToken(...)` is the existing helper.
- Plain REST endpoints, so one client implementation serves every platform.
- Firestore security rules deny all direct client access. Admin SDK bypasses rules by design.

## 3. Backend — `Web/functions/`

Add handlers under `Web/functions/api/`, export them from `Web/functions/index.js`, deploy via
`integrate-web-proxy`. Store the state under the caller's uid, never a uid taken from the request body:

```
users/{uid}/<collection>/{docId}
```

**Idempotency.** Any mutation the client might retry (a spend, a grant, a purchase) needs a
client-generated id used as the document id, so a retry over a flaky connection is a no-op instead of a
double-apply. The client already generates `Uuid.random()` in several places — reuse that value rather
than inventing a server id.

## 4. Client — normal API service, no Firebase types

Firebase stays invisible to the app. Follow `add-api-service`:

1. DTOs in `data/source/remote/request/` + `response/` (`@Serializable`, `asDomain()` mappers).
2. An `*ApiService` under `data/source/remote/apiservices/`, returning raw response types.
3. The repository wraps in `Result` via `BackgroundExecutor` and does the mapping.

Requests go through the Firebase-interceptor Ktor client against
`AppConfiguration.CLOUD_FUNCTIONS_URL` — the same path the AI proxy uses, so the ID token is attached
for you. A blank `CLOUD_FUNCTIONS_URL` means no backend is deployed; the feature must degrade to
local-only rather than crash.

## 5. Keep the local store as the offline layer

Don't delete the Room/DataStore path when the server arrives. Local becomes the cache:

- Reads serve from local, then reconcile with the server response.
- Writes apply locally first, queue for the server, and carry the idempotency id.
- Add a `synced` flag to the affected entity, plus a Room `@Database(version = …)` bump and a
  `Migration` if the app has already shipped.
- Decide and write down the conflict policy — usually server wins, clamped so a value can't go
  negative.
- **Migrate existing installs once.** Users already have local state. Upload it on first sync, guarded
  by a one-shot preference flag, or they lose it on update.

## 6. Worked example — credit balance

Credits are the most common thing developers want synced, so the shape is spelled out here.

Today they're fully local: per-source counters and `KEY_CREDIT_GENERAL_BALANCE` in DataStore, plus a
Room ledger in `credit_transaction`. `CreditRepository` applies the `CreditSystemConfig` rules
(one-time bonuses, recurring refills, `condition` lambdas that read `SubscriptionRepository`).

- Keep `CreditRepository`'s public API unchanged — `balance`, `useCredits`, `addCredits`,
  `getRecentTransactionsFlow`. Inject the remote source behind it so screens and ViewModels don't move.
- Firestore: `users/{uid}/credit` holding `{balance, sources: {id: {remaining, nextResetAt, granted}},
  updatedAt}` plus a `transactions/{clientTxId}` subcollection.
- The `CreditSystemConfig` DSL is Kotlin. Full server authority means porting it to JS or duplicating
  it. The cheap middle ground: rules stay client-side, the function only validates `balance >= amount`
  and applies the delta. Less strict, far less work — put the choice to the developer.
- The biggest actual win is independent of all this: move credit-pack grants off client
  `addCredits(...)` onto an Adapty/RevenueCat webhook → function → server grant. That closes the real
  abuse vector; hardening client-side spend matters much less.

## Validation

- Build the targets the developer chose to keep — including
  `./gradlew :webApp:wasmJsBrowserDistribution` if Wasm stayed.
- Kill the network mid-flow: the app still works from local state and reconciles on reconnect.
- Fire the same mutation twice with one idempotency id; the server applies it once.
- Confirm Firestore rules reject a direct client read.
- Run the `run-quality-gates` skill before committing.

## Related skills

`setup-firebase` (project + the same Wasm question) · `enable-auth` (real accounts — required for
cross-device) · `add-api-service` (client layering) · `integrate-web-proxy` (deploying functions) ·
`enable-credits` (local credit system this builds on) · `new-local-model` (the Room layer being cached).
