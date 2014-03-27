
package com.sonycsl.wamp;

import org.json.JSONArray;
import org.json.JSONObject;

public interface WampCallMessage {
    public int getRequestId();

    public JSONObject getOptions();

    public String getProcedure();

    public boolean hasArguments();

    public JSONArray getArguments();

    public boolean hasArgumentsKw();

    public JSONObject getArgumentsKw();
}
