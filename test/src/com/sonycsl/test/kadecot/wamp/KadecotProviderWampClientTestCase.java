/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.kadecot.wamp;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.sonycsl.kadecot.database.KadecotDAO;
import com.sonycsl.kadecot.wamp.KadecotProviderWampClient;
import com.sonycsl.kadecot.wamp.KadecotWampClientSetupCallback;
import com.sonycsl.kadecot.wamp.KadecotWampClientSetupCallback.OnCompletionListener;
import com.sonycsl.kadecot.wamp.KadecotWampRouter;
import com.sonycsl.test.wamp.TestableCallback;
import com.sonycsl.test.wamp.WampTestParam;
import com.sonycsl.test.wamp.WampTestUtil;
import com.sonycsl.test.wamp.mock.MockWampClient;
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

public class KadecotProviderWampClientTestCase extends AndroidTestCase {

    private static JSONObject createTestDevice() {
        try {
            return new JSONObject()
                    .put(KadecotDAO.DEVICE_PROTOCOL, "testprotocol")
                    .put(KadecotDAO.DEVICE_UUID, "testuuid")
                    .put(KadecotDAO.DEVICE_TYPE, "testdeviceType")
                    .put(KadecotDAO.DEVICE_DESCRIPTION, "testdescription")
                    .put(KadecotDAO.DEVICE_STATUS, true);
        } catch (JSONException e) {
            return null;
        }
    }

    private KadecotProviderWampClient mProvider;
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
    protected void setUp() {
        mProvider = new KadecotProviderWampClient(new RenamingDelegatingContext(getContext(),
                "test_"));
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
    protected void tearDown() {
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
                new JSONObject(), KadecotProviderWampClient.Procedures.PUT_DEVICE, new JSONArray(),
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
        try {
            assertEquals(1, deviceId.getLong("deviceId"));
        } catch (JSONException e) {
            fail();
        }
    }

    public void testRemoveDevice() {
        JSONObject testDevice = createTestDevice();
        TestableCallback plistener = (TestableCallback) mProvider.getCallback();
        TestableCallback dlistener = (TestableCallback) mDevice.getCallback();
        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        dlistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));

