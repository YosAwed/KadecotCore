
package com.sonycsl.wamp;

import org.json.JSONArray;

public interface WampMessenger {
    public void send(JSONArray msg);
}
