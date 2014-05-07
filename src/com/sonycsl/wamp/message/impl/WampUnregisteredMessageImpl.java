/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.WampUnregisteredMessage;

import org.json.JSONArray;
import org.json.JSONException;

public class WampUnregisteredMessageImpl extends WampAbstractMessage implements
        WampUnregisteredMessage {

    private static final int REQUEST_ID_INDEX = 1;

    public static WampMessage create(int requestId) {
        return new WampUnregisteredMessageImpl(new JSONArray().put(WampMessageType.UNREGISTERED)
                .put(requestId));
    }

    public WampUnregisteredMessageImpl(JSONArray msg) {
        super(msg);
    }

    @Override
    public boolean isUnregisteredMessage() {
        return true;
    }

    @Override
    public WampUnregisteredMessage asUnregisteredMessage() {
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

}
