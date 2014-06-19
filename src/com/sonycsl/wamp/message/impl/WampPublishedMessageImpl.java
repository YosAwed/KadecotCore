/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.WampPublishedMessage;

import org.json.JSONArray;
import org.json.JSONException;

public class WampPublishedMessageImpl extends WampAbstractMessage implements WampPublishedMessage {

    private static final int REQUEST_ID_INDEX = 1;
    private static final int PUBLICATION_ID_INDEX = 2;

    public static WampMessage create(int requestId, int publicationId) {
        return new WampPublishedMessageImpl(new JSONArray().put(WampMessageType.PUBLISHED)
                .put(requestId).put(publicationId));
    }

    public WampPublishedMessageImpl(JSONArray msg) {
        super(msg);
        try {
            if (msg.getInt(0) != WampMessageType.PUBLISHED) {
                throw new IllegalArgumentException("message type is mismatched");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean isPublishedMessage() {
        return true;
    }

    @Override
    public WampPublishedMessage asPublishedMessage() {
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
    public int getPublicationId() {
        try {
            return toJSON().getInt(PUBLICATION_ID_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no publication id");
        }
    }

}
