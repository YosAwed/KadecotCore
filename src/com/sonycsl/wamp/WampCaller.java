
package com.sonycsl.wamp;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampResultMessage;

public abstract class WampCaller extends WampClient {

    public WampCaller() {
    }

    public WampCaller(WampClient next) {
        super(next);
    }

    @Override
    protected final boolean consumeRoleBroadcast(WampMessage msg) {
        if (msg.isCallMessage()) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean consumeRoleMessage(WampMessenger friend, WampMessage msg) {
        if (consumeMyMessage(friend, msg)) {
            onConsumed(msg);
            return true;
        }

        return false;
    }

    private boolean consumeMyMessage(WampMessenger friend, WampMessage msg) {
        if (msg.isResultMessage()) {
            /**
             * TODO: verify requestId of result message.
             */
            result(msg.asResultMessage());
            return true;
        }
        return false;
    }

    abstract protected void result(WampResultMessage msg);

}
