
package com.sonycsl.wamp;

import org.json.JSONArray;
import org.json.JSONObject;

public interface WampYieldMessage {
    public int getRequestId();

    public JSONObject getOptions();

    public boolean hasArguments();

    public JSONArray getArguments();

    public boolean hasArgumentsKw();

    public JSONObject getArgumentsKw();
}
