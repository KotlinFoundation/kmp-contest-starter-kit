# Usage

> ⚠️ Make sure you have Fastlane installed. If not, run in your project root:
>
> ```bash
> gem install bundler
> bundle install
> ```

---

## Android

### First-time Build

**Lane:** `first_time_build`  
Generate keystore (if missing) and build signed release AAB. Saves keystore in `distribution/android/keystore` folder.
⚠️ Does **not** upload to Play Store. Manual upload required. Generated app-release.abb is saved in `distribution/android` folder.


```bash
bundle exec fastlane android first_time_build first_name:"Developer Name" organization:"Developer or Company Name"
```
#### Options

- `first_name` – Developer name for keystore (default: empty )
- `organization` – Organization name for keystore (default: applicationId will be used)


### Build & Upload to Play Store

**Lane:** `playstore_release`  
Build and upload release AAB to Google Play Store. Make sure for the first time you checked `first_time_build`.

to publish internal testing track:
```bash
bundle exec fastlane android playstore_release upload_metadata:false
```
or to publish to production track:
```bash
bundle exec fastlane android playstore_release track:"production" submit_for_review:true upload_metadata:false upload_screenshots:false upload_images:false
```


#### Options

- `track` – Play Store track: internal, alpha, beta, production (default: internal)
- `upload_metadata` – Updates metadata like screenshots, title, description in PlayStore (default: false)
- `submit_for_review` – Submit changes for review in Play Store (default: true)
- `release_status` – Release status: draft or completed (internal track always draft)
- `service_account` – Path to Google Play API JSON key (default: `~/credentials/google-service-app-publisher.json`)
- `skip_upload_images` – Skip uploading images (icon, featuregraphic) (default: true when `upload_metadata` is false)
- `skip_upload_screenshots` – Skip uploading screenshots (default: true when `upload_metadata` is false)
- `skip_upload_changelogs` – Skip uploading changelogs (default: true when `upload_metadata` is false)



### Update Play Store Metadata Locally

**Lane:** `update_local_metadata_from_playstore`  
Fetch current metadata (like screenshots, texts) and save locally in `distribution/android/playstore_metadata`.
This is useful if you'd like to fetch recent changes before uploading new ones to Play Store.

```bash
bundle exec fastlane android update_local_metadata_from_playstore
```

---

## iOS

### Build & Upload to App Store

**Lane:** `appstore_release`  
Build and upload IPA to App Store.

```bash
bundle exec fastlane ios appstore_release submit_for_review:true upload_metadata:false upload_screenshots:false
```

#### Options

- `upload_metadata` – Upload metadata texts (default: false)
- `upload_screenshots` – Upload and override screenshots (default: false)
- `submit_for_review` – Submit for review (default: false)


### Update App Store Metadata Locally

**Lane:** `update_local_metadata_from_appstore`  
Fetch current metadata (like screenshots, texts) and save locally in `distribution/ios/appstore_metadata`.
This is useful if you'd like to fetch recent changes before uploading new ones to Play Store.

```bash
bundle exec fastlane ios update_local_metadata_from_appstore use_live_version:true
```

#### Options
- `use_live_version` – Download metadata from live version (default: true), if app is not released yet, you'll need to pass `false`



# Store Publisher Credentials (Play Store & App Store)

This guide explains **how to obtain and update** the required publisher credentials used by Fastlane  
to upload builds and manage metadata for **Google Play Store** and **Apple App Store**.

---

## Android – Google Play Publisher

Fastlane uses a **Google Play Service Account** to authenticate with Google Play Console.

### 1. Create Google Play Service Account

1. Go to https://console.cloud.google.com
2. Create a **new project** (recommended) or select an existing one
3. Note the **Project ID** — all steps must use this project
4. In Google Cloud Console, open **APIs & Services → Library**, Search for **Google Play Android Developer API**,
   and Click **Enable**
5. Go to **IAM & Admin → Service Accounts**, and create a new service account (Skip role assignment)
6. Open the created service account, Go to **Keys**, Click **Add key → Create new key**, Select **JSON**,
   and Generate a **JSON key**. Download the key file and store it securely

### 2. Grant Access in Play Console

1. Open https://play.google.com/consoleReturn
2. Go to **Settings → Users and permissions**
3. Click **Invite new user**. Email:  
   `your-service-account@project-id.iam.gserviceaccount.com`
4. To avoid permission issues, you may enable **All permissions** for account, or just select the apps you want

### 3. Place the JSON File

Save the downloaded JSON key and save as in :

```text
~/credentials/google-service-app-publisher.json
```

## iOS – App Store Publisher

Fastlane uses an **App Store Connect API Key** to authenticate with Apple  
(no Apple ID login is required).

### This file is required for:
- Uploading iOS IPA builds to App Store Connect
- Downloading App Store metadata (texts and screenshots)
- Uploading App Store metadata and screenshots via Fastlane
- Authenticating with App Store Connect using API Key

---

### 1. Create App Store Connect API Key

1. Open **App Store Connect**
2. Go to **Users and Access → Integrations**
3. Give App Manager access, and create a new **API Key**
4. Download the `.p8` file
5. Note the following values:
    - **Issuer ID**
    - **Key ID**

---

### 2. Create Publisher JSON File

Create the following file:

```text
~/credentials/appstore-publisher.json
```

```json
{
  "key_id": "KEY_ID_HERE",
  "issuer_id": "ISSUER_ID_HERE",
  "key": "-----BEGIN PRIVATE KEY-----\nPASTE_P8_KEY_CONTENT_HERE\n-----END PRIVATE KEY-----",
}

```

