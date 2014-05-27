/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.role.WampRole.OnReplyListener;

import java.util.ArrayList;
import java.util.Set;

abstract public class WampPeer {

    public interface Callback {
        public void preConnect(WampPeer connecter, WampPeer connectee);

        public void postConnect(WampPeer connecter, WampPeer connectee);

        public void preTransmit(WampPeer transmitter, WampMessage msg);

        public void postTransmit(WampPeer transmitter, WampMessage msg);

        public void preReceive(WampPeer receiver, WampMessage msg);

        public void postReceive(WampPeer receiver, WampMessage msg);
    }

    private ArrayList<WampPeer> mReceivers = new ArrayList<WampPeer>();
    private Set<WampRole> mRoleSet;
    private Callback mCallback;

    public WampPeer() {
    }

    abstract protected Set<WampRole> getRoleSet();

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public Callback getCallback() {
        return mCallback;
    }

    public final void connect(final WampPeer receiver) {

        if (mReceivers.contains(receiver)) {
            return;
        }

        mReceivers.add(receiver);
        receiver.connect(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    mCallback.preConnect(WampPeer.this, receiver);
                }
                onConnected(receiver);
                if (mCallback != null) {
                    mCallback.postConnect(WampPeer.this, receiver);
                }
            }
        }).start();

        // TODO: Avoid to get multiple role instance.
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

        boolean transmitted = false;
        for (WampPeer receiver : mReceivers) {
            for (WampRole role : mRoleSet) {
                if (role.resolveTxMessage(receiver, msg)) {
                    transmitted = true;
                    if (mCallback != null) {
                        mCallback.preTransmit(WampPeer.this, msg);
                    }
                    receiver.onReceive(WampPeer.this, msg);
                    onTransmitted(receiver, msg);
                    if (mCallback != null) {
                        mCallback.postTransmit(WampPeer.this, msg);
                    }
                    break;
                }
            }
        }

        // TODO: Throw transmit exception not to handle message
        if (!transmitted) {
            throw new UnsupportedOperationException(msg.toString() + ", " + this.toString()
                    + ", roleSet=" + mRoleSet);
        }
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

        for (WampRole role : mRoleSet) {
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
                if (mCallback != null) {
                    mCallback.preReceive(WampPeer.this, msg);
                }
                onReceived(msg);
                if (mCallback != null) {
                    mCallback.postReceive(WampPeer.this, msg);
                }
            }
        }).start();
    }

    abstract protected void onConnected(WampPeer peer);

    abstract protected void onTransmitted(WampPeer peer, WampMessage msg);

    abstract protected void onReceived(WampMessage msg);
}
