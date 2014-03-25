
package com.sonycsl.wamp;

import org.json.JSONObject;

public interface WampAbortMessage {
    public JSONObject getDetails();

    public String getReason();
}
