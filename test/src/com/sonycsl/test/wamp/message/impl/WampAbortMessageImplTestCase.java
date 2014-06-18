
package com.sonycsl.test.wamp.message.impl;

import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.impl.WampAbortMessageImpl;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

public class WampAbortMessageImplTestCase extends TestCase {
    public void testCtor() {
        JSONArray msg = new JSONArray();
        msg.put(WampMessageType.ABORT);
        JSONObject details = new JSONObject();
        msg.put(details);
        String reason = WampError.NO_SUCH_REALM;
        msg.put(reason);

        WampAbortMessageImpl abort = new WampAbortMessageImpl(msg);

        assertNotNull(abort);
        assertTrue(abort.isAbortMessage());
        assertTrue(abort.getDetails() == details);
        assertTrue(abort.getReason().equals(reason));
    }

    public void testAbnormal() {
        JSONArray msg = new JSONArray();
        WampAbortMessageImpl abort = new WampAbortMessageImpl(msg);

        // no details
        try {
            JSONObject details = abort.getDetails();
            fail();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        // no reason
        try {
            String reason = abort.getReason();
            fail();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
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
