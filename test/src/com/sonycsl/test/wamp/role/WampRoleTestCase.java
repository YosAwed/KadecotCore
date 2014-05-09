/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.wamp.role;

import com.sonycsl.test.wamp.mock.MockWampPeer;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.role.WampRole.OnReplyListener;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampRoleTestCase extends TestCase {

    private static class TestWampRole extends WampRole {

        private CountDownLatch mLatch;
        private WampMessage mMsg;

        @Override
        public String getRoleName() {
            return "testRole";
        }

        public void setCountDownLatch(CountDownLatch latch) {
            mLatch = latch;
        }

        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return mLatch.await(timeout, unit);
        }

        public WampMessage getLatestMessage() {
            return mMsg;
        }

        @Override
        protected boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg) {
            mMsg = msg;
            if (mLatch != null) {
                mLatch.countDown();
            }

            return true;
        }

        @Override
        protected boolean resolveRxMessageImpl(WampPeer transmitter, WampMessage msg,
                OnReplyListener listener) {
            mMsg = msg;
            if (mLatch != null) {
                mLatch.countDown();
            }
            return true;
        }

    }

    private TestWampRole mRole;

    @Override
    protected void setUp() throws Exception {
        mRole = new TestWampRole();
    }

    public void testCtor() {
        assertNotNull(mRole);
    }

    public void testResolveTxMessage() {
        final WampMessage hello = WampMessageFactory.createHello("", new JSONObject());
        mRole.setCountDownLatch(new CountDownLatch(1));
        assertTrue(mRole.resolveTxMessage(new MockWampPeer(), hello));
        try {
            assertTrue(mRole.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(hello, mRole.getLatestMessage());
    }

    public void testResolveRxMessage() {
        final WampMessage welcome = WampMessageFactory.createWelcome(1, new JSONObject());
        mRole.setCountDownLatch(new CountDownLatch(1));
        assertTrue(mRole.resolveRxMessage(new MockWampPeer(), welcome, new OnReplyListener() {
            @Override
            public void onReply(WampPeer receiver, WampMessage reply) {
                fail();
            }
        }));
        try {
            assertTrue(mRole.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(welcome, mRole.getLatestMessage());
    }

    // public void testChainOfResponsibilityAtResolveTxMessage() {
    // final WampMessage hello = WampMessageFactory.createHello("", new
    // JSONObject());
    // TestWampRole firstRole = new TestWampRole(mRole);
    // firstRole.setTxResult(false);
    //
    // mRole.setCountDownLatch(new CountDownLatch(1));
    //
    // assertTrue(firstRole.resolveTxMessage(new MockWampPeer(), hello));
    // try {
    // assertTrue(mRole.await(1, TimeUnit.SECONDS));
    // } catch (InterruptedException e) {
    // fail();
    // }
    // assertEquals(hello, mRole.getLatestMessage());
    // }

    // public void testChainOfResponsibilityAtResolveRxMessage() {
    // final WampMessage welcome = WampMessageFactory.createWelcome(1, new
    // JSONObject());
    // TestWampRole firstRole = new TestWampRole(mRole);
    // firstRole.setRxResult(false);
    //
    // mRole.setCountDownLatch(new CountDownLatch(1));
    //
    // assertTrue(firstRole.resolveRxMessage(new MockWampPeer(), welcome, new
    // OnReplyListener() {
    // @Override
    // public void onReply(WampPeer receiver, WampMessage reply) {
    // }
    // }));
    // try {
    // assertTrue(mRole.await(1, TimeUnit.SECONDS));
    // } catch (InterruptedException e) {
    // fail();
    // }
    // assertEquals(welcome, mRole.getLatestMessage());
    // }
}
