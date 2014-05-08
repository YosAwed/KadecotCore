
package com.sonycsl.wamp.message.impl;

import com.sonycsl.wamp.message.WampErrorMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WampErrorMessageImpl extends WampAbstractMessage implements WampErrorMessage {

    private static final int REQUEST_TYPE_INDEX = 1;
    private static final int REQUEST_ID_INDEX = 2;
    private static final int DETAILS_INDEX = 3;
    private static final int URI_INDEX = 4;
    private static final int ARGUMENTS_INDEX = 5;
    private static final int ARGUMENTS_KW_INDEX = 6;

    public static WampMessage create(int requestType, int requestId, JSONObject details,
            String error) {
        return new WampErrorMessageImpl(new JSONArray().put(WampMessageType.ERROR).put(requestType)
                .put(requestId).put(details).put(error));
    }

    public static WampMessage create(int requestType, int requestId, JSONObject details,
            String error, JSONArray arguments) {
        return new WampErrorMessageImpl(new JSONArray().put(WampMessageType.ERROR).put(requestType)
                .put(requestId).put(details).put(error).put(arguments));
    }

    public static WampMessage create(int requestType, int requestId, JSONObject details,
            String error, JSONArray arguments, JSONObject argumentsKw) {
        return new WampErrorMessageImpl(new JSONArray().put(WampMessageType.ERROR).put(requestType)
                .put(requestId).put(details).put(error).put(arguments).put(argumentsKw));
    }

    public WampErrorMessageImpl(JSONArray msg) {
        super(msg);
    }

    @Override
    public boolean isErrorMessage() {
        return true;
    }

    @Override
    public WampErrorMessage asErrorMessage() {
        return this;
    }

    @Override
    public int getRequestType() {
        try {
            return toJSON().getInt(REQUEST_TYPE_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no request type");
        }
    }

    @Override
    public int getRequestId() {
        try {
            return toJSON().getInt(REQUEST_ID_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no request id");
        }
    }

    @Override
    public JSONObject getDetails() {
        try {
            return toJSON().getJSONObject(DETAILS_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no option");
        }
    }

    @Override
    public String getUri() {
        try {
            return toJSON().getString(URI_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no uri");
        }
    }

    @Override
    public JSONArray getArguments() {
        try {
            return toJSON().getJSONArray(ARGUMENTS_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no argument");
        }
    }

    @Override
    public JSONObject getArgumentsKw() {
        try {
            return toJSON().getJSONObject(ARGUMENTS_KW_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no argumentkw");
        }
    }

}
