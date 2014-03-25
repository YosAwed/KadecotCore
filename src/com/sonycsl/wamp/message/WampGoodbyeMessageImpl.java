
package com.sonycsl.wamp.message;

import com.sonycsl.wamp.WampAbstractMessage;
import com.sonycsl.wamp.WampGoodbyeMessage;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessageType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WampGoodbyeMessageImpl extends WampAbstractMessage implements WampGoodbyeMessage {

    private static final int DETAILS_INDEX = 1;
    private static final int REASON_INDEX = 2;

    public static WampMessage create(JSONObject details, String reason) {
        return new WampAbortMessageImpl(new JSONArray().put(WampMessageType.ABORT).put(details)
                .put(reason));
    }

    public WampGoodbyeMessageImpl(JSONArray msg) {
        super(msg);
    }

    @Override
    public boolean isGoodbyeMessage() {
        return true;
    }

    @Override
    public WampGoodbyeMessage asGoodbyeMessage() {
        return this;
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
    public String getReason() {
        try {
            return toJSON().getString(REASON_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no reason");
        }
    }

}
