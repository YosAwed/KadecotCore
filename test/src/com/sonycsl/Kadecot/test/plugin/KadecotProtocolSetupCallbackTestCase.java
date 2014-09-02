/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.test.plugin;

import com.sonycsl.Kadecot.plugin.KadecotProtocolSetupCallback;
import com.sonycsl.Kadecot.plugin.KadecotProtocolSetupCallback.OnCompletionListener;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.mock.MockWampPeer;
import com.sonycsl.wamp.util.TestableCallback;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotProtocolSetupCallbackTestCase extends TestCase {
    private MockWampPeer mClient;
    private MockWampPeer mRouter;

    Map<String, String> mTopics;
    Map<String, String> mProcedures;
    private KadecotProtocolSetupCallback mCallback;

    private CountDownLatch mCompleteLatch;
    private OnCompletionListener mCompleteListener;

    @Override
    protected void setUp() {
        mClient = new MockWampPeer();
        mRouter = new MockWampPeer();

        mClient.connect(mRouter);

        mTopics = new HashMap<String, String>();
        mTopics.put("com.sonycsl.kadecot.test.topic.topic1", "This is topic1");
        mTopics.put("com.sonycsl.kadecot.test.topic.topic2", "This is topic2");

        mProcedures = new HashMap<String, String>();
        mProcedures.put("com.sonycsl.kadecot.test.procedure.proc1", "This is proc1");
        mProcedures.put("com.sonycsl.kadecot.test.procedure.proc2", "This is proc2");

        mCompleteLatch = new CountDownLatch(1);
        mCompleteListener = new OnCompletionListener() {

            @Override
            public void onCompletion() {
                mCompleteLatch.countDown();
            }
        };
        mCallback = new KadecotProtocolSetupCallback(mTopics, mProcedures, mCompleteListener);
    }

    public void testConstructor() {
        assertNotNull(mClient);
        assertNotNull(mRouter);
        assertNotNull(mCallback);
        assertNotNull(mCompleteLatch);
        assertNotNull(mCompleteListener);
    }

    public void testPreTransmit() {
        TestableCallback routerCallback = new TestableCallback();
        routerCallback.setTargetMessageType(WampMessageType.GOODBYE, new CountDownLatch(1));
        mRouter.setCallback(routerCallback);

        mCallback.preTransmit(mClient, WampMessageFactory.createCall(0, new JSONObject(), "proc"));
        try {
            assertFalse(routerCallback.await(1, TimeUnit.NANOSECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        mRouter.removeCallback(routerCallback);

        routerCallback = new TestableCallback();
        routerCallback.setTargetMessageType(WampMessageType.CALL, new CountDownLatch(2));
        mRouter.setCallback(routerCallback);

        mCallback.preTransmit(mClient, WampMessageFactory.createGoodbye(new JSONObject(), ""));

        try {
            assertTrue(routerCallback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testPostReceive() {
        TestableCallback routerCallback = new TestableCallback();
        routerCallback.setTargetMessageType(WampMessageType.WELCOME, new CountDownLatch(1));
        mRouter.setCallback(routerCallback);

        mCallback.postReceive(mClient, WampMessageFactory.createWelcome(0, new JSONObject()));
        try {
            assertFalse(routerCallback.await(1, TimeUnit.NANOSECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        mRouter.removeCallback(routerCallback);

        routerCallback = new TestableCallback();
        routerCallback.setTargetMessageType(WampMessageType.CALL, new CountDownLatch(2));
        mRouter.setCallback(routerCallback);

        mCallback.postReceive(mClient, WampMessageFactory.createWelcome(0, new JSONObject()));

        try {
            assertTrue(routerCallback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }
}
