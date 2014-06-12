/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.wamp;

import com.sonycsl.test.mock.MockWampClient;
import com.sonycsl.test.util.TestableCallback;
import com.sonycsl.test.util.WampTestUtil;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.WampRouter;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.role.WampRole;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class WampRouterSessionTestCase extends TestCase {

    private WampRouter mRouter;
    private MockWampClient mClient;

    @Override
    protected void setUp() {
        mRouter = new WampRouter() {
            @Override
            protected void onTransmitted(WampPeer peer, WampMessage msg) {
            }

            @Override
            protected void onReceived(WampMessage msg) {
            }

            @Override
            protected void onConnected(WampPeer peer) {
            }

            @Override
            protected Set<WampRole> getRouterRoleSet() {
                Set<WampRole> roles = new HashSet<WampRole>();
                WampRole role = new WampRole() {

                    @Override
                    protected boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg) {
                        return false;
                    }

                    @Override
                    protected boolean resolveRxMessageImpl(WampPeer transmitter, WampMessage msg,
                            OnReplyListener listener) {
                        return false;
                    }

                    @Override
                    public String getRoleName() {
                        return "testRole";
                    }
                };
                roles.add(role);
                return roles;
            }
        };
        mRouter.setCallback(new TestableCallback());

        mClient = new MockWampClient();
        mClient.setCallback(new TestableCallback());

        mRouter.connect(mClient);
    }

    public void testCtor() {
        assertNotNull(mRouter);
        assertNotNull(mClient);
    }

    public void testHello() {
        WampTestUtil.transmitHelloSuccess(mClient, WampTestParam.REALM, mRouter);
    }

    public void testGoodbye() {
        WampTestUtil.transmitHelloSuccess(mClient, WampTestParam.REALM, mRouter);
        WampTestUtil.transmitGoodbyeSuccess(mClient, WampError.CLOSE_REALM, mRouter);

        WampTestUtil.transmitHelloSuccess(mClient, WampTestParam.REALM, mRouter);
        WampTestUtil.transmitGoodbyeSuccess(mRouter, WampError.CLOSE_REALM, mClient);

        WampTestUtil.transmitHelloSuccess(mClient, WampTestParam.REALM, mRouter);
        WampTestUtil.transmitGoodbyeSuccess(mRouter, WampError.SYSTEM_SHUTDOWN, mClient);
    }

    public void testRouterCapability() {
        WampMessage welcomeMsg = WampTestUtil.transmitHello(mClient, WampTestParam.REALM, mRouter);
        assertEquals(WampMessageType.WELCOME, welcomeMsg.getMessageType());
        try {
            JSONObject roles = welcomeMsg.asWelcomeMessage().getDetails().getJSONObject("roles");
            roles.get("testRole");
        } catch (JSONException e) {
            fail(welcomeMsg.toString());
        }
    }
}
