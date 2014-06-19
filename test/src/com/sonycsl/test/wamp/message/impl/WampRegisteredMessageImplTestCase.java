
package com.sonycsl.test.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.impl.WampRegisteredMessageImpl;

import junit.framework.TestCase;

import org.json.JSONArray;

public class WampRegisteredMessageImplTestCase extends TestCase {
    public void testCtor() {
        JSONArray msg = new JSONArray();
        msg.put(WampMessageType.REGISTERED);
        int requestId = 1;
        msg.put(requestId);
        int registrationId = 2;
        msg.put(registrationId);

        WampRegisteredMessageImpl registered = new WampRegisteredMessageImpl(msg);

        assertNotNull(registered);
        assertTrue(registered.isRegisteredMessage());
        assertTrue(registered.getRequestId() == requestId);
        assertTrue(registered.getRegistrationId() == registrationId);
    }

    public void testAbnormal() {
        JSONArray msg = new JSONArray();
        WampRegisteredMessageImpl registered = new WampRegisteredMessageImpl(msg);

        // no requestId
        try {
            int requestId = registered.getRequestId();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no registrationId
        try {
            int registraionId = registered.getRegistrationId();
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIllegalMessageType() {
        JSONArray msg = new JSONArray();
        msg.put(-1);
        try {
            WampRegisteredMessageImpl registered = new WampRegisteredMessageImpl(msg);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
