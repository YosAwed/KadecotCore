/*
 * Copyright (C) Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp;

import junit.framework.TestCase;

import org.json.JSONArray;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampPeerTestCase extends TestCase {

    private static class TestWampPeer extends WampPeer {

        private CountDownLatch mLatch;
        private WampMessenger mFriendMessenger;
        private JSONArray mMsg;
        private boolean mIsConsumed = true;

        public TestWampPeer() {
            super();
        }

        public TestWampPeer(WampPeer next) {
            super(next);
        }

        @Override
        protected boolean consumeMessage(WampMessenger friend, JSONArray msg) {
            if (!mIsConsumed) {
                return false;
            }

            mFriendMessenger = friend;
            mMsg = msg;
            mFriendMessenger.send(msg);
            mLatch.countDown();
            return true;
        }

        public void setConsumed(boolean isConsumed) {
            mIsConsumed = isConsumed;
        }

        public void setCountDownLatch(CountDownLatch latch) {
            mLatch = latch;
        }

        public WampMessenger getFriendMessenger() {
            return mFriendMessenger;
        }

        public JSONArray getMessage() {
            return mMsg;
        }

    }

    private TestWampPeer mPeer;
    private TestWampPeer mNext;
    private WampMessenger mPeerMessenger;
    private TestWampMessenger mFriendMessenger;

    @Override
    protected void setUp() throws Exception {
        mNext = new TestWampPeer();
        mPeer = new TestWampPeer(mNext);
        mFriendMessenger = new TestWampMessenger();
        mPeerMessenger = mPeer.connect(mFriendMessenger);
    }

    public void testCtor() {
        assertNotNull(mPeer);
        assertNotNull(mNext);
        assertNotNull(mFriendMessenger);
    }

    public void testConnect() {
        assertNotNull(mPeerMessenger);
    }

    public void testSend() {
        final CountDownLatch peerLatch = new CountDownLatch(1);
        final CountDownLatch friendLatch = new CountDownLatch(1);
        final JSONArray msg = new JSONArray();

        mPeer.setCountDownLatch(peerLatch);
        mFriendMessenger.setCountDownLatch(friendLatch);

        mPeerMessenger.send(msg);
        try {
            assertTrue(peerLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(mFriendMessenger, mPeer.getFriendMessenger());
        assertEquals(msg, mPeer.getMessage());

        try {
            assertTrue(friendLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(msg, mFriendMessenger.getReceivedMessage());
    }

    public void testChainOfResponsibility() {
        final CountDownLatch peerLatch = new CountDownLatch(1);
        final CountDownLatch nextLatch = new CountDownLatch(1);
        final CountDownLatch friendLatch = new CountDownLatch(1);
        final JSONArray msg = new JSONArray();

        mPeer.setConsumed(false);
        mPeer.setCountDownLatch(peerLatch);
        mNext.setCountDownLatch(nextLatch);
        mFriendMessenger.setCountDownLatch(friendLatch);

        mPeerMessenger.send(msg);
        try {
            assertFalse(peerLatch.await(1, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e1) {
            fail();
        }

        try {
            assertTrue(nextLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(mFriendMessenger, mNext.getFriendMessenger());
        assertEquals(msg, mNext.getMessage());

        try {
            assertTrue(friendLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(msg, mFriendMessenger.getReceivedMessage());
    }
}
