
package com.sonycsl.wamp.message;

import org.json.JSONObject;

public interface WampGoodbyeMessage {
    public JSONObject getDetails();

    public String getReason();
}
