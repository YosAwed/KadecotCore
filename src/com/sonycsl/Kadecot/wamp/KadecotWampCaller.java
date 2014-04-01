
package com.sonycsl.Kadecot.wamp;

import com.sonycsl.wamp.WampCaller;
import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampResultMessage;

public class KadecotWampCaller extends WampCaller {

    public KadecotWampCaller() {
    }

    public KadecotWampCaller(WampClient next) {
        super(next);
    }

    @Override
    protected void result(WampResultMessage msg) {
    }

    @Override
    protected void onConsumed(WampMessage msg) {
    }

    @Override
    protected boolean consumeRoleBroadcast(WampMessage msg) {
        return false;
    }

}
