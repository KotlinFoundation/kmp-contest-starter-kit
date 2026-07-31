# KMPStarterKit App Landing Page and AI Functions Backend
This repository contains two parts:

1. **Landing Page**: This is a simple app landing page template that you can use to showcase your app and deploy it into Firebase easily and free.  You can see the demo of the landing page of [AppIdeaHub](https://appideahub-kappmaker.web.app/). It has sections for a Hero, Problem, Features, and a Call-to-Action (CTA). It includes template Privacy Policy and Terms and Service files as well (but you should write your own according to your app, as this is just a template and not a legal document).
2. **AI Functions Backend**: Serverless backend built with Firebase Cloud Functions, for AI integrations like ChatGPT and Replicate AI.

#### Folder Structure
- **`public/`**: Contains the app landing page template.
- **`functions/`**: Contains the serverless backend logic using Firebase Cloud Functions.

# Contents

- [Requirements](#requirements)
- [Initialization](#initialization)
  - [App Landing Page Setup](#app-landing-page-setup)  
    - [Initialize Firebase Hosting](#1-initialize-firebase-hosting)  
    - [Test Locally](#2-test-locally)  
    - [Deployment](#3-deployment)  
  - [AI Backend functions setup](#ai-backend-functions-setup)
    - [Initialize Firebase Functions](#1-initialize-firebase-functions)  
    - [Test Locally](#2-test-locally-1)  
    - [Deployment](#3-deployment-1)
- [Configuration](#configuration)
   - [App Landing Page Configuration](#app-landing-page-configuration)
      - [Color Customization](#color-customization)
      - [AI Prompt for Better Config File](#ai-prompt-for-better-config-file)

---


## Requirements

Before starting, you need to initialize Firebase in your project. First download this repo, and navigate into root folder.

  ### 1. Install Firebase CLI

If not already installed:
```bash
npm install -g firebase-tools
```  

  ### 2. Login to Firebase
```bash
firebase login
```
---

## Initialization

### App Landing Page Setup

#### 1. Initialize Firebase Hosting

Set up Firebase Hosting:
```bash
firebase init hosting
```
1. Choose your Firebase project or create a new one.
2. When prompted:
   - **What do you want to use as your public directory?** Press **Enter** (default: `public`).
   - **Configure as a single-page app?** Press **N** (No).
   - **Set up automatic builds and deploys with GitHub?** Press **N** (No).
   - **File public/index.html already exists. Overwrite?** Press **N** (No).
  

#### 2. Test Locally
You can test your app locally before deploying it:
```bash
firebase serve
```

#### 3. Deployment
  ```bash
  firebase deploy --only hosting
  ```
---

### AI Backend functions setup

#### 1. Initialize Firebase Functions

Set up Firebase Functions:
```bash
firebase init functions
```
1. Choose your Firebase project or create a new one.
2. When prompted:
   - Select `JavaScript` as the language for functions.
   - Install dependencies when asked.

#### 2. Test Locally
You can test your app locally before deploying it:
```bash
firebase emulators:start --only functions
```
#### 3. Deployment

```bash
  firebase deploy --only functions
  ```
---

## Configuration

### App Landing Page Configuration

Update `public/images/hero.png`, `public/images/logo.png`, `public/images/favicon.ico` with your own app images.  

Edit the `public/config.js` file to customize the app's details:

```javascript
const CONFIG = {
    APP_NAME: "YOUR_APP_NAME",
    DEVELOPER_OR_COMPANY_NAME: "YOUR_NAME_OR_COMPANY_NAME",
    WEBSITE_TITLE: "App Landing Page", // Used for SEO title
    WEBSITE_DESCRIPTION: "Meta description (120-160 characters)", // Used for SEO description
    CONTACT_EMAIL: "test@example.com",
    PRIVACY_POLICY_LAST_UPDATE_DATE: "2024-12-01",
    TERMS_AND_SERVICE_LAST_UPDATE_DATE: "2024-12-01",
    PLAYSTORE_URL: "https://play.google.com/",
    APPSTORE_URL: "https://apps.apple.com/",
};

const TEXT_CONTENT = {
    // Hero Section
    HERO_TITLE: "[Insert Hero Title Here]",
    HERO_SUBTITLE: "[Insert Hero Subtitle Here. Example: Discover innovative app ideas for your next big project!]",

    // Problem Section
    PROBLEM_SECTION_TITLE: "[Insert Problem Section Title Here]",
    PROBLEM_SECTION_TEXT: "[Insert Problem Description Here. Example: Struggling to come up with unique app ideas? You're not alone.]",
    PROBLEM_CARD_TITLE1: "[Insert Problem Card Title 1 Here. Example: Lack of Inspiration]",
    PROBLEM_CARD_TEXT1: "[Insert Problem Card Text 1 Here. Example: It can be challenging to think of app ideas that stand out from the crowd.]",
    PROBLEM_CARD_TITLE2: "[Insert Problem Card Title 2 Here. Example: Unclear Development Costs]",
    PROBLEM_CARD_TEXT2: "[Insert Problem Card Text 2 Here. Example: Not knowing the development costs can prevent you from moving forward with your idea.]",
    PROBLEM_CARD_TITLE3: "[Insert Problem Card Title 3 Here. Example: Uncertain Earning Potential]",
    PROBLEM_CARD_TEXT3: "[Insert Problem Card Text 3 Here. Example: Without understanding the earning potential, it’s hard to know if an app is worth pursuing.]",

    // Feature Section
    FEATURE_SECTION_TITLE: "[Insert Feature Section Title Here]",
    FEATURE_CARD_TITLE1: "[Insert Feature Card Title 1 Here. Example: Discover New Ideas]",
    FEATURE_CARD_TEXT1: "[Insert Feature Card Text 1 Here. Example: Find unique app ideas across various categories to help you start your project.]",
    FEATURE_CARD_TITLE2: "[Insert Feature Card Title 2 Here. Example: Estimate Development Costs]",
    FEATURE_CARD_TEXT2: "[Insert Feature Card Text 2 Here. Example: Get a ballpark estimate for development costs to plan your budget.]",
    FEATURE_CARD_TITLE3: "[Insert Feature Card Title 3 Here. Example: Evaluate Earning Potential]",
    FEATURE_CARD_TEXT3: "[Insert Feature Card Text 3 Here. Example: See estimated earnings for each idea to help you decide if it’s worth pursuing.]",

    // CTA Section
    CTA_SECTION_TITLE: "[Insert CTA Section Title Here. Example: Start Your Journey Today!]",
    CTA_SECTION_TEXT: "[Insert CTA Section Text Here. Example: Explore app ideas, save your favorites, and begin building your next big app today.]"
};
```

---

#### Color Customization

Edit the `:root` CSS variables in `public/styles.css` to customize the colors for your landing page:

```css
:root {
    /* Main Colors */
    --primary: #4A148C; 
    --onPrimary: #FFFFFF; 
    
    /* Complementary Secondary Color */
    --secondary: #2C6B85; 
    --onSecondary: #FFFFFF; 
    
    /* Hero Section */
    --heroBg: #212121; 
    --onHeroBg: #FFFFFF; 
    
    /* Background and Text Colors */
    --background: #F5F5F5; 
    --onBackground: #333333; 
    
    /* Footer */
    --footerBg: #121212; 
    --onFooterBg: #FFFFFF; 
}
```


#### AI Prompt for Better Config File

If you're unsure about how to fill out the configuration file or want to improve the content based on your app's description, you can use this prompt to get a better config file:

**Prompt for AI:**

```
"Please generate a configuration file for my app landing page. My app is about ....". 
```

And paste `public/config.js` file at the end of file.

Using this prompt in an AI tool can help you generate a more tailored and effective configuration for your app's landing page.

---

### AI Backend Functions Configuration

The AI backend functions are pre-configured to support integration with popular APIs like **[Replicate](https://replicate.com/)** and **[OpenAI](https://platform.openai.com/docs/api-reference/)**. By default, all available functions are enabled and are implemented in the `functions/index.js` file. You can easily disable or customize any function by modifying this file.

#### Available AI Functions

1. **Replicate API Functions**:
   These functions handle interactions with the Replicate API.
   - `replicateCreatePrediction`
   - `replicateCreateModelPrediction`
   - `replicateGetPredictionStatus`
   - `replicateCancelPrediction`

   You can find these functions in the `api/replicate.js` file. Replicate API documentation for request and response: https://replicate.com/docs/topics/predictions 

2. **OpenAI API Functions**:
   These functions interact with the OpenAI API for generating text and images.
   - `openAiCreateTextCompletion`
   - `openAiCreateImage`

   These are located in the `api/openai.js` file. OpenAI API documentation for request and response: https://platform.openai.com/docs/api-reference/chat

#### API Key Management

To securely store and access your API keys, you can use **Google Cloud Secret Manager**. This is a secure and scalable way to manage sensitive information like API keys. Below are the steps to retrieve and store your keys securely.

##### Steps to Obtain API Keys

1. **OpenAI API Key**:
   - To use OpenAI’s API, you need an API key. You can obtain it by signing up for OpenAI at [OpenAI's API page](https://platform.openai.com/signup).
   - After signing in, go to the **API Keys** section under your account settings to create and retrieve your API key.

2. **Replicate API Key**:
   - To use Replicate's API, create an account on [Replicate](https://replicate.com/).
   - Once logged in, go to the **API** section of your account dashboard, where you can generate and retrieve your API key.

##### Saving API Keys with Google Cloud Secret Manager

Once you have your API keys, you can securely store them in Google Cloud Secret Manager.

1. **Enable Secret Manager**:
   - In the Google Cloud Console, go to the **Secret Manager** page and enable the API.
   
2. **Create Secrets**:
   - Click **Create Secret** and enter the name for the secret (e.g., `OPENAI_API_KEY` or `REPLICATE_API_KEY`).
   - Paste your API key in the **Secret Value** field and click **Create**.

3. **Grant Access**:
   - Ensure that the service account running your application has **access to the secret**. You can grant access by setting the appropriate permissions to allow your app to retrieve secrets.


#### Disabling Functions

If you don't need any of the API endpoints, simply **comment out the respective line** in the `functions/index.js` file:

```javascript
// Replicate API Function exports
exports.replicateCreatePrediction = replicateFunctions.createPrediction;
exports.replicateCreateModelPrediction = replicateFunctions.createModelPrediction;
exports.replicateGetPredictionStatus = replicateFunctions.getPredictionStatus;
exports.replicateCancelPrediction = replicateFunctions.cancelPrediction;

// OpenAI API Function exports
exports.openAiCreateTextCompletion = openAiFunctions.createTextCompletion;
exports.openAiCreateImage = openAiFunctions.createImage;
```

#### API Response Structure

All API responses are returned in JSON format. The structure follows this format:

```json
{
  "statusCode": <statusCode>,
  "errorMessage": <errorMessage>,
  "data": <data>
}
```

- `statusCode`: Represents the HTTP status code of the response.
- `errorMessage`: If an error occurs, this field contains the error message. If there’s no error, this field will be empty or `null`.
- `data`: This field contains the actual response data returned by the AI API, such as predictions, results, or generated content based on each AI api's response object.

#### Validation

By default, all API requests require **user authentication** via Firebase, primarily to secure access to the AI APIs due to their potential cost. Before making any API request, the user must be authenticated. If you want to allow unauthenticated access to any endpoint (e.g., for testing or development purposes), you can disable authentication by setting `requireAuth: false` in the validation function:

```javascript
createPrediction: onRequest(async (req, res) => {
    cors(req, res, async () => {
        if (!await Validation.validateAll(req, res, { requireAuth: false })) return; <--- IN THIS LINE WE SET AUTH REQUIREMENT FALSE
        await makeApiRequest(`${REPLICATE_API_BASE_URL}/predictions`, "post", getReplicateApiKey(), req.body, res);
    });
}),
```

