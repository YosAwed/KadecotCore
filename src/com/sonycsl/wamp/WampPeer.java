
package com.sonycsl.wamp;

import org.json.JSONArray;

public abstract class WampPeer {

    private WampPeer mNext;

    private WampMessenger mNextMessenger;

    public WampPeer() {
    }

    public WampPeer(WampPeer next) {
        mNext = next;
    }

    public WampMessenger connect(WampMessenger friend) {
        if (mNext != null) {
            mNextMessenger = mNext.connect(friend);
        }
        return new WampPeerAdapter(this, friend);
    }

    synchronized private void onMessage(WampMessenger friend, JSONArray msg) {
        if (msg == null) {
            throw new IllegalArgumentException("message should not be null");
        }

        // Delegation
        if (consumeMessage(friend, msg)) {
            return;
        }

        // Chain of Responsibility
        if (mNextMessenger != null) {
            mNextMessenger.send(msg);
            return;
        }

        throw new IllegalArgumentException("Illegal Message, message: " + msg);
    }

    protected abstract boolean consumeMessage(WampMessenger friend, JSONArray msg);

    private static class WampPeerAdapter implements WampMessenger {

        private final WampMessenger mFriend;
        private final WampPeer mPeer;

        public WampPeerAdapter(WampPeer peer, WampMessenger friend) {
            mPeer = peer;
            mFriend = friend;
        }

        @Override
        public void send(final JSONArray msg) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    mPeer.onMessage(mFriend, msg);
                }

            }).start();
        }
    }
}
