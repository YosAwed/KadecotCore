
package com.sonycsl.wamp;

import org.json.JSONArray;
import org.json.JSONObject;

public interface WampEventMessage {
    public int getSubscriptionId();

    public int getPublicationId();

    public JSONObject getDetails();

    public boolean hasArguments();

    public JSONArray getArguments();

    public boolean hasArgumentsKw();

    public JSONObject getArgumentsKw();

}
