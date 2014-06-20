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
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.role.WampCaller;
import com.sonycsl.wamp.role.WampRole.OnReplyListener;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class WampCallerTestCase extends TestCase {

    private static class TestWampCaller extends WampCaller {
    }

    private static final String PROCEDURE = "wamp.caller.test.case.procedure";

    private TestWampCaller mCaller;
    private MockWampPeer mPeer;

    @Override
    protected void setUp() throws Exception {
        mCaller = new TestWampCaller();
        mPeer = new MockWampPeer();
    }

    public void testCtor() {
        assertNotNull(mCaller);
        assertNotNull(mPeer);
    }

    public void testGetRoleName() {
        assertTrue(mCaller.getRoleName().equals("caller"));
    }

    public void testTxCall() {
        WampMessage[] msgs = {
                WampMessageFactory.createCall(WampRequestIdGenerator.getId(), new JSONObject(),
                        PROCEDURE),
                WampMessageFactory.createCall(WampRequestIdGenerator.getId(), new JSONObject(),
                        PROCEDURE, new JSONArray()),
                WampMessageFactory.createCall(WampRequestIdGenerator.getId(), new JSONObject(),
                        PROCEDURE, new JSONArray(), new JSONObject())
        };
        for (WampMessage msg : msgs) {
            assertTrue(mCaller.resolveTxMessage(mPeer, msg));
        }
    }

    public void testRxResult() {
        WampMessage[] msgs = {
                WampMessageFactory.createResult(WampRequestIdGenerator.getId(), new JSONObject()),
                WampMessageFactory.createResult(WampRequestIdGenerator.getId(), new JSONObject(),
                        new JSONArray()),
                WampMessageFactory.createResult(WampRequestIdGenerator.getId(), new JSONObject(),
                        new JSONArray(), new JSONObject())
        };
        for (WampMessage msg : msgs) {
            assertTrue(mCaller.resolveRxMessage(mPeer, msg, new OnReplyListener() {
                @Override
                public void onReply(WampPeer receiver, WampMessage reply) {
                    fail();
                }
            }));
        }
    }

    public void testRxCallError() {
        assertTrue(mCaller.resolveRxMessage(mPeer, WampMessageFactory.createError(
                WampMessageType.CALL, WampRequestIdGenerator.getId(), new JSONObject(),
                WampError.NO_SUCH_PROCEDURE), new OnReplyListener() {

            @Override
            public void onReply(WampPeer receiver, WampMessage reply) {
                fail();
            }
        }));

        assertFalse(mCaller.resolveRxMessage(mPeer, WampMessageFactory.createError(
                WampMessageType.INVOCATION, WampRequestIdGenerator.getId(), new JSONObject(),
                WampError.NO_SUCH_PROCEDURE), new OnReplyListener() {

            @Override
            public void onReply(WampPeer receiver, WampMessage reply) {
                fail();
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
        uncheckRx.add(WampMessageType.RESULT);

        WampRoleTestUtil.rxMessageOutOfRole(mCaller, mPeer, uncheckRx);

        Set<Integer> uncheckTx = new HashSet<Integer>();
        uncheckTx.add(WampMessageType.HELLO);
        uncheckTx.add(WampMessageType.GOODBYE);
        uncheckTx.add(WampMessageType.CALL);

        WampRoleTestUtil.txMessageOutOfRole(mCaller, mPeer, uncheckTx);
    }
}
