
package com.sonycsl.wamp;

import org.json.JSONObject;

public interface WampRegisterMessage {
    public int getRequestId();

    public JSONObject getOptions();

    public String getProcedure();
}
