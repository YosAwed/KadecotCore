/*
 * Copyright (C) Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp;

import com.sonycsl.wamp.mock.WampMockPeer;

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

        @Override
        protected void onBroadcast(WampMessage msg) {
        }
    }

    private TestWampBroker mBroker;
    private WampMockPeer mFriendPeer1;
    private WampMockPeer mFriendPeer2;
    private WampMockPeer mFriendPeer3;

    @Override
    protected void setUp() {
        mBroker = new TestWampBroker();
        mFriendPeer1 = new WampMockPeer();
        mFriendPeer2 = new WampMockPeer();
        mFriendPeer3 = new WampMockPeer();
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

    public void testSubscribe() {
        WampTestUtil.broadcastHelloSuccess(mFriendPeer1);
        WampTestUtil.broadcastSubscribeSuccess(mFriendPeer1, "some_topic");
    }

    public void testSubscribeWithoutHello() {
        assertTrue(WampTestUtil.broadcastSubscribe(mFriendPeer1, "some_topic").isErrorMessage());
    }

    public void testSubscribeWithTwoClient() {
        WampTestUtil.broadcastHelloSuccess(mFriendPeer1);
        WampTestUtil.broadcastHelloSuccess(mFriendPeer2);
        WampTestUtil.broadcastSubscribeSuccess(mFriendPeer1, "some_topic");
        WampTestUtil.broadcastSubscribeSuccess(mFriendPeer2, "some_topic");
    }

    public void testPublishWithoutHello() {
        assertTrue(WampTestUtil.broadcastPublish(mFriendPeer1, "some_topic").isErrorMessage());
    }

    public void testPublish() {
        WampTestUtil.broadcastHelloSuccess(mFriendPeer1);
        WampTestUtil.broadcastHelloSuccess(mFriendPeer2);
        WampTestUtil.broadcastSubscribeSuccess(mFriendPeer1, "some_topic");
        WampTestUtil.broadcastPublishSuccess(mFriendPeer2, "some_topic", new WampMockPeer[] {
                mFriendPeer1
        });
    }

    public void testPublishToUnknownTopic() {
        WampTestUtil.broadcastHelloSuccess(mFriendPeer1);
        WampTestUtil.broadcastHelloSuccess(mFriendPeer2);

        mFriendPeer2.setCountDownLatch(new CountDownLatch(1));
        assertTrue(WampTestUtil.broadcastPublish(mFriendPeer1, "unknown_topic")
                .isPublishedMessage());

        try {
            assertFalse(mFriendPeer2.await(1, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testPublishUnderMultiSubscribers() {
        WampTestUtil.broadcastHelloSuccess(mFriendPeer1);
        WampTestUtil.broadcastHelloSuccess(mFriendPeer2);
        WampTestUtil.broadcastHelloSuccess(mFriendPeer3);
        WampTestUtil.broadcastSubscribeSuccess(mFriendPeer1, "some_topic");
        WampTestUtil.broadcastSubscribeSuccess(mFriendPeer2, "some_topic");
        WampTestUtil.broadcastPublishSuccess(mFriendPeer3, "some_topic", new WampMockPeer[] {
                mFriendPeer1, mFriendPeer2
        });
    }

    public void testUnsubscribe() {
        WampTestUtil.broadcastHelloSuccess(mFriendPeer1);
        WampTestUtil.broadcastHelloSuccess(mFriendPeer2);

        WampMessage msg = WampTestUtil.broadcastSubscribe(mFriendPeer1, "some_topic");
        assertTrue(msg.isSubscribedMessage());

        WampSubscribedMessage subscribedMessege = msg.asSubscribedMessage();
        int subscriptionId = subscribedMessege.getSubscriptionId();
        WampTestUtil.broadcastUnsubscribeSuccess(mFriendPeer1, subscriptionId);

        mFriendPeer1.setCountDownLatch(new CountDownLatch(1));
        WampTestUtil.broadcastPublishSuccess(mFriendPeer2, "some_topic");

        try {
            assertFalse(mFriendPeer1.await(1, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            fail();
        }

    }
}
