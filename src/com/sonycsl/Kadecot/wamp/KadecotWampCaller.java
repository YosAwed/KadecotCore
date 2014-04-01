
package com.sonycsl.Kadecot.wamp;

import com.sonycsl.wamp.WampMessage;

public class KadecotWampCaller extends KadecotWampClient {

    public KadecotWampCaller() {
    }

    public KadecotWampCaller(KadecotWampClient next) {
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
    protected boolean consumeRoleBroadcast(WampMessage msg) {
        return false;
    }

}
