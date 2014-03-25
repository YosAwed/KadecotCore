
package com.sonycsl.wamp;

import org.json.JSONArray;
import org.json.JSONObject;

public interface WampEventMessage {
    public int getSubscriptionId();

    public int getPublicationId();

    public JSONObject getDetails();

    public JSONArray getArguments();

    public JSONObject getArgumentsKw();

}
