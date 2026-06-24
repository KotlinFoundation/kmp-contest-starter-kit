const { onRequest } = require("firebase-functions/v2/https");
const cors = require("cors")({ origin: true });
const { makeApiRequest } = require("../utils/utils");
const Validation = require("../utils/validation");
const { defineSecret } = require("firebase-functions/params");


const REPLICATE_API_BASE_URL = "https://api.replicate.com/v1";
const REPLICATE_API_KEY = defineSecret("REPLICATE_API_KEY");


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
            if (!await Validation.validateAll(req, res, { requireAuth: false })) return;
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
            await makeApiRequest(`${REPLICATE_API_BASE_URL}/predictions/${req.query.id}/cancel`, "post", REPLICATE_API_KEY.value(), null, res);
        });
    }),

}



module.exports = replicateFunctions;