/*
 * Copyright (C) Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp;

import android.util.Log;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

public class WampRouterTestCase extends TestCase {

    private static class TestWampRouter extends WampRouter {
        @Override
        protected boolean consumeRoleMessage(WampMessenger friend, JSONArray msg) {
            Log.e("hoge", msg.toString());
            return false;
        }
    }

    private TestWampRouter mRouter;
    private TestWampMessenger mFriendMessenger;
    private WampMessenger mRouterMessenger;

    @Override
    protected void setUp() {
        mRouter = new TestWampRouter();
        mFriendMessenger = new TestWampMessenger();
        mRouterMessenger = mRouter.connect(mFriendMessenger);
    }

    public void testCtor() {
        assertNotNull(mRouter);
        assertNotNull(mFriendMessenger);
    }

    public void testConnect() {
        assertNotNull(mRouterMessenger);
    }

    public void testSendHello() {
        WampMessage msg = WampMessageFactory.createHello("realm", new JSONObject());
        mRouterMessenger.send(msg.toJSONArray());
    }
}
