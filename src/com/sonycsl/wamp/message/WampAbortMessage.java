
package com.sonycsl.wamp.message;

import org.json.JSONObject;

public interface WampAbortMessage {
    public JSONObject getDetails();

    public String getReason();
}
