
package com.sonycsl.wamp;

import org.json.JSONArray;
import org.json.JSONObject;

public interface WampYieldMessage {
    public int getRequestId();

    public JSONObject getOptions();

    public JSONArray getArguments();

    public JSONObject getArgumentsKw();
}
