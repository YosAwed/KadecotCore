
package com.sonycsl.test.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.impl.WampPublishedMessageImpl;

import junit.framework.TestCase;

import org.json.JSONArray;

public class WampPublishedMessageImplTestCase extends TestCase {
    public void testCtor() {
        JSONArray msg = new JSONArray();
        msg.put(WampMessageType.PUBLISHED);
        int requestId = 1;
        msg.put(requestId);
        int publicationId = 2;
        msg.put(publicationId);

        WampPublishedMessageImpl published = new WampPublishedMessageImpl(msg);

        assertNotNull(published);
        assertTrue(published.isPublishedMessage());
        assertTrue(published.getRequestId() == requestId);
        assertTrue(published.getPublicationId() == publicationId);
    }

    public void testAbnormal() {
        JSONArray msg = new JSONArray();
        WampPublishedMessageImpl published = new WampPublishedMessageImpl(msg);

        // no requestId
        try {
            int requestId = published.getRequestId();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no publicationId
        try {
            int publicationId = published.getPublicationId();
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
