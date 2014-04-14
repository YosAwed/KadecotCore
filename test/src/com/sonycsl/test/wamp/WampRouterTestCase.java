/*
 * Copyright (C) Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.wamp;

import com.sonycsl.test.wamp.mock.WampMockPeer;
import com.sonycsl.wamp.WampRouter;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;

import junit.framework.TestCase;

import org.json.JSONObject;

public class WampRouterTestCase extends TestCase {

    private static class TestWampRouter extends WampRouter {
        @Override
        protected boolean consumeRoleMessage(WampMessenger friend, WampMessage msg) {
            return false;
        }

        @Override
        protected void onConsumed(WampMessage msg) {
        }

        @Override
        protected boolean consumeRoleBroadcast(WampMessage msg) {
            return true;
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
