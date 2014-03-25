
package com.sonycsl.wamp.message;

import com.sonycsl.wamp.WampAbstractMessage;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessageType;
import com.sonycsl.wamp.WampUnsubscribeMessage;

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
