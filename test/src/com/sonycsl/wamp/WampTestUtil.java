
package com.sonycsl.wamp;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class WampTestUtil {

    public static WampMessage broadcastHello(WampTest client) {
        client.setCountDownLatch(new CountDownLatch(1));

        WampMessage msg = WampMessageFactory.createHello("realm", new JSONObject());
        client.broadcast(msg);

        try {
            TestCase.assertTrue(client.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        return client.getMessage();
    }

    public static void broadcastHelloSuccess(WampTest client) {
        TestCase.assertTrue(broadcastHello(client).isWelcomeMessage());
    }

    public static void broadcastGoodbyeSuccess(WampTest from, WampTest to) {

        WampMessage msg = WampMessageFactory.createGoodbye(new JSONObject(), WampError.CLOSE_REALM);

        from.setCountDownLatch(new CountDownLatch(1));
        to.setCountDownLatch(new CountDownLatch(1));

        from.broadcast(msg);

        try {
            TestCase.assertTrue(to.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }
        TestCase.assertTrue(to.getMessage().isGoodbyeMessage());
        TestCase.assertEquals(WampError.CLOSE_REALM, to.getMessage().asGoodbyeMessage().getReason());

        try {
            TestCase.assertTrue(from.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }
        TestCase.assertTrue(from.getMessage().isGoodbyeMessage());
        TestCase.assertEquals(WampError.GOODBYE_AND_OUT, from.getMessage().asGoodbyeMessage()
                .getReason());
    }

    public static WampMessage broadcastSubscribe(WampTest subscriber, String topic) {
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

    public static void broadcastSubscribeSuccess(WampTest subscriber, String topic) {
        TestCase.assertTrue(broadcastSubscribe(subscriber, topic).isSubscribedMessage());
    }

    public static WampMessage broadcastPublish(WampTest publisher, String topic) {
        publisher.setCountDownLatch(new CountDownLatch(1));

        WampMessage msg = WampMessageFactory.createPublish(1, new JSONObject(), topic,
                new JSONArray(), new JSONObject());
        publisher.broadcast(msg);

        try {
            TestCase.assertTrue(publisher.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        return publisher.getMessage();
    }

    public static void broadcastPublishSuccess(WampTest publisher, String topic) {
        TestCase.assertTrue(broadcastPublish(publisher, topic).isPublishedMessage());
    }

    public static void broadcastPublishSuccess(WampTest publisher, String topic,
            WampTest[] subscribers) {

        for (WampTest subscriber : subscribers) {
            subscriber.setCountDownLatch(new CountDownLatch(1));
        }

        TestCase.assertTrue(broadcastPublish(publisher, topic).isPublishedMessage());

        for (WampTest subscriber : subscribers) {
            try {
                TestCase.assertTrue(subscriber.await(1, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                TestCase.fail();
            }
            TestCase.assertTrue(subscriber.getMessage().isEventMessage());
        }
    }

    public static WampMessage broadcastUnsubscribe(WampTest subscriber, int subscriptionId) {
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

    public static void broadcastUnsubscribeSuccess(WampTest subscriber, int subscriptionId) {
        TestCase.assertTrue(broadcastUnsubscribe(subscriber, subscriptionId)
                .isUnsubscribedMessage());
    }

    public static WampMessage broadcastRegister(WampTest callee, String procedure, WampTest router) {

        callee.setCountDownLatch(new CountDownLatch(1));
        router.setCountDownLatch(new CountDownLatch(1));

        callee.broadcast(WampMessageFactory.createRegister(1, new JSONObject(), procedure));

        try {
            router.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            TestCase.fail();
        }
        TestCase.assertTrue(router.getMessage().isRegisterMessage());

        try {
            callee.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            TestCase.fail();
        }
        return callee.getMessage();
    }

    public static void broadcastRegisterSuccess(WampTest callee, String procedure, WampTest router) {
        TestCase.assertTrue(broadcastRegister(callee, procedure, router).isRegisteredMessage());
    }

    public static WampMessage broadcastUnregister(WampTest callee, WampRegisteredMessage msg,
            WampTest router) {

        callee.setCountDownLatch(new CountDownLatch(1));
        router.setCountDownLatch(new CountDownLatch(1));

        callee.broadcast(WampMessageFactory.createUnregister(msg.getRequestId(),
                msg.getRegistrationId()));

        try {
            router.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            TestCase.fail();
        }
        TestCase.assertTrue(router.getMessage().isUnregisterMessage());

        try {
            callee.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            TestCase.fail();
        }
        return callee.getMessage();
    }

    public static void broadcastUnregisterSuccess(WampTest callee, WampRegisteredMessage msg,
            WampTest router) {
        TestCase.assertTrue(broadcastUnregister(callee, msg, router).isUnregisteredMessage());
    }

    public static WampMessage broadcastInvocation(WampTest callee, int registrationId,
            WampTest router) {

        callee.setCountDownLatch(new CountDownLatch(1));
        router.setCountDownLatch(new CountDownLatch(1));

        router.broadcast(WampMessageFactory.createInvocation(1, registrationId,
                new JSONObject()));

        try {
            callee.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            TestCase.fail();
        }
        TestCase.assertTrue(callee.getMessage().isInvocationMessage());

        try {
            router.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            TestCase.fail();
        }
        return router.getMessage();
    }

    public static void broadcastInvocationSuccess(WampTest callee, int registrationId,
            WampTest router) {
        TestCase.assertTrue(broadcastInvocation(callee, registrationId, router).isYieldMessage());
    }
}
