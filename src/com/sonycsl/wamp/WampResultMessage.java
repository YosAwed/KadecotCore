
package com.sonycsl.wamp;

import org.json.JSONArray;
import org.json.JSONObject;

public interface WampResultMessage {
    public int getRequestId();

    public JSONObject getDetails();

    public JSONArray getArguments();

    public JSONObject getArgumentKw();
}
