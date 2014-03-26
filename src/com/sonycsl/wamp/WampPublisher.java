
package com.sonycsl.wamp;

abstract public class WampPublisher extends WampClient {

    public WampPublisher() {
    }

    public WampPublisher(WampClient next) {
        super(next);
    }

    private boolean consumeMyMessage(WampMessenger friend, WampMessage msg) {
        if (msg.isPublishedMessage()) {
            return true;
        }
        return false;
    }

    @Override
    protected final void onBroadcast(WampMessage msg) {
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
