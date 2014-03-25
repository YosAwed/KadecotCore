
package com.sonycsl.wamp;

import org.json.JSONArray;
import org.json.JSONObject;

public interface WampCallMessage {
    public int getRequestId();

    public JSONObject getOptions();

    public String getProcedure();

    public JSONArray getArguments();

    public JSONObject getArgumentsKw();
}
