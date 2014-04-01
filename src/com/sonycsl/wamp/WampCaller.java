
package com.sonycsl.wamp;

public abstract class WampCaller extends WampClient {

    public WampCaller() {
    }

    public WampCaller(WampClient next) {
        super(next);
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
            result(msg.asResultMessage());
            return true;
        }
        return false;
    }

    abstract protected void result(WampResultMessage msg);

}
