
package com.sonycsl.test.Kadecot.wamp;

import com.sonycsl.test.Kadecot.wamp.mock.MockWebSocket;
import com.sonycsl.test.wamp.mock.WampMockRouter;
import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessageFactory;
import com.sonycsl.wamp.WampResultMessage;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotWampClientTestUtil {

    private static final String TEST_REALM = "realm";
    private static final String TEST_TOPIC = "topic";
    private static final int REQUEST_ID = 999;
    private static final String GET_DEVICE_LIST = "com.sonycsl.kadecot.procedure.getdevicelist";
    private static final int PUBLICATION_ID = 1000;
    private static final int SUBSCRIPTION_ID = 1001;

    public static WampMessage broadcastMessage(WampClient client, WampMessage msg,
            WampMockRouter router, MockWebSocket webSocket) {

        router.setCountDownLatch(new CountDownLatch(1));
        webSocket.setCountDownLatch(new CountDownLatch(1));

        client.broadcast(msg);

        try {
            TestCase.assertTrue(router.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        try {
            TestCase.assertTrue(webSocket.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }
        return webSocket.getMessage();
    }

    public static void broadcastHelloSuccess(WampClient client, WampMockRouter router,
            MockWebSocket webSocket) {
        TestCase.assertTrue(broadcastMessage(client,
                WampMessageFactory.createHello(TEST_REALM, new JSONObject()), router, webSocket)
                .isWelcomeMessage());
    }

    public static void broadcastCallSuccess(WampClient client, WampMockRouter router,
            MockWebSocket webSocket) {
        WampMessage msg = broadcastMessage(client,
                WampMessageFactory.createCall(REQUEST_ID, new JSONObject(), GET_DEVICE_LIST),
                router, webSocket);
        TestCase.assertTrue(msg.isResultMessage());
        WampResultMessage result = msg.asResultMessage();
        TestCase.assertEquals(REQUEST_ID, result.getRequestId());
    }

    public static void broadcastSubscribeSuccess(WampClient client, WampMockRouter router,
            MockWebSocket webSocket) {
        WampMessage msg = broadcastMessage(client,
                WampMessageFactory.createSubscribe(REQUEST_ID, new JSONObject(), TEST_TOPIC),
                router, webSocket);
        TestCase.assertTrue(msg.isSubscribedMessage());
    }

    public static void broadcastUnsubscribeSuccess(WampClient client, WampMockRouter router,
            MockWebSocket webSocket) {
        WampMessage msg = broadcastMessage(client,
                WampMessageFactory.createUnsubscribe(REQUEST_ID, SUBSCRIPTION_ID), router,
                webSocket);
        TestCase.assertTrue(msg.isUnsubscribedMessage());
    }

    public static WampMessage broadcastGoodbye(WampClient client, WampMockRouter router,
            MockWebSocket webSocket) {

        router.setCountDownLatch(new CountDownLatch(1));
        webSocket.setCountDownLatch(new CountDownLatch(1));

        router.broadcast(WampMessageFactory.createGoodbye(new JSONObject(), WampError.CLOSE_REALM));

        try {
            TestCase.assertTrue(webSocket.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        WampMessage msg = webSocket.getMessage();
        TestCase.assertTrue(msg.toString(), msg.isGoodbyeMessage());
        TestCase.assertEquals(WampError.CLOSE_REALM, msg.asGoodbyeMessage().getReason());

        try {
            TestCase.assertTrue(router.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        return router.getMessage();
    }

    public static void broadcastGoodbyeSuccess(WampClient client,
            WampMockRouter router, MockWebSocket webSocket) {
        WampMessage msg = broadcastGoodbye(client, router, webSocket);
        TestCase.assertTrue(msg.isGoodbyeMessage());
        TestCase.assertEquals(WampError.GOODBYE_AND_OUT, msg.asGoodbyeMessage().getReason());
    }

    public static WampMessage broadcastEvent(WampClient client, WampMockRouter router,
            MockWebSocket webSocket) {

        router.setCountDownLatch(new CountDownLatch(1));
        webSocket.setCountDownLatch(new CountDownLatch(1));

        router.broadcast(WampMessageFactory.createEvent(SUBSCRIPTION_ID, PUBLICATION_ID,
                new JSONObject()));

        try {
            TestCase.assertTrue(webSocket.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        return webSocket.getMessage();
    }

    public static void broadcastEventSuccess(WampClient client,
            WampMockRouter router, MockWebSocket webSocket) {
        WampMessage msg = broadcastEvent(client, router, webSocket);
        TestCase.assertTrue(msg.isEventMessage());
        TestCase.assertEquals(PUBLICATION_ID, msg.asEventMessage().getPublicationId());
        TestCase.assertEquals(SUBSCRIPTION_ID, msg.asEventMessage().getSubscriptionId());
    }
}
