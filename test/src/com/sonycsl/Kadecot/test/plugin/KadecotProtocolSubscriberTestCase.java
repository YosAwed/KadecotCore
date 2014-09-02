/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.test.plugin;

import com.sonycsl.Kadecot.plugin.KadecotProtocolClient.ProtocolSearchEventListener;
import com.sonycsl.Kadecot.plugin.KadecotProtocolSubscriber;
import com.sonycsl.Kadecot.wamp.KadecotWampTopic;
import com.sonycsl.Kadecot.wamp.client.provider.WampProviderAccessHelper;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.mock.MockWampClient;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotProtocolSubscriberTestCase extends TestCase {
    public static final class LatchSearchListener implements ProtocolSearchEventListener {

        private CountDownLatch mLatch;

        @Override
        public void search() {
            if (mLatch != null) {
                mLatch.countDown();
            }
        }

        public void setLatch(CountDownLatch latch) {
            mLatch = latch;
        }

        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return mLatch.await(timeout, unit);
        }
    }

    public static final class LatchTopicListener implements
            KadecotProtocolSubscriber.OnTopicListener {

        private CountDownLatch mLatch;
        private String mLastTopic;

        @Override
        public void onTopicStopped(String topic) {
            mLastTopic = topic;
            mLatch.countDown();
        }

        @Override
        public void onTopicStarted(String topic) {
            mLastTopic = topic;
            mLatch.countDown();
        }

        public void setCountDownLatch(CountDownLatch latch) {
            mLatch = latch;
        }

        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return mLatch.await(timeout, unit);
        }

        public String getLastTopic() {
            return mLastTopic;
        }
    }

    private KadecotProtocolSubscriber mSubscriber;
    private MockWampClient mClient;
    private LatchSearchListener mSearchListener;
    private LatchTopicListener mTopicListener;

    @Override
    protected void setUp() throws Exception {
        mSearchListener = new LatchSearchListener();
        mTopicListener = new LatchTopicListener();
        mSubscriber = new KadecotProtocolSubscriber(mSearchListener, mTopicListener);
        mClient = new MockWampClient();
    }

    public void testCtor() {
        assertNotNull(mSubscriber);
    }

    public void testOnSearchEvent() {
        mSubscriber.resolveTxMessage(mClient, WampMessageFactory.createSubscribe(1,
                new JSONObject(), KadecotWampTopic.TOPIC_PRIVATE_SEARCH));
        mSubscriber.resolveRxMessage(mClient, WampMessageFactory.createSubscribed(1, 2), null);
        mSearchListener.setLatch(new CountDownLatch(1));

        try {
            mSubscriber.resolveRxMessage(mClient, WampMessageFactory.createEvent(2, 1,
                    new JSONObject(), new JSONArray(), new JSONObject().put("topic",
                            KadecotWampTopic.TOPIC_PRIVATE_SEARCH)), null);
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        }

        try {
            assertTrue(mSearchListener.await(1, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testOnTopicStarted() {
        mSubscriber.resolveTxMessage(mClient, WampMessageFactory.createSubscribe(1,
                new JSONObject(), WampProviderAccessHelper.Topic.START.getUri()));
        mSubscriber.resolveRxMessage(mClient, WampMessageFactory.createSubscribed(1, 2), null);
        mTopicListener.setCountDownLatch(new CountDownLatch(1));
        try {
            mSubscriber.resolveRxMessage(
                    mClient,
                    WampMessageFactory.createEvent(
                            2,
                            1,
                            new JSONObject(),
                            new JSONArray(),
                            new JSONObject().put("topic",
                                    WampProviderAccessHelper.Topic.START.getUri())),
                    null);
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        }

        try {
            assertTrue(mTopicListener.await(1, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }

        assertEquals(WampProviderAccessHelper.Topic.START.getUri(), mTopicListener.getLastTopic());
    }

    public void testOnTopicStopped() {
        mSubscriber.resolveTxMessage(mClient, WampMessageFactory.createSubscribe(1,
                new JSONObject(), WampProviderAccessHelper.Topic.STOP.getUri()));
        mSubscriber.resolveRxMessage(mClient, WampMessageFactory.createSubscribed(1, 2), null);
        mTopicListener.setCountDownLatch(new CountDownLatch(1));
        try {
            mSubscriber.resolveRxMessage(
                    mClient,
                    WampMessageFactory.createEvent(
                            2,
                            1,
                            new JSONObject(),
                            new JSONArray(),
                            new JSONObject().put("topic",
                                    WampProviderAccessHelper.Topic.STOP.getUri())),
                    null);
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        }

        try {
            assertTrue(mTopicListener.await(1, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }

        assertEquals(WampProviderAccessHelper.Topic.STOP.getUri(), mTopicListener.getLastTopic());
    }
}
