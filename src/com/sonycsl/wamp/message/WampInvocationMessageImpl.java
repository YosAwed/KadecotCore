
package com.sonycsl.wamp.message;

import com.sonycsl.wamp.WampAbstractMessage;
import com.sonycsl.wamp.WampInvocationMessage;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessageType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WampInvocationMessageImpl extends WampAbstractMessage implements WampInvocationMessage {

    private static final int REQUEST_ID_INDEX = 1;
    private static final int REGISTRATION_ID_INDEX = 2;
    private static final int DETAILS_INDEX = 3;
    private static final int ARGUMENTS_INDEX = 4;
    private static final int ARGUMENTS_KW_INDEX = 5;

    public static WampMessage create(int requestId, int registrationId, JSONObject details) {
        return new WampInvocationMessageImpl(new JSONArray().put(WampMessageType.INVOCATION)
                .put(requestId).put(registrationId).put(details));
    }

    public static WampMessage create(int requestId, int registrationId, JSONObject details,
            JSONArray arguments) {
        return new WampInvocationMessageImpl(new JSONArray().put(WampMessageType.INVOCATION)
                .put(requestId).put(registrationId).put(details).put(arguments));
    }

    public static WampMessage create(int requestId, int registrationId, JSONObject details,
            JSONArray arguments, JSONObject argumentsKw) {
        return new WampInvocationMessageImpl(new JSONArray().put(WampMessageType.INVOCATION)
                .put(requestId).put(registrationId).put(details).put(arguments).put(argumentsKw));
    }

    public WampInvocationMessageImpl(JSONArray msg) {
        super(msg);
    }

    @Override
    public boolean isInvocationMessage() {
        return true;
    }

    @Override
    public WampInvocationMessage asInvocationMessage() {
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
    public int getRegistrationId() {
        try {
            return toJSON().getInt(REGISTRATION_ID_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no registration id");
        }
    }

    @Override
    public JSONObject getDetails() {
        try {
            return toJSON().getJSONObject(DETAILS_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no details");
        }
    }

    @Override
    public boolean hasArguments() {
        return !toJSON().isNull(ARGUMENTS_INDEX);
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
    public boolean hasArgumentsKw() {
        return !toJSON().isNull(ARGUMENTS_KW_INDEX);
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
