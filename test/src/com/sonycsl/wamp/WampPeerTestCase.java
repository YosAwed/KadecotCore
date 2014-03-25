/*
 * Copyright (C) Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp;

import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampPeerTestCase extends TestCase {

    private TestEchoWampPeer mPeer;
    private TestEchoWampPeer mNext;
    private TestEchoWampPeer mFriendPeer;

    @Override
    protected void setUp() throws Exception {
        mNext = new TestEchoWampPeer();
        mPeer = new TestEchoWampPeer(mNext);
        mFriendPeer = new TestEchoWampPeer();
        mPeer.connect(mFriendPeer);
    }

    public void testCtor() {
        assertNotNull(mPeer);
        assertNotNull(mNext);
        assertNotNull(mFriendPeer);
    }

    public void testSend() {
        final CountDownLatch peerLatch = new CountDownLatch(1);
        final CountDownLatch friendLatch = new CountDownLatch(1);
        final WampMessage msg = new WampTestMessage();

        mPeer.setCountDownLatch(peerLatch);
        mFriendPeer.setCountDownLatch(friendLatch);

        mFriendPeer.broadcast(msg);
        try {
            assertTrue(peerLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(msg, mPeer.getMessage());

        try {
            assertTrue(friendLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(msg, mFriendPeer.getMessage());
    }

    public void testChainOfResponsibility() {
        final CountDownLatch peerLatch = new CountDownLatch(1);
        final CountDownLatch nextLatch = new CountDownLatch(1);
        final CountDownLatch friendLatch = new CountDownLatch(1);
        final WampMessage msg = new WampTestMessage();

        mPeer.setConsumed(false);
        mPeer.setCountDownLatch(peerLatch);
        mNext.setCountDownLatch(nextLatch);
        mFriendPeer.setCountDownLatch(friendLatch);

        mFriendPeer.broadcast(msg);
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

        assertEquals(msg, mNext.getMessage());

        try {
            assertTrue(friendLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(msg, mFriendPeer.getMessage());
    }
}
