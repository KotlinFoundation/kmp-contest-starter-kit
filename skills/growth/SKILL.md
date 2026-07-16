---
name: growth
description: Phase-5 guide to retain and scale a monetizing KMP app — Firebase Analytics + Crashlytics + Remote Config feature flags, push + local notifications, onboarding polish, and virality/referral loops with in-app review. Use when the developer asks to do growth / retention / scale the KMP contest starter kit, add analytics, wire push notifications, improve onboarding, or add referral/share/win-back loops. Prereq: Firebase from the `integrations` phase.
---

# Phase 5 — Growth (retention & scale)

**Goal:** take the monetizing app and grow it — instrument it so you can *see* behaviour
(Analytics + Crashlytics + Remote Config feature flags), bring users back (push + local
notifications), sharpen the first-run funnel (onboarding), and turn happy users into new ones
(virality / referral loops + in-app review prompts).

This is the **blueprint orchestrator** for the phase. Work the checklist top to bottom. Each step is
tagged **Agent Action** (you do it), **User Action** (the developer must do it in a browser/console),
or **Validation** (prove it works). Steps compose the four atomic skills — read the referenced skill
before doing that step.

Track progress in a copy of [`progress-template.md`](progress-template.md).

## Prerequisite

A Firebase project already exists and the app builds against it (the `integrations` phase). Analytics,
Crashlytics, Remote Config, and Cloud Messaging are all **the same Firebase project** — no new project.

## STOP rule (read verbatim)

> When the next unchecked item is a **User Action**, stop and wait for the developer to confirm
> they've done it before continuing. Never fabricate analytics data, remote-config values, or push
> tokens.

Console work (upload the APNs `.p8` key, add Remote Config parameters, send a test push, read
DebugView) is **User Action** — you cannot do it. Guide, then wait.

## Keys / secrets you'll need

No new `local.properties` keys. Analytics / Crashlytics / Remote Config reuse the existing Firebase
project; push notifications need an **APNs `.p8`** uploaded in the Firebase console (see
`enable-notifications`).

Verify: `./scripts/check_env.sh --phase growth`.

## Checklist

### 1. Instrument the app — `setup-analytics`
- **Agent Action** — Read the `setup-analytics` skill. Add analytics events at the funnel points that
  matter (screen views via `Analytics.logScreenView`, key conversions), log crashes via Crashlytics,
  and add any new feature flag to `FeatureFlagManager.Keys` + `DEFAULT_VALUES`.
- **User Action** — In the Firebase Console enable **Analytics**, **Crashlytics**, and **Remote
  Config**, and add each flag key as a Remote Config parameter with its production value. **Stop and
  confirm.**
- **Validation** — With a debug build, an event appears in Firebase **DebugView**.

### 2. Wire notifications — `enable-notifications`
- **Agent Action** — Read the `enable-notifications` skill (KMPNotifier 2.0: push via FCM + local
  notifications). Confirm the notification listeners in `AppInitializer.initializeNotification()`,
  and prime the runtime permission with `rememberNotificationPermissionState()` (see the
  `add-permission` skill).
- **User Action** — In the Apple Developer portal generate an **APNs auth key** (`.p8`) and upload it
  under Firebase **Project settings → Cloud Messaging**; in Xcode add the **Push Notifications** +
  **Background Modes (Remote notifications)** capabilities (deployment target 16.0+). **Stop and
  confirm.**
- **User Action** — In Firebase **Cloud Messaging → Send a message**, send a test push to the device
  token (logged as `Firebase onNewToken`). **Stop and confirm.**
- **Validation** — The test push is received on a real device.

### 3. Optimize onboarding — `design-onboarding`

Onboarding was designed and built in Phase 1 (`new-app` wrote `onboarding.md`, `build-features` branded
the screens). This step **optimizes activation**, it doesn't design from scratch.

- **Agent Action** — Read the `design-onboarding` skill. Instrument each step with screen-view/funnel
  events, tie the **notification-permission priming** to step 2, and refine the copy against real
  drop-off. If `AiGuidelines/project/onboarding.md` still has `TAILOR PER APP` markers, fill them now.
- **Validation** — Onboarding runs end to end; screen-view events for each step land in DebugView.

### 4. Add virality + review loops — `add-virality-loop`
- **Agent Action** — Read the `add-virality-loop` skill. Fill the `TAILOR PER APP` markers in
  `AiGuidelines/project/virality_loops.md` (share/referral artifact, high-intent prompts, win-back),
  build the surfaces, and wire `rememberInAppReviewTrigger()` at a genuine post-value moment.
- **Validation** — A share/referral surface works and the in-app review prompt fires (respecting its
  7-day cooldown).

### 5. Validation gate
- **Validation** — An analytics event shows in the Firebase console **DebugView** *and* a test push
  notification is received on-device. Run the `run-quality-gates` skill.

## Done → the loop repeats

There is no "next phase". Growth is a loop, not a finish line: **measure → improve → measure**. Read
the funnel (onboarding drop-off, event counts, crash-free rate), form a hypothesis, ship a change
behind a Remote Config flag, and measure again. The deeper conversion science and the self-improve
loop live in `AiGuidelines/` — `AiGuidelines/agents/` (onboarding / paywall / UX designers) and, when
installed, `AiGuidelines/loop/CONVERSION_PLAYBOOK.md`.
