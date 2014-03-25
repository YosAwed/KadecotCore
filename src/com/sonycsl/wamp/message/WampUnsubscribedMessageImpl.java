
package com.sonycsl.wamp.message;

import com.sonycsl.wamp.WampAbstractMessage;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessageType;
import com.sonycsl.wamp.WampUnsubscribedMessage;

import org.json.JSONArray;
import org.json.JSONException;

public class WampUnsubscribedMessageImpl extends WampAbstractMessage implements
        WampUnsubscribedMessage {

    private static final int REQUEST_ID_INDEX = 1;

    public static WampMessage create(int requestId) {
        return new WampUnsubscribedMessageImpl(new JSONArray().put(WampMessageType.UNSUBSCRIBED)
                .put(requestId));
    }

    public WampUnsubscribedMessageImpl(JSONArray msg) {
        super(msg);
    }

    @Override
    public boolean isUnsubscribedMessage() {
        return true;
    }

    @Override
    public WampUnsubscribedMessage asUnsubscribedMessage() {
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
