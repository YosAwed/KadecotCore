
package com.sonycsl.test.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.impl.WampResultMessageImpl;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

public class WampResultMessageImplTestCase extends TestCase {
    public void testCtor() {
        JSONArray msg = new JSONArray();
        msg.put(WampMessageType.RESULT);
        int requestId = 1;
        msg.put(requestId);
        JSONObject details = new JSONObject();
        msg.put(details);
        JSONArray arguments = new JSONArray();
        msg.put(arguments);
        JSONObject argumentsKw = new JSONObject();
        msg.put(argumentsKw);

        WampResultMessageImpl result = new WampResultMessageImpl(msg);

        assertNotNull(result);
        assertTrue(result.isResultMessage());
        assertTrue(result.getRequestId() == requestId);
        assertTrue(result.getDetails() == details);
        assertTrue(result.hasArguments());
        assertTrue(result.getArguments() == arguments);
        assertTrue(result.hasArgumentsKw());
        assertTrue(result.getArgumentsKw() == argumentsKw);
    }

    public void testAbnormal() {
        JSONArray msg = new JSONArray();
        WampResultMessageImpl result = new WampResultMessageImpl(msg);

        // no requestId
        try {
            int requestId = result.getRequestId();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no details
        try {
            JSONObject details = result.getDetails();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no arguments
        try {
            assertFalse(result.hasArguments());
            JSONArray arguments = result.getArguments();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no argumentsKw
        try {
            assertFalse(result.hasArgumentsKw());
            JSONObject argumentsKw = result.getArgumentsKw();
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIllegalMessageType() {
        JSONArray msg = new JSONArray();
        msg.put(-1);
        try {
            WampResultMessageImpl result = new WampResultMessageImpl(msg);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
