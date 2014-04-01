
package com.sonycsl.Kadecot.wamp;

import com.sonycsl.wamp.WampBroker;
import com.sonycsl.wamp.WampMessage;

import org.json.JSONArray;
import org.json.JSONObject;

public class KadecotWampBroker extends WampBroker {

    @Override
    protected JSONObject createEventDetails(JSONObject options, JSONArray arguments,
            JSONObject argumentKw) {
        return null;
    }

    @Override
    protected void onConsumed(WampMessage msg) {
    }

}
