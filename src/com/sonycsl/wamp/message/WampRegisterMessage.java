
package com.sonycsl.wamp.message;

import org.json.JSONObject;

public interface WampRegisterMessage {
    public int getRequestId();

    public JSONObject getOptions();

    public String getProcedure();
}
