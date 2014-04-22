
package com.sonycsl.wamp;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.role.WampRole.OnReplyListener;

import java.util.ArrayList;

abstract public class WampPeer {

    private ArrayList<WampPeer> mReceivers = new ArrayList<WampPeer>();
    private WampRole mRole;

    public WampPeer() {
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

        if (mRole != null) {
            return;
        }

        mRole = getRole();
        if (mRole == null) {
            throw new NullPointerException("Role is null");
        }
    }

    public void transmit(WampMessage msg) {
        if (msg == null) {
            throw new IllegalArgumentException("message should not be null");
        }

        for (WampPeer receiver : mReceivers) {
            if (!mRole.resolveTxMessage(receiver, msg)) {
                continue;
            }
            receiver.onReceive(this, msg);
            notifyTransmit(receiver, msg);
        }
    }

    private void notifyTransmit(final WampPeer receiver, final WampMessage msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OnTransmitted(receiver, msg);
            }
        }).start();
    }

    private void onReceive(final WampPeer transmitter, final WampMessage msg) {
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
            notifyOnReceive(transmitter, msg);
            return;
        }

        throw new UnsupportedOperationException(msg.toString() + this.toString());
    }

    private void notifyOnReceive(final WampPeer transmitter, final WampMessage msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OnReceived(msg);
            }
        }).start();
    }

    abstract protected void OnConnected(WampPeer peer);

    abstract protected void OnTransmitted(WampPeer peer, WampMessage msg);

    abstract protected void OnReceived(WampMessage msg);
}
