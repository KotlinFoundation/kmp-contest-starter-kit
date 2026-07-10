const {initializeApp} = require("firebase-admin/app");
const replicateFunctions = require("./api/replicate");
const openAiFunctions = require("./api/openai");



// Initialize Firebase app
initializeApp();


// Enable only the AI providers you have keys for.
//
// Single-provider baseline: by default ONLY Replicate is deployed, so a fresh project needs just
// one key (REPLICATE_API_KEY) in Secret Manager and the first deploy succeeds with zero config.
// To change the set, set AI_PROVIDERS in functions/.env (comma-separated). A provider's endpoints
// are only exported when it is listed, and an unexported function does not bind its secret — so you
// only need the secret(s) for the provider(s) you enable.
//   (unset)                        -> only Replicate endpoints (only REPLICATE_API_KEY needed)
//   AI_PROVIDERS=openai            -> only OpenAI endpoints (only OPENAI_API_KEY needed)
//   AI_PROVIDERS=replicate,openai  -> both (needs both secrets)
const enabledProviders = (process.env.AI_PROVIDERS || "replicate")
  .split(",")
  .map((provider) => provider.trim().toLowerCase())
  .filter(Boolean);

// Replicate API Function exports
if (enabledProviders.includes("replicate")) {
  exports.replicateCreatePrediction = replicateFunctions.createPrediction;
  exports.replicateCreateModelPrediction = replicateFunctions.createModelPrediction;
  exports.replicateGetPredictionStatus = replicateFunctions.getPredictionStatus;
  exports.replicateCancelPrediction = replicateFunctions.cancelPrediction;
}

// OpenAI API Function exports
if (enabledProviders.includes("openai")) {
  exports.openAiCreateTextCompletion = openAiFunctions.createTextCompletion;
  exports.openAiCreateImage = openAiFunctions.createImage;
}
