
package com.sonycsl.test.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.impl.WampEventMessageImpl;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

public class WampEventMessageImplTestCase extends TestCase {
    public void testCtor() {
        JSONArray msg = new JSONArray();
        msg.put(WampMessageType.EVENT);
        int subscriptionId = 1;
        msg.put(subscriptionId);
        int publicationId = 2;
        msg.put(publicationId);
        JSONObject details = new JSONObject();
        msg.put(details);
        JSONArray arguments = new JSONArray();
        msg.put(arguments);
        JSONObject argumentsKw = new JSONObject();
        msg.put(argumentsKw);

        WampEventMessageImpl event = new WampEventMessageImpl(msg);

        assertNotNull(event);
        assertTrue(event.isEventMessage());
        assertTrue(event.getSubscriptionId() == subscriptionId);
        assertTrue(event.getPublicationId() == publicationId);
        assertTrue(event.getDetails() == details);
        assertTrue(event.hasArguments());
        assertTrue(event.getArguments() == arguments);
        assertTrue(event.hasArgumentsKw());
        assertTrue(event.getArgumentsKw() == argumentsKw);
    }

    public void testAbnormal() {
        JSONArray msg = new JSONArray();
        WampEventMessageImpl event = new WampEventMessageImpl(msg);

        // no subscriptionId
        try {
            int subscriptionId = event.getSubscriptionId();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no publicationId
        try {
            int publicationId = event.getPublicationId();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no details
        try {
            JSONObject details = event.getDetails();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no arguments
        try {
            assertFalse(event.hasArguments());
            JSONArray arguments = event.getArguments();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no argumentsKw
        try {
            assertFalse(event.hasArgumentsKw());
            JSONObject argumentsKw = event.getArgumentsKw();
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIllegalMessageType() {
        JSONArray msg = new JSONArray();
        msg.put(-1);
        try {
            WampEventMessageImpl event = new WampEventMessageImpl(msg);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
