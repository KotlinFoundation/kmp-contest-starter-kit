const admin = require("firebase-admin");
const { sendApiResponse } = require("./utils");
const { logger } = require("firebase-functions/v2");

class Validation {

  // Only POST methods are allowed
  static validateRequestMethod(req, res) {
    if (req.method !== "POST") {
      sendApiResponse(res, 405, null, "Method Not Allowed. Only POST requests are allowed.");
      return false;
    }
    return true;
  }

  // Validate Authorization header containing Bearer token. Ex: Authorization: Bearer UserToken
  static async validateAuthorization(req, res) {
    const { authorization } = req.headers;

      // Check if the user is authenticated against Firebase
    if (!authorization) {
      sendApiResponse(res, 403, null, "Missing Authorization header. User is not authenticated.");
      return false;
    }

      // Check if the Authorization header is in the correct format
    const parts = authorization.split("Bearer ");
    if (parts.length !== 2) {
      sendApiResponse(res, 403, null, "Invalid Authorization header format. Please, reauthenticate.");
      return false;
    }

    // Retrieve Firebase ID token from Authorization header
    req.idToken = parts[1];
    if (!req.idToken) {
      sendApiResponse(res, 403, null, "User is not authenticated. Please provide a valid user token.");
      return false;
    }

    return true;
  }

  //  Validate Firebase ID token
  static async validateFirebaseToken(req, res) {
    try {
      const decodedToken = await admin.auth().verifyIdToken(req.idToken);
      req.user = decodedToken;
      return true;
    } catch (error) {
      logger.error("Firebase ID token has expired.", error);
      sendApiResponse(res, 403, null, "User token has expired. Please re-authenticate.");
      return false;
    }
  }

  // Combined validation method. 
  // By default checks if the request is a POST request, if the user is authenticated, and if the Firebase ID token is valid
  static async validateAll(req, res, options = { requirePostRequest: true, requireAuth: true }) {
    const { requirePostRequest = true, requireAuth = true } = options;

    if (requirePostRequest && !Validation.validateRequestMethod(req, res)) return false;
    if (requireAuth && !await Validation.validateAuthorization(req, res)) return false;
    if (requireAuth && !await Validation.validateFirebaseToken(req, res)) return false;

    // At this point, the user is authenticated. You can access the user's id using req.user.uid
    // const userId = req.user.uid;

    return true;
  }

  
}

module.exports = Validation;