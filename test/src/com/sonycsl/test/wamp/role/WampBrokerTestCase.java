/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.wamp.role;

import com.sonycsl.test.mock.MockWampPeer;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampBroker;
import com.sonycsl.wamp.role.WampBroker.PubSubMessageHandler;
import com.sonycsl.wamp.role.WampRole.OnReplyListener;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WampBrokerTestCase extends TestCase {

    private static class TestWampBroker extends WampBroker {
        private TestWampBroker() {
            super();
        }

        private TestWampBroker(PubSubMessageHandler handler) {
            super(handler);
        }
    }

    private static final String TOPIC = "wamp.broaker.test.case.topic";
    private static final String TOPIC1 = "wamp.broker.test.case.topic1";
    private static final String TOPIC2 = "wamp.broker.test.case.topic2";
    private static final String UNKNOWN_TOPIC = "wamp.broaker.test.case.unknown.topic";

    private TestWampBroker mBroker;
    private MockWampPeer mPeer1;
    private MockWampPeer mPeer2;
    private MockWampPeer mPeer3;

    @Override
    protected void setUp() {
        mBroker = new TestWampBroker(new PubSubMessageHandler() {

            @Override
            public void onUnsubscribe(String topic) {
            }

            @Override
            public void onSubscribe(String topic) {
            }

        });
        mPeer1 = new MockWampPeer();
        mPeer2 = new MockWampPeer();
        mPeer3 = new MockWampPeer();
    }

    public void testCtor() {
        assertNotNull(new TestWampBroker());
        assertNotNull(mBroker);
        assertNotNull(mPeer1);
        assertNotNull(mPeer2);
        assertNotNull(mPeer3);
    }

    public void testGetRoleName() {
        assertEquals(mBroker.getRoleName(), "broker");
    }

    public void testRxSubscribe() {
        final CountDownLatch latch = new CountDownLatch(1);
        assertTrue(mBroker.resolveRxMessage(mPeer1, WampMessageFactory.createSubscribe(
                WampRequestIdGenerator.getId(), new JSONObject(), TOPIC), new OnReplyListener() {
            @Override
            public void onReply(WampPeer receiver, WampMessage reply) {
                assertTrue(reply.isSubscribedMessage());
                latch.countDown();
            }
        }));
        try {
            assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testRxPublish() {
        final CountDownLatch latch = new CountDownLatch(1);
        assertTrue(mBroker.resolveRxMessage(mPeer1, WampMessageFactory.createSubscribe(
                WampRequestIdGenerator.getId(), new JSONObject(), TOPIC), new OnReplyListener() {
            @Override
            public void onReply(WampPeer receiver, WampMessage reply) {
                assertTrue(reply.isSubscribedMessage());
                latch.countDown();
            }
        }));
        try {
            assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        WampMessage[] msgs = {
                WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                        TOPIC),
                WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                        TOPIC, new JSONArray()),
                WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                        TOPIC, new JSONArray(), new JSONObject()),
        };
        for (WampMessage msg : msgs) {
            final CountDownLatch latch2 = new CountDownLatch(2);
            assertTrue(mBroker.resolveRxMessage(mPeer2, msg, new OnReplyListener() {
                @Override
                public void onReply(WampPeer receiver, WampMessage reply) {
                    if (receiver == mPeer2) {
                        assertTrue(reply.isPublishedMessage());
                        latch2.countDown();
                        return;
                    }
                    if (receiver == mPeer1) {
                        assertTrue(reply.isEventMessage());
                        latch2.countDown();
                        return;
                    }
                    fail();
                }
            }));
            try {
                latch2.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                fail();
            }
        }

    }

    public void testRxPublishToUnknownTopic() {
        final CountDownLatch latch = new CountDownLatch(1);
        assertTrue(mBroker.resolveRxMessage(mPeer1, WampMessageFactory.createSubscribe(
                WampRequestIdGenerator.getId(), new JSONObject(), TOPIC), new OnReplyListener() {
            @Override
            public void onReply(WampPeer receiver, WampMessage reply) {
                assertTrue(reply.isSubscribedMessage());
                latch.countDown();
            }
        }));
        try {
            assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        WampMessage[] msgs = {
                WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                        UNKNOWN_TOPIC),
                WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                        UNKNOWN_TOPIC, new JSONArray()),
                WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                        UNKNOWN_TOPIC, new JSONArray(), new JSONObject()),
        };
        for (WampMessage msg : msgs) {
            final CountDownLatch l = new CountDownLatch(1);
            assertTrue(mBroker.resolveRxMessage(mPeer2, msg, new OnReplyListener() {
                @Override
                public void onReply(WampPeer receiver, WampMessage reply) {
                    if (receiver == mPeer2) {
                        assertTrue(reply.isPublishedMessage());
                        l.countDown();
                        return;
                    }
                    fail();
                }
            }));
            try {
                l.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                fail();
            }
        }
    }

    public void testRxSubscribeWithTwoClient() {
        WampPeer[] peers = {
                mPeer1, mPeer2
        };

        for (WampPeer peer : peers) {
            final CountDownLatch latch = new CountDownLatch(1);
            assertTrue(mBroker.resolveRxMessage(peer, WampMessageFactory.createSubscribe(
                    WampRequestIdGenerator.getId(), new JSONObject(), TOPIC),
                    new OnReplyListener() {
                        @Override
                        public void onReply(WampPeer receiver, WampMessage reply) {
                            assertTrue(reply.isSubscribedMessage());
                            latch.countDown();
                        }
                    }));
            try {
                assertTrue(latch.await(1, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                fail();
            }
        }
    }

    public void testRxPublishToSubscribers() {
        WampPeer[] peers = {
                mPeer1, mPeer2
        };

        for (WampPeer peer : peers) {
            final CountDownLatch latch = new CountDownLatch(1);
            assertTrue(mBroker.resolveRxMessage(peer, WampMessageFactory.createSubscribe(
                    WampRequestIdGenerator.getId(), new JSONObject(), TOPIC),
                    new OnReplyListener() {
                        @Override
                        public void onReply(WampPeer receiver, WampMessage reply) {
                            assertTrue(reply.isSubscribedMessage());
                            latch.countDown();
                        }
                    }));
            try {
                assertTrue(latch.await(1, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                fail();
            }
        }

        WampMessage[] msgs = {
                WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                        TOPIC),
                WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                        TOPIC, new JSONArray()),
                WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                        TOPIC, new JSONArray(), new JSONObject()),
        };
        for (WampMessage msg : msgs) {
            final CountDownLatch latch3 = new CountDownLatch(3);
            assertTrue(mBroker.resolveRxMessage(mPeer3, msg, new OnReplyListener() {
                @Override
                public void onReply(WampPeer receiver, WampMessage reply) {
                    if (receiver == mPeer3) {
                        assertTrue(reply.isPublishedMessage());
                        latch3.countDown();
                        return;
                    }
                    if (receiver == mPeer1) {
                        assertTrue(reply.isEventMessage());
                        latch3.countDown();
                        return;
                    }
                    if (receiver == mPeer2) {
                        assertTrue(reply.isEventMessage());
                        latch3.countDown();
                        return;
                    }
                    fail();
                }
            }));
            try {
                latch3.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                fail();
            }
        }
    }

    public void testRxUnsubscribe() {
        final CountDownLatch subLatch = new CountDownLatch(1);
        final AtomicInteger subscriptionId = new AtomicInteger(-1);

        assertTrue(mBroker.resolveRxMessage(mPeer1, WampMessageFactory.createSubscribe(
                WampRequestIdGenerator.getId(), new JSONObject(), TOPIC), new OnReplyListener() {
            @Override
            public void onReply(WampPeer receiver, WampMessage reply) {
                assertTrue(reply.isSubscribedMessage());
                subscriptionId.set(reply.asSubscribedMessage().getSubscriptionId());
                subLatch.countDown();
            }
        }));
        try {
            assertTrue(subLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        final CountDownLatch unSubLatch = new CountDownLatch(1);
        assertTrue(mBroker.resolveRxMessage(mPeer1, WampMessageFactory.createUnsubscribe(
                WampRequestIdGenerator.getId(), subscriptionId.get()),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        if (reply.isUnsubscribedMessage()) {
                            unSubLatch.countDown();
                            return;
                        }
                        fail();
                    }
                }));
        try {
            unSubLatch.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail();
        }

    }

    public void testRxUnsubscribeAndRxPublish() {
        final CountDownLatch subLatch = new CountDownLatch(1);
        final AtomicInteger subscriptionId = new AtomicInteger(-1);

        assertTrue(mBroker.resolveRxMessage(mPeer1, WampMessageFactory.createSubscribe(
                WampRequestIdGenerator.getId(), new JSONObject(), TOPIC), new OnReplyListener() {
            @Override
            public void onReply(WampPeer receiver, WampMessage reply) {
                assertTrue(reply.isSubscribedMessage());
                subscriptionId.set(reply.asSubscribedMessage().getSubscriptionId());
                subLatch.countDown();
            }
        }));
        try {
            assertTrue(subLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        final CountDownLatch unSubLatch = new CountDownLatch(1);
        assertTrue(mBroker.resolveRxMessage(mPeer1, WampMessageFactory.createUnsubscribe(
                WampRequestIdGenerator.getId(), subscriptionId.get()),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        if (reply.isUnsubscribedMessage()) {
                            unSubLatch.countDown();
                            return;
                        }
                        fail();
                    }
                }));
        try {
            unSubLatch.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail();
        }

        WampMessage[] msgs = {
                WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                        TOPIC),
                WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                        TOPIC, new JSONArray()),
                WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                        TOPIC, new JSONArray(), new JSONObject()),
        };
        for (WampMessage msg : msgs) {
            final CountDownLatch l = new CountDownLatch(1);
            assertTrue(mBroker.resolveRxMessage(mPeer2, msg, new OnReplyListener() {
                @Override
                public void onReply(WampPeer receiver, WampMessage reply) {
                    if (receiver == mPeer2) {
                        assertTrue(reply.isPublishedMessage());
                        l.countDown();
                        return;
                    }
                    fail();
                }
            }));
            try {
                l.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                fail();
            }
        }
    }

    public void testResolveTxMessage() {
        try {
            mBroker.resolveTxMessage(null, null);
        } catch (UnsupportedOperationException e) {
            return;
        }
        fail();
    }

    // abnormal
    public void testNoSuchSubscription() {
        mBroker.resolveRxMessage(mPeer1, WampMessageFactory.createUnsubscribe(1, -1),
                new OnReplyListener() {

                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        assertTrue(reply.isErrorMessage());
                        assertEquals(WampError.NO_SUCH_SUBSCRIPTION, reply.asErrorMessage()
                                .getUri());
                    }
                });
    }

    public void testRxManySubscribe() {
        int regNum = 200;
        final CountDownLatch latch2 = new CountDownLatch(regNum);

        // subscribe topic1 * 100

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    mBroker.resolveRxMessage(mPeer1, WampMessageFactory.createSubscribe(
                            WampRequestIdGenerator.getId(), new JSONObject(), TOPIC1 + "." + i),
                            new OnReplyListener() {
                                @Override
                                public void onReply(WampPeer receiver, WampMessage reply) {
                                    if (reply.isSubscribedMessage()) {
                                        latch2.countDown();
                                        return;
                                    }
                                    fail();
                                }
                            });
                }
            }
        }).start();

        // subscribe topic2 * 100
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    mBroker.resolveRxMessage(mPeer2, WampMessageFactory.createSubscribe(
                            WampRequestIdGenerator.getId(), new JSONObject(), TOPIC2 + "." + i),
                            new OnReplyListener() {
                                @Override
                                public void onReply(WampPeer receiver, WampMessage reply) {
                                    if (reply.isSubscribedMessage()) {
                                        latch2.countDown();
                                        return;
                                    }
                                    fail(reply.toString());
                                }
                            });
                }
            }
        }).start();

        try {
            assertTrue(latch2.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        // call procedures
        for (int i = 0; i < regNum / 2; i++) {
            assertTrue(mBroker.resolveRxMessage(mPeer1, WampMessageFactory.createPublish(
                    WampRequestIdGenerator.getId(), new JSONObject(), TOPIC1 + "." + i),
                    new OnReplyListener() {
                        @Override
                        public void onReply(WampPeer receiver, WampMessage reply) {
                            if (reply.isErrorMessage()) {
                                fail(reply.toString());
                            }
                        }
                    }));

            assertTrue(mBroker.resolveRxMessage(mPeer2, WampMessageFactory.createPublish(
                    WampRequestIdGenerator.getId(), new JSONObject(), TOPIC2 + "." + i),
                    new OnReplyListener() {
                        @Override
                        public void onReply(WampPeer receiver, WampMessage reply) {
                            if (reply.isErrorMessage()) {
                                fail(reply.toString());
                            }
                        }
                    }));
        }
    }
}
