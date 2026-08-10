---
name: new-app
description: Turn a raw app idea into a defined product — interview the developer, write prd.md / user_flow.md / ui_ux.md, decide the app name + id, and record deferred config decisions — then hand off to getting-started to build it. Use when the developer says "build me a <X> app", "I want to build …", "start a new app", or arrives with an app idea before any product docs exist.
---

# New app — from idea to a defined product

**Goal:** turn a raw idea ("build me a habit tracker") into a **defined product** the rest of the
journey can build from. This skill writes **documents and decisions only — no app code**.

**Zero setup required.** Phase 1 needs **no Firebase, no Adapty/RevenueCat account, no Play/App Store
account**. The kit ships a **mock subscription provider** (the paywall + purchase + unlock flow all work
with no keys) and a direct-mode AI path. Every account-creating decision is **deferred** to the phase
that actually needs it. Say so up front — it's the reason the developer can start in minutes.

## How to ask (follow this exactly)

- **Never ask an open-ended question.** Every question offers **concrete options** with one marked
  **✅ recommended**, plus an escape hatch: *"decide for me"* or *"later"*.
- Ask in **small batches** (2–3 at a time), not a wall of questions.
- **STOP after each batch** and wait for the answer. Never invent the developer's product for them.
- If the developer says *"you decide"* — take the ✅ recommended option and tell them what you picked.

## Steps

### 1. Interview the idea — **Agent asks / User answers**

Adopt the role in [`AiGuidelines/agents/product_designer.md`](../../AiGuidelines/agents/product_designer.md)
(raw idea → development-ready PRD). Ask about:

1. **Core loop** — what does the user do on a typical day? (offer 2–3 interpretations of their idea, ✅ one)
2. **Target audience** — who is it for? (offer options)
3. **Pain points** — the top 3 problems this app resolves (propose 3, ✅, let them edit)
4. **MVP scope** — which 3–5 features ship first? (propose a list, ✅ recommended, let them cut/add)
5. **First-taste moment** — the one moment onboarding should deliver value in (propose 2, ✅)
6. **Monetization intent** — free · subscription (✅ recommended) · credits/consumables · ads · decide later
7. **UI direction** — visual style/vibe (offer 2–3 directions; see
   [`AiGuidelines/agents/uiux_strategy.md`](../../AiGuidelines/agents/uiux_strategy.md) and
   [`AiGuidelines/creative/`](../../AiGuidelines/creative/))

Derive everything else (onboarding pattern, permission framing) yourself from these answers and state
what you chose — don't ask more questions than the list above.

### 2. Name + app id — **Agent suggests / User picks**

- If the developer has no name, **suggest 3** with one ✅ recommended.
- Derive the app id from it: `com.<org>.<appname>` (lowercase, dot-separated).
- **Do not run the rebrand here** — `getting-started` does that with the
  **`refactor-package`** skill. Just record the decided name + id and pass them along.

### 3. Write the product docs — **Agent Action**, then User confirms

These ship blank (just a heading) — author them fully from the interview:

| File | Role prompt to use |
|---|---|
| [`AiGuidelines/project/prd.md`](../../AiGuidelines/project/prd.md) | `agents/product_designer.md` — goal, audience, core features, monetization, competitors |
| [`AiGuidelines/project/user_flow.md`](../../AiGuidelines/project/user_flow.md) | `agents/user_flow_architect.md` — screen map + navigation paths |
| [`AiGuidelines/project/ui_ux.md`](../../AiGuidelines/project/ui_ux.md) | `agents/uiux_strategy.md` — colors, typography, motion, design direction |

**Also fill these two now** — the kit already ships an onboarding flow and a paywall, so they'd
otherwise show boilerplate copy about the wrong product on first run. Fill the `TAILOR PER APP` blanks:

