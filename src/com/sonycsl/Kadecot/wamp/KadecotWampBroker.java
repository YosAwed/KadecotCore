
package com.sonycsl.Kadecot.wamp;

import com.sonycsl.wamp.WampBroker;
import com.sonycsl.wamp.WampRouter;
import com.sonycsl.wamp.message.WampMessage;

public class KadecotWampBroker extends WampBroker {

    public KadecotWampBroker() {
    }

    public KadecotWampBroker(WampRouter next) {
        super(next);
    }

    @Override
    protected void onConsumed(WampMessage msg) {
    }

}
