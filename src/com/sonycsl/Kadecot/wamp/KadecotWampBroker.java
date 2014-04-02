
package com.sonycsl.Kadecot.wamp;

import com.sonycsl.wamp.WampBroker;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampRouter;

import org.json.JSONArray;
import org.json.JSONObject;

public class KadecotWampBroker extends WampBroker {

    public KadecotWampBroker() {
    }

    public KadecotWampBroker(WampRouter next) {
        super(next);
    }

    @Override
    protected JSONObject createEventDetails(JSONObject options, JSONArray arguments,
            JSONObject argumentKw) {
        return null;
    }

    @Override
    protected void onConsumed(WampMessage msg) {
    }

}
