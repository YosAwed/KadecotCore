/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.wamp;

import com.sonycsl.test.wamp.mock.MockWampRouter;
import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampRole;

import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;

public class WampClientSessionTestCase extends TestCase {

    private WampClient mClient;
    private MockWampRouter mRouter;

    @Override
    protected void setUp() {
        mClient = new WampClient() {

            @Override
            protected void onTransmitted(WampPeer peer, WampMessage msg) {
            }

            @Override
            protected void onReceived(WampMessage msg) {
            }

            @Override
            protected void onConnected(WampPeer peer) {
            }

            @Override
            protected Set<WampRole> getClientRoleSet() {
                return new HashSet<WampRole>();
            }
        };
        mClient.setCallback(new TestableCallback());

        mRouter = new MockWampRouter();
        mRouter.setCallback(new TestableCallback());

        mClient.connect(mRouter);
    }

    public void testCtor() {
        assertNotNull(mClient);
        assertNotNull(mRouter);
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
