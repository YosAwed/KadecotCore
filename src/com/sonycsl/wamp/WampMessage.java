/*
 * Copyright (C) Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WampMessage {

    public static final int HELLO = 1;
    public static final int WELCOME = 2;
    public static final int ABORT = 3;
    public static final int CHALLENGE = 4;
    public static final int AUTHENTICATE = 5;
    public static final int GOODBYE = 6;
    public static final int HEATBEAT = 7;
    public static final int ERROR = 8;

    public static final int PUBLISH = 16;
    public static final int PUBLISHED = 17;

    public static final int SUBSCRIBE = 32;
    public static final int SUBSCRIBED = 33;
    public static final int UNSUBSCRIBE = 34;
    public static final int UNSUBSCRIBED = 35;
    public static final int EVENT = 36;

    public static final int CALL = 48;
    public static final int CANCEL = 49;
    public static final int RESULT = 50;

    public static final int REGISTER = 64;
    public static final int REGISTERED = 65;
    public static final int UNREGISTER = 66;
    public static final int UNREGISTERED = 67;
    public static final int INVOCATION = 68;
    public static final int INTERRUPT = 69;
    public static final int YIELD = 70;

    public static class Builder {

        private final JSONArray mMsg;

        public Builder(int messageType) {
            mMsg = new JSONArray();
            mMsg.put(messageType);
        }

        public Builder addInteger(int integer) {
            mMsg.put(integer);
            return this;
        }

        public Builder addString(String str) {
            mMsg.put(str);
            return this;
        }

        public Builder addBool(boolean bool) {
            mMsg.put(bool);
            return this;
        }

        public Builder addId(int id) {
            mMsg.put(id);
            return this;
        }

        public Builder addUri(String uri) {
            mMsg.put(uri);
            return this;
        }

        public Builder addDict(JSONObject dict) {
            mMsg.put(dict);
            return this;
        }

        public Builder addList(JSONArray list) {
            mMsg.put(list);
            return this;
        }

        public WampMessage build() {
            return new WampMessage(mMsg);
        }

    }

    private final JSONArray mMsg;

    protected WampMessage(JSONArray msg) {
        mMsg = msg;
    }

    @Override
    public String toString() {
        return mMsg.toString();
    }

    public JSONArray toJSONArray() {
        return mMsg;
    }

    public static int extractMessageType(JSONArray msg) {
        try {
            return msg.getInt(0);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid message.");
        }
    }

    public boolean isAdvanced() {
        int messageType;
        try {
            messageType = mMsg.getInt(0);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid message.");
        }

        switch (messageType) {
            case CHALLENGE:
            case AUTHENTICATE:
            case HEATBEAT:
            case CANCEL:
            case INTERRUPT:
                return true;
            default:
                return false;
        }
    }
}
