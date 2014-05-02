
package com.sonycsl.test.wamp.mock;

import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampRole;

import org.json.JSONObject;

public class MockWampClientRole extends WampRole {

    @Override
    public String getRoleName() {
        return "mock";
    }

    @Override
    protected boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg) {
        if (msg.isHelloMessage()) {
            return true;
        }

        if (msg.isGoodbyeMessage()) {
            return true;
        }

        if (msg.isPublishMessage()) {
            return true;
        }

        if (msg.isCallMessage()) {
            return true;
        }

        if (msg.isSubscribeMessage()) {
            return true;
        }

        return false;
    }

    @Override
    protected boolean resolveRxMessageImpl(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {
        if (msg.isWelcomeMessage()) {
            return true;
        }

        if (msg.isGoodbyeMessage()) {
            if (!msg.asGoodbyeMessage().getReason().equals(WampError.GOODBYE_AND_OUT)) {
                listener.onReply(transmitter,
                        WampMessageFactory.createGoodbye(new JSONObject(),
                                WampError.GOODBYE_AND_OUT));
            }
            return true;
        }

        if (msg.isSubscribedMessage()) {
            return true;
        }

        if (msg.isPublishedMessage()) {
            return true;
        }

        if (msg.isResultMessage()) {
            return true;
        }

        if (msg.isEventMessage()) {
            return true;
        }

        return false;
    }
}
