
package com.sonycsl.wamp.message;

import org.json.JSONObject;

public interface WampHelloMessage {
    public String getRealm();

    public JSONObject getDetails();
}