| File | Fill now | Leave for later |
|---|---|---|
| [`AiGuidelines/project/onboarding.md`](../../AiGuidelines/project/onboarding.md) (`agents/onboarding_designer.md`) | **all of it** — pattern + why, goal-capture question(s), first-taste moment, permission benefit framing, top-3 pain points. It's the first thing every user sees = MVP UX. | — |
| [`AiGuidelines/project/paywall.md`](../../AiGuidelines/project/paywall.md) (`agents/paywall_designer.md`) | **primary model + why** (from the monetization answer) | **prices & packages**, **trial length**, **pack sizes** → mark `TODO(monetization)` — you set these in Phase 4 when the store products are created |

You can design **and test** the whole paywall now — the mock provider runs the paywall → purchase →
unlock flow with no account and no keys. Only the real provider/products wait for Phase 4.

Then **summarize the product back in a few lines and get an explicit confirm** before continuing.

> `virality_loops.md` keeps its `TAILOR PER APP` markers — that's genuinely Phase 5 (`add-virality-loop`).

### 4. Decisions checkpoint — **Agent asks / User picks or defers**

Ask each in order, with options + ✅ recommended + *"later"*. Record the answer immediately as
described, so the right phase picks it up. Everything here is **safe to defer** — the app runs without
any of it. Skip any row whose stated condition isn't met.

| Decision | Where it goes | Default if deferred |
|---|---|---|
| Does the app have **premium (paid) features**? | `root/AppConfiguration.kt` → `PREMIUM_FEATURES_ENABLED` | `false` ✅ (everything free; turn premium on in `monetization` when the store products exist) |
| **Subscription provider** — **only ask if premium was set to `true`**; ask it as a plain two-way pick, **Adapty ✅ or RevenueCat**, with no comparison | `MobileApp/gradle.properties` → `SUBSCRIPTION_PROVIDER` | `ADAPTY` ✅ — **mock provider runs until a real key is set, so no account is needed now** |
| **Google/Apple sign-in**? | `AppConfiguration.AUTH_SOCIAL_LOGIN_ENABLED` | `false` ✅ (anonymous auth is enough; revisit in `integrations`) |
| **Backend / AI proxy** | `AppConfiguration.CLOUD_FUNCTIONS_URL` | `""` ✅ — **if the app uses AI**, direct mode works right now with **no Firebase**: put `OPENAI_API_KEY` / `REPLICATE_API_KEY` in `MobileApp/local.properties` and leave this blank. The proxy (keys off-device) comes in `integrations` before you publish. |
| **Legal URLs, support email, App Store id** | `AppConfiguration.{URL_PRIVACY_POLICY, URL_TERMS_CONDITIONS, CONTACT_EMAIL, APPSTORE_APP_ID}` | leave as-is ✅ — **required only to publish** |

For every **deferred** item, leave a marker on the field in `root/AppConfiguration.kt` so the owning
phase can find it:

```kotlin
// TODO(publish): your live privacy policy URL — stores reject placeholders (see `publishing` skill)
const val URL_PRIVACY_POLICY = ""
```

Use `TODO(integrations)` / `TODO(publish)` / `TODO(monetization)` to name the phase that resolves it.

### 5. Hand off — **Agent Action**

Product is defined. **Continue into the [`getting-started`](../getting-started/SKILL.md) guide** (don't
make the developer ask) — it installs prereqs, runs the app, rebrands it with the name and id decided in
step 2, and builds the MVP features via **`build-features`**.

**If `getting-started` invoked you** (it found the product docs / name+id missing and called `new-app`
to fill them): once the docs are written and the name/id chosen, just **hand back** — `getting-started`
resumes at its next unchecked item. It will **not** call `new-app` again, because the values it checks
for now exist. This is the only link between the two skills, and it is one-way + state-guarded, so it
cannot loop.

---

## Validation gate (new-app done)

> **`prd.md`, `user_flow.md`, and `ui_ux.md` are filled and confirmed; app name + id decided;
> deferred decisions recorded as `TODO(<phase>)` markers.**

Then run **`getting-started`**. Nothing here required a single account or API key.
