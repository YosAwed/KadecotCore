
package com.sonycsl.test.wamp.message.impl;

import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.impl.WampErrorMessageImpl;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

public class WampErrorMessageImplTestCase extends TestCase {
    public void testCtor() {
        JSONArray msg = new JSONArray();
        msg.put(WampMessageType.ERROR);
        int requestType = WampMessageType.CALL;
        msg.put(requestType);
        int requestId = 1;
        msg.put(requestId);
        JSONObject details = new JSONObject();
        msg.put(details);
        String error = WampError.NO_SUCH_PROCEDURE;
        msg.put(error);
        JSONArray arguments = new JSONArray();
        msg.put(arguments);
        JSONObject argumentsKw = new JSONObject();
        msg.put(argumentsKw);

        WampErrorMessageImpl errorMsg = new WampErrorMessageImpl(msg);

        assertNotNull(errorMsg);
        assertTrue(errorMsg.isErrorMessage());
        assertTrue(errorMsg.getRequestType() == requestType);
        assertTrue(errorMsg.getRequestId() == requestId);
        assertTrue(errorMsg.getDetails() == details);
        assertTrue(errorMsg.getUri().equals(error));
        assertTrue(errorMsg.hasArguments());
        assertTrue(errorMsg.getArguments() == arguments);
        assertTrue(errorMsg.hasArgumentsKw());
        assertTrue(errorMsg.getArgumentsKw() == argumentsKw);
    }

    public void testAbnormal() {
        JSONArray msg = new JSONArray();
        WampErrorMessageImpl error = new WampErrorMessageImpl(msg);

        // no requestType
        try {
            error.getRequestType();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no requestId
        try {
            error.getRequestId();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no details
        try {
            error.getDetails();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no uri
        try {
            error.getUri();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no arguments
        try {
            assertFalse(error.hasArguments());
            error.getArguments();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no argumentsKw
        try {
            assertFalse(error.hasArgumentsKw());
            error.getArgumentsKw();
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIllegalMessageType() {
        JSONArray msg = new JSONArray();
        msg.put(-1);
        try {
            new WampErrorMessageImpl(msg);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
