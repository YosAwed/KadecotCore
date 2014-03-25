
package com.sonycsl.wamp;

import org.json.JSONObject;

public interface WampSubscribeMessage {
    public int getRequestId();

    public JSONObject getOptions();

    public String getTopic();
}
