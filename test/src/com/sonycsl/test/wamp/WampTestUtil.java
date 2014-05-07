/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.wamp;

import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class WampTestUtil {

    public static WampMessage transmitMessage(Testable requester, WampMessage request,
            Testable responder) {
        requester.setCountDownLatch(new CountDownLatch(1));
        responder.setCountDownLatch(new CountDownLatch(1));

        requester.transmit(request);
        try {
            TestCase.assertTrue(responder.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }
        TestCase.assertEquals(request, responder.getLatestMessage());

        try {
            TestCase.assertTrue(requester.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }
        return requester.getLatestMessage();
    }

    public static WampMessage transmitHello(Testable requester, String realm, Testable responder) {
        return transmitMessage(requester,
                WampMessageFactory.createHello(realm, new JSONObject()), responder);
    }

    public static void transmitHelloSuccess(Testable requester, String realm, Testable responder) {
        TestCase.assertTrue(transmitHello(requester, realm, responder).isWelcomeMessage());
    }

    public static WampMessage transmitGoodbye(Testable requester, String reason, Testable responder) {
        return transmitMessage(requester,
                WampMessageFactory.createGoodbye(new JSONObject(), reason), responder);
    }

    public static void transmitGoodbyeSuccess(Testable requester, String reason, Testable responder) {
        WampMessage reply = transmitGoodbye(requester, reason, responder);
        TestCase.assertTrue(reply.isGoodbyeMessage());
        TestCase.assertEquals(WampError.GOODBYE_AND_OUT, reply.asGoodbyeMessage().getReason());
    }
}
