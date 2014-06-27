/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.wamp.role;

import com.sonycsl.test.mock.MockWampPeer;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampEventMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.role.WampRole.OnReplyListener;
import com.sonycsl.wamp.role.WampSubscriber;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class WampSubscriberTestCase extends TestCase {

    private static class TestWampSubscriber extends WampSubscriber {

        private String mTopic;
        private WampMessage mMsg;

        public String getLatestTopic() {
            return mTopic;
        }

        public WampMessage getLatestMessage() {
            return mMsg;
        }

        @Override
        protected void onEvent(String topic, WampMessage msg) {
            mTopic = topic;
            mMsg = msg;
        }
    }

    private static final String TOPIC = "wamp.subscribe.test.case.topic";

    private TestWampSubscriber mSubscriber;
    private MockWampPeer mPeer;

    @Override
    protected void setUp() throws Exception {
        mSubscriber = new TestWampSubscriber();
        mPeer = new MockWampPeer();
    }

    public void testCtor() {
        assertNotNull(mSubscriber);
        assertNotNull(mPeer);
    }

    public void testGetRoleName() {
        assertTrue(mSubscriber.getRoleName().equals("subscriber"));
    }

    public void testTxSubscribe() {
        assertTrue(mSubscriber.resolveTxMessage(mPeer, WampMessageFactory.createSubscribe(
                WampRequestIdGenerator.getId(), new JSONObject(), TOPIC)));
    }

    public void testRxSubscribed() {
        int requestId = WampRequestIdGenerator.getId();
        final int subscriptionId = 100;

        assertFalse(mSubscriber
                .resolveRxMessage(mPeer,
                        WampMessageFactory.createSubscribed(WampRequestIdGenerator.getId(),
                                subscriptionId),
                        new OnReplyListener() {
                            @Override
                            public void onReply(WampPeer receiver, WampMessage reply) {
                                fail();
                            }
                        }));
        assertTrue(mSubscriber.resolveTxMessage(mPeer,
                WampMessageFactory.createSubscribe(requestId, new JSONObject(), TOPIC)));
        assertTrue(mSubscriber.resolveRxMessage(mPeer,
                WampMessageFactory.createSubscribed(requestId, subscriptionId),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));

    }

    public void testRxSubscribedTwice() {
        int subscriptionId = 100;

        WampMessage[] msgs = {
                WampMessageFactory.createSubscribe(WampRequestIdGenerator.getId(),
                        new JSONObject(), TOPIC),
                WampMessageFactory.createSubscribe(WampRequestIdGenerator.getId(),
                        new JSONObject(), TOPIC)
        };

        for (WampMessage msg : msgs) {
            assertTrue(mSubscriber.resolveTxMessage(mPeer, msg));
        }
        for (WampMessage msg : msgs) {
            ++subscriptionId;
            assertTrue(mSubscriber.resolveRxMessage(mPeer,
                    WampMessageFactory.createSubscribed(msg.asSubscribeMessage().getRequestId(),
                            subscriptionId),
                    new OnReplyListener() {
                        @Override
                        public void onReply(WampPeer receiver, WampMessage reply) {
                            fail();
                        }
                    }));
        }

    }

    public void testTxUnSubscribe() {
        int requestId = WampRequestIdGenerator.getId();
        final int subscriptionId = 100;

        assertTrue(mSubscriber.resolveTxMessage(mPeer,
                WampMessageFactory.createSubscribe(requestId, new JSONObject(), TOPIC)));
        assertTrue(mSubscriber.resolveRxMessage(mPeer,
                WampMessageFactory.createSubscribed(requestId, subscriptionId),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));

        assertTrue(mSubscriber.resolveTxMessage(mPeer,
                WampMessageFactory.createUnsubscribe(requestId, subscriptionId)));
    }

    public void testRxUnsubscribed() {
        int requestId = WampRequestIdGenerator.getId();
        final int subscriptionId = 100;

        assertTrue(mSubscriber.resolveTxMessage(mPeer,
                WampMessageFactory.createSubscribe(requestId, new JSONObject(), TOPIC)));
        assertTrue(mSubscriber.resolveRxMessage(mPeer,
                WampMessageFactory.createSubscribed(requestId, subscriptionId),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));

        assertFalse(mSubscriber.resolveRxMessage(mPeer,
                WampMessageFactory.createUnsubscribed(WampRequestIdGenerator.getId()),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));
        requestId = WampRequestIdGenerator.getId();
        assertTrue(mSubscriber.resolveTxMessage(mPeer,
                WampMessageFactory.createUnsubscribe(requestId, subscriptionId)));
        assertTrue(mSubscriber.resolveRxMessage(mPeer,
                WampMessageFactory.createUnsubscribed(requestId), new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));
    }

    public void testRxUnsubscribedTwice() {
        int subscriptionId = 100;

        WampMessage[] msgs = {
                WampMessageFactory.createSubscribe(WampRequestIdGenerator.getId(),
                        new JSONObject(), TOPIC),
                WampMessageFactory.createSubscribe(WampRequestIdGenerator.getId(),
                        new JSONObject(), TOPIC)
        };

        Set<WampMessage> unsubMsgs = new HashSet<WampMessage>();

        for (WampMessage msg : msgs) {
            unsubMsgs.add(WampMessageFactory.createUnsubscribe(WampRequestIdGenerator.getId(),
                    ++subscriptionId));

            assertTrue(mSubscriber.resolveTxMessage(mPeer, msg));
            assertTrue(mSubscriber.resolveRxMessage(mPeer,
                    WampMessageFactory.createSubscribed(msg.asSubscribeMessage().getRequestId(),
                            subscriptionId),
                    new OnReplyListener() {
                        @Override
                        public void onReply(WampPeer receiver, WampMessage reply) {
                            fail();
                        }
                    }));
        }

        for (WampMessage unsubMsg : unsubMsgs) {
            assertTrue(mSubscriber.resolveTxMessage(mPeer, unsubMsg));
        }
        for (WampMessage unsubMsg : unsubMsgs) {
            assertTrue(mSubscriber.resolveRxMessage(mPeer,
                    WampMessageFactory.createUnsubscribed(unsubMsg.asUnsubscribeMessage()
                            .getRequestId()), new OnReplyListener() {
                        @Override
                        public void onReply(WampPeer receiver, WampMessage reply) {
                            fail();
                        }
                    }));
        }
    }

    public void testRxEvent() {
        int requestId = WampRequestIdGenerator.getId();
        final int subscriptionId = 100;
        final int publicationId = 200;

        assertTrue(mSubscriber.resolveTxMessage(mPeer,
                WampMessageFactory.createSubscribe(requestId, new JSONObject(), TOPIC)));
        assertTrue(mSubscriber.resolveRxMessage(mPeer,
                WampMessageFactory.createSubscribed(requestId, subscriptionId),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));

        WampMessage[] msgs = {
                WampMessageFactory.createEvent(subscriptionId, publicationId, new JSONObject()),
                WampMessageFactory.createEvent(subscriptionId, publicationId, new JSONObject(),
                        new JSONArray()),
                WampMessageFactory.createEvent(subscriptionId, publicationId, new JSONObject(),
                        new JSONArray(), new JSONObject())
        };

        for (WampMessage msg : msgs) {
            assertTrue(mSubscriber.resolveRxMessage(mPeer, msg, new OnReplyListener() {
                @Override
                public void onReply(WampPeer receiver, WampMessage reply) {
                    fail();
                }
            }));
            assertEquals(TOPIC, mSubscriber.getLatestTopic());
            WampEventMessage expected = msg.asEventMessage();
            WampEventMessage actual = mSubscriber.getLatestMessage().asEventMessage();
            assertNotNull(expected);
            assertNotNull(actual);
            assertEquals(expected.getSubscriptionId(), actual.getSubscriptionId());
            assertEquals(expected.getPublicationId(), actual.getPublicationId());
            assertEquals(expected.getDetails(), actual.getDetails());
            if (expected.hasArguments()) {
                assertTrue(actual.hasArguments());
                assertEquals(expected.getArguments(), actual.getArguments());
            } else {
                assertFalse(actual.hasArguments());
            }
            if (expected.hasArgumentsKw()) {
                assertTrue(actual.hasArgumentsKw());
                assertEquals(expected.getArgumentsKw(), actual.getArgumentsKw());
            } else {
                assertFalse(actual.hasArgumentsKw());
            }
        }

        requestId = WampRequestIdGenerator.getId();
        assertTrue(mSubscriber.resolveTxMessage(mPeer,
                WampMessageFactory.createUnsubscribe(requestId, subscriptionId)));
        assertTrue(mSubscriber.resolveRxMessage(mPeer,
                WampMessageFactory.createUnsubscribed(requestId), new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));
    }

    // abnormal
    public void testSubscribedWithNoSubscribe() {
        assertFalse(mSubscriber.resolveRxMessage(mPeer,
                WampMessageFactory.createSubscribed(1, -1), new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));
    }

    // abnormal
    public void testUnsubscribedWithNoUnsubscribe() {
        assertFalse(mSubscriber.resolveRxMessage(mPeer,
                WampMessageFactory.createUnsubscribed(-1), new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));
    }

    // abnormal
    public void testNoSubscriptionEvent() {
        assertFalse(mSubscriber.resolveRxMessage(mPeer,
                WampMessageFactory.createEvent(1, 1, new JSONObject()), new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));
    }

    // abnormal
    public void testNoSuchSubscription() {
        int requestId = 1;
        assertTrue(mSubscriber.resolveTxMessage(mPeer,
                WampMessageFactory.createSubscribe(requestId, new JSONObject(), TOPIC)));
        assertTrue(mSubscriber.resolveRxMessage(mPeer,
                WampMessageFactory.createEvent(-1, 1, new JSONObject()), new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        assertTrue(reply.isErrorMessage());
                        assertEquals(WampError.NO_SUCH_SUBSCRIPTION, reply.asErrorMessage()
                                .getUri());
                    }
                }));
    }

    // abnormal
    public void testMessageOutOfRole() {
        Set<Integer> uncheckRx = new HashSet<Integer>();
        uncheckRx.add(WampMessageType.WELCOME);
        uncheckRx.add(WampMessageType.ABORT);
        uncheckRx.add(WampMessageType.GOODBYE);
        uncheckRx.add(WampMessageType.ERROR);
        uncheckRx.add(WampMessageType.SUBSCRIBED);
        uncheckRx.add(WampMessageType.UNSUBSCRIBED);
        uncheckRx.add(WampMessageType.EVENT);

        WampRoleTestUtil.rxMessageOutOfRole(mSubscriber, mPeer, uncheckRx);

        Set<Integer> uncheckTx = new HashSet<Integer>();
        uncheckTx.add(WampMessageType.HELLO);
        uncheckTx.add(WampMessageType.GOODBYE);
        uncheckTx.add(WampMessageType.SUBSCRIBE);
        uncheckTx.add(WampMessageType.UNSUBSCRIBE);

        WampRoleTestUtil.txMessageOutOfRole(mSubscriber, mPeer, uncheckTx);
    }
}
