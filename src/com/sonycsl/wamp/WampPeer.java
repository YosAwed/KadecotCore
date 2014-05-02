
package com.sonycsl.wamp;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.role.WampRole.OnReplyListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

abstract public class WampPeer {

    private ArrayList<WampPeer> mReceivers = new ArrayList<WampPeer>();
    private Set<WampRole> mRoleSet;

    public WampPeer() {
    }

    abstract protected Set<WampRole> getRoleSet();

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

        if (mRoleSet != null) {
            return;
        }

        mRoleSet = getRoleSet();
        if (mRoleSet == null) {
            throw new NullPointerException("Role is null");
        }
    }

    public void transmit(WampMessage msg) {
        if (msg == null) {
            throw new IllegalArgumentException("message should not be null");
        }

        Iterator<WampRole> iter = mRoleSet.iterator();
        for (WampPeer receiver : mReceivers) {
            while (iter.hasNext()) {
                if (iter.next().resolveTxMessage(receiver, msg)) {
                    receiver.onReceive(this, msg);
                    notifyTransmit(receiver, msg);
                    break;
                }
            }
        }

        // TODO: Throw transmit exception not to handle message
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

        Iterator<WampRole> iter = mRoleSet.iterator();
        while (iter.hasNext()) {
            WampRole role = iter.next();
            if (role.resolveRxMessage(transmitter, msg, listener)) {
                notifyOnReceive(transmitter, msg);
                return;
            }
        }

        // TODO: return error
        throw new UnsupportedOperationException(msg.toString() + ", " + this.toString()
                + ", roleSet=" + mRoleSet);
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
