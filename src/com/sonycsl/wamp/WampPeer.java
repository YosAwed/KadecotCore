
package com.sonycsl.wamp;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.role.WampRole.OnReplyListener;

import java.util.ArrayList;

abstract public class WampPeer {

    private ArrayList<WampPeer> mReceivers = new ArrayList<WampPeer>();
    private final WampRole mRole;

    public WampPeer() {
        super();
        mRole = getRole();
        if (mRole == null) {
            throw new NullPointerException("Role is null");
        }
    }

    abstract protected WampRole getRole();

    public final void connect(WampPeer receiver) {
        if (mReceivers.contains(receiver)) {
            return;
        }
        mReceivers.add(receiver);
        receiver.connect(this);
    }

    public void transmit(WampMessage msg) {
        if (msg == null) {
            throw new IllegalArgumentException("message should not be null");
        }

        for (WampPeer receiver : mReceivers) {
            mRole.resolveTxMessage(receiver, msg);
            receiver.onReceive(this, msg);
        }
    }

    private void onReceive(final WampPeer transmitter, WampMessage msg) {
        if (msg == null) {
            throw new IllegalArgumentException("message should not be null");
        }

        OnReplyListener listener = new OnReplyListener() {
            @Override
            public void onReply(WampPeer receiver, WampMessage reply) {
                receiver.onReceive(WampPeer.this, reply);
            }
        };

        if (mRole.resolveRxMessage(transmitter, msg, listener)) {
            onReceived(msg);
            return;
        }

        throw new UnsupportedOperationException(msg.toString() + this.toString());
    }

    abstract protected void onReceived(WampMessage msg);
}
