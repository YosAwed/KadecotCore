/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.WampRegisteredMessage;

import org.json.JSONArray;
import org.json.JSONException;

public class WampRegisteredMessageImpl extends WampAbstractMessage implements WampRegisteredMessage {

    private static final int REQUEST_ID_INDEX = 1;
    private static final int REGISTRATION_ID_INDEX = 2;

    public static WampMessage create(int requestId, int registrationId) {
        return new WampRegisteredMessageImpl(new JSONArray().put(WampMessageType.REGISTERED)
                .put(requestId).put(registrationId));
    }

    public WampRegisteredMessageImpl(JSONArray msg) {
        super(msg);
        try {
            if (msg.getInt(0) != WampMessageType.REGISTERED) {
                throw new IllegalArgumentException("message type is mismatched");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean isRegisteredMessage() {
        return true;
    }

    @Override
    public WampRegisteredMessage asRegisteredMessage() {
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
            throw new IllegalArgumentException("there is no registraion id");
        }
    }

}
