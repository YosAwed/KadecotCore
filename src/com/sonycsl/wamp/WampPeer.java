
package com.sonycsl.wamp;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.role.WampRole.OnReplyListener;

import java.util.ArrayList;

abstract public class WampPeer {

    private ArrayList<WampPeer> mReceivers = new ArrayList<WampPeer>();
    private final WampRole mRole;

    public WampPeer() {
        mRole = getRole();
        if (mRole == null) {
            throw new NullPointerException("Role is null");
        }
    }

    abstract protected WampRole getRole();

    public final void connect(final WampPeer receiver) {
        if (mReceivers.contains(receiver)) {
            return;
        }
        mReceivers.add(receiver);
        receiver.connect(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                OnConnected(receiver);
            }
        }).start();
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

    private void onReceive(final WampPeer transmitter, final WampMessage msg) {
        if (msg == null) {
            throw new IllegalArgumentException("message should not be null");
        }

        final OnReplyListener listener = new OnReplyListener() {
            @Override
            public void onReply(WampPeer receiver, WampMessage reply) {
                receiver.onReceive(WampPeer.this, reply);
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mRole.resolveRxMessage(transmitter, msg, listener)) {
                    OnReceived(msg);
                    return;
                }
                throw new UnsupportedOperationException(msg.toString() + this.toString());
            }
        }).start();
    }

    abstract protected void OnConnected(WampPeer peer);

    abstract protected void OnReceived(WampMessage msg);
}
