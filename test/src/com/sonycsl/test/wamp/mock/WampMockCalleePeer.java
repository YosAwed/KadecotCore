
package com.sonycsl.test.wamp.mock;

import com.sonycsl.wamp.WampInvocationMessage;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessageFactory;

import org.json.JSONObject;

public class WampMockCalleePeer extends WampMockPeer {
    protected boolean consumeMessage(WampMessenger friend, WampMessage msg) {
        super.consumeMessage(friend, msg);

        if (msg.isInvocationMessage()) {
            invocation(friend, msg.asInvocationMessage());
        }
        return true;
    }

    private void invocation(WampMessenger friend, WampInvocationMessage msg) {
        friend.send(WampMessageFactory.createYield(msg.getRequestId(), new JSONObject()));
    }
}
