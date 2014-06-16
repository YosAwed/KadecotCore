/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.wamp.role;

import com.sonycsl.test.mock.MockWampPeer;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampPublisher;
import com.sonycsl.wamp.role.WampRole.OnReplyListener;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

public class WampPublisherTestCase extends TestCase {

    private static final class TestWampPublisher extends WampPublisher {
    }

    private static final String TOPIC = "com.myapp.mytopic1";
    private TestWampPublisher mPublisher;
    private MockWampPeer mPeer;

    @Override
    protected void setUp() throws Exception {
        mPublisher = new TestWampPublisher();
        mPeer = new MockWampPeer();
    }

    public void testCtor() {
        assertNotNull(mPublisher);
    }

    public void testGetRoleName() {
        assertTrue(mPublisher.getRoleName().equals("publisher"));
    }

    public void testTxPublish() {
        WampMessage[] msgs = {
                WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                        TOPIC),
                WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                        TOPIC, new JSONArray()),
                WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                        TOPIC, new JSONArray(), new JSONObject())
        };

        for (WampMessage msg : msgs) {
            assertTrue(mPublisher.resolveTxMessage(mPeer, msg));
        }
    }

    public void testRxPublished() {
        WampMessage[] msgs = {
                WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                        TOPIC),
                WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                        TOPIC, new JSONArray()),
                WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                        TOPIC, new JSONArray(), new JSONObject())
        };

        for (WampMessage msg : msgs) {
            assertFalse(mPublisher.resolveRxMessage(mPeer,
                    WampMessageFactory.createPublished(msg.asPublishMessage().getRequestId(),
                            WampRequestIdGenerator.getId()),
                    new OnReplyListener() {
                        @Override
                        public void onReply(WampPeer receiver, WampMessage reply) {
                            fail();
                        }
                    }));
            assertTrue(mPublisher.resolveTxMessage(mPeer, msg));
            assertTrue(mPublisher.resolveRxMessage(mPeer,
                    WampMessageFactory.createPublished(msg.asPublishMessage().getRequestId(),
                            WampRequestIdGenerator.getId()),
                    new OnReplyListener() {
                        @Override
                        public void onReply(WampPeer receiver, WampMessage reply) {
                            fail();
                        }
                    }));
        }

    }
}
