
package com.sonycsl.wamp;

import org.json.JSONArray;
import org.json.JSONObject;

public interface WampPublishMessage {
    public int getRequestId();

    public JSONObject getOptions();

    public String getTopic();

    public JSONArray getArguments();

    public JSONObject getArgumentsKw();
}
