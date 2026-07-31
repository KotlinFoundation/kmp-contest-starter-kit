const { onRequest } = require("firebase-functions/v2/https");
const cors = require("cors")({ origin: true });
const { makeApiRequest } = require("../utils/utils");
const Validation = require("../utils/validation");
const { defineSecret } = require("firebase-functions/params");

const OPENAI_API_KEY = defineSecret("OPENAI_API_KEY")



const openAiFunctions = {

    /*
        Example request body:
        {
            "model": "gpt-4o-mini",
            "messages": [
                {
                "role": "user",
                "content": "Say this is a test!"
                }
            ]
        }
    */
    createTextCompletion: onRequest({secrets: [OPENAI_API_KEY]}, async (req, res) => {
        cors(req, res, async () => {
            if (!await Validation.validateAll(req, res, { requireAuth: true })) return;
            await makeApiRequest("https://api.openai.com/v1/chat/completions", "post", OPENAI_API_KEY.value(), req.body, res);
        });
    }),

    /*
        Generates image from the input text. Example request body:
        {
            "model": "dall-e-3",
            "prompt": "Logo of creative app in neon colors with a modern look",
            "n": 1,
            "size": "512x512",
        }
    */

    createImage: onRequest({secrets: [OPENAI_API_KEY]}, async (req, res) => {
        cors(req, res, async () => {
            if (!await Validation.validateAll(req, res, { requireAuth: true })) return;
            await makeApiRequest("https://api.openai.com/v1/images/generations", "post", OPENAI_API_KEY.value(), req.body, res);
        });
    }),

};



module.exports = openAiFunctions;