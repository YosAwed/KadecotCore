
package com.sonycsl.Kadecot.wamp;

import com.sonycsl.wamp.WampDealer;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampRouter;

public class KadecotWampDealer extends WampDealer {

    public KadecotWampDealer() {
    }

    public KadecotWampDealer(WampRouter next) {
        super(next);
    }

    @Override
    protected void onConsumed(WampMessage msg) {
    }

}
