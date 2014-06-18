
package com.sonycsl.test.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.impl.WampUnsubscribeMessageImpl;

import junit.framework.TestCase;

import org.json.JSONArray;

public class WampUnsubscribeMessageImplTestCase extends TestCase {
    public void testCtor() {
        JSONArray msg = new JSONArray();
        msg.put(WampMessageType.PUBLISHED);
        int requestId = 1;
        msg.put(requestId);
        int subscriptionId = 2;
        msg.put(subscriptionId);

        WampUnsubscribeMessageImpl unsubscribe = new WampUnsubscribeMessageImpl(msg);

        assertNotNull(unsubscribe);
        assertTrue(unsubscribe.isUnsubscribeMessage());
        assertTrue(unsubscribe.getRequestId() == requestId);
        assertTrue(unsubscribe.getSubscriptionId() == subscriptionId);
    }

    public void testAbnormal() {
        JSONArray msg = new JSONArray();
        WampUnsubscribeMessageImpl unsubscribe = new WampUnsubscribeMessageImpl(msg);

        // no requestId
        try {
            int requestId = unsubscribe.getRequestId();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no subscriptionId
        try {
            int subscriptionId = unsubscribe.getSubscriptionId();
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
