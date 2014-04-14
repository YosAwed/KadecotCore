
package com.sonycsl.wamp;

import com.sonycsl.wamp.message.WampMessage;

abstract public class WampPublisher extends WampClient {

    public WampPublisher() {
    }

    public WampPublisher(WampClient next) {
        super(next);
    }

    @Override
    protected boolean consumeRoleBroadcast(WampMessage msg) {
        if (msg.isPublishMessage()) {
            return true;
        }
        return false;
    }

    private boolean consumeMyMessage(WampMessenger friend, WampMessage msg) {
        /**
         * TODO: verify request id of published message.
         */
        if (msg.isPublishedMessage()) {
            return true;
        }
        return false;
    }

    @Override
    protected final boolean consumeRoleMessage(WampMessenger friend, WampMessage msg) {
        if (consumeMyMessage(friend, msg)) {
            onConsumed(msg);
            return true;
        }

        return false;
    }
}
