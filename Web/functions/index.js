const {initializeApp} = require("firebase-admin/app");
const replicateFunctions = require("./api/replicate");
const openAiFunctions = require("./api/openai");



// Initialize Firebase app
initializeApp();


//If you don't want any of the API endpoints, comment out the respective line

// Replicate API Function exports
exports.replicateCreatePrediction = replicateFunctions.createPrediction;
exports.replicateCreateModelPrediction = replicateFunctions.createModelPrediction;
exports.replicateGetPredictionStatus = replicateFunctions.getPredictionStatus;
exports.replicateCancelPrediction = replicateFunctions.cancelPrediction;


// OpenAI API Function exports
exports.openAiCreateTextCompletion = openAiFunctions.createTextCompletion;
exports.openAiCreateImage = openAiFunctions.createImage;
