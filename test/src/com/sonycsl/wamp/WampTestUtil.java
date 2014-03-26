
package com.sonycsl.wamp;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class WampTestUtil {

    public static WampMessage broadcastHello(TestWampPeer from) {
        CountDownLatch latch = new CountDownLatch(1);
        from.setCountDownLatch(latch);

        WampMessage msg = WampMessageFactory.createHello("realm", new JSONObject());
        from.broadcast(msg);

        try {
            TestCase.assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        return from.getMessage();
    }

    public static void broadcastHelloSuccess(TestWampPeer from) {
        TestCase.assertTrue(broadcastHello(from).isWelcomeMessage());
    }

    public static WampMessage sendSubscribe(TestWampPeer from, String topic) {
        CountDownLatch latch = new CountDownLatch(1);
        from.setCountDownLatch(latch);

        WampMessage msg = WampMessageFactory.createSubscribe(1, new JSONObject(), topic);
        from.broadcast(msg);

        try {
            TestCase.assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        return from.getMessage();
    }

    public static void broadcastSubscribeSuccess(TestWampPeer from, String topic) {
        TestCase.assertTrue(sendSubscribe(from, topic).isSubscribedMessage());
    }

    public static WampMessage broadcastPublish(TestWampPeer from, String topic) {
        CountDownLatch latch = new CountDownLatch(1);
        from.setCountDownLatch(latch);

        WampMessage msg = WampMessageFactory.createPublish(1, new JSONObject(), topic,
                new JSONArray(), new JSONObject());
        from.broadcast(msg);

        try {
            TestCase.assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        return from.getMessage();
    }

    public static void broadcastPublishSuccess(TestWampPeer from, String topic) {
        TestCase.assertTrue(broadcastPublish(from, topic).isPublishedMessage());
    }

    public static void broadcastPublishSuccess(TestWampPeer publisher, String topic,
            TestWampPeer[] subscribers) {

        for (TestWampPeer subscriber : subscribers) {
            final CountDownLatch latch = new CountDownLatch(1);
            subscriber.setCountDownLatch(latch);
        }

        TestCase.assertTrue(broadcastPublish(publisher, topic).isPublishedMessage());

        for (TestWampPeer subscriber : subscribers) {
            try {
                TestCase.assertTrue(subscriber.await(1, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                TestCase.fail();
            }
            TestCase.assertTrue(subscriber.getMessage().isEventMessage());
        }
    }

    public static WampMessage broadcastUnsubscribe(TestWampPeer from, int subscriptionId) {
        from.setCountDownLatch(new CountDownLatch(1));
        WampMessage msg = WampMessageFactory.createUnsubscribe(1, subscriptionId);
        from.broadcast(msg);

        try {
            TestCase.assertTrue(from.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        return from.getMessage();
    }

    public static void broadcastUnsubscribeSuccess(TestWampPeer from, int subscriptionId) {
        TestCase.assertTrue(broadcastUnsubscribe(from, subscriptionId).isUnsubscribedMessage());
    }
}
