
package com.sonycsl.test.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.impl.WampYieldMessageImpl;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

public class WampYieldMessageImplTestCase extends TestCase {
    public void testCtor() {
        JSONArray msg = new JSONArray();
        msg.put(WampMessageType.YIELD);
        int requestId = 1;
        msg.put(requestId);
        JSONObject options = new JSONObject();
        msg.put(options);
        JSONArray arguments = new JSONArray();
        msg.put(arguments);
        JSONObject argumentsKw = new JSONObject();
        msg.put(argumentsKw);

        WampYieldMessageImpl yield = new WampYieldMessageImpl(msg);

        assertNotNull(yield);
        assertTrue(yield.isYieldMessage());
        assertTrue(yield.getRequestId() == requestId);
        assertTrue(yield.getOptions() == options);
        assertTrue(yield.hasArguments());
        assertTrue(yield.getArguments() == arguments);
        assertTrue(yield.hasArgumentsKw());
        assertTrue(yield.getArgumentsKw() == argumentsKw);
    }

    public void testAbnormal() {
        JSONArray msg = new JSONArray();
        WampYieldMessageImpl yield = new WampYieldMessageImpl(msg);

        // no requestId
        try {
            yield.getRequestId();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no options
        try {
            yield.getOptions();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no arguments
        try {
            assertFalse(yield.hasArguments());
            yield.getArguments();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no argumentsKw
        try {
            assertFalse(yield.hasArgumentsKw());
            yield.getArgumentsKw();
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIllegalMessageType() {
        JSONArray msg = new JSONArray();
        msg.put(-1);
        try {
            new WampYieldMessageImpl(msg);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
