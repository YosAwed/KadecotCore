
package com.sonycsl.wamp;

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
