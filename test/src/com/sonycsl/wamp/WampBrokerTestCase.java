/*
 * Copyright (C) Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp;

import android.util.Log;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampBrokerTestCase extends TestCase {

    private static class TestWampBroker extends WampBroker {

        @Override
        protected JSONObject createEventDetails(JSONObject options, JSONArray arguments,
                JSONObject argumentKw) {
            Log.e("WampBrokerTest", "WampBrokerTest");
            JSONObject eventDetails = new JSONObject();
            try {
                return eventDetails.put("detail", "test");
            } catch (JSONException e) {
                /**
                 * Never happens
                 */
                return null;
            }
        }
    }

    private TestWampBroker mBroker;
    private TestWampPeer mFriendPeer1;
    private TestWampPeer mFriendPeer2;
    private TestWampPeer mFriendPeer3;

    private static final int SUBSCRIPTIONID_IDX = 2;

    @Override
    protected void setUp() {
        mBroker = new TestWampBroker();
        mFriendPeer1 = new TestWampPeer();
        mFriendPeer2 = new TestWampPeer();
        mFriendPeer3 = new TestWampPeer();
        mBroker.connect(mFriendPeer1);
        mBroker.connect(mFriendPeer2);
        mBroker.connect(mFriendPeer3);
    }

    public void testCtor() {
        assertNotNull(mBroker);
        assertNotNull(mFriendPeer1);
        assertNotNull(mFriendPeer2);
        assertNotNull(mFriendPeer3);
    }

    private void sendHello(TestWampPeer messanger) {
        CountDownLatch helloLatch = new CountDownLatch(1);
        messanger.setCountDownLatch(helloLatch);
        WampMessage msg = WampMessageFactory.createHello("realm", new JSONObject());
        messanger.broadcast(msg);

        try {
            assertTrue(helloLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        assertTrue(messanger.getMessage().isWelcomeMessage());
    }

    public void testSendSubscribeWithoutHello() {
        sendSubscribe(mFriendPeer1, "some_topic");
        assertTrue(mFriendPeer1.getMessage().isErrorMessage());
    }

    public void testSendSubscribe() {
        sendHello(mFriendPeer1);
        sendSubscribe(mFriendPeer1, "some_topic");
        assertTrue(mFriendPeer1.getMessage().isSubscribedMessage());
    }

    public void testSendSubscribeWithTwoClient() {
        sendHello(mFriendPeer1);
        sendHello(mFriendPeer2);
        sendSubscribe(mFriendPeer1, "some_topic");
        sendSubscribe(mFriendPeer2, "some_topic");
        assertTrue(mFriendPeer1.getMessage().isSubscribedMessage());
        assertTrue(mFriendPeer2.getMessage().isSubscribedMessage());
    }

    private void sendSubscribe(TestWampPeer testMessenger, String topic) {
        CountDownLatch subscribeLatch = new CountDownLatch(1);
        testMessenger.setCountDownLatch(subscribeLatch);
        WampMessage msg = WampMessageFactory.createSubscribe(1, new JSONObject(), topic);
        testMessenger.broadcast(msg);

        try {
            assertTrue(subscribeLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testSendPublishWithoutHello() {
        try {
            sendPublish(mFriendPeer1, "some_topic");
            assertNotNull(mFriendPeer1.getMessage());
        } catch (IllegalAccessError e) {
            fail();
        }
    }

    public void testSendPublish() {
        testSendSubscribe();
        sendPublish(mFriendPeer1, "some_topic");
        assertTrue(mFriendPeer1.getMessage().isEventMessage());
    }

    public void testSendPublishToUnknownTopic() {
        testSendSubscribe();
        try {
            sendPublish(mFriendPeer1, "unknown_topic");
        } catch (AssertionFailedError e) {
        }
    }

    public void testSendPublishUnderMultiSubscribers() {
        testSendSubscribeWithTwoClient();
        sendHello(mFriendPeer3);
        sendPublish(mFriendPeer1, mFriendPeer2, mFriendPeer3, "some_topic");
        assertTrue(mFriendPeer1.getMessage().isEventMessage());
        assertTrue(mFriendPeer2.getMessage().isEventMessage());
    }

    private void sendPublish(TestWampPeer testMessenger, String topic) {
        CountDownLatch publishLatch = new CountDownLatch(1);
        testMessenger.setCountDownLatch(publishLatch);
        WampMessage msg = WampMessageFactory.createPublish(1, new JSONObject(), topic,
                new JSONArray(), new JSONObject());
        testMessenger.broadcast(msg);

        try {
            assertTrue(publishLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    private void sendPublish(TestWampPeer testMessenger1, TestWampPeer testMessenger2,
            TestWampPeer testMessenger3,
            String topic) {
        CountDownLatch publishLatch1 = new CountDownLatch(1);
        testMessenger1.setCountDownLatch(publishLatch1);
        CountDownLatch publishLatch2 = new CountDownLatch(1);
        testMessenger2.setCountDownLatch(publishLatch2);

        WampMessage msg = WampMessageFactory.createPublish(1, new JSONObject(), topic,
                new JSONArray(), new JSONObject());
        testMessenger3.broadcast(msg);

        try {
            assertTrue(publishLatch1.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        try {
            assertTrue(publishLatch2.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testSendUnsubscribe() {
        testSendSubscribe();
        WampMessage msg = mFriendPeer1.getMessage();
        assertTrue(msg.isSubscribedMessage());
        WampSubscribedMessage subscribedMessege = msg.asSubscribedMessage();
        int subscriptionId = subscribedMessege.getSubscriptionId();
        sendUnsubscribe(mFriendPeer1, subscriptionId);
        assertTrue(mFriendPeer1.getMessage().isUnsubscribedMessage());
        try {
            sendPublish(mFriendPeer1, "some_topic");
            fail();
        } catch (AssertionFailedError e) {
        }

    }

    private void sendUnsubscribe(TestWampPeer testMessenger, int subscriptionId) {
        CountDownLatch unsubscribeLatch = new CountDownLatch(1);
        testMessenger.setCountDownLatch(unsubscribeLatch);
        WampMessage msg = WampMessageFactory.createUnsubscribe(1, subscriptionId);
        testMessenger.broadcast(msg);

        try {
            assertTrue(unsubscribeLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }
}
