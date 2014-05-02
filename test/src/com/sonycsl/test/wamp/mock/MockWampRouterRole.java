
package com.sonycsl.test.wamp.mock;

import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampRole;

import org.json.JSONObject;

public class MockWampRouterRole extends WampRole {

    @Override
    public String getRoleName() {
        return "mockRouter";
    }

    @Override
    protected boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg) {
        return msg.isGoodbyeMessage();
    }

    @Override
    protected boolean resolveRxMessageImpl(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {

        if (msg.isHelloMessage()) {
            listener.onReply(transmitter, WampMessageFactory.createWelcome(1, new JSONObject()));
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

        if (msg.isRegisterMessage()) {
            listener.onReply(transmitter,
                    WampMessageFactory.createRegistered(msg.asRegisterMessage().getRequestId(),
                            1));
            return true;
        }

        if (msg.isUnregisterMessage()) {
            listener.onReply(transmitter,
                    WampMessageFactory.createUnregistered(msg.asUnregisterMessage()
                            .getRequestId()));
            return true;
        }

        if (msg.isPublishMessage()) {
            listener.onReply(transmitter,
                    WampMessageFactory.createPublished(msg.asPublishMessage().getRequestId(), 1));
            return true;
        }

        if (msg.isCallMessage()) {
            listener.onReply(transmitter,
                    WampMessageFactory.createResult(msg.asCallMessage().getRequestId(),
                            new JSONObject()));
            return true;
        }

        if (msg.isSubscribeMessage()) {
            listener.onReply(transmitter, WampMessageFactory.createSubscribed(
                    msg.asSubscribeMessage().getRequestId(), 1));
            return true;
        }

        if (msg.isUnsubscribeMessage()) {
            listener.onReply(transmitter,
                    WampMessageFactory.createUnsubscribed(msg.asUnsubscribeMessage()
                            .getRequestId()));
            return true;
        }

        return false;
    }
}
