/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp.message.impl;

import com.sonycsl.wamp.message.WampAbortMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.util.NullChecker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WampAbortMessageImpl extends WampAbstractMessage implements WampAbortMessage {

    private static final int DETAILS_INDEX = 1;
    private static final int REASON_INDEX = 2;

    public static WampMessage create(JSONObject details, String reason) {
        NullChecker.nullCheck(details, reason);
        return new WampAbortMessageImpl(new JSONArray().put(WampMessageType.ABORT).put(details)
                .put(reason));
    }

    public WampAbortMessageImpl(JSONArray msg) {
        super(msg);
        try {
            if (msg.getInt(0) != WampMessageType.ABORT) {
                throw new IllegalArgumentException("message type is mismatched");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isAbortMessage() {
        return true;
    }

    @Override
    public WampAbortMessage asAbortMessage() {
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
