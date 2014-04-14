
package com.sonycsl.wamp.message;

import org.json.JSONArray;
import org.json.JSONObject;

public interface WampErrorMessage {
    public int getRequestType();

    public int getRequestId();

    public JSONObject getDetails();

    public String getUri();

    public JSONArray getArguments();

    public JSONObject getArgumentsKw();
}
