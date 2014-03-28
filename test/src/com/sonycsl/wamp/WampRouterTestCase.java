/*
 * Copyright (C) Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp;

import com.sonycsl.wamp.mock.WampMockPeer;

import junit.framework.TestCase;

import org.json.JSONObject;

public class WampRouterTestCase extends TestCase {

    private static class TestWampRouter extends WampRouter {
        @Override
        protected boolean consumeRoleMessage(WampMessenger friend, WampMessage msg) {
            return false;
        }

        @Override
        protected void onBroadcast(WampMessage msg) {
        }

        @Override
        protected void onConsumed(WampMessage msg) {
        }
    }

    private TestWampRouter mRouter;
    private WampMockPeer mFriend;

    @Override
    protected void setUp() {
        mRouter = new TestWampRouter();
        mFriend = new WampMockPeer();
        mRouter.connect(mFriend);
    }

    public void testCtor() {
        assertNotNull(mRouter);
        assertNotNull(mFriend);
    }

    public void testHello() {
        WampMessage msg = WampMessageFactory.createHello("realm", new JSONObject());
        mFriend.broadcast(msg);
    }
}
