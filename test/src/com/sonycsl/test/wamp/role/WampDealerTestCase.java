
package com.sonycsl.test.wamp.role;

import com.sonycsl.test.wamp.mock.MockWampPeer;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampDealer;
import com.sonycsl.wamp.role.WampRole.OnReplyListener;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WampDealerTestCase extends TestCase {

    private static class TestWampDealer extends WampDealer {
    }

    private TestWampDealer mDealer;
    private MockWampPeer mPeer1;
    private MockWampPeer mPeer2;
    private MockWampPeer mPeer3;

    private static final String PROCEDURE1 = "wamp.dealer.test.case.procedure1";
    private static final String PROCEDURE2 = "wamp.dealer.test.case.procedure2";

    @Override
    protected void setUp() {
        mDealer = new TestWampDealer();
        mPeer1 = new MockWampPeer();
        mPeer2 = new MockWampPeer();
        mPeer3 = new MockWampPeer();
    }

    public void testCtor() {
        assertNotNull(mDealer);
        assertNotNull(mPeer1);
        assertNotNull(mPeer2);
        assertNotNull(mPeer3);
    }

    public void testRxRegister() {
        final CountDownLatch latch = new CountDownLatch(1);
        assertTrue(mDealer.resolveRxMessage(mPeer1, WampMessageFactory.createRegister(
                WampRequestIdGenerator.getId(), new JSONObject(), PROCEDURE1),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        if (reply.isRegisteredMessage()) {
                            latch.countDown();
                            return;
                        }
                        fail();
                    }
                }));
        try {
            assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testRxRegisterWithTwoClient() {
        final CountDownLatch latch2 = new CountDownLatch(2);
        assertTrue(mDealer.resolveRxMessage(mPeer1, WampMessageFactory.createRegister(
                WampRequestIdGenerator.getId(), new JSONObject(), PROCEDURE1),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        if (reply.isRegisteredMessage()) {
                            latch2.countDown();
                            return;
                        }
                        fail();
                    }
                }));

        assertTrue(mDealer.resolveRxMessage(mPeer2, WampMessageFactory.createRegister(
                WampRequestIdGenerator.getId(), new JSONObject(), PROCEDURE2),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        if (reply.isRegisteredMessage()) {
                            latch2.countDown();
                            return;
                        }
                        fail(reply.toString());
                    }
                }));
        try {
            assertTrue(latch2.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testRxRegisterTwice() {
        final CountDownLatch firstLatch = new CountDownLatch(1);
        assertTrue(mDealer.resolveRxMessage(mPeer1, WampMessageFactory.createRegister(
                WampRequestIdGenerator.getId(), new JSONObject(), PROCEDURE1),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        if (reply.isRegisteredMessage()) {
                            firstLatch.countDown();
                            return;
                        }
                        fail();
                    }
                }));
        try {
            assertTrue(firstLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        final CountDownLatch secondLatch = new CountDownLatch(1);
        assertTrue(mDealer.resolveRxMessage(mPeer1, WampMessageFactory.createRegister(
                WampRequestIdGenerator.getId(), new JSONObject(), PROCEDURE1),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        if (!reply.isErrorMessage()) {
                            fail();
                        }
                        assertEquals(WampError.PROCEDURE_ALREADY_EXISTS, reply.asErrorMessage()
                                .getUri());
                        secondLatch.countDown();
                    }
                }));
        try {
            assertTrue(secondLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testRxUnregister() {
        final CountDownLatch regLatch = new CountDownLatch(1);
        final AtomicInteger registrationId = new AtomicInteger(-1);

        assertTrue(mDealer.resolveRxMessage(mPeer1, WampMessageFactory.createRegister(
                WampRequestIdGenerator.getId(), new JSONObject(), PROCEDURE1),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        if (reply.isRegisteredMessage()) {
                            registrationId.set(reply.asRegisteredMessage().getRegistrationId());
                            assertTrue(registrationId.get() > 0);
                            regLatch.countDown();
                            return;
                        }
                        fail();
                    }
                }));
        try {
            assertTrue(regLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        final CountDownLatch unregLatch = new CountDownLatch(1);
        assertTrue(mDealer
                .resolveRxMessage(mPeer1, WampMessageFactory.createUnregister(
                        WampRequestIdGenerator.getId(), registrationId.get()),
                        new OnReplyListener() {
                            @Override
                            public void onReply(WampPeer receiver, WampMessage reply) {
                                if (reply.isUnregisteredMessage()) {
                                    unregLatch.countDown();
                                    return;
                                }
                                fail();
                            }
                        }));
        try {
            assertTrue(unregLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testRxCall() {
        final CountDownLatch latch = new CountDownLatch(1);
        assertTrue(mDealer.resolveRxMessage(mPeer1, WampMessageFactory.createRegister(
                WampRequestIdGenerator.getId(), new JSONObject(), PROCEDURE1),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        if (reply.isRegisteredMessage()) {
                            latch.countDown();
                            return;
                        }
                        fail();
                    }
                }));
        try {
            assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        final WampMessage[] msgs = {
                WampMessageFactory.createCall(WampRequestIdGenerator.getId(), new JSONObject(),
                        PROCEDURE1),
                WampMessageFactory.createCall(WampRequestIdGenerator.getId(), new JSONObject(),
                        PROCEDURE1, new JSONArray()),
                WampMessageFactory.createCall(WampRequestIdGenerator.getId(), new JSONObject(),
                        PROCEDURE1, new JSONArray(), new JSONObject()),
        };

        for (WampMessage msg : msgs) {
            final CountDownLatch callLatch = new CountDownLatch(1);
            assertTrue(mDealer.resolveRxMessage(mPeer2, msg, new OnReplyListener() {
                @Override
                public void onReply(WampPeer receiver, WampMessage reply) {
                    if (reply.isInvocationMessage()) {
                        assertEquals(receiver, mPeer1);
                        callLatch.countDown();
                        return;
                    }
                    fail();
                }
            }));
            try {
                assertTrue(callLatch.await(1, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                fail();
            }
        }
    }

    public void testRxYield() {
        final CountDownLatch latch = new CountDownLatch(1);
        assertTrue(mDealer.resolveRxMessage(mPeer1, WampMessageFactory.createRegister(
                WampRequestIdGenerator.getId(), new JSONObject(), PROCEDURE1),
                new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        if (reply.isRegisteredMessage()) {
                            assertEquals(mPeer1, receiver);
                            latch.countDown();
                            return;
                        }
                        fail();
                    }
                }));
        try {
            assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        final CountDownLatch callLatch = new CountDownLatch(1);
        final AtomicInteger requestId = new AtomicInteger(-1);
        assertTrue(mDealer.resolveRxMessage(mPeer2,
                WampMessageFactory.createCall(WampRequestIdGenerator.getId(), new JSONObject(),
                        PROCEDURE1), new OnReplyListener() {
                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        if (reply.isInvocationMessage()) {
                            assertEquals(mPeer1, receiver);
                            requestId.set(reply.asInvocationMessage().getRequestId());
                            assertTrue(requestId.get() > 0);
                            callLatch.countDown();
                            return;
                        }
                        fail();
                    }
                }));
        try {
            assertTrue(callLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        final CountDownLatch yieldLatch = new CountDownLatch(1);
        assertTrue(mDealer.resolveRxMessage(mPeer1,
                WampMessageFactory.createYield(requestId.get(), new JSONObject()),
                new OnReplyListener() {

                    @Override
                    public void onReply(WampPeer receiver, WampMessage reply) {
                        if (reply.isResultMessage()) {
                            assertEquals(receiver, mPeer2);
                            yieldLatch.countDown();
                            return;
                        }
                        fail();
                    }
                }));
        try {
            assertTrue(yieldLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }
}
