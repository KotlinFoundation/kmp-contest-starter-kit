const axios = require("axios");
const { logger } = require("firebase-functions/v2");
const e = require("cors");


// Helper function to send API responses
function sendApiResponse(res, statusCode, data = null, errorMessage = null) {
    res.status(statusCode).send({
      statusCode: statusCode,
      errorMessage: errorMessage,
      data: data
    });
  }

// Helper function to make API requests
async function makeApiRequest(url, method, apiKey, data = null, res) {
  try {

      const config = {
        url,
        method,
        headers: {
            "Authorization": `Bearer ${apiKey}`,
            "Content-Type": "application/json",
            "Prefer": "wait"
        },
      };

      // Only add data if the method is not GET (GET requests should not have a body)
      if (method !== 'GET' && data) {
        config.data = data;
      }

      const response = await axios(config);
      sendApiResponse(res, 200, response.data);
  } catch (error) {
      logger.error("Error:", error);

      if (error.response) {
        // If there's a response error (e.g., 400, 500, etc.)
        const errorMessage = error.response.data.error ? error.response.data.error.message : 'Internal Server Error.';
        const statusCode = error.response.status; // Get the HTTP status code from the response
        
        sendApiResponse(res, statusCode, null, errorMessage);
      } else {
        // If no response (e.g., network issues)
        sendApiResponse(res, 500, null, "Internal Server Error.");
      }
  }
}
  
module.exports = { sendApiResponse, makeApiRequest };