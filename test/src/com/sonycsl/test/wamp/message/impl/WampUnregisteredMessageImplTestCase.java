
package com.sonycsl.test.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.impl.WampUnregisteredMessageImpl;

import junit.framework.TestCase;

import org.json.JSONArray;

public class WampUnregisteredMessageImplTestCase extends TestCase {
    public void testCtor() {
        JSONArray msg = new JSONArray();
        msg.put(WampMessageType.UNREGISTERED);
        int requestId = 1;
        msg.put(requestId);

        WampUnregisteredMessageImpl unregistered = new WampUnregisteredMessageImpl(msg);

        assertNotNull(unregistered);
        assertTrue(unregistered.isUnregisteredMessage());
        assertTrue(unregistered.getRequestId() == requestId);
    }

    public void testAbnormal() {
        JSONArray msg = new JSONArray();
        WampUnregisteredMessageImpl unregistered = new WampUnregisteredMessageImpl(msg);

        // no requestId
        try {
            unregistered.getRequestId();
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIllegalMessageType() {
        JSONArray msg = new JSONArray();
        msg.put(-1);
        try {
            new WampUnregisteredMessageImpl(msg);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
