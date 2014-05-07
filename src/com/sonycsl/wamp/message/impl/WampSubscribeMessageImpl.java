/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.WampSubscribeMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WampSubscribeMessageImpl extends WampAbstractMessage implements WampSubscribeMessage {

    private static final int REQUEST_ID_INDEX = 1;
    private static final int OPTIONS_INDEX = 2;
    private static final int TOPIC_INDEX = 3;

    public static WampMessage create(int requestId, JSONObject options, String topic) {
        return new WampSubscribeMessageImpl(new JSONArray().put(WampMessageType.SUBSCRIBE)
                .put(requestId).put(options).put(topic));
    }

    public WampSubscribeMessageImpl(JSONArray msg) {
        super(msg);
    }

    @Override
    public boolean isSubscribeMessage() {
        return true;
    }

    @Override
    public WampSubscribeMessage asSubscribeMessage() {
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
    public String getTopic() {
        try {
            return toJSON().getString(TOPIC_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no topic");
        }
    }

}
