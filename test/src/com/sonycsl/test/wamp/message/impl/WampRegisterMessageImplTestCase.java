
package com.sonycsl.test.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.impl.WampRegisterMessageImpl;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

public class WampRegisterMessageImplTestCase extends TestCase {
    public void testCtor() {
        JSONArray msg = new JSONArray();
        msg.put(WampMessageType.REGISTER);
        int requestId = 1;
        msg.put(requestId);
        JSONObject optionss = new JSONObject();
        msg.put(optionss);
        String procedure = "procedure.test";
        msg.put(procedure);

        WampRegisterMessageImpl register = new WampRegisterMessageImpl(msg);

        assertNotNull(register);
        assertTrue(register.isRegisterMessage());
        assertTrue(register.getRequestId() == requestId);
        assertTrue(register.getOptions() == optionss);
        assertTrue(register.getProcedure().equals(procedure));
    }

    public void testAbnormal() {
        JSONArray msg = new JSONArray();
        WampRegisterMessageImpl register = new WampRegisterMessageImpl(msg);

        // no requestId
        try {
            int requestId = register.getRequestId();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no options
        try {
            JSONObject options = register.getOptions();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no procedure
        try {
            String procedure = register.getProcedure();
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIllegalMessageType() {
        JSONArray msg = new JSONArray();
        msg.put(-1);
        try {
            WampRegisterMessageImpl register = new WampRegisterMessageImpl(msg);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
