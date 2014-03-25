
package com.sonycsl.wamp;

import org.json.JSONObject;

public interface WampHelloMessage {
    public String getRealm();

    public JSONObject getDetails();
}
