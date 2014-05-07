/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.WampWelcomeMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WampWelcomeMessageImpl extends WampAbstractMessage implements WampWelcomeMessage {

    private static final int SESSION_INDEX = 1;
    private static final int DETAILS_INDEX = 2;

    public static WampMessage create(int session, JSONObject details) {
        return new WampWelcomeMessageImpl(new JSONArray().put(WampMessageType.WELCOME).put(session)
                .put(details));
    }

    public WampWelcomeMessageImpl(JSONArray msg) {
        super(msg);
    }

    @Override
    public boolean isWelcomeMessage() {
        return true;
    }

    @Override
    public WampWelcomeMessage asWelcomeMessage() {
        return this;
    }

    @Override
    public int getSession() {
        try {
            return toJSON().getInt(SESSION_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no session");
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

}
