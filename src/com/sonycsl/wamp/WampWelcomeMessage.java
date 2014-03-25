
package com.sonycsl.wamp;

import org.json.JSONObject;

public interface WampWelcomeMessage {
    public int getSession();

    public JSONObject getDetails();
}
