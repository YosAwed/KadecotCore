
package com.sonycsl.test.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.impl.WampUnsubscribedMessageImpl;

import junit.framework.TestCase;

import org.json.JSONArray;

public class WampUnsubscribedMessageImplTestCase extends TestCase {
    public void testCtor() {
        JSONArray msg = new JSONArray();
        msg.put(WampMessageType.UNSUBSCRIBED);
        int requestId = 1;
        msg.put(requestId);
        int subscriptionId = 2;
        msg.put(subscriptionId);

        WampUnsubscribedMessageImpl unsubscribed = new WampUnsubscribedMessageImpl(msg);

        assertNotNull(unsubscribed);
        assertTrue(unsubscribed.isUnsubscribedMessage());
        assertTrue(unsubscribed.getRequestId() == requestId);
    }

    public void testAbnormal() {
        JSONArray msg = new JSONArray();
        WampUnsubscribedMessageImpl unsubscribed = new WampUnsubscribedMessageImpl(msg);

        // no requestId
        try {
            unsubscribed.getRequestId();
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIllegalMessageType() {
        JSONArray msg = new JSONArray();
        msg.put(-1);
        try {
            new WampUnsubscribedMessageImpl(msg);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
