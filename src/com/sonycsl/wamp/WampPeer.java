/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.role.WampRole.OnReplyListener;

import java.util.ArrayList;
import java.util.HashSet;
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

    private static class Callbacks implements Callback {

        private Set<Callback> callbacks = new HashSet<WampPeer.Callback>();

        public boolean add(Callback callback) {
            synchronized (callbacks) {
                return callbacks.add(callback);
            }
        }

        public void remove(Callback callback) {
            synchronized (callbacks) {
                callbacks.remove(callback);
            }
        }

        @Override
        public void preConnect(WampPeer connecter, WampPeer connectee) {
            synchronized (callbacks) {
                for (Callback callback : callbacks) {
                    callback.preConnect(connecter, connectee);
                }
            }
        }

        @Override
        public void postConnect(WampPeer connecter, WampPeer connectee) {
            synchronized (callbacks) {
                for (Callback callback : callbacks) {
                    callback.postConnect(connecter, connectee);
                }
            }
        }

        @Override
        public void preTransmit(WampPeer transmitter, WampMessage msg) {
            synchronized (callbacks) {
                for (Callback callback : callbacks) {
                    callback.preTransmit(transmitter, msg);
                }
            }
        }

        @Override
        public void postTransmit(WampPeer transmitter, WampMessage msg) {
            synchronized (callbacks) {
                for (Callback callback : callbacks) {
                    callback.postTransmit(transmitter, msg);
                }
            }
        }

        @Override
        public void preReceive(WampPeer receiver, WampMessage msg) {
            synchronized (callbacks) {
                for (Callback callback : callbacks) {
                    callback.preReceive(receiver, msg);
                }
            }
        }

        @Override
        public void postReceive(WampPeer receiver, WampMessage msg) {
            synchronized (callbacks) {
                for (Callback callback : callbacks) {
                    callback.postReceive(receiver, msg);
                }
            }
        }

    }

    private ArrayList<WampPeer> mReceivers = new ArrayList<WampPeer>();
    private Set<WampRole> mRoleSet;
    private Callbacks mCallbacks = new Callbacks();

    public WampPeer() {
    }

    abstract protected Set<WampRole> getRoleSet();

    public void setCallback(Callback callback) {
        mCallbacks.add(callback);
    }

    public void removeCallback(Callback callback) {
        mCallbacks.remove(callback);
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
                mCallbacks.preConnect(WampPeer.this, receiver);
                onConnected(receiver);
                mCallbacks.postConnect(WampPeer.this, receiver);
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

        if (mReceivers.size() == 0) {
            return;
        }

        boolean transmitted = false;
        for (WampPeer receiver : mReceivers) {
            for (WampRole role : mRoleSet) {
                if (role.resolveTxMessage(receiver, msg)) {
                    transmitted = true;
                    mCallbacks.preTransmit(WampPeer.this, msg);
                    receiver.onReceive(WampPeer.this, msg);
                    onTransmitted(receiver, msg);
                    mCallbacks.postTransmit(WampPeer.this, msg);
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
                if (mCallbacks != null) {
                    mCallbacks.preReceive(WampPeer.this, msg);
                }
                onReceived(msg);
                if (mCallbacks != null) {
                    mCallbacks.postReceive(WampPeer.this, msg);
                }
            }
        }).start();
    }

    abstract protected void onConnected(WampPeer peer);

    abstract protected void onTransmitted(WampPeer peer, WampMessage msg);

    abstract protected void onReceived(WampMessage msg);
}
