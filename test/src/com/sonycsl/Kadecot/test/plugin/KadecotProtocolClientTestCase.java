/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.test.plugin;

import com.sonycsl.Kadecot.plugin.KadecotProtocolClient;
import com.sonycsl.Kadecot.plugin.KadecotProtocolSubscriber.OnTopicListener;
import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.wamp.client.provider.WampProviderAccessHelper;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.mock.MockWampPeer;
import com.sonycsl.wamp.util.TestableCallback;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotProtocolClientTestCase extends TestCase {

    private TestKadecotProtocolClient mProtocolClient;
    private MockWampPeer mPeer;

    private static class TestKadecotProtocolClient extends KadecotProtocolClient {

        @Override
        public Map<String, String> getSubscribableTopics() {
            return new HashMap<String, String>();
        }

        @Override
        public Map<String, String> getRegisterableProcedures() {
            return new HashMap<String, String>();
        }

        @Override
        protected OnTopicListener getTopicListenter() {
            return null;
        }

        @Override
        protected void deviceSearch() {
        }

        @Override
        protected void onConnected(WampPeer peer) {
        }

        @Override
        protected void onTransmitted(WampPeer peer, WampMessage msg) {
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

    public void testRxResult() {
        final String protocol = "protocol";
        final String uuid = "uuid";
        final String deviceType = "deviceType";
        final String description = "description";
        final boolean status = true;
        final String ipAddress = "127.0.0.1";

        TestableCallback callback = new TestableCallback();
        callback.setTargetMessageType(WampMessageType.WELCOME, new CountDownLatch(1));
        mProtocolClient.setCallback(callback);

        mProtocolClient.transmit(WampMessageFactory.createHello("realm", new JSONObject()));
        mPeer.transmit(WampMessageFactory.createWelcome(1, new JSONObject()));

        try {
            assertTrue(callback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e1) {
            fail();
        }
        JSONObject device = null;
        int deviceId = WampRequestIdGenerator.getId();

        try {
            device = WampProviderAccessHelper.createPutDeviceArgsKw(protocol, uuid,
                    deviceType, description, status, ipAddress).put(
                    KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID, deviceId);
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        }

        callback.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));
        mProtocolClient.callPutDevice(protocol, uuid, deviceType, description, status, ipAddress);

        mPeer.transmit(WampMessageFactory.createResult(WampRequestIdGenerator.getId() - 1,
                new JSONObject(), new JSONArray(), device));

        try {
            assertTrue(callback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        assertTrue(callback.getTargetMessage().isResultMessage());
        JSONObject argsKw = callback.getTargetMessage().asResultMessage().getArgumentsKw();
        try {
            assertEquals(deviceId, argsKw.getInt(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID));
        } catch (JSONException e) {
            fail();
        }
    }
}
