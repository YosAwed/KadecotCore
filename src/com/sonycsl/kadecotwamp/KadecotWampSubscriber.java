
package com.sonycsl.kadecotwamp;

import com.sonycsl.wamp.WampMessage;

public class KadecotWampSubscriber extends KadecotWampClient {

    public KadecotWampSubscriber() {
    }

    public KadecotWampSubscriber(KadecotWampClient next) {
        super(next);
    }

    @Override
    protected boolean consumeRoleMessage(WampMessenger friend, WampMessage msg) {
        return false;
    }

    @Override
    protected void onConsumed(WampMessage msg) {
    }

    @Override
    protected void onBroadcast(WampMessage msg) {
    }

}
