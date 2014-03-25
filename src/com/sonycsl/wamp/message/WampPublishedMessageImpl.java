
package com.sonycsl.wamp.message;

import com.sonycsl.wamp.WampAbstractMessage;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessageType;
import com.sonycsl.wamp.WampPublishedMessage;

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
