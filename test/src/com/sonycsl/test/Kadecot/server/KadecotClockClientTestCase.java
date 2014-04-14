
package com.sonycsl.test.Kadecot.server;

import com.sonycsl.Kadecot.wamp.KadecotClockClient;
import com.sonycsl.Kadecot.wamp.KadecotWampBroker;
import com.sonycsl.Kadecot.wamp.KadecotWampTopic;
import com.sonycsl.test.wamp.WampTestUtil;
import com.sonycsl.test.wamp.mock.WampMockPeer;
import com.sonycsl.wamp.message.WampSubscribedMessage;

import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotClockClientTestCase extends TestCase {

    KadecotClockClient mClockClient;
    KadecotWampBroker mBroker;
    WampMockPeer mSubscriber;

    @Override
    protected void setUp() {
        mBroker = new KadecotWampBroker();
        mSubscriber = new WampMockPeer();

        mSubscriber.connect(mBroker);
        mClockClient = new KadecotClockClient(mBroker, 5, TimeUnit.SECONDS);
    }

    public void testStart() {
        WampSubscribedMessage msg = WampTestUtil.broadcastSubscribe(mSubscriber,
                KadecotWampTopic.TOPIC_PRIVATE_SEARCH).asSubscribedMessage();
        mClockClient.start();

        CountDownLatch eventLatch = new CountDownLatch(1);
        mSubscriber.setCountDownLatch(eventLatch);
        try {
            TestCase.assertTrue(eventLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }
        TestCase.assertTrue(mSubscriber.getMessage().isEventMessage());
        int eventSubscriptionId = mSubscriber.getMessage().asEventMessage().getSubscriptionId();
        TestCase.assertTrue(eventSubscriptionId == msg.getSubscriptionId());
    }

    public void testStop() {
        mClockClient.start();
        mClockClient.stop();
        WampTestUtil.broadcastSubscribeSuccess(mSubscriber, KadecotWampTopic.TOPIC_PRIVATE_SEARCH);

        CountDownLatch eventLatch = new CountDownLatch(1);
        mSubscriber.setCountDownLatch(eventLatch);
        try {
            TestCase.assertFalse(eventLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }
        TestCase.assertTrue(mSubscriber.getMessage().isSubscribedMessage());
    }
}
