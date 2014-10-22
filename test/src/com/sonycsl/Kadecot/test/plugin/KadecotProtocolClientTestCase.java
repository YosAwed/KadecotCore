/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.test.plugin;

import com.sonycsl.Kadecot.plugin.DeviceData;
import com.sonycsl.Kadecot.plugin.KadecotProtocolClient;
import com.sonycsl.Kadecot.plugin.KadecotProtocolClient.DeviceRegistrationListener;
import com.sonycsl.Kadecot.plugin.KadecotProtocolClient.InitializeListener;
import com.sonycsl.Kadecot.plugin.KadecotProtocolSubscriber.EventListener;
import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.wamp.KadecotWampTopic;
import com.sonycsl.Kadecot.wamp.client.provider.WampProviderAccessHelper;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.WampPeer.Callback;
import com.sonycsl.wamp.message.WampEventMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.mock.MockWampPeer;
import com.sonycsl.wamp.role.WampCallee.WampInvocationReplyListener;
import com.sonycsl.wamp.util.TestableCallback;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotProtocolClientTestCase extends TestCase {

    private TestKadecotProtocolClient mProtocolClient;
    private MockWampPeer mPeer;
    private static final int DEVICE_ID = 1000;
    private static final String UUID = "id1";

    private static class TestKadecotProtocolClient extends KadecotProtocolClient {

        private CountDownLatch mSearchLatch;
        private CountDownLatch mInvocLatch;
        private String mUuid;

        public void setSearchLatch(CountDownLatch latch) {
            mSearchLatch = latch;
        }

        public void setInvocationLatch(CountDownLatch latch) {
            mInvocLatch = latch;
        }

        public String getLastInvocationUuid() {
            return mUuid;
        }

        @Override
        public void onSearchEvent(WampEventMessage msg) {
            if (mSearchLatch != null) {
                mSearchLatch.countDown();
            }
        }

        @Override
        protected void onInvocation(int requestId, String procedure, String uuid,
                JSONObject argumentsKw, WampInvocationReplyListener listener) {

            mUuid = uuid;

            if (mInvocLatch != null) {
                mInvocLatch.countDown();
            }

            try {
                listener.replyYield(WampMessageFactory.createYield(requestId, new JSONObject(),
                        new JSONArray(), new JSONObject().put("echo", "echo").put("uuid", "uuid"))
                        .asYieldMessage());
            } catch (JSONException e) {
                throw new IllegalStateException("Never happens");
            }
        }

        @Override
        public Map<String, String> getSubscribableTopics() {
            Map<String, String> topics = new HashMap<String, String>();
            topics.put("test_topic1", "test topic 1");
            return topics;
        }

        @Override
        public Map<String, String> getRegisterableProcedures() {
            Map<String, String> topics = new HashMap<String, String>();
            topics.put("test_proc1", "test proc 1");
            return topics;
        }

        @Override
        public Set<String> getTopicsToSubscribe() {
            Set<String> topics = new HashSet<String>();
            topics.add("topic1");
            return topics;
        }

    }

    private static class TestCallback implements Callback {

        private CountDownLatch mLatch;
        private List<WampMessage> mMessages;

        public TestCallback() {
            mMessages = new ArrayList<WampMessage>();
        }

        public void setCountDownLatch(CountDownLatch latch) {
            mLatch = latch;
        }

        public List<WampMessage> getAllMessages() {
            return mMessages;
        }

        public void clearMessages() {
            mMessages.clear();
        }

        @Override
        public void preTransmit(WampPeer transmitter, WampMessage msg) {
        }

        @Override
        public void preReceive(WampPeer receiver, WampMessage msg) {
        }

        @Override
        public void preConnect(WampPeer connecter, WampPeer connectee) {
        }

        @Override
        public void postTransmit(WampPeer transmitter, WampMessage msg) {
        }

        @Override
        public void postReceive(WampPeer receiver, WampMessage msg) {
            mMessages.add(msg);

            if (mLatch != null) {
                mLatch.countDown();
            }
        }

        @Override
        public void postConnect(WampPeer connecter, WampPeer connectee) {
        }

    }

    @Override
    protected void setUp() throws Exception {
        mProtocolClient = new TestKadecotProtocolClient();
        mPeer = new MockWampPeer();

        mProtocolClient.connect(mPeer);
    }

    public void testCtor() {
        assertNotNull(mProtocolClient);
    }

    public void testTxHello() {
        TestableCallback callback = new TestableCallback();
        callback.setTargetMessageType(WampMessageType.HELLO, new CountDownLatch(1));
        mPeer.setCallback(callback);
        mProtocolClient.transmit(WampMessageFactory.createHello("realm", new JSONObject()));

        try {
            assertTrue(callback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testRxWelcome() {
        mProtocolClient.transmit(WampMessageFactory.createHello("realm", new JSONObject()));

        TestableCallback callback = new TestableCallback();
        callback.setTargetMessageType(WampMessageType.WELCOME, new CountDownLatch(1));
        mProtocolClient.setCallback(callback);
        mPeer.transmit(WampMessageFactory.createWelcome(0, new JSONObject()));

        try {
            assertTrue(callback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testRxInitialized() {
        final CountDownLatch initLatch = new CountDownLatch(1);
        mProtocolClient.setInitializeListener(new InitializeListener() {

            @Override
            public void onInitialized() {
                initLatch.countDown();
            }

            @Override
            public void onError() {
            }
        });

        TestableCallback helloCallback = new TestableCallback();
        helloCallback.setTargetMessageType(WampMessageType.HELLO, new CountDownLatch(1));
        mPeer.setCallback(helloCallback);

        mProtocolClient.transmit(WampMessageFactory.createHello("realm", new JSONObject()));
        try {
            assertTrue(helloCallback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        mPeer.clearMessages();

        String[] systemTopics = {
                KadecotWampTopic.TOPIC_PRIVATE_SEARCH,
                WampProviderAccessHelper.Topic.START.getUri(),
                WampProviderAccessHelper.Topic.STOP.getUri()
        };
        final CountDownLatch initSequencelatch = new CountDownLatch(systemTopics.length
                + mProtocolClient.getRegisterableProcedures().keySet().size()
                + mProtocolClient.getTopicsToSubscribe().size());

        TestCallback callback = new TestCallback();
        callback.setCountDownLatch(initSequencelatch);
        mPeer.setCallback(callback);

        mPeer.transmit(WampMessageFactory.createWelcome(0, new JSONObject()));

        try {
            assertTrue(initSequencelatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        List<WampMessage> subOrRegMessage = mPeer.getAllMessages();
        for (WampMessage msg : subOrRegMessage) {
            if (msg.isRegisterMessage()) {
                mPeer.transmit(WampMessageFactory.createRegistered(msg.asRegisterMessage()
                        .getRequestId(), WampRequestIdGenerator.getId()));
                continue;
            }

            if (msg.isSubscribeMessage()) {
                mPeer.transmit(WampMessageFactory.createSubscribed(msg.asSubscribeMessage()
                        .getRequestId(), WampRequestIdGenerator.getId()));
                continue;
            }

            fail();
        }

        try {
            assertTrue(initLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        mProtocolClient.removeCallback(callback);
    }

    public void testRxOnEvent() {
        TestCallback callback = new TestCallback();
        mProtocolClient.setCallback(callback);
        testRxInitialized();
        final CountDownLatch latch = new CountDownLatch(1);
        mProtocolClient.setEventListener(new EventListener() {

            @Override
            public void onEvent(String topic, WampEventMessage msg) {
                assertEquals("topic1", topic);
                latch.countDown();
            }
        });

        int requestId = -1;
        for (WampMessage peerMsg : mPeer.getAllMessages()) {
            if (peerMsg.isSubscribeMessage()) {
                if ("topic1".equals(peerMsg.asSubscribeMessage().getTopic())) {
                    requestId = peerMsg.asSubscribeMessage().getRequestId();
                }
            }
        }
        assertFalse(requestId == -1);

        for (WampMessage msg : callback.getAllMessages()) {
            if (msg.isSubscribedMessage()) {
                if (requestId == msg.asSubscribedMessage().getRequestId()) {
                    mPeer.transmit(WampMessageFactory.createEvent(msg
                            .asSubscribedMessage().getSubscriptionId(), 0,
                            new JSONObject()));
                    break;
                }
            }
        }

        try {
            assertTrue(latch.await(3, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

    }

    public void testRxOnSearchEvent() {
        TestCallback callback = new TestCallback();
        mProtocolClient.setCallback(callback);
        testRxInitialized();
        final CountDownLatch latch = new CountDownLatch(1);
        mProtocolClient.setSearchLatch(latch);

        int requestId = -1;
        for (WampMessage peerMsg : mPeer.getAllMessages()) {
            if (peerMsg.isSubscribeMessage()) {
                if (KadecotWampTopic.TOPIC_PRIVATE_SEARCH.equals(peerMsg.asSubscribeMessage()
                        .getTopic())) {
                    requestId = peerMsg.asSubscribeMessage().getRequestId();
                }
            }
        }
        assertFalse(requestId == -1);

        for (WampMessage msg : callback.getAllMessages()) {
            if (msg.isSubscribedMessage()) {
                if (requestId == msg.asSubscribedMessage().getRequestId()) {
                    mPeer.transmit(WampMessageFactory.createEvent(msg
                            .asSubscribedMessage().getSubscriptionId(), 0,
                            new JSONObject()));
                    break;
                }
            }
        }

        try {
            assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testRxRegisteredDevice() {
        testRxInitialized();
        final CountDownLatch latch = new CountDownLatch(1);
        mProtocolClient.setDeviceRegistrationListener(new DeviceRegistrationListener() {

            @Override
            public void onRegistered(long deviceId, String uuid) {
                latch.countDown();
            }
        });

        final String protocol = "protocol";
        final String deviceType = "deviceType";
        final String description = "description";
        final boolean status = true;
        final String ipAddress = "127.0.0.1";

        mPeer.clearMessages();

        TestableCallback putDeviceCallback = new TestableCallback();
        putDeviceCallback.setTargetMessageType(WampMessageType.CALL, new CountDownLatch(1));
        mPeer.setCallback(putDeviceCallback);

        mProtocolClient.registerDevice(new DeviceData.Builder(protocol, UUID, deviceType,
                description, status, ipAddress).build());

        try {
            assertTrue(putDeviceCallback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e1) {
            fail();
        }

        for (WampMessage msg : mPeer.getAllMessages()) {
            if (msg.isCallMessage()) {
                try {
                    mPeer.transmit(WampMessageFactory.createResult(msg.asCallMessage()
                            .getRequestId(), new JSONObject(), new JSONArray(), new JSONObject()
                            .put("uuid", UUID).put("deviceId", DEVICE_ID)));
                    break;
                } catch (JSONException e) {
                    fail();
                }
            }
            fail();
        }

        try {
            assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testIsRegistered() {
        assertFalse(mProtocolClient.isRegistered(UUID));
        testRxRegisteredDevice();
        assertTrue(mProtocolClient.isRegistered(UUID));
    }

    public void testSendPublish() {
        String testTopic = "test.send.publish.topic";

        testRxRegisteredDevice();
        assertTrue(mProtocolClient.isRegistered(UUID));

        mPeer.clearMessages();
        TestableCallback callback = new TestableCallback();
        mPeer.setCallback(callback);
        callback.setTargetMessageType(WampMessageType.PUBLISH, new CountDownLatch(1));

        mProtocolClient.sendPublish(UUID, testTopic, new JSONArray(), new JSONObject());

        try {
            assertTrue(callback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        WampMessage msg = callback.getTargetMessage();
        try {
            assertEquals(msg.asPublishMessage().getOptions()
                    .getLong(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID), DEVICE_ID);
        } catch (JSONException e) {
            fail();
        }
    }

    public void testRxInvocation() {
        TestCallback callback = new TestCallback();
        mProtocolClient.setCallback(callback);
        testRxRegisteredDevice();

        CountDownLatch latch = new CountDownLatch(1);
        mProtocolClient.setInvocationLatch(latch);

        int registrationId = -1;
        for (WampMessage msg : callback.getAllMessages()) {
            if (msg.isRegisteredMessage()) {
                registrationId = msg.asRegisteredMessage().getRegistrationId();
            }
        }
        assertFalse(registrationId == -1);

        try {
            mPeer.transmit(WampMessageFactory.createInvocation(WampRequestIdGenerator.getId(),
                    registrationId, new JSONObject().put("deviceId", DEVICE_ID), new JSONArray(),
                    new JSONObject()));
        } catch (JSONException e) {
            fail();
        }

        try {
            assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(UUID, mProtocolClient.getLastInvocationUuid());
    }
}
