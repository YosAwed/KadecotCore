/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.WampPublishMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WampPublishMessageImpl extends WampAbstractMessage implements WampPublishMessage {

    private static final int REQUEST_ID_INDEX = 1;
    private static final int OPTIONS_INDEX = 2;
    private static final int TOPIC_INDEX = 3;
    private static final int ARGUMENTS_INDEX = 4;
    private static final int ARGUMENTS_KW_INDEX = 5;

    public static WampMessage create(int requestId, JSONObject options, String topic) {
        return new WampPublishMessageImpl(new JSONArray().put(WampMessageType.PUBLISH)
                .put(requestId).put(options).put(topic));
    }

    public static WampMessage create(int requestId, JSONObject options, String topic,
            JSONArray arguments) {
        return new WampPublishMessageImpl(new JSONArray().put(WampMessageType.PUBLISH)
                .put(requestId).put(options).put(topic).put(arguments));
    }

    public static WampMessage create(int requestId, JSONObject options, String topic,
            JSONArray arguments, JSONObject argumentsKw) {
        return new WampPublishMessageImpl(new JSONArray().put(WampMessageType.PUBLISH)
                .put(requestId).put(options).put(topic).put(arguments).put(argumentsKw));
    }

    public WampPublishMessageImpl(JSONArray msg) {
        super(msg);
    }

    @Override
    public boolean isPublishMessage() {
        return true;
    }

    @Override
    public WampPublishMessage asPublishMessage() {
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
            throw new IllegalArgumentException("there is no options");
        }
    }

    @Override
    public String getTopic() {
        try {
            return toJSON().getString(TOPIC_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no topic");
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
