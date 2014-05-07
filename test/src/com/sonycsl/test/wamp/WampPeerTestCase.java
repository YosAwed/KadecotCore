/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.wamp;

import com.sonycsl.test.wamp.mock.MockWampRole;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampRole;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampPeerTestCase extends TestCase {

    abstract private static class TestWampPeer extends WampPeer {
        private CountDownLatch mLatch;
        private WampMessage mMsg;

        public final void setCountDownLatch(CountDownLatch latch) {
            mLatch = latch;
        }

        public final boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return mLatch.await(timeout, unit);
        }

        public final WampMessage getLatestMessage() {
            return mMsg;
        }

        @Override
        protected void OnConnected(WampPeer peer) {
        }

        @Override
        protected void OnTransmitted(WampPeer peer, WampMessage msg) {
        }

        @Override
        protected void OnReceived(WampMessage msg) {
            mMsg = msg;
            if (mLatch != null) {
                mLatch.countDown();
            }
        }
    }

    private static final class TestPeer1 extends TestWampPeer {

        @Override
        protected Set<WampRole> getRoleSet() {
            Set<WampRole> roleSet = new HashSet<WampRole>();
            roleSet.add(new MockWampRole());
            return roleSet;
        }
    }

    private static final class TestPeer2 extends TestWampPeer {

        @Override
        protected Set<WampRole> getRoleSet() {
            Set<WampRole> roleSet = new HashSet<WampRole>();
            roleSet.add(new MockWampRole());
            return roleSet;
        }

        @Override
        protected void OnReceived(WampMessage msg) {
            super.OnReceived(msg);
            transmit(msg);
        }
    }

    private static final String TEST_REALM = "test";
    private static final JSONObject DETAILS = new JSONObject();

    private TestPeer1 mPeer1;
    private TestPeer2 mPeer2;

    @Override
    protected void setUp() throws Exception {
        mPeer1 = new TestPeer1();
        mPeer2 = new TestPeer2();
        mPeer1.connect(mPeer2);
    }

    public void testCtor() {
        assertNotNull(mPeer1);
        assertNotNull(mPeer2);
    }

    public void testTramsmit() {
        mPeer1.setCountDownLatch(new CountDownLatch(1));
        mPeer2.setCountDownLatch(new CountDownLatch(1));
        WampMessage hello = WampMessageFactory.createHello(TEST_REALM, DETAILS);
        mPeer1.transmit(hello);
        try {
            assertTrue(mPeer2.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(hello, mPeer2.getLatestMessage());

        try {
            mPeer1.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(hello, mPeer1.getLatestMessage());
    }
}
