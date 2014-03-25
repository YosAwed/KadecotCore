
package com.sonycsl.wamp.message;

import com.sonycsl.wamp.WampAbstractMessage;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessageType;
import com.sonycsl.wamp.WampRegisteredMessage;

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
