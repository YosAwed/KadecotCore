
package com.sonycsl.wamp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class WampPeer {

    private WampPeer mNext;

    private Map<WampPeer, WampMessenger> mAdapterList = new ConcurrentHashMap<WampPeer, WampMessenger>();

    public WampPeer() {
    }

    public WampPeer(WampPeer next) {
        mNext = next;
    }

    public final void connect(WampPeer friend) {
        if (mAdapterList.get(friend) == null) {
            mAdapterList.put(friend, new IdentifiedWampMessenger(friend, this));
            friend.connect(this);
        }
    }

    public final void broadcast(WampMessage msg) {
        for (WampMessenger messenger : mAdapterList.values()) {
            messenger.send(msg);
        }
        onBroadcast(msg);
    }

    synchronized protected final void onMessage(WampMessenger friend, WampMessage msg) {
        if (msg == null) {
            throw new IllegalArgumentException("message should not be null");
        }

        // Delegation
        if (consumeMessage(friend, msg)) {
            return;
        }

        // Chain of Responsibility
        if (mNext != null) {
            mNext.onMessage(friend, msg);
            return;
        }

        throw new IllegalArgumentException("Illegal Message, message: " + msg);
    }

    private Map<WampPeer, WampMessenger> getIdentifierList() {
        return mAdapterList;
    }

    protected abstract boolean consumeMessage(WampMessenger friend, WampMessage msg);

    protected abstract void onBroadcast(WampMessage msg);

    private static class IdentifiedWampMessenger implements WampMessenger {

        private final WampPeer mPeer;
        private final WampPeer mIdentifiedPeer;

        public IdentifiedWampMessenger(WampPeer peer, WampPeer identifier) {
            mPeer = peer;
            mIdentifiedPeer = identifier;
        }

        @Override
        public void send(final WampMessage msg) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    WampMessenger friend = mPeer.getIdentifierList().get(mIdentifiedPeer);
                    mPeer.onMessage(friend, msg);
                }

            }).start();
        }
    }
}
