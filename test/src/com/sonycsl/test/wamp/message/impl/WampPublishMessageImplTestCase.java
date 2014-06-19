
package com.sonycsl.test.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.impl.WampAbortMessageImpl;
import com.sonycsl.wamp.message.impl.WampPublishMessageImpl;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

public class WampPublishMessageImplTestCase extends TestCase {
    public void testCtor() {
        JSONArray msg = new JSONArray();
        msg.put(WampMessageType.PUBLISH);
        int requestId = 1;
        msg.put(requestId);
        JSONObject options = new JSONObject();
        msg.put(options);
        String topic = "topic.test";
        msg.put(topic);
        JSONArray arguments = new JSONArray();
        msg.put(arguments);
        JSONObject argumentsKw = new JSONObject();
        msg.put(argumentsKw);

        WampPublishMessageImpl publish = new WampPublishMessageImpl(msg);

        assertNotNull(publish);
        assertTrue(publish.isPublishMessage());
        assertTrue(publish.getRequestId() == requestId);
        assertTrue(publish.getOptions() == options);
        assertTrue(publish.getTopic().equals(topic));
        assertTrue(publish.hasArguments());
        assertTrue(publish.getArguments() == arguments);
        assertTrue(publish.hasArgumentsKw());
        assertTrue(publish.getArgumentsKw() == argumentsKw);
    }

    public void testAbnormal() {
        JSONArray msg = new JSONArray();
        WampPublishMessageImpl publish = new WampPublishMessageImpl(msg);

        // no requestId
        try {
            int requestId = publish.getRequestId();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no options
        try {
            JSONObject options = publish.getOptions();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no topic
        try {
            String topic = publish.getTopic();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no arguments
        try {
            assertFalse(publish.hasArguments());
            JSONArray arguments = publish.getArguments();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no argumentsKw
        try {
            assertFalse(publish.hasArgumentsKw());
            JSONObject argumentsKw = publish.getArgumentsKw();
            fail();
        } catch (IllegalArgumentException e) {
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
