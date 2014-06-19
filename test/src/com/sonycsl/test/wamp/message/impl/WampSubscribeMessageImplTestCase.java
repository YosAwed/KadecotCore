
package com.sonycsl.test.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.impl.WampSubscribeMessageImpl;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

public class WampSubscribeMessageImplTestCase extends TestCase {
    public void testCtor() {
        JSONArray msg = new JSONArray();
        msg.put(WampMessageType.SUBSCRIBE);
        int requestId = 1;
        msg.put(requestId);
        JSONObject options = new JSONObject();
        msg.put(options);
        String topic = "topic.test";
        msg.put(topic);

        WampSubscribeMessageImpl subscribe = new WampSubscribeMessageImpl(msg);

        assertNotNull(subscribe);
        assertTrue(subscribe.isSubscribeMessage());
        assertTrue(subscribe.getRequestId() == requestId);
        assertTrue(subscribe.getOptions() == options);
    }

    public void testAbnormal() {
        JSONArray msg = new JSONArray();
        WampSubscribeMessageImpl subscribe = new WampSubscribeMessageImpl(msg);

        // no requestId
        try {
            int requestId = subscribe.getRequestId();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no options
        try {
            JSONObject options = subscribe.getOptions();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no topic
        try {
            String topic = subscribe.getTopic();
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIllegalMessageType() {
        JSONArray msg = new JSONArray();
        msg.put(-1);
        try {
            WampSubscribeMessageImpl subscribe = new WampSubscribeMessageImpl(msg);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
