/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.wamp;

import com.sonycsl.test.util.TestableCallback;
import com.sonycsl.test.util.TestablePeer;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.role.WampRole;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
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

    public void testNullMsg() {
        try {
            mPeer.transmit(null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testNullRole() {
        WampPeer peer = new WampPeer() {
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
            protected Set<WampRole> getRoleSet() {
                return null;
            }
        };

        try {
            peer.connect(mPeer);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testTransmitWithNoRole() {
        WampPeer peer = new WampPeer() {
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
            protected Set<WampRole> getRoleSet() {
                Set roles = new HashSet<WampRole>();
                return roles;
            }
        };

        peer.connect(mPeer);

        int id = 1;
        JSONObject jsonObject = new JSONObject();
        String reason = WampError.NO_SUCH_REALM;
        String topic = "topic.test";
        String procedure = "procedure.test";

        // peer throws UnsupportedOperationException on transmit
        cannnotHandleMsg(peer, WampMessageFactory.createHello("realm", jsonObject));
        cannnotHandleMsg(peer, WampMessageFactory.createWelcome(id, jsonObject));
        cannnotHandleMsg(peer, WampMessageFactory.createAbort(jsonObject, reason));
        cannnotHandleMsg(peer, WampMessageFactory.createGoodbye(jsonObject, reason));
        cannnotHandleMsg(peer, WampMessageFactory.createError(id, id, jsonObject, reason));
        cannnotHandleMsg(peer, WampMessageFactory.createPublish(id, jsonObject, topic));
        cannnotHandleMsg(peer, WampMessageFactory.createPublished(id, id));
        cannnotHandleMsg(peer, WampMessageFactory.createSubscribe(id, jsonObject, topic));
        cannnotHandleMsg(peer, WampMessageFactory.createSubscribed(id, id));
        cannnotHandleMsg(peer, WampMessageFactory.createUnsubscribe(id, id));
        cannnotHandleMsg(peer, WampMessageFactory.createUnsubscribed(id));
        cannnotHandleMsg(peer, WampMessageFactory.createEvent(id, id, jsonObject));
        cannnotHandleMsg(peer,
                WampMessageFactory.createCall(id, new JSONObject(), procedure));
        cannnotHandleMsg(peer, WampMessageFactory.createResult(id, jsonObject));
        cannnotHandleMsg(peer, WampMessageFactory.createRegister(id, jsonObject, procedure));
        cannnotHandleMsg(peer, WampMessageFactory.createRegistered(id, id));
        cannnotHandleMsg(peer, WampMessageFactory.createUnregister(id, id));
        cannnotHandleMsg(peer, WampMessageFactory.createUnregistered(id));
        cannnotHandleMsg(peer, WampMessageFactory.createInvocation(id, id, jsonObject));
        cannnotHandleMsg(peer, WampMessageFactory.createYield(id, jsonObject));
    }

    public void testReceiveWithNoRole() {
        WampPeer peer = new WampPeer() {
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
            protected Set<WampRole> getRoleSet() {
                Set roles = new HashSet<WampRole>();
                return roles;
            }
        };

        peer.connect(mPeer);

        int id = 1;
        JSONObject jsonObject = new JSONObject();
        String reason = WampError.NO_SUCH_REALM;
        String topic = "topic.test";
        String procedure = "procedure.test";

        // Not mPeer but peer throws UnsupportedOperationException on onReceive
        cannnotHandleMsg(mPeer, WampMessageFactory.createHello("realm", jsonObject));
        cannnotHandleMsg(mPeer, WampMessageFactory.createWelcome(id, jsonObject));
        cannnotHandleMsg(mPeer, WampMessageFactory.createAbort(jsonObject, reason));
        cannnotHandleMsg(mPeer, WampMessageFactory.createGoodbye(jsonObject, reason));
        cannnotHandleMsg(mPeer, WampMessageFactory.createError(id, id, jsonObject, reason));
        cannnotHandleMsg(mPeer, WampMessageFactory.createPublish(id, jsonObject, topic));
        cannnotHandleMsg(mPeer, WampMessageFactory.createPublished(id, id));
        cannnotHandleMsg(mPeer, WampMessageFactory.createSubscribe(id, jsonObject, topic));
        cannnotHandleMsg(mPeer, WampMessageFactory.createSubscribed(id, id));
        cannnotHandleMsg(mPeer, WampMessageFactory.createUnsubscribe(id, id));
        cannnotHandleMsg(mPeer, WampMessageFactory.createUnsubscribed(id));
        cannnotHandleMsg(mPeer, WampMessageFactory.createEvent(id, id, jsonObject));
        cannnotHandleMsg(mPeer,
                WampMessageFactory.createCall(id, new JSONObject(), procedure));
        cannnotHandleMsg(mPeer, WampMessageFactory.createResult(id, jsonObject));
        cannnotHandleMsg(mPeer, WampMessageFactory.createRegister(id, jsonObject, procedure));
        cannnotHandleMsg(mPeer, WampMessageFactory.createRegistered(id, id));
        cannnotHandleMsg(mPeer, WampMessageFactory.createUnregister(id, id));
        cannnotHandleMsg(mPeer, WampMessageFactory.createUnregistered(id));
        cannnotHandleMsg(mPeer, WampMessageFactory.createInvocation(id, id, jsonObject));
        cannnotHandleMsg(mPeer, WampMessageFactory.createYield(id, jsonObject));
    }

    private void cannnotHandleMsg(WampPeer peer, WampMessage msg) {
        try {
            peer.transmit(msg);
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }
    }
}
