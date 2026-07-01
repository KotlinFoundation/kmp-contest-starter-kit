---
name: setup-signing
description: Create the Android upload keystore + iOS signing identities and move all signing secrets out of the committed app into CI (GitHub Actions) secrets. Use when the user needs release signing, a keystore, provisioning profiles, or to secure signing keys before publishing.
---

# Set up release signing (and move keys out of the app)

Release builds must be **signed**, and the signing material must **never** live in the
committed repo. This skill (1) generates the Android upload keystore, (2) wires it via a
gitignored properties file, (3) sets up iOS certificates + provisioning profiles, and
(4) moves everything into GitHub Actions secrets so CI can sign without secrets on disk.

> Security principle: the keystore `.jks`, `keystore.properties`, the iOS `.p12`, the App
> Store Connect API key, and any embedded API keys must be **gitignored on disk** and stored
> as **CI secrets** (or a backend) — never committed. The starter kit's `.gitignore` already
> excludes `distribution/android/keystore/keystore.jks`, `keystore.properties`, `*.aab`,
> `*.ipa`, and `local.properties`. Keep it that way.

## Android — upload keystore

### Option A — helper script (recommended)

From `MobileApp/`:

```bash
./scripts/generate_android_keystore.sh "Your Name" "YourCompany"
```

It generates `distribution/android/keystore/keystore.jks` **and**
`distribution/android/keystore/keystore.properties` with auto-generated secure passwords
(alias defaults to `aliasKey`, validity 10000 days).

### Option B — manual `keytool`

```bash
cd MobileApp/distribution/android/keystore
keytool -genkeypair -v \
  -keystore keystore.jks \
  -alias aliasKey \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass <STORE_PASSWORD> -keypass <KEY_PASSWORD> \
  -dname "CN=Your Name, O=YourCompany, C=US"
```

Then create `distribution/android/keystore/keystore.properties` (copy the committed
`keystore.example.properties` and fill it in — this file is **gitignored**):

```properties
keystorePassword=<STORE_PASSWORD>
keyPassword=<KEY_PASSWORD>
keyAlias=aliasKey
storeFile=../distribution/android/keystore/keystore.jks
```

**Back up `keystore.jks` + its passwords somewhere safe (password manager).** Losing it
means you can never update the app on Google Play under the same signing identity.

### Wiring (already in place)

`androidApp/build.gradle.kts` reads the properties automatically — no edit needed:

```kotlin
val keystorePropertiesFile = rootProject.file("distribution/android/keystore/keystore.properties")
val isSigningKeyExists = keystorePropertiesFile.exists()
// signingConfigs { create("release") { storeFile = file(keystoreProperties["storeFile"]) … } }
// release { signingConfig = signingConfigs.getByName(if (isSigningKeyExists) "release" else "debug") }
```

When `keystore.properties` is present, `:androidApp:bundleRelease` signs with it;
otherwise it falls back to the debug key. So the only thing you do is provide the two
gitignored files.

## iOS — certificates + provisioning profiles

Do this in **Xcode / Apple Developer** (User Action — the agent can't create signing
identities):

1. **Certificates** — In Keychain Access → *Certificate Assistant → Request a Certificate
   From a Certificate Authority* (save to disk). At
   https://developer.apple.com/account/resources/certificates/list create an **Apple
   Development** and an **Apple Distribution** certificate from that request. Download both,
   import into Keychain, then select both under *My Certificates*, right-click → *Export 2
   Items* as **`Certificates.p12`** with a password.
2. **Provisioning profiles** — At
   https://developer.apple.com/account/resources/profiles/add create an **iOS App
   Development** profile and an **App Store Connect** (distribution) profile for the app's
   bundle id. Download and double-click each to install in Xcode. Note each profile's
   **UUID** (open the file, search `UUID`).
3. **Xcode** — In the `iosApp` target → *Signing & Capabilities*, select the correct team
   and profiles for Debug and Release.

## Move all secrets into CI (GitHub Actions)

Add these under **repo Settings → Secrets and variables → Actions**. This is what removes
the keys from developer machines and lets `publish_android_playstore.yml` /
`publish_ios_appstore.yml` sign in CI.

**Android** (base64-encode the gitignored files):

```bash
# run from MobileApp/
base64 -i distribution/android/keystore/keystore.jks | pbcopy        # → SIGNING_KEY_STORE_FILE_BASE64
base64 -i distribution/android/keystore/keystore.properties | pbcopy # → SIGNING_KEY_STORE_PROPERTIES_BASE64
```

| Secret | Value |
|--------|-------|
| `SIGNING_KEY_STORE_FILE_BASE64` | base64 of `keystore.jks` |
| `SIGNING_KEY_STORE_PROPERTIES_BASE64` | base64 of `keystore.properties` |
| `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | Play service-account JSON contents (see `setup-google-play`) |
| `GRADLE_CACHE_ENCRYPTION_KEY` | `openssl rand -base64 16` |

**iOS:**

```bash
base64 -i Certificates.p12 | pbcopy   # → IOS_APP_CERTIFICATE_P12_BASE64
```

| Secret | Value |
|--------|-------|
| `IOS_APP_CERTIFICATE_P12_BASE64` | base64 of `Certificates.p12` |
| `IOS_APP_CERTIFICATE_P12_PASSWORD` | password you set when exporting the p12 |
| `APPSTORE_KEY_ID` | App Store Connect API **Key ID** |
| `APPSTORE_ISSUER_ID` | App Store Connect API **Issuer ID** |
| `APPSTORE_PRIVATE_KEY` | contents of the downloaded `AuthKey_*.p8` |
| `APPSTORE_TEAM_ID` | App Store Connect Team ID |
| `IOS_APP_DEVELOPMENT_PROVISION_UUID` | UUID of the development profile |
| `IOS_APP_DISTRIBUTION_PROVISION_UUID` | UUID of the distribution profile |

Create the ASC API key at App Store Connect → **Users and Access → Integrations → API Keys**
(role *App Manager*); the Issuer ID and Key ID show there, and the `.p8` downloads once.

## Other embedded secrets — out of the app too

Runtime API keys (`GOOGLE_WEB_CLIENT_ID`, subscription provider keys, AdMob IDs, etc.) live
in gitignored `MobileApp/local.properties` on disk and as GitHub secrets for CI — never in
committed source. Server-side keys (OpenAI/Replicate) stay in the backend / Secret Manager,
never in the app binary.

## Validate

- Android: `./gradlew :androidApp:bundleRelease` from `MobileApp/` produces a **signed** AAB
  at `androidApp/build/outputs/bundle/release/androidApp-release.aab`. Verify the signer:
  `keytool -printcert -jarfile <aab>` shows your certificate (not the Android debug cert).
- CI: confirm the required secrets exist in repo settings; a tagged release build
  (see `publish-release`) signs and uploads without any keys committed.
