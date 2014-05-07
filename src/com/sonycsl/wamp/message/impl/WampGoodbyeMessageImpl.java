/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp.message.impl;

import com.sonycsl.wamp.message.WampGoodbyeMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WampGoodbyeMessageImpl extends WampAbstractMessage implements WampGoodbyeMessage {

    private static final int DETAILS_INDEX = 1;
    private static final int REASON_INDEX = 2;

    public static WampMessage create(JSONObject details, String reason) {
        return new WampGoodbyeMessageImpl(new JSONArray().put(WampMessageType.GOODBYE).put(details)
                .put(reason));
    }

    public WampGoodbyeMessageImpl(JSONArray msg) {
        super(msg);
    }

    @Override
    public boolean isGoodbyeMessage() {
        return true;
    }

    @Override
    public WampGoodbyeMessage asGoodbyeMessage() {
        return this;
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
    public String getReason() {
        try {
            return toJSON().getString(REASON_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no reason");
        }
    }

}
