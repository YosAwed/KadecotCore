
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

        if (msg.isRegisterMessage()) {
            friend.send(WampMessageFactory.createRegistered(msg.asRegisterMessage().getRequestId(),
                    1));
        }

        if (msg.isUnregisterMessage()) {
            friend.send(WampMessageFactory.createUnregistered(msg.asUnregisterMessage()
                    .getRequestId()));
        }

        if (msg.isPublishMessage()) {
            friend.send(WampMessageFactory
                    .createPublished(msg.asPublishMessage().getRequestId(), 1));
        }

        return consumed;
    }
}
