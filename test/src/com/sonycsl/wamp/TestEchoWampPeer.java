
package com.sonycsl.wamp;

public class TestEchoWampPeer extends TestWampPeer {

    public TestEchoWampPeer() {
        super();
    }

    public TestEchoWampPeer(WampPeer next) {
        super(next);
    }

    @Override
    protected boolean consumeMessage(WampMessenger friend, WampMessage msg) {
        boolean consumed = super.consumeMessage(friend, msg);
        friend.send(msg);
        return consumed;
    }
}
