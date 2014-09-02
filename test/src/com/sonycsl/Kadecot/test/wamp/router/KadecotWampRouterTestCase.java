/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.test.wamp.router;

import android.test.mock.MockContentResolver;

import com.sonycsl.Kadecot.wamp.router.KadecotWampRouter;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.mock.MockWampPeer;
import com.sonycsl.wamp.util.TestableCallback;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotWampRouterTestCase extends TestCase {

    MockWampPeer mPeer;
    KadecotWampRouter mRouter;

    @Override
    public void setUp() {
        mPeer = new MockWampPeer();
        mRouter = new KadecotWampRouter(new MockContentResolver());
        mRouter.connect(mPeer);
    }

    public void testTxGoodbye() {
        TestableCallback goodbyeCallback = new TestableCallback();
        goodbyeCallback.setTargetMessageType(WampMessageType.GOODBYE, new CountDownLatch(1));
        mPeer.setCallback(goodbyeCallback);

        mRouter.transmit(WampMessageFactory.createGoodbye(new JSONObject(), "reason"));

        try {
            assertTrue(goodbyeCallback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testRxHello() {
        TestableCallback welcomeCallback = new TestableCallback();
        welcomeCallback.setTargetMessageType(WampMessageType.WELCOME, new CountDownLatch(1));
        mPeer.setCallback(welcomeCallback);

        mPeer.transmit(WampMessageFactory.createHello("realm", new JSONObject()));

        try {
            assertTrue(welcomeCallback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testRxSubscribe() {
        TestableCallback welcomeCallback = new TestableCallback();
        welcomeCallback.setTargetMessageType(WampMessageType.WELCOME, new CountDownLatch(1));
        mPeer.setCallback(welcomeCallback);

        mPeer.transmit(WampMessageFactory.createHello("realm", new JSONObject()));

        try {
            assertTrue(welcomeCallback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        TestableCallback subscribedCallback = new TestableCallback();
        subscribedCallback.setTargetMessageType(WampMessageType.SUBSCRIBED, new CountDownLatch(1));
        mPeer.setCallback(subscribedCallback);

        int requestId = WampRequestIdGenerator.getId();
        String topic = "topic";

        mPeer.transmit(WampMessageFactory.createSubscribe(requestId, new JSONObject(), topic));

        try {
            assertTrue(subscribedCallback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(requestId, subscribedCallback.getTargetMessage().asSubscribedMessage()
                .getRequestId());
    }

    public void testRxRegister() {
        TestableCallback welcomeCallback = new TestableCallback();
        welcomeCallback.setTargetMessageType(WampMessageType.WELCOME, new CountDownLatch(1));
        mPeer.setCallback(welcomeCallback);

        mPeer.transmit(WampMessageFactory.createHello("realm", new JSONObject()));

        try {
            assertTrue(welcomeCallback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        TestableCallback registeredCallback = new TestableCallback();
        registeredCallback.setTargetMessageType(WampMessageType.REGISTERED, new CountDownLatch(1));
        mPeer.setCallback(registeredCallback);

        int requestId = WampRequestIdGenerator.getId();
        String procedure = "proc";

        mPeer.transmit(WampMessageFactory.createRegister(requestId, new JSONObject(), procedure));

        try {
            assertTrue(registeredCallback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(requestId, registeredCallback.getTargetMessage().asRegisteredMessage()
                .getRequestId());
    }
}
