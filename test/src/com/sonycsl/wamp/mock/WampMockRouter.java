
package com.sonycsl.wamp.mock;

import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessageFactory;
import com.sonycsl.wamp.WampMessenger;

import org.json.JSONObject;

public class WampMockRouter extends WampMockPeer {

    @Override
    protected boolean consumeMessage(WampMessenger friend, WampMessage msg) {
        final boolean consumed = super.consumeMessage(friend, msg);

        if (msg.isHelloMessage()) {
            friend.send(WampMessageFactory.createWelcome(1, new JSONObject()));
        }

        return consumed;
    }

}
