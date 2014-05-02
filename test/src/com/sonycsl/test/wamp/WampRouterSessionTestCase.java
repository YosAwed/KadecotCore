/*
 * Copyright (C) Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.wamp;

import com.sonycsl.test.wamp.mock.MockWampClient;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.WampRouter;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampRole;

import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampRouterSessionTestCase extends TestCase {

    private static class TestWampRouter extends WampRouter implements Testable {
        private CountDownLatch mLatch;
        private WampMessage mMsg;

        @Override
        protected Set<WampRole> getRouterRoleSet() {
            return new HashSet<WampRole>();
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

        @Override
        public void setCountDownLatch(CountDownLatch latch) {
            mLatch = latch;
        }

        @Override
        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return mLatch.await(timeout, unit);
        }

        @Override
        public WampMessage getLatestMessage() {
            return mMsg;
        }
    }

    private TestWampRouter mRouter;
    private MockWampClient mClient;

    @Override
    protected void setUp() {
        mRouter = new TestWampRouter();
        mClient = new MockWampClient();
        mRouter.connect(mClient);
    }

    public void testCtor() {
        assertNotNull(mRouter);
        assertNotNull(mClient);
    }

    public void testHello() {
        WampTestUtil.transmitHelloSuccess(mClient, WampTestParam.REALM, mRouter);
    }

    public void testGoodbye() {
        WampTestUtil.transmitHelloSuccess(mClient, WampTestParam.REALM, mRouter);
        WampTestUtil.transmitGoodbyeSuccess(mClient, WampError.CLOSE_REALM, mRouter);

        WampTestUtil.transmitHelloSuccess(mClient, WampTestParam.REALM, mRouter);
        WampTestUtil.transmitGoodbyeSuccess(mRouter, WampError.CLOSE_REALM, mClient);

        WampTestUtil.transmitHelloSuccess(mClient, WampTestParam.REALM, mRouter);
        WampTestUtil.transmitGoodbyeSuccess(mRouter, WampError.SYSTEM_SHUTDOWN, mClient);
    }
}
