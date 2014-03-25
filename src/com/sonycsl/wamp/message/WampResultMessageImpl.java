
package com.sonycsl.wamp.message;

import com.sonycsl.wamp.WampAbstractMessage;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessageType;
import com.sonycsl.wamp.WampResultMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WampResultMessageImpl extends WampAbstractMessage implements WampResultMessage {

    private static final int REQUEST_ID_INDEX = 1;
    private static final int DETAILS_INDEX = 2;
    private static final int ARGUMENTS_INDEX = 3;
    private static final int ARGUMENTS_KW_INDEX = 4;

    public static WampMessage create(int requestId, JSONObject details) {
        return new WampResultMessageImpl(new JSONArray().put(WampMessageType.RESULT).put(requestId)
                .put(details));
    }

    public static WampMessage create(int requestId, JSONObject details, JSONArray arguments) {
        return new WampResultMessageImpl(new JSONArray().put(WampMessageType.RESULT).put(requestId)
                .put(details).put(arguments));
    }

    public static WampMessage create(int requestId, JSONObject details, JSONArray arguments,
            JSONObject argumentsKw) {
        return new WampResultMessageImpl(new JSONArray().put(WampMessageType.RESULT).put(requestId)
                .put(details).put(arguments).put(argumentsKw));
    }

    public WampResultMessageImpl(JSONArray msg) {
        super(msg);
    }

    @Override
    public boolean isResuleMessage() {
        return true;
    }

    @Override
    public WampResultMessage asResultMessage() {
        return this;
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
            throw new IllegalArgumentException("there is no detail");
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
    public JSONObject getArgumentKw() {
        try {
            return toJSON().getJSONObject(ARGUMENTS_KW_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no argumentkw");
        }
    }

}
