/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.test.wamp.client;

import com.sonycsl.Kadecot.wamp.client.KadecotAppClientWrapper;
import com.sonycsl.Kadecot.wamp.client.KadecotAppClientWrapper.WampCallListener;
import com.sonycsl.Kadecot.wamp.client.KadecotAppClientWrapper.WampSubscribeListener;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.mock.MockWampPeer;
import com.sonycsl.wamp.util.TestableCallback;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotAppClientWrapperTestCase extends TestCase {

    private MockWampPeer mPeer;

    private KadecotAppClientWrapper mClient;

    @Override
    protected void setUp() {
        mPeer = new MockWampPeer();
        mClient = new KadecotAppClientWrapper();
        mClient.connect(mPeer);
    }

    public void testCall() {
        final CountDownLatch mResultLatch = new CountDownLatch(1);
        final TestableCallback mPeerCallback = new TestableCallback();
        mPeerCallback.setTargetMessageType(WampMessageType.CALL, new CountDownLatch(1));
        mPeer.setCallback(mPeerCallback);

        mClient.call("testProcedure", new JSONObject(), new JSONObject(), new WampCallListener() {

            @Override
            public void onResult(JSONObject details, JSONObject argumentsKw) {
                mResultLatch.countDown();
            }

            @Override
            public void onError(JSONObject details, String error) {
                fail();
            }
        });

        try {
            TestCase.assertTrue(mPeerCallback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        WampMessage msg = mPeerCallback.getTargetMessage();

        TestCase.assertEquals(WampMessageType.CALL, msg.getMessageType());

        final int requestId = msg.asCallMessage().getRequestId();

        mPeer.transmit(WampMessageFactory.createResult(requestId, new JSONObject(),
                new JSONArray(), new JSONObject()));

        try {
            TestCase.assertTrue(mResultLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }
    }

    public void testCallError() {
        final CountDownLatch mErrorLatch = new CountDownLatch(1);
        final TestableCallback mPeerCallback = new TestableCallback();
        mPeerCallback.setTargetMessageType(WampMessageType.CALL, new CountDownLatch(1));
        mPeer.setCallback(mPeerCallback);

        mClient.call("testProcedure", new JSONObject(), new JSONObject(), new WampCallListener() {

            @Override
            public void onResult(JSONObject details, JSONObject argumentsKw) {
                fail();
            }

            @Override
            public void onError(JSONObject details, String error) {
                mErrorLatch.countDown();
            }
        });

        try {
            TestCase.assertTrue(mPeerCallback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        WampMessage msg = mPeerCallback.getTargetMessage();
        TestCase.assertEquals(WampMessageType.CALL, msg.getMessageType());

        final int requestId = msg.asCallMessage().getRequestId();

        mPeer.transmit(WampMessageFactory.createError(WampMessageType.CALL, requestId,
                new JSONObject(), "testError", new JSONArray(), new JSONObject()));

        try {
            TestCase.assertTrue(mErrorLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }
    }

    public void testSubscribe() {
        final CountDownLatch mSubscribedLatch = new CountDownLatch(1);
        final TestableCallback mPeerCallback = new TestableCallback();
        mPeerCallback.setTargetMessageType(WampMessageType.SUBSCRIBE, new CountDownLatch(1));
        mPeer.setCallback(mPeerCallback);

        mClient.subscribe("testTopic", new JSONObject(), new WampSubscribeListener() {

            @Override
            public void onSubscribed(int subscriptionId) {
                mSubscribedLatch.countDown();
            }

            @Override
            public void onEvent(JSONObject details, JSONObject argumentsKw) {
                fail();
            }

            @Override
            public void onError(JSONObject details, String error) {
                fail();
            }
        });

        try {
            TestCase.assertTrue(mPeerCallback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        WampMessage msg = mPeerCallback.getTargetMessage();
        TestCase.assertEquals(WampMessageType.SUBSCRIBE, msg.getMessageType());

        final int requestId = msg.asSubscribeMessage().getRequestId();

        mPeer.transmit(WampMessageFactory.createSubscribed(requestId, 1));

        try {
            TestCase.assertTrue(mSubscribedLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }
    }

    public void testSubscribeError() {
        final CountDownLatch mErrorLatch = new CountDownLatch(1);
        final TestableCallback mPeerCallback = new TestableCallback();
        mPeerCallback.setTargetMessageType(WampMessageType.SUBSCRIBE, new CountDownLatch(1));
        mPeer.setCallback(mPeerCallback);

        mClient.subscribe("testTopic", new JSONObject(), new WampSubscribeListener() {

            @Override
            public void onSubscribed(int subscriptionId) {
                fail();
            }

            @Override
            public void onEvent(JSONObject details, JSONObject argumentsKw) {
                fail();
            }

            @Override
            public void onError(JSONObject details, String error) {
                mErrorLatch.countDown();
            }
        });

        try {
            TestCase.assertTrue(mPeerCallback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        WampMessage msg = mPeerCallback.getTargetMessage();
        TestCase.assertEquals(WampMessageType.SUBSCRIBE, msg.getMessageType());

        final int requestId = msg.asSubscribeMessage().getRequestId();

        mPeer.transmit(WampMessageFactory.createError(WampMessageType.SUBSCRIBE, requestId,
                new JSONObject(), "testError"));

        try {
            TestCase.assertTrue(mErrorLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }
    }

    private static class SubscriptionIdHolder {
        public int mSubscriptionId;
    }

    public void testEvent() {
        final CountDownLatch mSubscribedLatch = new CountDownLatch(1);
        final CountDownLatch mEventLatch = new CountDownLatch(1);
        final TestableCallback mPeerCallback = new TestableCallback();
        mPeerCallback.setTargetMessageType(WampMessageType.SUBSCRIBE, new CountDownLatch(1));
        mPeer.setCallback(mPeerCallback);

        final SubscriptionIdHolder holder = new SubscriptionIdHolder();

        mClient.subscribe("testTopic", new JSONObject(), new WampSubscribeListener() {

            @Override
            public void onSubscribed(int subscriptionId) {
                holder.mSubscriptionId = subscriptionId;
                mSubscribedLatch.countDown();
            }

            @Override
            public void onEvent(JSONObject details, JSONObject argumentsKw) {
                mEventLatch.countDown();
            }

            @Override
            public void onError(JSONObject details, String error) {
                fail();
            }
        });

        try {
            TestCase.assertTrue(mPeerCallback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        WampMessage msg = mPeerCallback.getTargetMessage();

        TestCase.assertEquals(WampMessageType.SUBSCRIBE, msg.getMessageType());

        final int requestId = msg.asSubscribeMessage().getRequestId();

        mPeer.transmit(WampMessageFactory.createSubscribed(requestId, 1));

        try {
            TestCase.assertTrue(mSubscribedLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        mPeer.transmit(WampMessageFactory.createEvent(holder.mSubscriptionId, 1, new JSONObject(),
                new JSONArray(), new JSONObject()));

        try {
            TestCase.assertTrue(mEventLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }
    }
}
