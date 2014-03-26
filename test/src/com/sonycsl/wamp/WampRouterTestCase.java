/*
 * Copyright (C) Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp;

import junit.framework.TestCase;

import org.json.JSONObject;

public class WampRouterTestCase extends TestCase {

    private static class TestWampRouter extends WampRouter {
        @Override
        protected boolean consumeRoleMessage(WampMessenger friend, WampMessage msg) {
            return false;
        }
    }

    private TestWampRouter mRouter;
    private TestWampPeer mFriendMessenger;

    @Override
    protected void setUp() {
        mRouter = new TestWampRouter();
        mFriendMessenger = new TestWampPeer();
        mRouter.connect(mFriendMessenger);
    }

    public void testCtor() {
        assertNotNull(mRouter);
        assertNotNull(mFriendMessenger);
    }

    public void testHello() {
        WampMessage msg = WampMessageFactory.createHello("realm", new JSONObject());
        mFriendMessenger.broadcast(msg);
    }
}
