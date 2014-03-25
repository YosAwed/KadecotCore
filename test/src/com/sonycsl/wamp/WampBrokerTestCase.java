/*
 * Copyright (C) Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp;

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
    private WampTestMessenger mFriendMessenger1;
    private WampTestMessenger mFriendMessenger2;
    private WampTestMessenger mFriendMessenger3;
    private WampMessenger mBrokerMessenger1;
    private WampMessenger mBrokerMessenger2;
    private WampMessenger mBrokerMessenger3;

    private static final int SUBSCRIPTIONID_IDX = 2;

    @Override
    protected void setUp() {
        mBroker = new TestWampBroker();
        mFriendMessenger1 = new WampTestMessenger();
        mFriendMessenger2 = new WampTestMessenger();
        mFriendMessenger3 = new WampTestMessenger();
        mBrokerMessenger1 = mBroker.connect(mFriendMessenger1);
        mBrokerMessenger2 = mBroker.connect(mFriendMessenger2);
        mBrokerMessenger3 = mBroker.connect(mFriendMessenger3);
    }

    public void testCtor() {
        assertNotNull(mBroker);
        assertNotNull(mFriendMessenger1);
        assertNotNull(mFriendMessenger2);
        assertNotNull(mFriendMessenger3);
    }

    public void testConnect() {
        assertNotNull(mBrokerMessenger1);
        assertNotNull(mBrokerMessenger2);
        assertNotNull(mBrokerMessenger3);
    }

    private void sendHello(WampTestMessenger messanger, WampMessenger brokerMessenger) {
        CountDownLatch helloLatch = new CountDownLatch(1);
        messanger.setCountDownLatch(helloLatch);
        WampMessage msg = WampMessageFactory.createHello("realm", new JSONObject());
        brokerMessenger.send(msg);

        try {
            assertTrue(helloLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        assertTrue(messanger.getRecievedMessage().isWelcomeMessage());
    }

    public void testSendSubscribeWithoutHello() {
        sendSubscribe(mFriendMessenger1, mBrokerMessenger1, "some_topic");
        assertTrue(mFriendMessenger1.getRecievedMessage().isErrorMessage());
    }

    public void testSendSubscribe() {
        sendHello(mFriendMessenger1, mBrokerMessenger1);
        sendSubscribe(mFriendMessenger1, mBrokerMessenger1, "some_topic");
        assertTrue(mFriendMessenger1.getRecievedMessage().isSubscribedMessage());
    }

    public void testSendSubscribeWithTwoClient() {
        sendHello(mFriendMessenger1, mBrokerMessenger1);
        sendHello(mFriendMessenger2, mBrokerMessenger2);
        sendSubscribe(mFriendMessenger1, mBrokerMessenger1, "some_topic");
        sendSubscribe(mFriendMessenger2, mBrokerMessenger2, "some_topic");
        assertTrue(mFriendMessenger1.getRecievedMessage().isSubscribedMessage());
        assertTrue(mFriendMessenger2.getRecievedMessage().isSubscribedMessage());
    }

    private void sendSubscribe(WampTestMessenger testMessenger, WampMessenger brokerMessenger,
            String topic) {
        CountDownLatch subscribeLatch = new CountDownLatch(1);
        testMessenger.setCountDownLatch(subscribeLatch);
        WampMessage msg = WampMessageFactory.createSubscribe(1, new JSONObject(), topic);
        brokerMessenger.send(msg);

        try {
            assertTrue(subscribeLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testSendPublishWithoutHello() {
        try {
            sendPublish(mFriendMessenger1, mBrokerMessenger1, "some_topic");
            assertNotNull(mFriendMessenger1.getRecievedMessage());
        } catch (IllegalAccessError e) {
            fail();
        }
    }

    public void testSendPublish() {
        testSendSubscribe();
        sendPublish(mFriendMessenger1, "some_topic");
        assertTrue(mFriendMessenger1.getRecievedMessage().isEventMessage());
    }

    public void testSendPublishToUnknownTopic() {
        testSendSubscribe();
        try {
            sendPublish(mFriendMessenger1, "unknown_topic");
        } catch (AssertionFailedError e) {
        }
    }

    public void testSendPublishUnderMultiSubscribers() {
        testSendSubscribeWithTwoClient();
        sendPublish(mFriendMessenger1, mFriendMessenger2, "some_topic");
        assertTrue(mFriendMessenger1.getRecievedMessage().isEventMessage());
        assertTrue(mFriendMessenger2.getRecievedMessage().isEventMessage());
    }

    private void sendPublish(WampTestMessenger testMessenger, String topic) {
        sendPublish(testMessenger, mBrokerMessenger3, topic);
    }

    private void sendPublish(WampTestMessenger testMessenger, WampMessenger brokerMessenger,
            String topic) {
        CountDownLatch publishLatch = new CountDownLatch(1);
        testMessenger.setCountDownLatch(publishLatch);
        WampMessage msg = WampMessageFactory.createPublish(1, new JSONObject(), topic,
                new JSONArray(), new JSONObject());
        brokerMessenger.send(msg);

        try {
            assertTrue(publishLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    private void sendPublish(WampTestMessenger testMessenger1, WampTestMessenger testMessenger2,
            String topic) {
        CountDownLatch publishLatch1 = new CountDownLatch(1);
        testMessenger1.setCountDownLatch(publishLatch1);
        CountDownLatch publishLatch2 = new CountDownLatch(1);
        testMessenger2.setCountDownLatch(publishLatch2);

        WampMessage msg = WampMessageFactory.createPublish(1, new JSONObject(), topic,
                new JSONArray(), new JSONObject());
        mBrokerMessenger3.send(msg);

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
        WampMessage msg = mFriendMessenger1.getRecievedMessage();
        assertTrue(msg.isSubscribedMessage());
        WampSubscribedMessage subscribedMessege = msg.asSubscribedMessage();
        int subscriptionId = subscribedMessege.getSubscriptionId();
        sendUnsubscribe(mFriendMessenger1, mBrokerMessenger1, subscriptionId);
        assertTrue(mFriendMessenger1.getRecievedMessage().isUnsubscribedMessage());
        try {
            sendPublish(mFriendMessenger1, "some_topic");
            fail();
        } catch (AssertionFailedError e) {
        }

    }

    private void sendUnsubscribe(WampTestMessenger testMessenger, WampMessenger brokerMessenger,
            int subscriptionId) {
        CountDownLatch unsubscribeLatch = new CountDownLatch(1);
        testMessenger.setCountDownLatch(unsubscribeLatch);
        WampMessage msg = WampMessageFactory.createUnsubscribe(1, subscriptionId);
        brokerMessenger.send(msg);

        try {
            assertTrue(unsubscribeLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }
}
