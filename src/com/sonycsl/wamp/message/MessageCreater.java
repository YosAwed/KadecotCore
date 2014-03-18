
package com.sonycsl.wamp.message;

import org.json.JSONArray;
import org.json.JSONObject;

public class MessageCreater {

    public static JSONArray createWelcomeMessage(int sessionId, JSONObject details) {
        throw new NullPointerException();
    }

    public static JSONArray createGoodbyeMessage(JSONObject details, String reason) {
        throw new NullPointerException();
    }

    public static JSONArray createSubscribedMessage(int subscriptionId) {
        throw new NullPointerException();
    }

    public static JSONArray createPublishedMessage(int requestId, int publicationId) {
        throw new NullPointerException();
    }

    public static JSONArray createEventMessage(int subscriptionId, int publicationId,
            JSONObject details, JSONArray arguments, JSONObject argumentKw) {
        throw new NullPointerException();
    }

    public static JSONArray createErrorMessage(int errorType, int requestId, JSONObject details,
            String error) {
        throw new NullPointerException();
    }

}
