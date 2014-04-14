
package com.sonycsl.wamp.message;

import org.json.JSONObject;

public interface WampSubscribeMessage {
    public int getRequestId();

    public JSONObject getOptions();

    public String getTopic();
}
