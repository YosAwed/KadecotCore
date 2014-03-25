
package com.sonycsl.wamp.message;

import com.sonycsl.wamp.WampAbstractMessage;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessageType;
import com.sonycsl.wamp.WampUnregisteredMessage;

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
