
package com.sonycsl.test.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.impl.WampUnregisterMessageImpl;

import junit.framework.TestCase;

import org.json.JSONArray;

public class WampUnregisterMessageImplTestCase extends TestCase {
    public void testCtor() {
        JSONArray msg = new JSONArray();
        msg.put(WampMessageType.UNREGISTER);
        int requestId = 1;
        msg.put(requestId);
        int registrationId = 2;
        msg.put(registrationId);

        WampUnregisterMessageImpl unregister = new WampUnregisterMessageImpl(msg);

        assertNotNull(unregister);
        assertTrue(unregister.isUnregisterMessage());
        assertTrue(unregister.getRequestId() == requestId);
        assertTrue(unregister.getRegistrationId() == registrationId);
    }

    public void testAbnormal() {
        JSONArray msg = new JSONArray();
        WampUnregisterMessageImpl unregister = new WampUnregisterMessageImpl(msg);

        // no requestId
        try {
            int requestId = unregister.getRequestId();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no registrationId
        try {
            int registraionId = unregister.getRegistrationId();
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIllegalMessageType() {
        JSONArray msg = new JSONArray();
        msg.put(-1);
        try {
            WampUnregisterMessageImpl unregister = new WampUnregisterMessageImpl(msg);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
