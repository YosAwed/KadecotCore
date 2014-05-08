
package com.sonycsl.wamp.message;

import org.json.JSONArray;
import org.json.JSONObject;

public interface WampInvocationMessage {
    public int getRequestId();

    public int getRegistrationId();

    public JSONObject getDetails();

    public boolean hasArguments();

    public JSONArray getArguments();

    public boolean hasArgumentsKw();

    public JSONObject getArgumentsKw();
}
