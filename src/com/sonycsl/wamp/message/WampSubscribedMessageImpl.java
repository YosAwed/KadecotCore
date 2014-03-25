
package com.sonycsl.wamp.message;

import com.sonycsl.wamp.WampAbstractMessage;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessageType;
import com.sonycsl.wamp.WampSubscribedMessage;

import org.json.JSONArray;
import org.json.JSONException;

public class WampSubscribedMessageImpl extends WampAbstractMessage implements WampSubscribedMessage {

    private static final int REQUEST_ID_INDEX = 1;
    private static final int SUBSCRIPTION_ID_INDEX = 2;

    public static WampMessage create(int requestId, int subscriptionId) {
        return new WampSubscribedMessageImpl(new JSONArray().put(WampMessageType.SUBSCRIBED)
                .put(requestId).put(subscriptionId));
    }

    public WampSubscribedMessageImpl(JSONArray msg) {
        super(msg);
    }

    @Override
    public boolean isSubscribedMessage() {
        return true;
    }

    @Override
    public WampSubscribedMessage asSubscribedMessage() {
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
    public int getSubscriptionId() {
        try {
            return toJSON().getInt(SUBSCRIPTION_ID_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no subscription id");
        }
    }
}
