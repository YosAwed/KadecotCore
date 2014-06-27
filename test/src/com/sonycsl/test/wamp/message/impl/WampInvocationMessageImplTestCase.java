
package com.sonycsl.test.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.impl.WampInvocationMessageImpl;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

public class WampInvocationMessageImplTestCase extends TestCase {
    public void testCtor() {
        JSONArray msg = new JSONArray();
        msg.put(WampMessageType.INVOCATION);
        int requestId = 1;
        msg.put(requestId);
        int registrationId = 2;
        msg.put(registrationId);
        JSONObject details = new JSONObject();
        msg.put(details);
        JSONArray arguments = new JSONArray();
        msg.put(arguments);
        JSONObject argumentsKw = new JSONObject();
        msg.put(argumentsKw);

        WampInvocationMessageImpl invocation = new WampInvocationMessageImpl(msg);

        assertNotNull(invocation);
        assertTrue(invocation.isInvocationMessage());
        assertTrue(invocation.getRequestId() == requestId);
        assertTrue(invocation.getDetails() == details);
        assertTrue(invocation.getRegistrationId() == registrationId);
        assertTrue(invocation.hasArguments());
        assertTrue(invocation.getArguments() == arguments);
        assertTrue(invocation.hasArgumentsKw());
        assertTrue(invocation.getArgumentsKw() == argumentsKw);
    }

    public void testAbnormal() {
        JSONArray msg = new JSONArray();
        WampInvocationMessageImpl invocation = new WampInvocationMessageImpl(msg);

        // no requestId
        try {
            invocation.getRequestId();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no registrationId
        try {
            invocation.getRegistrationId();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no details
        try {
            invocation.getDetails();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no arguments
        try {
            assertFalse(invocation.hasArguments());
            invocation.getArguments();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no argumentsKw
        try {
            assertFalse(invocation.hasArgumentsKw());
            invocation.getArgumentsKw();
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIllegalMessageType() {
        JSONArray msg = new JSONArray();
        msg.put(-1);
        try {
            new WampInvocationMessageImpl(msg);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
