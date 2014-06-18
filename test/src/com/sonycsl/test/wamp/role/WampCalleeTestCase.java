/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.wamp.role;

import com.sonycsl.test.mock.MockWampPeer;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampInvocationMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampYieldMessage;
import com.sonycsl.wamp.role.WampCallee;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.role.WampRole.OnReplyListener;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampCalleeTestCase extends TestCase {

    private static class TestWampCallee extends WampCallee {

        private WampMessage mMsg;

        public void setInvocationReturn(WampMessage msg) {
            mMsg = msg;
        }

        @Override
        protected WampMessage invocation(String procedure, WampMessage msg) {
            return mMsg;
        }

    }

    private static final class TestOnReplyListener implements OnReplyListener {

        private WampPeer mReceiver;
        private WampMessage mReply;
        private CountDownLatch mLatch;

        public WampPeer getReceiver() {
            return mReceiver;
        }

        public WampMessage getLatestReply() {
            return mReply;
        }

        public void setCountDownLatch(CountDownLatch latch) {
            mLatch = latch;
        }

        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return mLatch.await(timeout, unit);
        }

        @Override
        public void onReply(WampPeer receiver, WampMessage reply) {
            mReceiver = receiver;
            mReply = reply;
            if (mLatch != null) {
                mLatch.countDown();

            }
        }

    }

    private static final String PROCEDURE = "com.myapp.myprocedure1";
    private static final String PROCEDURE2 = "com.myapp.myprocedure2";

    private TestWampCallee mCallee;
    private MockWampPeer mPeer;
    private TestOnReplyListener mListener;

    @Override
    protected void setUp() throws Exception {
        mCallee = new TestWampCallee();
        mPeer = new MockWampPeer();
        mListener = new TestOnReplyListener();
    }

    public void testCtor() {
        assertNotNull(mCallee);
        assertNotNull(mPeer);
        assertNotNull(mListener);
    }

    public void testGetRoleName() {
        assertTrue(mCallee.getRoleName().equals("callee"));
    }

    public void testTxRegister() {
        assertTrue(mCallee.resolveTxMessage(mPeer, WampMessageFactory.createRegister(
                WampRequestIdGenerator.getId(), new JSONObject(), PROCEDURE)));
    }

    public void testRxRegistered() {
        final int requestId = WampRequestIdGenerator.getId();
        final int registrationId = 100;

        assertFalse(mCallee
                .resolveRxMessage(mPeer,
                        WampMessageFactory.createRegistered(WampRequestIdGenerator.getId(),
                                registrationId),
                        new OnReplyListener() {
                            @Override
                            public void onReply(WampPeer receiver, WampMessage reply) {
                                fail();
                            }
                        }));
        assertTrue(mCallee.resolveTxMessage(mPeer,
                WampMessageFactory.createRegister(requestId, new JSONObject(), PROCEDURE)));
        assertTrue(mCallee.resolveRxMessage(mPeer,
                WampMessageFactory.createRegistered(requestId, registrationId),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));
    }

    public void testRxRegisterdTwice() {
        final int requestId1 = WampRequestIdGenerator.getId();
        final int requestId2 = WampRequestIdGenerator.getId();
        final int registrationId1 = 100;
        final int registrationId2 = 200;
        assertTrue(mCallee.resolveTxMessage(mPeer,
                WampMessageFactory.createRegister(requestId1, new JSONObject(), PROCEDURE)));
        assertTrue(mCallee.resolveTxMessage(mPeer,
                WampMessageFactory.createRegister(requestId2, new JSONObject(), PROCEDURE2)));
        assertTrue(mCallee.resolveRxMessage(mPeer,
                WampMessageFactory.createRegistered(requestId1, registrationId1),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));
        assertTrue(mCallee.resolveRxMessage(mPeer,
                WampMessageFactory.createRegistered(requestId2, registrationId2),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));
    }

    public void testTxUnregister() {
        final int requestId = WampRequestIdGenerator.getId();
        final int registrationId = 100;
        assertTrue(mCallee.resolveTxMessage(mPeer,
                WampMessageFactory.createRegister(requestId, new JSONObject(), PROCEDURE)));
        assertTrue(mCallee.resolveRxMessage(mPeer,
                WampMessageFactory.createRegistered(requestId, registrationId),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));
        assertTrue(mCallee
                .resolveTxMessage(mPeer,
                        WampMessageFactory.createUnregister(WampRequestIdGenerator.getId(),
                                registrationId)));
    }

    public void testRxUnregisterd() {
        int requestId = WampRequestIdGenerator.getId();
        final int registrationId = 100;
        assertTrue(mCallee.resolveTxMessage(mPeer,
                WampMessageFactory.createRegister(requestId, new JSONObject(), PROCEDURE)));
        assertTrue(mCallee.resolveRxMessage(mPeer,
                WampMessageFactory.createRegistered(requestId, registrationId),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));

        assertFalse(mCallee.resolveRxMessage(mPeer,
                WampMessageFactory.createUnregistered(WampRequestIdGenerator.getId()),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));
        requestId = WampRequestIdGenerator.getId();
        assertTrue(mCallee
                .resolveTxMessage(mPeer,
                        WampMessageFactory.createUnregister(requestId, registrationId)));
        assertTrue(mCallee.resolveRxMessage(mPeer,
                WampMessageFactory.createUnregistered(requestId), new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));
    }

    public void testRxUnregisterdTwice() {
        int requestId1 = WampRequestIdGenerator.getId();
        final int registrationId1 = 100;
        int requestId2 = WampRequestIdGenerator.getId();
        final int registrationId2 = 200;
        assertTrue(mCallee.resolveTxMessage(mPeer,
                WampMessageFactory.createRegister(requestId1, new JSONObject(), PROCEDURE)));
        assertTrue(mCallee.resolveRxMessage(mPeer,
                WampMessageFactory.createRegistered(requestId1, registrationId1),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));
        assertTrue(mCallee.resolveTxMessage(mPeer,
                WampMessageFactory.createRegister(requestId2, new JSONObject(), PROCEDURE2)));
        assertTrue(mCallee.resolveRxMessage(mPeer,
                WampMessageFactory.createRegistered(requestId2, registrationId2),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));

        requestId1 = WampRequestIdGenerator.getId();
        requestId2 = WampRequestIdGenerator.getId();
        assertTrue(mCallee
                .resolveTxMessage(mPeer,
                        WampMessageFactory.createUnregister(requestId1, registrationId1)));
        assertTrue(mCallee
                .resolveTxMessage(mPeer,
                        WampMessageFactory.createUnregister(requestId2, registrationId2)));
        assertTrue(mCallee.resolveRxMessage(mPeer,
                WampMessageFactory.createUnregistered(requestId1), new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));
        assertTrue(mCallee.resolveRxMessage(mPeer,
                WampMessageFactory.createUnregistered(requestId2), new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));
    }

    public void testRxInvocation() {
        int requestId = WampRequestIdGenerator.getId();
        final int registrationId = 100;

        final Map<WampMessage, OnReplyListener> args = new HashMap<WampMessage, WampRole.OnReplyListener>() {
            private static final long serialVersionUID = 1L;

            {
                put(WampMessageFactory.createInvocation(WampRequestIdGenerator.getId(),
                        registrationId, new JSONObject()), mListener);
                put(WampMessageFactory.createInvocation(WampRequestIdGenerator.getId(),
                        registrationId, new JSONObject(), new JSONArray()), mListener);
                put(WampMessageFactory.createInvocation(WampRequestIdGenerator.getId(),
                        registrationId, new JSONObject(), new JSONArray(), new JSONObject()),
                        mListener);
            }
        };

        for (Entry<WampMessage, OnReplyListener> entry : args.entrySet()) {

            WampInvocationMessage invocation = entry.getKey().asInvocationMessage();
            JSONObject options = new JSONObject();
            JSONArray yargs = new JSONArray();
            JSONObject yargskw = new JSONObject();

            WampMessage[] rets = {
                    WampMessageFactory.createYield(invocation.getRequestId(), options),
                    WampMessageFactory.createYield(invocation.getRequestId(), options, yargs),
                    WampMessageFactory.createYield(invocation.getRequestId(), options, yargs,
                            yargskw)
            };

            for (WampMessage ret : rets) {
                mCallee.setInvocationReturn(ret);
                assertFalse(mCallee.resolveRxMessage(mPeer, entry.getKey(), entry.getValue()));
            }
        }

        assertTrue(mCallee.resolveTxMessage(mPeer,
                WampMessageFactory.createRegister(requestId, new JSONObject(), PROCEDURE)));
        assertTrue(mCallee.resolveRxMessage(mPeer,
                WampMessageFactory.createRegistered(requestId, registrationId),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));

        for (Entry<WampMessage, OnReplyListener> entry : args.entrySet()) {

            WampInvocationMessage invocation = entry.getKey().asInvocationMessage();
            JSONObject options = new JSONObject();
            JSONArray yargs = new JSONArray();
            JSONObject yargskw = new JSONObject();

            WampMessage[] rets = {
                    WampMessageFactory.createYield(invocation.getRequestId(), options),
                    WampMessageFactory.createYield(invocation.getRequestId(), options, yargs),
                    WampMessageFactory.createYield(invocation.getRequestId(), options, yargs,
                            yargskw)
            };

            for (WampMessage ret : rets) {

                mCallee.setInvocationReturn(ret);
                mListener.setCountDownLatch(new CountDownLatch(1));
                assertTrue(mCallee.resolveRxMessage(mPeer, entry.getKey(), entry.getValue()));
                try {
                    assertTrue(mListener.await(1, TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                    fail();
                }
                assertEquals(mListener.getReceiver(), mPeer);
                WampMessage reply = mListener.getLatestReply();
                assertNotNull(reply);
                assertTrue(reply.isYieldMessage());

                WampYieldMessage expected = ret.asYieldMessage();
                WampYieldMessage actual = reply.asYieldMessage();

                assertEquals(expected.getRequestId(), actual.getRequestId());
                assertEquals(expected.getOptions(), actual.getOptions());

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
        }

        requestId = WampRequestIdGenerator.getId();
        assertTrue(mCallee
                .resolveTxMessage(mPeer,
                        WampMessageFactory.createUnregister(requestId, registrationId)));
        assertTrue(mCallee.resolveRxMessage(mPeer,
                WampMessageFactory.createUnregistered(requestId), new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        fail();
                    }
                }));
    }
}
