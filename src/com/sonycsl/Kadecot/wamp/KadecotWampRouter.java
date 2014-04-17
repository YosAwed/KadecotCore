
package com.sonycsl.Kadecot.wamp;

import com.sonycsl.wamp.WampRouter;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampBroker;
import com.sonycsl.wamp.role.WampDealer;
import com.sonycsl.wamp.role.WampRole;

public class KadecotWampRouter extends WampRouter {

    public static final String REALM = "realm";

    @Override
    protected WampRole getRouterRole() {
        return new WampBroker(new WampDealer());
    }

    @Override
    protected void onReceived(WampMessage msg) {
    }

}
