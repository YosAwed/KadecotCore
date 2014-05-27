/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.wamp;

import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class WampTestUtil {

    public static WampMessage transmitMessage(WampPeer requester, WampMessage request,
            WampPeer responder, int responseType) {

        TestableCallback reqCallback = (TestableCallback) requester.getCallback();
        TestableCallback resCallback = (TestableCallback) responder.getCallback();
        reqCallback.setTargetMessageType(responseType, new CountDownLatch(1));
        resCallback.setTargetMessageType(request.getMessageType(), new CountDownLatch(1));

        requester.transmit(request);
        try {
            TestCase.assertTrue(resCallback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }
        TestCase.assertEquals(request, resCallback.getTargetMessage());

        try {
            TestCase.assertTrue(reqCallback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        return reqCallback.getTargetMessage();
    }

    public static WampMessage transmitHello(WampPeer requester, String realm, WampPeer responder) {
        return transmitMessage(requester, WampMessageFactory.createHello(realm, new JSONObject()),
                responder, WampMessageType.WELCOME);
    }

    public static void transmitHelloSuccess(WampPeer requester, String realm, WampPeer responder) {
        WampMessage msg = transmitHello(requester, realm, responder);
        TestCase.assertTrue(msg.toString(), msg.isWelcomeMessage());
    }

    public static WampMessage transmitGoodbye(WampPeer requester, String reason, WampPeer responder) {
        return transmitMessage(requester,
                WampMessageFactory.createGoodbye(new JSONObject(), reason), responder,
                WampMessageType.GOODBYE);

    }

    public static void transmitGoodbyeSuccess(WampPeer requester, String reason, WampPeer responder) {
        WampMessage reply = transmitGoodbye(requester, reason, responder);
        TestCase.assertTrue(reply.toString(), reply.isGoodbyeMessage());
        TestCase.assertEquals(WampError.GOODBYE_AND_OUT, reply.asGoodbyeMessage().getReason());
    }

}
