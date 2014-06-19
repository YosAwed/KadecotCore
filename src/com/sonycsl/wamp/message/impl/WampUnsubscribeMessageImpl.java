/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.WampUnsubscribeMessage;

import org.json.JSONArray;
import org.json.JSONException;

public class WampUnsubscribeMessageImpl extends WampAbstractMessage implements
        WampUnsubscribeMessage {

    private static final int REQUEST_ID_INDEX = 1;
    private static final int SUBSCRIPTION_ID_INDEX = 2;

    public static WampMessage create(int requestId, int subscriptionId) {
        return new WampUnsubscribeMessageImpl(new JSONArray().put(WampMessageType.UNSUBSCRIBE)
                .put(requestId).put(subscriptionId));
    }

    public WampUnsubscribeMessageImpl(JSONArray msg) {
        super(msg);
        try {
            if (msg.getInt(0) != WampMessageType.UNSUBSCRIBE) {
                throw new IllegalArgumentException("message type is mismatched");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isUnsubscribeMessage() {
        return true;
    }

    @Override
    public WampUnsubscribeMessage asUnsubscribeMessage() {
        return this;
    }

    @Override
    public int getRequestId() {
        try {
            return toJSON().getInt(REQUEST_ID_INDEX);
        } catch (Exception e) {
            throw new IllegalArgumentException("there is no request id");
        }
    }

    @Override
    public int getSubscriptionId() {
        try {
            return toJSON().getInt(SUBSCRIPTION_ID_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no subscription id");
        }
    }
}
