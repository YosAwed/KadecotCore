
package com.sonycsl.wamp.message;

import org.json.JSONObject;

public interface WampWelcomeMessage {
    public int getSession();

    public JSONObject getDetails();
}
