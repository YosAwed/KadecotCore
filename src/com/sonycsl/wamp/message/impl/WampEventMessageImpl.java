
package com.sonycsl.wamp.message.impl;

import com.sonycsl.wamp.message.WampEventMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WampEventMessageImpl extends WampAbstractMessage implements WampEventMessage {

    private static final int SUBSCRIPTION_ID_INDEX = 1;
    private static final int PUBLICATION_ID_INDEX = 2;
    private static final int DETAILS_INDEX = 3;
    private static final int ARGUMENTS_INDEX = 4;
    private static final int ARGUMENTS_KW_INDEX = 5;

    public static WampMessage create(int subscriptionId, int publicationId, JSONObject details) {
        return new WampEventMessageImpl(new JSONArray().put(WampMessageType.EVENT)
                .put(subscriptionId).put(publicationId).put(details));
    }

    public static WampMessage create(int subscriptionId, int publicationId, JSONObject details,
            JSONArray arguments) {
        return new WampEventMessageImpl(new JSONArray().put(WampMessageType.EVENT)
                .put(subscriptionId).put(publicationId).put(details).put(arguments));
    }

    public static WampMessage create(int subscriptionId, int publicationId, JSONObject details,
            JSONArray arguments, JSONObject argumentsKw) {
        return new WampEventMessageImpl(new JSONArray().put(WampMessageType.EVENT)
                .put(subscriptionId).put(publicationId).put(details).put(arguments)
                .put(argumentsKw));
    }

    public WampEventMessageImpl(JSONArray msg) {
        super(msg);
    }

    @Override
    public boolean isEventMessage() {
        return true;
    }

    @Override
    public WampEventMessage asEventMessage() {
        return this;
    }

    @Override
    public int getSubscriptionId() {
        try {
            return toJSON().getInt(SUBSCRIPTION_ID_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no subscription id");
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

    @Override
    public JSONObject getDetails() {
        try {
            return toJSON().getJSONObject(DETAILS_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no details");
        }
    }

    @Override
    public boolean hasArguments() {
        return !toJSON().isNull(ARGUMENTS_INDEX);
    }

    @Override
    public JSONArray getArguments() {
        try {
            return toJSON().getJSONArray(ARGUMENTS_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no argument");
        }
    }

    @Override
    public boolean hasArgumentsKw() {
        return !toJSON().isNull(ARGUMENTS_KW_INDEX);
    }

    @Override
    public JSONObject getArgumentsKw() {
        try {
            return toJSON().getJSONObject(ARGUMENTS_KW_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no argumetkw");
        }
    }

}
