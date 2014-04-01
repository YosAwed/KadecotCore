
package com.sonycsl.Kadecot.wamp;

import com.sonycsl.wamp.WampClient;

abstract public class KadecotWampClient extends WampClient {

    public KadecotWampClient() {
        super();
    }

    public KadecotWampClient(WampClient next) {
        super(next);
    }
}
