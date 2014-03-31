
package com.sonycsl.kadecotwamp;

import com.sonycsl.wamp.WampClient;

abstract public class KadecotWampClient extends WampClient {

    public KadecotWampClient() {
        super();
    }

    public KadecotWampClient(KadecotWampClient next) {
        super(next);
    }
}
