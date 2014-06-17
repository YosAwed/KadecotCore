/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.Kadecot.wamp;

import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.sonycsl.Kadecot.core.provider.KadecotCoreProvider;
import com.sonycsl.Kadecot.core.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.wamp.KadecotWampClientSetupCallback;
import com.sonycsl.Kadecot.wamp.KadecotWampClientSetupCallback.OnCompletionListener;
import com.sonycsl.Kadecot.wamp.KadecotWampRouter;
import com.sonycsl.Kadecot.wamp.provider.KadecotProviderClient;
import com.sonycsl.test.mock.MockWampClient;
import com.sonycsl.test.util.TestableCallback;
import com.sonycsl.test.util.WampTestUtil;
import com.sonycsl.test.wamp.WampTestParam;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotProviderClientTestCase extends ProviderTestCase2<KadecotCoreProvider> {

    public KadecotProviderClientTestCase() {
        super(KadecotCoreProvider.class, KadecotCoreStore.AUTHORITY);
    }

    private static JSONObject createTestDevice() {
        try {
            return new JSONObject()
                    .put(KadecotCoreStore.Devices.DeviceColumns.PROTOCOL, "testprotocol")
                    .put(KadecotCoreStore.Devices.DeviceColumns.UUID, "testuuid")
                    .put(KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE, "testdeviceType")
                    .put(KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION, "testdescription")
                    .put(KadecotCoreStore.Devices.DeviceColumns.STATUS, true);
        } catch (JSONException e) {
            return null;
        }
    }

    private KadecotProviderClient mProvider;
    private MockWampClient mDevice;
    private MockWampClient mApp;
    private KadecotWampRouter mRouter;
    private WampPeer[] mClients;
    private CountDownLatch mLatch;

    private static class SetupCallback extends TestableCallback {

        private KadecotWampClientSetupCallback mCallback;

        public SetupCallback(Set<String> topics, Set<String> procedures,
                OnCompletionListener listener) {
            mCallback = new KadecotWampClientSetupCallback(topics, procedures, listener);
        }

        @Override
        public void preConnect(WampPeer connecter, WampPeer connectee) {
            mCallback.preConnect(connecter, connectee);
            super.preConnect(connecter, connectee);
        }

        @Override
        public void postConnect(WampPeer connecter, WampPeer connectee) {
            mCallback.postConnect(connecter, connectee);
            super.postConnect(connecter, connectee);
        }

        @Override
        public void preTransmit(WampPeer transmitter, WampMessage msg) {
            mCallback.preTransmit(transmitter, msg);
            super.preTransmit(transmitter, msg);
        }

        @Override
        public void postTransmit(WampPeer transmitter, WampMessage msg) {
            mCallback.postTransmit(transmitter, msg);
            super.postTransmit(transmitter, msg);
        }

        @Override
        public void preReceive(WampPeer receiver, WampMessage msg) {
            mCallback.preReceive(receiver, msg);
            super.preReceive(receiver, msg);
        }

        @Override
        public void postReceive(WampPeer receiver, WampMessage msg) {
            mCallback.postReceive(receiver, msg);
            super.postReceive(receiver, msg);
        }

    }

    @Override
    public MockContentResolver getMockContentResolver() {
        MockContentResolver mock = new MockContentResolver();
        mock.addProvider(KadecotCoreStore.AUTHORITY, getProvider());
        return mock;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mProvider = new KadecotProviderClient(getContext());

        mLatch = new CountDownLatch(1);
        mProvider.setCallback(new SetupCallback(
                mProvider.getSubscribableTopics(),
                mProvider.getRegisterableProcedures(),
                new OnCompletionListener() {
                    @Override
                    public void onCompletion() {
                        mLatch.countDown();
                    }
                }));

        mDevice = new MockWampClient();
        mDevice.setCallback(new TestableCallback());

        mApp = new MockWampClient();
        mApp.setCallback(new TestableCallback());

        mRouter = new KadecotWampRouter();
        mRouter.setCallback(new TestableCallback());

        mClients = new WampPeer[] {
                mDevice, mApp, mProvider
        };

        for (WampPeer client : mClients) {
            mRouter.connect(client);
        }

        for (WampPeer client : mClients) {
            WampTestUtil.transmitHelloSuccess(client, WampTestParam.REALM, mRouter);
        }

        try {
            mLatch.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e1) {
            fail();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        getContext().getContentResolver().delete(KadecotCoreStore.Devices.CONTENT_URI, null, null);
        getContext().getContentResolver().delete(KadecotCoreStore.Topics.CONTENT_URI, null, null);
        getContext().getContentResolver().delete(KadecotCoreStore.Procedures.CONTENT_URI, null,
                null);
        for (WampPeer client : mClients) {
            WampTestUtil.transmitGoodbyeSuccess(client, WampError.CLOSE_REALM, mRouter);
        }
    }

    public void testPutDevice() {
        JSONObject testDevice = createTestDevice();
        TestableCallback plistener = (TestableCallback) mProvider.getCallback();
        TestableCallback dlistener = (TestableCallback) mDevice.getCallback();
        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        dlistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));

        mDevice.transmit(WampMessageFactory.createCall(WampRequestIdGenerator.getId(),
                new JSONObject(), KadecotProviderClient.Procedure.PUT_DEVICE.getUri(),
                new JSONArray(),
                testDevice));

        try {
            assertTrue(plistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        try {
            assertTrue(dlistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        WampMessage reply = dlistener.getTargetMessage();
        assertTrue(reply.toString(), reply.isResultMessage());
        assertTrue(reply.asResultMessage().hasArgumentsKw());
    }

    public void testRemoveDevice() {
        JSONObject testDevice = createTestDevice();
        TestableCallback plistener = (TestableCallback) mProvider.getCallback();
        TestableCallback dlistener = (TestableCallback) mDevice.getCallback();
        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        dlistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));

        mDevice.transmit(WampMessageFactory.createCall(WampRequestIdGenerator.getId(),
                new JSONObject(), KadecotProviderClient.Procedure.PUT_DEVICE.getUri(),
                new JSONArray(),
                testDevice));

        try {
            assertTrue(plistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        try {
            assertTrue(dlistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        WampMessage reply = dlistener.getTargetMessage();
        assertTrue(reply.toString(), reply.isResultMessage());
        assertTrue(reply.asResultMessage().hasArgumentsKw());
        JSONObject deviceId = reply.asResultMessage().getArgumentKw();

        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        dlistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));
        try {
            mDevice.transmit(WampMessageFactory.createCall(
                    WampRequestIdGenerator.getId(),
                    new JSONObject(),
                    KadecotProviderClient.Procedure.REMOVE_DEVICE.getUri(),
                    new JSONArray(),
                    new JSONObject().put(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID,
                            deviceId.getLong(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID))));
        } catch (JSONException e) {
            fail();
        }

        try {
            assertTrue(plistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        try {
            assertTrue(dlistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        reply = dlistener.getTargetMessage();
        assertTrue(reply.toString(), reply.isResultMessage());
        assertFalse(reply.asResultMessage().hasArguments());
        assertFalse(reply.asResultMessage().hasArgumentsKw());
    }

    public void testGetDeviceList() {
        JSONObject testDevice = createTestDevice();
        TestableCallback plistener = (TestableCallback) mProvider.getCallback();
        TestableCallback dlistener = (TestableCallback) mDevice.getCallback();
        TestableCallback alistener = (TestableCallback) mApp.getCallback();

        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        dlistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));

        mDevice.transmit(WampMessageFactory.createCall(WampRequestIdGenerator.getId(),
                new JSONObject(), KadecotProviderClient.Procedure.PUT_DEVICE.getUri(),
                new JSONArray(),
                testDevice));

        try {
            assertTrue(plistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        try {
            assertTrue(dlistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        WampMessage reply = dlistener.getTargetMessage();
        assertTrue(reply.toString(), reply.isResultMessage());
        assertTrue(reply.asResultMessage().hasArgumentsKw());
        JSONObject deviceId = reply.asResultMessage().getArgumentKw();

        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        alistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));
        mApp.transmit(WampMessageFactory.createCall(WampRequestIdGenerator.getId(),
                new JSONObject(), KadecotProviderClient.Procedure.GET_DEVICE_LIST.getUri()));

        try {
            assertTrue(plistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        try {
            assertTrue(alistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        reply = alistener.getTargetMessage();
        assertTrue(reply.toString(), reply.isResultMessage());
        assertTrue(reply.asResultMessage().hasArguments());
        assertTrue(reply.asResultMessage().hasArgumentsKw());

        JSONObject device = null;
        try {
            device = reply.asResultMessage().getArgumentKw().getJSONArray("deviceList")
                    .getJSONObject(0);
        } catch (JSONException e) {
            fail();
        }

        try {
            assertEquals(deviceId.getLong(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID),
                    device.getLong(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID));
            assertEquals(testDevice.getString(KadecotCoreStore.Devices.DeviceColumns.PROTOCOL),
                    device.getString(KadecotCoreStore.Devices.DeviceColumns.PROTOCOL));
            assertEquals(testDevice.getString(KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE),
                    device.getString(KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE));
            assertEquals(testDevice.getString(KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION),
                    device.getString(KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION));
            assertEquals(testDevice.getBoolean(KadecotCoreStore.Devices.DeviceColumns.STATUS),
                    device.getBoolean(KadecotCoreStore.Devices.DeviceColumns.STATUS));
        } catch (JSONException e) {
            fail();
        }

    }

    public void testChangeNickname() {
        JSONObject testDevice = createTestDevice();
        TestableCallback plistener = (TestableCallback) mProvider.getCallback();
        TestableCallback dlistener = (TestableCallback) mDevice.getCallback();
        TestableCallback alistener = (TestableCallback) mApp.getCallback();

        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        dlistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));

        mDevice.transmit(WampMessageFactory.createCall(WampRequestIdGenerator.getId(),
                new JSONObject(), KadecotProviderClient.Procedure.PUT_DEVICE.getUri(),
                new JSONArray(), testDevice));

        try {
            assertTrue(plistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        try {
            assertTrue(dlistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        WampMessage reply = dlistener.getTargetMessage();
        assertTrue(reply.toString(), reply.isResultMessage());
        assertTrue(reply.asResultMessage().hasArgumentsKw());
        JSONObject deviceId = reply.asResultMessage().getArgumentKw();

        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        alistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));
        try {
            mApp.transmit(WampMessageFactory.createCall(
                    WampRequestIdGenerator.getId(),
                    new JSONObject(),
                    KadecotProviderClient.Procedure.CHANGE_NICKNAME.getUri(),
                    new JSONArray(),
                    new JSONObject()
                            .put(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID,
                                    deviceId.getLong(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID))
                            .put(KadecotCoreStore.Devices.DeviceColumns.NICKNAME, "testnickname")));
        } catch (JSONException e) {
            fail();
        }

        try {
            assertTrue(plistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        try {
            assertTrue(alistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        reply = alistener.getTargetMessage();
        assertTrue(reply.toString(), reply.isResultMessage());
        assertFalse(reply.asResultMessage().hasArguments());
        assertFalse(reply.asResultMessage().hasArgumentsKw());

    }

    public void testPutTopic() {
        TestableCallback plistener = (TestableCallback) mProvider.getCallback();
        TestableCallback dlistener = (TestableCallback) mDevice.getCallback();
        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        dlistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));

        try {
            mDevice.transmit(WampMessageFactory.createCall(
                    WampRequestIdGenerator.getId(),
                    new JSONObject(),
                    KadecotProviderClient.Procedure.PUT_TOPIC.getUri(),
                    new JSONArray(),
                    new JSONObject().put(KadecotCoreStore.Topics.TopicColumns.NAME,
                            "com.sonycsl.kadecot.testprotocol.topic.test")
                            .put(KadecotCoreStore.Topics.TopicColumns.DESCRIPTION,
                                    "testdescription")));
        } catch (JSONException e) {
            fail();
        }

        try {
            assertTrue(plistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        try {
            assertTrue(dlistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        WampMessage reply = dlistener.getTargetMessage();
        assertTrue(reply.toString(), reply.isResultMessage());
        assertFalse(reply.asResultMessage().hasArguments());
        assertFalse(reply.asResultMessage().hasArgumentsKw());
    }

    public void testRemoveTopic() {
        TestableCallback plistener = (TestableCallback) mProvider.getCallback();
        TestableCallback dlistener = (TestableCallback) mDevice.getCallback();
        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        dlistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));

        try {
            mDevice.transmit(WampMessageFactory.createCall(
                    WampRequestIdGenerator.getId(),
                    new JSONObject(),
                    KadecotProviderClient.Procedure.PUT_TOPIC.getUri(),
                    new JSONArray(),
                    new JSONObject().put(KadecotCoreStore.Topics.TopicColumns.NAME,
                            "com.sonycsl.kadecot.testprotocol.topic.test")
                            .put(KadecotCoreStore.Topics.TopicColumns.DESCRIPTION,
                                    "testdescription")));
        } catch (JSONException e) {
            fail();
        }

        try {
            assertTrue(plistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        try {
            assertTrue(dlistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        WampMessage reply = dlistener.getTargetMessage();
        assertTrue(reply.toString(), reply.isResultMessage());
        assertFalse(reply.asResultMessage().hasArguments());
        assertFalse(reply.asResultMessage().hasArgumentsKw());

        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        dlistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));

        try {
            mDevice.transmit(WampMessageFactory.createCall(
                    WampRequestIdGenerator.getId(),
                    new JSONObject(),
                    KadecotProviderClient.Procedure.REMOVE_TOPIC.getUri(),
                    new JSONArray(),
                    new JSONObject().put(KadecotCoreStore.Topics.TopicColumns.NAME,
                            "com.sonycsl.kadecot.testprotocol.topic.test")));
        } catch (JSONException e) {
            fail();
        }

        try {
            assertTrue(plistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        try {
            assertTrue(dlistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        reply = dlistener.getTargetMessage();
        assertTrue(reply.toString(), reply.isResultMessage());
        assertFalse(reply.asResultMessage().hasArguments());
        assertFalse(reply.asResultMessage().hasArgumentsKw());
    }

    public void testGetTopicList() {
        TestableCallback plistener = (TestableCallback) mProvider.getCallback();
        TestableCallback dlistener = (TestableCallback) mDevice.getCallback();
        TestableCallback alistener = (TestableCallback) mApp.getCallback();

        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        dlistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));

        try {
            mDevice.transmit(WampMessageFactory.createCall(
                    WampRequestIdGenerator.getId(),
                    new JSONObject(),
                    KadecotProviderClient.Procedure.PUT_TOPIC.getUri(),
                    new JSONArray(),
                    new JSONObject().put(KadecotCoreStore.Topics.TopicColumns.NAME,
                            "com.sonycsl.kadecot.testprotocol.topic.test")
                            .put(KadecotCoreStore.Topics.TopicColumns.DESCRIPTION,
                                    "testdescription")));
        } catch (JSONException e) {
            fail();
        }

        try {
            assertTrue(plistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        try {
            assertTrue(dlistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        WampMessage reply = dlistener.getTargetMessage();
        assertTrue(reply.toString(), reply.isResultMessage());
        assertFalse(reply.asResultMessage().hasArguments());
        assertFalse(reply.asResultMessage().hasArgumentsKw());

        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        alistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));

        try {
            mApp.transmit(WampMessageFactory.createCall(
                    WampRequestIdGenerator.getId(),
                    new JSONObject(),
                    KadecotProviderClient.Procedure.GET_TOPIC_LIST.getUri(),
                    new JSONArray(),
                    new JSONObject().put(KadecotCoreStore.Topics.TopicColumns.PROTOCOL,
                            "testprotocol")));
        } catch (JSONException e) {
            fail();
        }

        try {
            assertTrue(plistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        try {
            assertTrue(alistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        reply = alistener.getTargetMessage();
        assertTrue(reply.toString(), reply.isResultMessage());
        assertTrue(reply.asResultMessage().hasArguments());
        assertTrue(reply.asResultMessage().hasArgumentsKw());
        String topic;

        try {
            topic = reply.asResultMessage().getArgumentKw().getJSONArray("topicList")
                    .getString(0);
        } catch (JSONException e) {
            fail();
            return;
        }
        assertEquals("com.sonycsl.kadecot.testprotocol.topic.test", topic);
    }

    public void testPutProcedure() {
        TestableCallback plistener = (TestableCallback) mProvider.getCallback();
        TestableCallback dlistener = (TestableCallback) mDevice.getCallback();
        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        dlistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));

        try {
            mDevice.transmit(WampMessageFactory.createCall(
                    WampRequestIdGenerator.getId(),
                    new JSONObject(),
                    KadecotProviderClient.Procedure.PUT_PROCEDURE.getUri(),
                    new JSONArray(),
                    new JSONObject().put(KadecotCoreStore.Procedures.ProcedureColumns.NAME,
                            "com.sonycsl.kadecot.testprotocol.procedure.test")
                            .put(KadecotCoreStore.Procedures.ProcedureColumns.DESCRIPTION,
                                    "testdescription")));
        } catch (JSONException e) {
            fail();
        }

        try {
            assertTrue(plistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        try {
            assertTrue(dlistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        WampMessage reply = dlistener.getTargetMessage();
        assertTrue(reply.toString(), reply.isResultMessage());
        assertFalse(reply.asResultMessage().hasArguments());
        assertFalse(reply.asResultMessage().hasArgumentsKw());
    }

    public void testRemoveProcedure() {
        TestableCallback plistener = (TestableCallback) mProvider.getCallback();
        TestableCallback dlistener = (TestableCallback) mDevice.getCallback();
        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        dlistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));

        try {
            mDevice.transmit(WampMessageFactory.createCall(
                    WampRequestIdGenerator.getId(),
                    new JSONObject(),
                    KadecotProviderClient.Procedure.PUT_PROCEDURE.getUri(),
                    new JSONArray(),
                    new JSONObject().put(KadecotCoreStore.Procedures.ProcedureColumns.NAME,
                            "com.sonycsl.kadecot.testprotocol.procedure.test")
                            .put(KadecotCoreStore.Procedures.ProcedureColumns.DESCRIPTION,
                                    "testdescription")));
        } catch (JSONException e) {
            fail();
        }

        try {
            assertTrue(plistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        try {
            assertTrue(dlistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        WampMessage reply = dlistener.getTargetMessage();
        assertTrue(reply.toString(), reply.isResultMessage());
        assertFalse(reply.asResultMessage().hasArguments());
        assertFalse(reply.asResultMessage().hasArgumentsKw());

        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        dlistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));

        try {
            mDevice.transmit(WampMessageFactory.createCall(
                    WampRequestIdGenerator.getId(),
                    new JSONObject(),
                    KadecotProviderClient.Procedure.REMOVE_PROCEDURE.getUri(),
                    new JSONArray(),
                    new JSONObject().put(KadecotCoreStore.Procedures.ProcedureColumns.NAME,
                            "com.sonycsl.kadecot.testprotocol.procedure.test")));
        } catch (JSONException e) {
            fail();
        }

        try {
            assertTrue(plistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        try {
            assertTrue(dlistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        reply = dlistener.getTargetMessage();
        assertTrue(reply.toString(), reply.isResultMessage());
        assertFalse(reply.asResultMessage().hasArguments());
        assertFalse(reply.asResultMessage().hasArgumentsKw());
    }

    public void testGetProcedureList() {
        TestableCallback plistener = (TestableCallback) mProvider.getCallback();
        TestableCallback dlistener = (TestableCallback) mDevice.getCallback();
        TestableCallback alistener = (TestableCallback) mApp.getCallback();

        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        dlistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));

        try {
            mDevice.transmit(WampMessageFactory.createCall(
                    WampRequestIdGenerator.getId(),
                    new JSONObject(),
                    KadecotProviderClient.Procedure.PUT_PROCEDURE.getUri(),
                    new JSONArray(),
                    new JSONObject().put(KadecotCoreStore.Procedures.ProcedureColumns.NAME,
                            "com.sonycsl.kadecot.testprotocol.procedure.test")
                            .put(KadecotCoreStore.Procedures.ProcedureColumns.DESCRIPTION,
                                    "testdescription")));
        } catch (JSONException e) {
            fail();
        }

        try {
            assertTrue(plistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        try {
            assertTrue(dlistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        WampMessage reply = dlistener.getTargetMessage();
        assertTrue(reply.toString(), reply.isResultMessage());
        assertFalse(reply.asResultMessage().hasArguments());
        assertFalse(reply.asResultMessage().hasArgumentsKw());

        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        alistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));

        try {
            mApp.transmit(WampMessageFactory.createCall(
                    WampRequestIdGenerator.getId(),
                    new JSONObject(),
                    KadecotProviderClient.Procedure.GET_PROCEDURE_LIST.getUri(),
                    new JSONArray(),
                    new JSONObject().put(KadecotCoreStore.Procedures.ProcedureColumns.PROTOCOL,
                            "testprotocol")));
        } catch (JSONException e) {
            fail();
        }

        try {
            assertTrue(plistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        try {
            assertTrue(alistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        reply = alistener.getTargetMessage();
        assertTrue(reply.toString(), reply.isResultMessage());
        assertTrue(reply.asResultMessage().hasArguments());
        assertTrue(reply.asResultMessage().hasArgumentsKw());
        String procedure;

        try {
            procedure = reply.asResultMessage().getArgumentKw().getJSONArray("procedureList")
                    .getString(0);
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
            return;
        }
        assertEquals("com.sonycsl.kadecot.testprotocol.procedure.test", procedure);
    }
}
