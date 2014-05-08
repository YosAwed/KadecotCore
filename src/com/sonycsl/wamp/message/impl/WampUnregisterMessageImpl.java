
package com.sonycsl.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.WampUnregisterMessage;

import org.json.JSONArray;
import org.json.JSONException;

public class WampUnregisterMessageImpl extends WampAbstractMessage implements WampUnregisterMessage {

    private static final int REQUEST_ID_INDEX = 1;
    private static final int REGISTRATION_ID_INDEX = 2;

    public static WampMessage create(int requestId, int registrationId) {
        return new WampUnregisterMessageImpl(new JSONArray().put(WampMessageType.UNREGISTER)
                .put(requestId).put(registrationId));
    }

    public WampUnregisterMessageImpl(JSONArray msg) {
        super(msg);
    }

    @Override
    public boolean isUnregisterMessage() {
        return true;
    }

    @Override
    public WampUnregisterMessage asUnregisterMessage() {
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
