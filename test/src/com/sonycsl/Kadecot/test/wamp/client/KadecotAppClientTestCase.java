/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.test.wamp.client;

import com.sonycsl.Kadecot.wamp.client.KadecotAppClient;
import com.sonycsl.Kadecot.wamp.client.KadecotAppClient.MessageListener;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.mock.MockWampPeer;
import com.sonycsl.wamp.util.TestableCallback;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotAppClientTestCase extends TestCase {
    private MockWampPeer mPeer;

    private KadecotAppClient mClient;

    @Override
    protected void setUp() {
        mPeer = new MockWampPeer();
        mClient = new KadecotAppClient();
        mClient.connect(mPeer);
    }

    public void testTxHello() {
        TestableCallback helloListener = new TestableCallback();
        helloListener.setTargetMessageType(WampMessageType.HELLO, new CountDownLatch(1));
        mPeer.setCallback(helloListener);
        mClient.transmit(WampMessageFactory.createHello("", new JSONObject()));
        try {
            assertTrue(helloListener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testRxWelcome() {
        final CountDownLatch latch = new CountDownLatch(1);
        mClient.setOnMessageListener(new MessageListener() {

            @Override
            public void onMessage(WampMessage msg) {
                assertEquals(WampMessageType.WELCOME, msg.getMessageType());
                latch.countDown();
            }
        });
        mPeer.transmit(WampMessageFactory.createWelcome(0, new JSONObject()));
        try {
            assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testCall() {
        TestableCallback helloListener = new TestableCallback();
        helloListener.setTargetMessageType(WampMessageType.HELLO, new CountDownLatch(1));
        mPeer.setCallback(helloListener);
        mClient.transmit(WampMessageFactory.createHello("", new JSONObject()));
        try {
            assertTrue(helloListener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        TestableCallback callListener = new TestableCallback();
        callListener.setTargetMessageType(WampMessageType.CALL, new CountDownLatch(1));
        mPeer.setCallback(callListener);
        mClient.transmit(WampMessageFactory.createCall(1, new JSONObject(), ""));
        try {
            assertTrue(callListener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testRxResult() {
        TestableCallback helloListener = new TestableCallback();
        helloListener.setTargetMessageType(WampMessageType.HELLO, new CountDownLatch(1));
        mPeer.setCallback(helloListener);
        mClient.transmit(WampMessageFactory.createHello("", new JSONObject()));
        try {
            assertTrue(helloListener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        TestableCallback callListener = new TestableCallback();
        callListener.setTargetMessageType(WampMessageType.CALL, new CountDownLatch(1));
        mPeer.setCallback(callListener);
        mClient.transmit(WampMessageFactory.createCall(WampRequestIdGenerator.getId(),
                new JSONObject(), ""));
        try {
            assertTrue(callListener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        final CountDownLatch result = new CountDownLatch(1);
        mClient.setOnMessageListener(new MessageListener() {

            @Override
            public void onMessage(WampMessage msg) {
                assertEquals(WampMessageType.RESULT, msg.getMessageType());
                result.countDown();
            }
        });

        mPeer.transmit(WampMessageFactory.createResult(callListener.getTargetMessage()
                .asCallMessage().getRequestId(), new JSONObject()));
        try {
            assertTrue(result.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testTxSubscribe() {
        TestableCallback subscribeListener = new TestableCallback();
        subscribeListener.setTargetMessageType(WampMessageType.SUBSCRIBE, new CountDownLatch(1));
        mPeer.setCallback(subscribeListener);
        mClient.transmit(WampMessageFactory.createSubscribe(WampRequestIdGenerator.getId(),
                new JSONObject(), "topic"));
        try {
            assertTrue(subscribeListener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testRxSubscribed() {
        TestableCallback subscribeListener = new TestableCallback();
        subscribeListener.setTargetMessageType(WampMessageType.SUBSCRIBE, new CountDownLatch(1));
        mPeer.setCallback(subscribeListener);
        mClient.transmit(WampMessageFactory.createSubscribe(WampRequestIdGenerator.getId(),
                new JSONObject(), "topic"));
        try {
            assertTrue(subscribeListener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        TestableCallback subscribedListener = new TestableCallback();
        subscribedListener.setTargetMessageType(WampMessageType.SUBSCRIBED, new CountDownLatch(1));
        mClient.setCallback(subscribedListener);
        mPeer.transmit(WampMessageFactory.createSubscribed(subscribeListener.getTargetMessage()
                .asSubscribeMessage().getRequestId(), 1));
        try {
            assertTrue(subscribedListener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testRxEvent() {
        TestableCallback subscribeListener = new TestableCallback();
        subscribeListener.setTargetMessageType(WampMessageType.SUBSCRIBE, new CountDownLatch(1));
        mPeer.setCallback(subscribeListener);
        mClient.transmit(WampMessageFactory.createSubscribe(WampRequestIdGenerator.getId(),
                new JSONObject(), "topic"));
        try {
            assertTrue(subscribeListener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        TestableCallback subscribedListener = new TestableCallback();
        subscribedListener.setTargetMessageType(WampMessageType.SUBSCRIBED, new CountDownLatch(1));
        mClient.setCallback(subscribedListener);
        mPeer.transmit(WampMessageFactory.createSubscribed(subscribeListener.getTargetMessage()
                .asSubscribeMessage().getRequestId(), 1));
        try {
            assertTrue(subscribedListener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        TestableCallback eventListener = new TestableCallback();
        eventListener.setTargetMessageType(WampMessageType.EVENT, new CountDownLatch(1));
        mClient.setCallback(eventListener);
        mPeer.transmit(WampMessageFactory.createEvent(subscribedListener.getTargetMessage()
                .asSubscribedMessage().getSubscriptionId(), WampRequestIdGenerator.getId(),
                new JSONObject()));
        try {
            assertTrue(eventListener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testTxUnsubscribe() {
        TestableCallback unsubscribeListener = new TestableCallback();
        unsubscribeListener
                .setTargetMessageType(WampMessageType.UNSUBSCRIBE, new CountDownLatch(1));
        mPeer.setCallback(unsubscribeListener);
        mClient.transmit(WampMessageFactory.createUnsubscribe(WampRequestIdGenerator.getId(), 0));
        try {
            assertTrue(unsubscribeListener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testRxUnsubscribed() {
        TestableCallback subscribeListener = new TestableCallback();
        subscribeListener.setTargetMessageType(WampMessageType.SUBSCRIBE, new CountDownLatch(1));
        mPeer.setCallback(subscribeListener);
        mClient.transmit(WampMessageFactory.createSubscribe(WampRequestIdGenerator.getId(),
                new JSONObject(), "topic"));
        try {
            assertTrue(subscribeListener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        TestableCallback subscribedListener = new TestableCallback();
        subscribedListener.setTargetMessageType(WampMessageType.SUBSCRIBED, new CountDownLatch(1));
        mClient.setCallback(subscribedListener);
        mPeer.transmit(WampMessageFactory.createSubscribed(subscribeListener.getTargetMessage()
                .asSubscribeMessage().getRequestId(), 1));
        try {
            assertTrue(subscribedListener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        TestableCallback unsubscribeListener = new TestableCallback();
        unsubscribeListener.setTargetMessageType(WampMessageType.UNSUBSCRIBE, new CountDownLatch(
                1));
        mPeer.setCallback(unsubscribeListener);
        mClient.transmit(WampMessageFactory.createUnsubscribe(WampRequestIdGenerator.getId(),
                subscribedListener.getTargetMessage().asSubscribedMessage().getSubscriptionId()));
        try {
            assertTrue(unsubscribeListener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        TestableCallback unsubscribedListener = new TestableCallback();
        unsubscribedListener.setTargetMessageType(WampMessageType.UNSUBSCRIBED, new CountDownLatch(
                1));
        mClient.setCallback(unsubscribedListener);
        mPeer.transmit(WampMessageFactory.createUnsubscribed(unsubscribeListener
                .getTargetMessage().asUnsubscribeMessage().getRequestId()));
        try {
            assertTrue(unsubscribedListener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }
}
