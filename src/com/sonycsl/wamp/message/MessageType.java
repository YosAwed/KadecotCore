
package com.sonycsl.wamp.message;

import org.json.JSONArray;
import org.json.JSONException;

public class MessageType {
    public static final int HELLO = 1;

    public static final int WELCOME = 2;

    public static final int ABORT = 3;

    public static final int CHALLENGE = 4;

    public static final int AUTHENTICATE = 5;

    public static final int GOODBYE = 6;

    public static final int HEARTBEAT = 7;

    public static final int ERROR = 8;

    public static final int PUBLISH = 16;

    public static final int PUBLISHED = 17;

    public static final int SUBSCRIBE = 32;

    public static final int SUBSCRIBED = 33;

    public static final int UNSUBSCRIBE = 34;

    public static final int UNSUBSCRIBED = 35;

    public static final int EVENT = 36;

    // TODO: List up

    private static final int MESSAGETYPE_IDX = 0;

    private static final int REQUESTID_IDX = 1;

    private static final int OPTION_IDX = 2;

    private static final int TOPIC_IDX = 3;

    private static final int ARGUMENTS_IDX = 4;

    private static final int ARGUMENTKW_IDX = 5;

    public static int getMessageType(JSONArray message) {
        try {
            return message.getInt(MESSAGETYPE_IDX);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Illegal message: " + message);
    }
}
