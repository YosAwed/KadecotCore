/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.wamp.role;

import com.sonycsl.test.mock.MockWampPeer;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampEventMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampRole.OnReplyListener;
import com.sonycsl.wamp.role.WampSubscriber;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

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
        protected void event(String topic, WampMessage msg) {
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
}
