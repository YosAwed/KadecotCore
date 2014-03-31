/*
 * Copyright (C) Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.wamp;

import com.sonycsl.test.wamp.mock.WampEchoMockPeer;
import com.sonycsl.test.wamp.mock.WampMockMessage;
import com.sonycsl.wamp.WampMessage;

import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampPeerTestCase extends TestCase {

    private WampEchoMockPeer mPeer;
    private WampEchoMockPeer mNext;
    private WampEchoMockPeer mFriendPeer;

    @Override
    protected void setUp() throws Exception {
        mNext = new WampEchoMockPeer();
        mPeer = new WampEchoMockPeer(mNext);
        mFriendPeer = new WampEchoMockPeer();
        mPeer.connect(mFriendPeer);
    }

    public void testCtor() {
        assertNotNull(mPeer);
        assertNotNull(mNext);
        assertNotNull(mFriendPeer);
    }

    public void testEcho() {
        final CountDownLatch peerLatch = new CountDownLatch(1);
        final CountDownLatch friendLatch = new CountDownLatch(1);
        final WampMessage msg = new WampMockMessage();

        mPeer.setCountDownLatch(peerLatch);
        mFriendPeer.setCountDownLatch(friendLatch);

        mFriendPeer.broadcast(msg);
        try {
            assertTrue(mPeer.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(msg, mPeer.getMessage());

        try {
            assertTrue(mFriendPeer.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(msg, mFriendPeer.getMessage());
    }

    public void testChainOfResponsibility() {
        final WampMessage msg = new WampMockMessage();

        mPeer.setConsumed(false);
        mPeer.setCountDownLatch(new CountDownLatch(1));
        mNext.setCountDownLatch(new CountDownLatch(1));
        mFriendPeer.setCountDownLatch(new CountDownLatch(1));

        mFriendPeer.broadcast(msg);
        try {
            assertFalse(mPeer.await(1, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e1) {
            fail();
        }

        try {
            assertTrue(mNext.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(msg, mNext.getMessage());

        try {
            assertTrue(mFriendPeer.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(msg, mFriendPeer.getMessage());
    }
}
