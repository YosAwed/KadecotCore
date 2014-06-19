/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.WampResultMessage;
import com.sonycsl.wamp.util.NullChecker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WampResultMessageImpl extends WampAbstractMessage implements WampResultMessage {

    private static final int REQUEST_ID_INDEX = 1;
    private static final int DETAILS_INDEX = 2;
    private static final int ARGUMENTS_INDEX = 3;
    private static final int ARGUMENTS_KW_INDEX = 4;

    public static WampMessage create(int requestId, JSONObject details) {
        NullChecker.nullCheck(details);
        return new WampResultMessageImpl(new JSONArray().put(WampMessageType.RESULT).put(requestId)
                .put(details));
    }

    public static WampMessage create(int requestId, JSONObject details, JSONArray arguments) {
        NullChecker.nullCheck(details, arguments);
        return new WampResultMessageImpl(new JSONArray().put(WampMessageType.RESULT).put(requestId)
                .put(details).put(arguments));
    }

    public static WampMessage create(int requestId, JSONObject details, JSONArray arguments,
            JSONObject argumentsKw) {
        NullChecker.nullCheck(details, arguments, argumentsKw);
        return new WampResultMessageImpl(new JSONArray().put(WampMessageType.RESULT).put(requestId)
                .put(details).put(arguments).put(argumentsKw));
    }

    public WampResultMessageImpl(JSONArray msg) {
        super(msg);
        try {
            if (msg.getInt(0) != WampMessageType.RESULT) {
                throw new IllegalArgumentException("message type is mismatched");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean isResultMessage() {
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
