
package com.sonycsl.wamp;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class WampPeer {

    protected interface WampMessenger {
        public void send(WampMessage msg);
    }

    private WampPeer mNext;

    private Map<WampPeer, WampMessenger> mMessengers = new ConcurrentHashMap<WampPeer, WampMessenger>();

    public WampPeer() {
    }

    public WampPeer(WampPeer next) {
        mNext = next;
    }

    private Map<WampPeer, WampMessenger> getMessengers() {
        return mMessengers;
    }

    public final void connect(final WampPeer friend) {
        if (mMessengers.get(friend) != null) {
            return;
        }

        final WampMessenger messenger = new WampMessenger() {

            @Override
            public void send(final WampMessage msg) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        friend.onMessage(friend.getMessengers().get(WampPeer.this), msg);
                    }
                }).start();
            }
        };

        mMessengers.put(friend, messenger);
        friend.connect(this);
    }

    public final void broadcast(WampMessage msg) {
        broadcast(mMessengers.values(), msg);
    }

    synchronized protected final void broadcast(Collection<WampMessenger> messengerList,
            WampMessage msg) {
        if (msg == null) {
            throw new IllegalArgumentException("message should not be null");
        }

        if (consumeBroadcast(msg)) {
            for (WampMessenger messenger : messengerList) {
                messenger.send(msg);
            }
            return;
        }

        if (mNext != null) {
            mNext.broadcast(messengerList, msg);
            return;
        }

        throw new IllegalArgumentException("Can not handle broadcast: " + msg);
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

    protected abstract boolean consumeMessage(WampMessenger friend, WampMessage msg);

    protected abstract boolean consumeBroadcast(WampMessage msg);

}
