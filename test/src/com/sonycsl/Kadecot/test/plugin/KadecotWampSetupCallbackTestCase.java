/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.test.plugin;

import com.sonycsl.Kadecot.plugin.KadecotWampSetupCallback;
import com.sonycsl.Kadecot.plugin.KadecotWampSetupCallback.OnCompletionListener;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.WampPeer.Callback;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.mock.MockWampPeer;
import com.sonycsl.wamp.util.TestableCallback;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotWampSetupCallbackTestCase extends TestCase {
    private MockWampPeer mClient;
    private MockWampPeer mRouter;

    Collection<String> mTopics;
    Collection<String> mProcedures;
    private KadecotWampSetupCallback mCallback;

    private CountDownLatch mCompleteLatch;
    private OnCompletionListener mCompleteListener;

    @Override
    protected void setUp() {
        mClient = new MockWampPeer();
        mRouter = new MockWampPeer();

        mClient.connect(mRouter);
        mClient.transmit(WampMessageFactory.createHello("realm", new JSONObject()));
        mRouter.clearMessages();

        mTopics = new HashSet<String>();
        mTopics.add("topic1");
        mTopics.add("topic2");

        mProcedures = new HashSet<String>();
        mProcedures.add("proc1");
        mProcedures.add("proc2");

        mCompleteLatch = new CountDownLatch(1);
        mCompleteListener = new OnCompletionListener() {

            @Override
            public void onCompletion() {
                mCompleteLatch.countDown();
            }
        };
        mCallback = new KadecotWampSetupCallback(mTopics, mProcedures, mCompleteListener);
    }

    public void testConstructor() {
        assertNotNull(mClient);
        assertNotNull(mRouter);
        assertNotNull(mCallback);
        assertNotNull(mCompleteLatch);
        assertNotNull(mCompleteListener);
    }

    public void testPostReceive() {
        TestableCallback routerCallback = new TestableCallback();
        routerCallback.setTargetMessageType(WampMessageType.WELCOME, new CountDownLatch(1));
        mRouter.setCallback(routerCallback);

        mCallback.postReceive(mClient, WampMessageFactory.createCall(0, new JSONObject(), "proc"));
        try {
            assertFalse(routerCallback.await(1, TimeUnit.NANOSECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        mRouter.removeCallback(routerCallback);

        final CountDownLatch routerLatch = new CountDownLatch(mTopics.size() + mProcedures.size());
        mRouter.setCallback(new Callback() {

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
                if (msg.getMessageType() == WampMessageType.REGISTER
                        || msg.getMessageType() == WampMessageType.SUBSCRIBE) {
                    routerLatch.countDown();
                }
            }

            @Override
            public void postConnect(WampPeer connecter, WampPeer connectee) {
            }
        });

        mCallback.postReceive(mClient, WampMessageFactory.createWelcome(0, new JSONObject()));

        try {
            assertTrue(routerLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testPreTransmit() {
        /**
         * Not handle CALL message test.
         */
        TestableCallback routerCallback = new TestableCallback();
        routerCallback.setTargetMessageType(WampMessageType.WELCOME, new CountDownLatch(1));
        mRouter.setCallback(routerCallback);

        mCallback.postReceive(mClient, WampMessageFactory.createCall(0, new JSONObject(), "proc"));
        try {
            assertFalse(routerCallback.await(1, TimeUnit.NANOSECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        mRouter.removeCallback(routerCallback);
        mRouter.clearMessages();

        /**
         * Handle SUBSCRIBE/REGISTER message
         */
        final CountDownLatch routerLatch = new CountDownLatch(mTopics.size() + mProcedures.size());
        mRouter.setCallback(new Callback() {

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
                if (msg.getMessageType() == WampMessageType.REGISTER
                        || msg.getMessageType() == WampMessageType.SUBSCRIBE) {
                    routerLatch.countDown();
                }
            }

            @Override
            public void postConnect(WampPeer connecter, WampPeer connectee) {
            }
        });

        mCallback.postReceive(mClient, WampMessageFactory.createWelcome(0, new JSONObject()));

        try {
            assertTrue(routerLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        mClient.clearMessages();

        /**
         * Handler SUBSCRIBED/REGISTERED message
         */
        final CountDownLatch clientLatch = new CountDownLatch(mTopics.size() + mProcedures.size());
        mClient.setCallback(new Callback() {

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
                if (msg.getMessageType() == WampMessageType.REGISTERED
                        || msg.getMessageType() == WampMessageType.SUBSCRIBED) {
                    clientLatch.countDown();
                }

            }

            @Override
            public void postConnect(WampPeer connecter, WampPeer connectee) {
            }
        });

        for (WampMessage msg : mRouter.getAllMessages()) {
            if (msg.getMessageType() == WampMessageType.SUBSCRIBE) {
                WampMessage subscribedMsg = WampMessageFactory.createSubscribed(msg
                        .asSubscribeMessage().getRequestId(), WampRequestIdGenerator.getId());
                mRouter.transmit(subscribedMsg);
                mCallback.postReceive(mClient, subscribedMsg);
            }
            if (msg.getMessageType() == WampMessageType.REGISTER) {
                WampMessage registeredMsg = WampMessageFactory.createRegistered(msg
                        .asRegisterMessage().getRequestId(), WampRequestIdGenerator.getId());
                mRouter.transmit(registeredMsg);
                mCallback.postReceive(mClient, registeredMsg);
            }
        }

        try {
            assertTrue(clientLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        /**
         * Handle UNREGISTRER/UNSUBSCRIBE message
         */
        final CountDownLatch removeLatch = new CountDownLatch(mTopics.size() + mProcedures.size());
        mRouter.setCallback(new Callback() {

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
                if (msg.getMessageType() == WampMessageType.UNREGISTER
                        || msg.getMessageType() == WampMessageType.UNSUBSCRIBE) {
                    removeLatch.countDown();
                }
            }

            @Override
            public void postConnect(WampPeer connecter, WampPeer connectee) {
            }
        });

        /**
         * Handle GOODBYE message
         */
        mCallback.preTransmit(mClient, WampMessageFactory.createGoodbye(new JSONObject(), ""));

        try {
            assertTrue(removeLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

    }

}
