
package com.sonycsl.test.wamp.message.impl;

import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.impl.WampAbortMessageImpl;
import com.sonycsl.wamp.message.impl.WampGoodbyeMessageImpl;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

public class WampGoodbyeMessageImplTestCase extends TestCase {
    public void testCotr() {
        JSONArray msg = new JSONArray();
        msg.put(WampMessageType.GOODBYE);
        JSONObject details = new JSONObject();
        msg.put(details);
        String reason = WampError.GOODBYE_AND_OUT;
        msg.put(reason);

        WampGoodbyeMessageImpl goodbye = new WampGoodbyeMessageImpl(msg);

        assertNotNull(goodbye);
        assertTrue(goodbye.isGoodbyeMessage());
        assertTrue(goodbye.getDetails() == details);
        assertTrue(goodbye.getReason().equals(reason));
    }

    public void testAbnormal() {
        JSONArray msg = new JSONArray();
        WampGoodbyeMessageImpl goodbye = new WampGoodbyeMessageImpl(msg);

        // no details
        try {
            JSONObject details = goodbye.getDetails();
            fail();
        } catch (Exception e) {
        }

        // no reason
        try {
            String reason = goodbye.getReason();
            fail();
        } catch (Exception e) {
        }
    }

    public void testIllegalMessageType() {
        JSONArray msg = new JSONArray();
        msg.put(-1);
        try {
            WampAbortMessageImpl abort = new WampAbortMessageImpl(msg);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
