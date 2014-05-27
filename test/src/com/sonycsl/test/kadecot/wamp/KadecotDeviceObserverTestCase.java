/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.kadecot.wamp;

import com.sonycsl.kadecot.wamp.KadecotDeviceInfo;
import com.sonycsl.kadecot.wamp.KadecotDeviceObserver;
import com.sonycsl.kadecot.wamp.KadecotWampClientSetupCallback;
import com.sonycsl.kadecot.wamp.KadecotWampClientSetupCallback.OnCompletionListener;
import com.sonycsl.kadecot.wamp.KadecotWampRouter;
import com.sonycsl.kadecot.wamp.KadecotWampTopic;
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

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotDeviceObserverTestCase extends TestCase {

    private static JSONObject createTestDevice() {
        return createDevice("testNickname");
    }

    private static JSONObject createDevice(String nickName) {
        try {
            return new JSONObject().put(KadecotDeviceInfo.DEVICE_STATUS_KEY, 1)
                    .put(KadecotDeviceInfo.DEVICE_PROTOCOL_KEY, "")
                    .put(KadecotDeviceInfo.DEVICE_DEVICENAME_KEY, "")
                    .put(KadecotDeviceInfo.DEVICE_NICKNAME_KEY, nickName)
                    .put(KadecotDeviceInfo.DEVICE_PARENT_KEY, "")
                    .put(KadecotDeviceInfo.DEVICE_DEVICETYPE_KEY, "");
        } catch (JSONException e) {
            return null;
        }
    }

    private KadecotDeviceObserver mDeviceObserver;
    private MockWampClient mDevicePublisher;
    private MockWampClient mCaller;
    private MockWampClient mSubscriber;
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
        mDeviceObserver = new KadecotDeviceObserver();
        mLatch = new CountDownLatch(1);
        mDeviceObserver.setCallback(new SetupCallback(
                Collections.unmodifiableSet(new HashSet<String>() {

                    private static final long serialVersionUID = 1L;

                    {
                        add(KadecotWampTopic.TOPIC_PRIVATE_DEVICE);
                    }
                }),
                Collections.unmodifiableSet(new HashSet<String>() {

                    private static final long serialVersionUID = 1L;

                    {
                        add(KadecotDeviceObserver.DEVICE_LIST_PROCEDURE);
                    }
                }),
                new OnCompletionListener() {
                    @Override
                    public void onCompletion() {
                        mLatch.countDown();
                    }
                }));

        mDevicePublisher = new MockWampClient();
        mDevicePublisher.setCallback(new TestableCallback());

        mCaller = new MockWampClient();
        mCaller.setCallback(new TestableCallback());

        mSubscriber = new MockWampClient();
        mSubscriber.setCallback(new TestableCallback());

        mRouter = new KadecotWampRouter();
        mRouter.setCallback(new TestableCallback());

        mClients = new WampPeer[] {
                mCaller, mSubscriber, mDevicePublisher, mDeviceObserver
        };

        for (WampPeer client : mClients) {
            mRouter.connect(client);
        }

        for (WampPeer client : mClients) {
            WampTestUtil.transmitHelloSuccess(client, WampTestParam.REALM, mRouter);
        }
    }

    @Override
    protected void tearDown() {
        for (WampPeer client : mClients) {
            WampTestUtil.transmitGoodbyeSuccess(client, WampError.CLOSE_REALM, mRouter);
        }
    }

    public void testGetDeviceList() {
        JSONObject expected = createTestDevice();
        try {
            mLatch.await();
        } catch (InterruptedException e1) {
            fail();
        }
        TestableCallback listener = (TestableCallback) mDeviceObserver.getCallback();
        listener.setTargetMessageType(WampMessageType.EVENT, new CountDownLatch(1));
        mDevicePublisher.transmit(WampMessageFactory.createPublish(WampRequestIdGenerator.getId(),
                new JSONObject(),
                KadecotWampTopic.TOPIC_PRIVATE_DEVICE, new JSONArray(), expected));
        try {
            assertTrue(listener.await(2, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        listener = (TestableCallback) mCaller.getCallback();
        listener.setTargetMessageType(WampMessageType.RESULT, new CountDownLatch(1));
        mCaller.transmit(WampMessageFactory.createCall(WampRequestIdGenerator.getId(),
                new JSONObject(), KadecotDeviceObserver.DEVICE_LIST_PROCEDURE));
        try {
            assertTrue(listener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        assertTrue(listener.getTargetMessage().isResultMessage());
        try {
            assertTrue(expected.equals(listener.getTargetMessage().asResultMessage().getArguments()
                    .getJSONObject(0)));
        } catch (JSONException e) {
            fail();
        }
    }

    public void testOnDeviceStateChanged() {
        TestableCallback sublistener = (TestableCallback) mSubscriber.getCallback();
        sublistener.setTargetMessageType(WampMessageType.SUBSCRIBED, new CountDownLatch(1));
        mSubscriber.transmit(WampMessageFactory.createSubscribe(WampRequestIdGenerator.getId(),
                new JSONObject(), KadecotWampTopic.TOPIC_DEVICE));
        try {
            assertTrue(sublistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e1) {
            fail();
        }
        assertTrue(sublistener.getTargetMessage().isSubscribedMessage());

        JSONObject expected = createDevice("TV1");
        TestableCallback obslistener = (TestableCallback) mDeviceObserver.getCallback();
        obslistener.setTargetMessageType(WampMessageType.EVENT, new CountDownLatch(1));
        sublistener.setTargetMessageType(WampMessageType.EVENT, new CountDownLatch(1));
        mDevicePublisher.transmit(WampMessageFactory.createPublish(WampRequestIdGenerator.getId(),
                new JSONObject(),
                KadecotWampTopic.TOPIC_PRIVATE_DEVICE, new JSONArray(), expected));
        try {
            assertTrue(obslistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        assertTrue(obslistener.getTargetMessage().isEventMessage());

        try {
            assertTrue(sublistener.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        assertTrue(sublistener.getTargetMessage().isEventMessage());
    }

}
