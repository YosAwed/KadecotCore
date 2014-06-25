/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.Kadecot.wamp;

import com.sonycsl.Kadecot.wamp.KadecotTopicTimer;
import com.sonycsl.test.mock.MockWampClient;
import com.sonycsl.test.mock.MockWampRouter;
import com.sonycsl.test.util.TestableCallback;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotTopicTimerTestCase extends TestCase {

    private static final String TOPIC = "kadecot.topic.timer.test.case.topic";

    private KadecotTopicTimer mTopicTimer;
    private MockWampRouter mRouter;
    private MockWampClient mSubscriber;

    @Override
    protected void setUp() {
        mTopicTimer = new KadecotTopicTimer(TOPIC, 100, TimeUnit.MILLISECONDS);
        mSubscriber = new MockWampClient();
        mRouter = new MockWampRouter();

        mRouter.connect(mSubscriber);
        mRouter.connect(mTopicTimer);
    }

    public void testCtor() {
        assertNotNull(mTopicTimer);
        assertNotNull(mSubscriber);
        assertNotNull(mRouter);

    }

    public void testTxPublish() {
        TestableCallback subListener = new TestableCallback();
        subListener.setTargetMessageType(WampMessageType.WELCOME, new CountDownLatch(1));
        mSubscriber.setCallback(subListener);
        mSubscriber.transmit(WampMessageFactory.createHello("", new JSONObject()));
        try {
            assertTrue(subListener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        TestableCallback topicTimerlistener = new TestableCallback();
        topicTimerlistener.setTargetMessageType(WampMessageType.WELCOME, new CountDownLatch(1));
        mTopicTimer.transmit(WampMessageFactory.createHello("", new JSONObject()));
        mTopicTimer.setCallback(topicTimerlistener);
        try {
            assertTrue(topicTimerlistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        subListener.setTargetMessageType(WampMessageType.SUBSCRIBED, new CountDownLatch(1));
        mSubscriber.transmit(WampMessageFactory.createSubscribe(WampRequestIdGenerator.getId(),
                new JSONObject(), TOPIC));
        try {
            assertTrue(subListener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        assertNotNull(mSubscriber.getAllMessages());
        assertTrue(mSubscriber.getAllMessages().size() > 0);

        topicTimerlistener.setTargetMessageType(WampMessageType.GOODBYE, new CountDownLatch(1));
        mTopicTimer.transmit(WampMessageFactory.createGoodbye(new JSONObject(),
                WampError.CLOSE_REALM));
        try {
            assertTrue(topicTimerlistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }
}
