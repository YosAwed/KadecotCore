
package com.sonycsl.wamp.message;

import com.sonycsl.wamp.WampAbstractMessage;
import com.sonycsl.wamp.WampCallMessage;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessageType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WampCallMessageImpl extends WampAbstractMessage implements WampCallMessage {

    private static final int REQUEST_ID_INDEX = 1;
    private static final int OPTIONS_INDEX = 2;
    private static final int PROCEDURE_INDEX = 3;
    private static final int ARGUMENTS_INDEX = 4;
    private static final int ARGUMENTS_KW_INDEX = 5;

    public static WampMessage create(int requestId, JSONObject option, String procedure) {
        return new WampCallMessageImpl(new JSONArray().put(WampMessageType.CALL).put(requestId)
                .put(option).put(procedure));
    }

    public static WampMessage create(int requestId, JSONObject options, String procedure,
            JSONArray arguments) {
        return new WampCallMessageImpl(new JSONArray().put(WampMessageType.CALL).put(requestId)
                .put(options).put(procedure).put(arguments));
    }

    public static WampMessage create(int requestId, JSONObject options, String procedure,
            JSONArray arguments, JSONObject argumentsKw) {
        return new WampCallMessageImpl(new JSONArray().put(WampMessageType.CALL).put(requestId)
                .put(options).put(procedure).put(arguments).put(argumentsKw));
    }

    public WampCallMessageImpl(JSONArray msg) {
        super(msg);
    }

    @Override
    public boolean isCallMessage() {
        return true;
    }

    @Override
    public WampCallMessage asCallMessage() {
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
            throw new IllegalArgumentException("there is no detail");
        }
    }

    @Override
    public String getProcedure() {
        try {
            return toJSON().getString(PROCEDURE_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no procedure");
        }
    }

    @Override
    public boolean hasArguments() {
        return toJSON().length() + 1 > ARGUMENTS_INDEX;
    }

    @Override
    public JSONArray getArguments() {
        try {
            return toJSON().getJSONArray(ARGUMENTS_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no arguments");
        }
    }

    @Override
    public boolean hasArgumentsKw() {
        return toJSON().length() + 1 > ARGUMENTS_KW_INDEX;
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
