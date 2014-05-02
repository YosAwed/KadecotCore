
package com.sonycsl.test.wamp.mock;

import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampRole;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;

public class MockWampDealer extends WampRole {

    @Override
    public String getRoleName() {
        return "mockDealer";
    }

    private Queue<WampMessage> mMessageQueue = new LinkedList<WampMessage>();

    public WampMessage getMessage() {
        return mMessageQueue.remove();
    }

    @Override
    protected boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg) {
        return false;
    }

    @Override
    protected boolean resolveRxMessageImpl(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {

        if (msg.isRegisterMessage()) {
            listener.onReply(transmitter,
                    WampMessageFactory.createRegistered(msg.asRegisterMessage().getRequestId(),
                            0));
            mMessageQueue.add(msg);
            return true;
        }

        if (msg.isCallMessage()) {
            listener.onReply(transmitter,
                    WampMessageFactory.createResult(msg.asCallMessage().getRequestId(),
                            new JSONObject()));
            mMessageQueue.add(msg);
            return true;
        }

        if (msg.isUnregisterMessage()) {
            listener.onReply(transmitter, WampMessageFactory
                    .createUnregistered(msg.asUnregisterMessage().getRequestId()));
            mMessageQueue.add(msg);
            return true;
        }

        return false;
    }
}
