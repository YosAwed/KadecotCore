
package com.sonycsl.wamp;

import com.sonycsl.wamp.mock.WampMockPeer;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class WampTestUtil {

    public static WampMessage broadcastHello(WampMockPeer client) {
        CountDownLatch latch = new CountDownLatch(1);
        client.setCountDownLatch(latch);

        WampMessage msg = WampMessageFactory.createHello("realm", new JSONObject());
        client.broadcast(msg);

        try {
            TestCase.assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        return client.getMessage();
    }

    public static void broadcastHelloSuccess(WampMockPeer client) {
        TestCase.assertTrue(broadcastHello(client).isWelcomeMessage());
    }

    public static WampMessage sendSubscribe(WampMockPeer subscriber, String topic) {
        CountDownLatch latch = new CountDownLatch(1);
        subscriber.setCountDownLatch(latch);

        WampMessage msg = WampMessageFactory.createSubscribe(1, new JSONObject(), topic);
        subscriber.broadcast(msg);

        try {
            TestCase.assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        return subscriber.getMessage();
    }

    public static void broadcastSubscribeSuccess(WampMockPeer subscriber, String topic) {
        TestCase.assertTrue(sendSubscribe(subscriber, topic).isSubscribedMessage());
    }

    public static WampMessage broadcastPublish(WampMockPeer publisher, String topic) {
        CountDownLatch latch = new CountDownLatch(1);
        publisher.setCountDownLatch(latch);

        WampMessage msg = WampMessageFactory.createPublish(1, new JSONObject(), topic,
                new JSONArray(), new JSONObject());
        publisher.broadcast(msg);

        try {
            TestCase.assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        return publisher.getMessage();
    }

    public static void broadcastPublishSuccess(WampMockPeer publisher, String topic) {
        TestCase.assertTrue(broadcastPublish(publisher, topic).isPublishedMessage());
    }

    public static void broadcastPublishSuccess(WampMockPeer publisher, String topic,
            WampMockPeer[] subscribers) {

        for (WampMockPeer subscriber : subscribers) {
            final CountDownLatch latch = new CountDownLatch(1);
            subscriber.setCountDownLatch(latch);
        }

        TestCase.assertTrue(broadcastPublish(publisher, topic).isPublishedMessage());

        for (WampMockPeer subscriber : subscribers) {
            try {
                TestCase.assertTrue(subscriber.await(1, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                TestCase.fail();
            }
            TestCase.assertTrue(subscriber.getMessage().isEventMessage());
        }
    }

    public static WampMessage broadcastUnsubscribe(WampMockPeer subscriber, int subscriptionId) {
        subscriber.setCountDownLatch(new CountDownLatch(1));
        WampMessage msg = WampMessageFactory.createUnsubscribe(1, subscriptionId);
        subscriber.broadcast(msg);

        try {
            TestCase.assertTrue(subscriber.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        return subscriber.getMessage();
    }

    public static void broadcastUnsubscribeSuccess(WampMockPeer subscriber, int subscriptionId) {
        TestCase.assertTrue(broadcastUnsubscribe(subscriber, subscriptionId).isUnsubscribedMessage());
    }
}
