
package com.sonycsl.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.WampYieldMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WampYieldMessageImpl extends WampAbstractMessage implements WampYieldMessage {

    private static final int REQUEST_ID_INDEX = 1;
    private static final int OPTIONS_INDEX = 2;
    private static final int ARGUMENTS_INDEX = 3;
    private static final int ARGUMENTS_KW_INDEX = 4;

    public static WampMessage create(int requestId, JSONObject options) {
        return new WampYieldMessageImpl(new JSONArray().put(WampMessageType.YIELD).put(requestId)
                .put(options));
    }

    public static WampMessage create(int requestId, JSONObject options, JSONArray arguments) {
        return new WampYieldMessageImpl(new JSONArray().put(WampMessageType.YIELD).put(requestId)
                .put(options).put(arguments));
    }

    public static WampMessage create(int requestId, JSONObject options, JSONArray arguments,
            JSONObject argumentsKw) {
        return new WampYieldMessageImpl(new JSONArray().put(WampMessageType.YIELD).put(requestId)
                .put(options).put(arguments).put(argumentsKw));
    }

    public WampYieldMessageImpl(JSONArray msg) {
        super(msg);
    }

    @Override
    public boolean isYieldMessage() {
        return true;
    }

    @Override
    public WampYieldMessage asYieldMessage() {
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
    public JSONObject getOptions() {
        try {
            return toJSON().getJSONObject(OPTIONS_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no option");
        }
    }

    @Override
    public boolean hasArguments() {
        return toJSON().length() > ARGUMENTS_INDEX;
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
        return toJSON().length() > ARGUMENTS_KW_INDEX;
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
