/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.test.plugin;

import com.sonycsl.Kadecot.plugin.KadecotProtocolCallee;
import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampInvocationMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.mock.MockWampClient;
import com.sonycsl.wamp.role.WampRole.OnReplyListener;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotProtocolCalleeTestCase extends TestCase {

    private static final String PROCEDURE = "procedure";
    private TestKadecotProtocolCallee mCallee;
    private MockWampClient mClient;

    private static class TestKadecotProtocolCallee extends KadecotProtocolCallee {
        public TestKadecotProtocolCallee(Set<String> procedureSet) {
            super(procedureSet);
        }

        private CountDownLatch mLatch;

        public void setLatch(CountDownLatch latch) {
            mLatch = latch;
        }

        @Override
        protected WampMessage resolveInvocationMsg(String procedure,
                WampInvocationMessage invocMsg) {
            if (mLatch != null) {
                mLatch.countDown();
            }

            return WampMessageFactory.createYield(1, new JSONObject());
        }

        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            if (mLatch != null) {
                return mLatch.await(timeout, unit);
            }
            return false;
        }
    }

    @Override
    protected void setUp() throws Exception {
        Set<String> procedureSet = new HashSet<String>();
        procedureSet.add(PROCEDURE);
        mCallee = new TestKadecotProtocolCallee(procedureSet);
        mClient = new MockWampClient();
    }

    public void testCtor() {
        assertNotNull(mCallee);
    }

    public void testInvocation() {
        final CountDownLatch latch = new CountDownLatch(1);
        mCallee.setLatch(latch);

        int registrationId = 100;
        int requestId = 1;

        assertTrue(mCallee.resolveTxMessage(mClient,
                WampMessageFactory.createRegister(requestId, new JSONObject(), PROCEDURE)));
        assertTrue(mCallee.resolveRxMessage(mClient,
                WampMessageFactory.createRegistered(requestId++, registrationId),
                null));

        try {
            mCallee.resolveRxMessage(mClient,
                    WampMessageFactory.createInvocation(requestId++, registrationId,
                            new JSONObject().put(
                                    KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID, 1L)),
                    new OnReplyListener() {

                        @Override
                        public void onReply(WampPeer receiver, WampMessage reply) {
                            if (reply.isErrorMessage()) {
                                fail();
                            }
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        }

        try {
            assertTrue(mCallee.await(1, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }
}
