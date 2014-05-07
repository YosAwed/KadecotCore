/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.Kadecot.wamp;

import com.sonycsl.Kadecot.wamp.KadecotDeviceInfo;
import com.sonycsl.Kadecot.wamp.KadecotDeviceObserver;
import com.sonycsl.Kadecot.wamp.KadecotWampRouter;
import com.sonycsl.Kadecot.wamp.KadecotWampTopic;
import com.sonycsl.test.wamp.Testable;
import com.sonycsl.test.wamp.mock.MockWampClient;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotDeviceObserverTestCase extends TestCase {

    private static final class TestKadecotDeviceObserver extends KadecotDeviceObserver implements
            Testable {

        private CountDownLatch mLatch;
        private WampMessage mMsg;

        @Override
        public void setCountDownLatch(CountDownLatch latch) {
            mLatch = latch;
        }

        @Override
        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return mLatch.await(timeout, unit);
        }

        @Override
        public WampMessage getLatestMessage() {
            return mMsg;
        }

        @Override
        protected void OnReceived(WampMessage msg) {
            super.OnReceived(msg);
            mMsg = msg;
            if (mLatch != null) {
                mLatch.countDown();
            }
        }

    }

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

    private TestKadecotDeviceObserver mDeviceObserver;
    private MockWampClient mDevicePublisher;
    private MockWampClient mCaller;
    private MockWampClient mSubscriber;
    private KadecotWampRouter mRouter;

    @Override
    protected void setUp() {
        mDeviceObserver = new TestKadecotDeviceObserver();
        mDevicePublisher = new MockWampClient();
        mCaller = new MockWampClient();
        mSubscriber = new MockWampClient();

        mRouter = new KadecotWampRouter();

        final WampPeer[] peers = {
                mDeviceObserver, mDevicePublisher, mCaller, mSubscriber
        };

        for (WampPeer peer : peers) {
            mRouter.connect(peer);
        }

        final Testable[] testables = {
                mDeviceObserver, mDevicePublisher, mCaller, mSubscriber
        };

        for (Testable testable : testables) {
            testable.setCountDownLatch(new CountDownLatch(1));
            testable.transmit(WampMessageFactory.createHello("", new JSONObject()));
            try {
                assertTrue(testable.await(1, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                fail();
            }
        }
    }

    @Override
    protected void tearDown() {
        final Testable[] testables = {
                mDeviceObserver, mDevicePublisher, mCaller, mSubscriber
        };

        for (Testable testable : testables) {
            testable.setCountDownLatch(new CountDownLatch(1));
            testable.transmit(WampMessageFactory.createGoodbye(new JSONObject(),
                    WampError.CLOSE_REALM));
            try {
                assertTrue(testable.await(1, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                fail();
            }
        }
    }

    public void testGetDeviceList() {
        JSONObject expected = createTestDevice();
        mDeviceObserver.setCountDownLatch(new CountDownLatch(1));
        mDevicePublisher.transmit(WampMessageFactory.createPublish(WampRequestIdGenerator.getId(),
                new JSONObject(),
                KadecotWampTopic.TOPIC_PRIVATE_DEVICE, new JSONArray(), expected));
        try {
            assertTrue(mDeviceObserver.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        mCaller.setCountDownLatch(new CountDownLatch(1));
        mCaller.transmit(WampMessageFactory.createCall(WampRequestIdGenerator.getId(),
                new JSONObject(), KadecotDeviceObserver.DEVICE_LIST_PROCEDURE));
        try {
            assertTrue(mCaller.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        assertTrue(mCaller.getLatestMessage().isResultMessage());
        try {
            assertTrue(expected.equals(mCaller.getLatestMessage().asResultMessage().getArguments()
                    .getJSONObject(0)));
        } catch (JSONException e) {
            fail();
        }
    }

    public void testOnDeviceStateChanged() {
        mSubscriber.setCountDownLatch(new CountDownLatch(1));
        mSubscriber.transmit(WampMessageFactory.createSubscribe(WampRequestIdGenerator.getId(),
                new JSONObject(), KadecotWampTopic.TOPIC_DEVICE));
        try {
            assertTrue(mSubscriber.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e1) {
            fail();
        }
        assertTrue(mSubscriber.getLatestMessage().isSubscribedMessage());

        JSONObject expected = createDevice("TV1");
        mSubscriber.setCountDownLatch(new CountDownLatch(1));
        mDevicePublisher.transmit(WampMessageFactory.createPublish(WampRequestIdGenerator.getId(),
                new JSONObject(),
                KadecotWampTopic.TOPIC_PRIVATE_DEVICE, new JSONArray(), expected));
        try {
            assertTrue(mDeviceObserver.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        try {
            assertTrue(mSubscriber.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        assertTrue(mSubscriber.getLatestMessage().isEventMessage());
    }

}
