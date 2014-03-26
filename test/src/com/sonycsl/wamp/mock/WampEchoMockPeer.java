
package com.sonycsl.wamp.mock;

import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessenger;
import com.sonycsl.wamp.WampPeer;

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
