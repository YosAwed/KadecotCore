
package com.sonycsl.test.wamp.mock;

import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;

public class WampEchoMockPeer extends WampMockPeer {

    public WampEchoMockPeer() {
        super();
    }

    public WampEchoMockPeer(WampPeer next) {
        super(next);
    }

    @Override
    protected boolean consumeMessage(WampMessenger friend, WampMessage msg) {
        boolean consumed = super.consumeMessage(friend, msg);
        friend.send(msg);
        return consumed;
    }
}
