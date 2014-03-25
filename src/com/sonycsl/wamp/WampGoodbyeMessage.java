
package com.sonycsl.wamp;

import org.json.JSONObject;

public interface WampGoodbyeMessage {
    public JSONObject getDetails();

    public String getReason();
}