        mDevice.transmit(WampMessageFactory.createCall(WampRequestIdGenerator.getId(),
                new JSONObject(), KadecotProviderWampClient.Procedures.PUT_DEVICE, new JSONArray(),
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
        try {
            assertEquals(1, deviceId.getLong("deviceId"));
        } catch (JSONException e) {
            fail();
        }

        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        dlistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));
        try {
            mDevice.transmit(WampMessageFactory.createCall(WampRequestIdGenerator.getId(),
                    new JSONObject(), KadecotProviderWampClient.Procedures.REMOVE_DEVICE,
                    new JSONArray(), new JSONObject().put("deviceId", deviceId.getLong("deviceId"))));
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
                new JSONObject(), KadecotProviderWampClient.Procedures.PUT_DEVICE, new JSONArray(),
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
        try {
            assertEquals(1, deviceId.getLong("deviceId"));
        } catch (JSONException e) {
            fail();
        }

        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        alistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));
        mApp.transmit(WampMessageFactory.createCall(WampRequestIdGenerator.getId(),
                new JSONObject(), KadecotProviderWampClient.Procedures.GET_DEVICE_LIST));

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
            assertEquals(deviceId.getLong("deviceId"),
                    device.getLong(KadecotDAO.DEVICE_ID));
            assertEquals(testDevice.getString(KadecotDAO.DEVICE_PROTOCOL),
                    device.getString(KadecotDAO.DEVICE_PROTOCOL));
            assertEquals(testDevice.getString(KadecotDAO.DEVICE_TYPE),
                    device.getString(KadecotDAO.DEVICE_TYPE));
            assertEquals(testDevice.getString(KadecotDAO.DEVICE_DESCRIPTION),
                    device.getString(KadecotDAO.DEVICE_DESCRIPTION));
            assertEquals(testDevice.getBoolean(KadecotDAO.DEVICE_STATUS),
                    device.getBoolean(KadecotDAO.DEVICE_STATUS));
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
                new JSONObject(), KadecotProviderWampClient.Procedures.PUT_DEVICE, new JSONArray(),
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
        try {
            assertEquals(1, deviceId.getLong("deviceId"));
        } catch (JSONException e) {
            fail();
        }

        plistener.setTargetMessageType(WampMessageType.INVOCATION, new CountDownLatch(1));
        alistener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));
        try {
            mApp.transmit(WampMessageFactory.createCall(
                    WampRequestIdGenerator.getId(),
                    new JSONObject(),
                    KadecotProviderWampClient.Procedures.CHANGE_NICKNAME,
                    new JSONArray(),
                    new JSONObject().put(KadecotDAO.DEVICE_ID, deviceId.getLong("deviceId")).put(
                            KadecotDAO.DEVICE_NICKNAME, "testnickname")));
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
                    KadecotProviderWampClient.Procedures.PUT_TOPIC,
                    new JSONArray(),
                    new JSONObject().put(KadecotDAO.TOPIC_NAME,
                            "com.sonycsl.kadecot.testprotocol.topic.test")
                            .put(KadecotDAO.TOPIC_DESCRIPTION, "testdescription")));
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
                    KadecotProviderWampClient.Procedures.PUT_TOPIC,
                    new JSONArray(),
                    new JSONObject().put(KadecotDAO.TOPIC_NAME,
                            "com.sonycsl.kadecot.testprotocol.topic.test")
                            .put(KadecotDAO.TOPIC_DESCRIPTION, "testdescription")));
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
                    KadecotProviderWampClient.Procedures.REMOVE_TOPIC,
                    new JSONArray(),
                    new JSONObject().put(KadecotDAO.TOPIC_NAME,
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
                    KadecotProviderWampClient.Procedures.PUT_TOPIC,
                    new JSONArray(),
                    new JSONObject().put(KadecotDAO.TOPIC_NAME,
                            "com.sonycsl.kadecot.testprotocol.topic.test")
                            .put(KadecotDAO.TOPIC_DESCRIPTION, "testdescription")));
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
                    KadecotProviderWampClient.Procedures.GET_TOPIC_LIST,
                    new JSONArray(),
                    new JSONObject().put(KadecotDAO.TOPIC_PROTOCOL, "testprotocol")));
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
                    KadecotProviderWampClient.Procedures.PUT_PROCEDURE,
                    new JSONArray(),
                    new JSONObject().put(KadecotDAO.PROCEDURE_NAME,
                            "com.sonycsl.kadecot.testprotocol.procedure.test")
                            .put(KadecotDAO.PROCEDURE_DESCRIPTION, "testdescription")));
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
                    KadecotProviderWampClient.Procedures.PUT_PROCEDURE,
                    new JSONArray(),
                    new JSONObject().put(KadecotDAO.PROCEDURE_NAME,
                            "com.sonycsl.kadecot.testprotocol.procedure.test")
                            .put(KadecotDAO.PROCEDURE_DESCRIPTION, "testdescription")));
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
                    KadecotProviderWampClient.Procedures.REMOVE_PROCEDURE,
                    new JSONArray(),
                    new JSONObject().put(KadecotDAO.PROCEDURE_NAME,
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
                    KadecotProviderWampClient.Procedures.PUT_PROCEDURE,
                    new JSONArray(),
                    new JSONObject().put(KadecotDAO.PROCEDURE_NAME,
                            "com.sonycsl.kadecot.testprotocol.procedure.test")
                            .put(KadecotDAO.PROCEDURE_DESCRIPTION, "testdescription")));
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
                    KadecotProviderWampClient.Procedures.GET_PROCEDURE_LIST,
                    new JSONArray(),
                    new JSONObject().put(KadecotDAO.PROCEDURE_PROTOCOL, "testprotocol")));
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
