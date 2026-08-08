const { onRequest } = require("firebase-functions/v2/https");
const cors = require("cors")({ origin: true });
const { makeApiRequest, sendApiResponse } = require("../utils/utils");
const Validation = require("../utils/validation");
const { defineSecret } = require("firebase-functions/params");


const REPLICATE_API_BASE_URL = "https://api.replicate.com/v1";
const REPLICATE_API_KEY = defineSecret("REPLICATE_API_KEY");

// Path segments interpolated into the Replicate URL (model owner/name, prediction id).
// Reject anything outside this charset so crafted values (e.g. "..") can't be
// URL-normalized into a different Replicate API path called with our key.
const SAFE_PATH_SEGMENT = /^[A-Za-z0-9._-]+$/;

function validatePathParams(res, params) {
    for (const [name, value] of Object.entries(params)) {
        if (!value || !SAFE_PATH_SEGMENT.test(value)) {
            sendApiResponse(res, 400, null, `Invalid or missing query parameter: ${name}`);
            return false;
        }
    }
    return true;
}


const replicateFunctions = {

    /*
        Community models. Example request body:
        {
        "version": "5c7d5dc6dd8bf75c1acaa8565735e7986bc5b66206b55cca93cb72c9bf15ccaa",
        "input": {
            "text": "KMPStarterKit.",
        },
    */
    createPrediction: onRequest({secrets: [REPLICATE_API_KEY]}, async (req, res) => {
        cors(req, res, async () => {
            // requireAuth is intentionally true: this endpoint spends the developer's
            // Replicate credits. Without Firebase ID-token auth, anyone who discovers the
            // function URL can run arbitrary models on the developer's account.
            if (!await Validation.validateAll(req, res, { requireAuth: true })) return;
            await makeApiRequest(`${REPLICATE_API_BASE_URL}/predictions`, "post", REPLICATE_API_KEY.value(), req.body, res);
        });
    }),

    /*
        Official models. 
        Example request query:
        {
            "model_owner": "black-forest-labs",
            "model_name": "flux-1.1-pro"
        }
        Example request body:
        {
            "input": {
                "prompt": "black forest gateau cake spelling out the words \\"FLUX 1 . 1 Pro\\", tasty, food photography",
                "aspect_ratio": "1:1",
                "output_format": "webp",
                "output_quality": 80,
                "safety_tolerance": 2
                "prompt_upsampling": true
            },
        }
    
    */
    createModelPrediction: onRequest({secrets: [REPLICATE_API_KEY]}, async (req, res) => {
        cors(req, res, async () => {
            if (!await Validation.validateAll(req, res, { requireAuth: true })) return;
            if (!validatePathParams(res, { model_owner: req.query.model_owner, model_name: req.query.model_name })) return;
            await makeApiRequest(`${REPLICATE_API_BASE_URL}/models/${req.query.model_owner}/${req.query.model_name}/predictions`, "post", REPLICATE_API_KEY.value(), req.body, res);
        });
    }),

    /* 
        Get prediction status.
        Example request query:
        {
            "id": "prediction_id"
        }
    */
    getPredictionStatus: onRequest({secrets: [REPLICATE_API_KEY]}, async (req, res) => {
        cors(req, res, async () => {
            if (!await Validation.validateAll(req, res, { requirePostRequest: false })) return;
            if (!validatePathParams(res, { id: req.query.id })) return;
            await makeApiRequest(`${REPLICATE_API_BASE_URL}/predictions/${req.query.id}`, "get", REPLICATE_API_KEY.value(), null, res);
        });
    }),

    /*
        Cancel prediction.
        Example request query:
        {
            "id": "prediction_id"
        }
    */
    cancelPrediction: onRequest({secrets: [REPLICATE_API_KEY]}, async (req, res) => {
        cors(req, res, async () => {
            if (!await Validation.validateAll(req, res)) return;
            if (!validatePathParams(res, { id: req.query.id })) return;
            await makeApiRequest(`${REPLICATE_API_BASE_URL}/predictions/${req.query.id}/cancel`, "post", REPLICATE_API_KEY.value(), null, res);
        });
    }),

}



module.exports = replicateFunctions;