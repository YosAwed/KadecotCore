/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.kadecot.wamp;

import com.sonycsl.kadecot.wamp.KadecotTopicTimer;
import com.sonycsl.test.wamp.Testable;
import com.sonycsl.test.wamp.mock.MockWampClient;
import com.sonycsl.test.wamp.mock.MockWampRouter;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotTopicTimerTestCase extends TestCase {

    private static final String TOPIC = "kadecot.topic.timer.test.case.topic";

    private static final class TestKadecotTopicTimer extends KadecotTopicTimer implements Testable {

        private CountDownLatch mLatch;
        private WampMessage mMsg;

        public TestKadecotTopicTimer(String topic, long pollingTime, TimeUnit timeUnit) {
            super(topic, pollingTime, timeUnit);
        }

        @Override
        public void setCountDownLatch(CountDownLatch latch) {
            mLatch = latch;
        }

        @Override
        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return mLatch.await(timeout, unit);
        }

        @Override
        public WampMessage getLatestMessage() {
            return mMsg;
        }

        @Override
        protected void OnReceived(WampMessage msg) {
            super.OnReceived(msg);
            mMsg = msg;
            if (mLatch != null) {
                mLatch.countDown();
            }
        }

    }

    private TestKadecotTopicTimer mTopicTimer;
    private MockWampRouter mRouter;
    private MockWampClient mSubscriber;

    @Override
    protected void setUp() {
        mTopicTimer = new TestKadecotTopicTimer(TOPIC, 100, TimeUnit.MILLISECONDS);
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
        mSubscriber.setCountDownLatch(new CountDownLatch(1));
        mSubscriber.transmit(WampMessageFactory.createHello("", new JSONObject()));
        try {
            assertTrue(mSubscriber.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        mTopicTimer.setCountDownLatch(new CountDownLatch(1));
        mTopicTimer.transmit(WampMessageFactory.createHello("", new JSONObject()));
        try {
            assertTrue(mTopicTimer.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        mSubscriber.setCountDownLatch(new CountDownLatch(1));
        mSubscriber.transmit(WampMessageFactory.createSubscribe(WampRequestIdGenerator.getId(),
                new JSONObject(), TOPIC));
        try {
            assertTrue(mSubscriber.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        assertNotNull(mSubscriber.getAllMessages());
        assertTrue(mSubscriber.getAllMessages().size() > 0);

        mTopicTimer.setCountDownLatch(new CountDownLatch(1));
        mTopicTimer.transmit(WampMessageFactory.createGoodbye(new JSONObject(),
                WampError.CLOSE_REALM));
        try {
            assertTrue(mTopicTimer.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }
}
