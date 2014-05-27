/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.wamp;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampPeerTestCase extends TestCase {

    private static class EchoPeer extends TestablePeer {

        @Override
        protected void onReceived(WampMessage msg) {
            super.onReceived(msg);
            transmit(msg);
        }

    }

    private static final String TEST_REALM = "test";
    private static final JSONObject DETAILS = new JSONObject();

    private TestablePeer mPeer;
    private EchoPeer mEchoPeer;

    @Override
    protected void setUp() throws Exception {
        mPeer = new TestablePeer();
        mPeer.setCallback(new TestableCallback());

        mEchoPeer = new EchoPeer();
        mEchoPeer.setCallback(new TestableCallback());

        mPeer.connect(mEchoPeer);
    }

    public void testCtor() {
        assertNotNull(mPeer);
        assertNotNull(mEchoPeer);
    }

    public void testTramsmit() {
        mPeer.getCallback().setTargetMessageType(WampMessageType.HELLO, new CountDownLatch(1));
        mEchoPeer.getCallback()
                .setTargetMessageType(WampMessageType.HELLO, new CountDownLatch(1));

        WampMessage hello = WampMessageFactory.createHello(TEST_REALM, DETAILS);
        mPeer.transmit(hello);
        try {
            assertTrue(mEchoPeer.getCallback().await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(hello, mEchoPeer.getCallback().getTargetMessage());

        try {
            mPeer.getCallback().await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(hello, mPeer.getCallback().getTargetMessage());
    }
}
