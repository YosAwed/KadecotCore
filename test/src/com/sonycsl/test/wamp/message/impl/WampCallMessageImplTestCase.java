
package com.sonycsl.test.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.impl.WampCallMessageImpl;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

public class WampCallMessageImplTestCase extends TestCase {
    public void testCtor() {
        JSONArray msg = new JSONArray();
        msg.put(WampMessageType.CALL);
        int requestId = 1;
        msg.put(requestId);
        JSONObject options = new JSONObject();
        msg.put(options);
        String procedure = "procedure.test";
        msg.put(procedure);
        JSONArray arguments = new JSONArray();
        msg.put(arguments);
        JSONObject argumentsKw = new JSONObject();
        msg.put(argumentsKw);

        WampCallMessageImpl call = new WampCallMessageImpl(msg);

        assertNotNull(call);
        assertTrue(call.isCallMessage());
        assertTrue(call.getRequestId() == requestId);
        assertTrue(call.getOptions() == options);
        assertTrue(call.getProcedure().equals(procedure));
        assertTrue(call.hasArguments());
        assertTrue(call.getArguments() == arguments);
        assertTrue(call.hasArgumentsKw());
        assertTrue(call.getArgumentsKw() == argumentsKw);
    }

    public void testAbnormal() {
        JSONArray msg = new JSONArray();
        WampCallMessageImpl call = new WampCallMessageImpl(msg);

        // no requestId
        try {
            int requestId = call.getRequestId();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no options
        try {
            JSONObject options = call.getOptions();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no procedure
        try {
            String procedure = call.getProcedure();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no arguments
        try {
            assertFalse(call.hasArguments());
            JSONArray arguments = call.getArguments();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no argumentsKw
        try {
            assertFalse(call.hasArgumentsKw());
            JSONObject argumentsKw = call.getArgumentsKw();
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIllegalMessageType() {
        JSONArray msg = new JSONArray();
        msg.put(-1);
        try {
            WampCallMessageImpl call = new WampCallMessageImpl(msg);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
