
package com.sonycsl.wamp;

import org.json.JSONArray;
import org.json.JSONObject;

public interface WampInvocationMessage {
    public int getRequestId();

    public int getRegistrationId();

    public JSONObject getDetails();

    public JSONArray getArguments();

    public JSONObject getArgumentsKw();
}
