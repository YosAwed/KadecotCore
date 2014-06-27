
package com.sonycsl.test.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.impl.WampWelcomeMessageImpl;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WampWelcomeMessageImplTestCase extends TestCase {
    private static int SESSION_ID = 1;

    public void testCtor() {
        try {
            JSONArray msg = new JSONArray();
            msg.put(WampMessageType.WELCOME);
            msg.put(SESSION_ID);
            JSONObject role = new JSONObject("{\"roles\":{\"dealer\":{}}}");
            msg.put(role);

            WampWelcomeMessageImpl welcome = new WampWelcomeMessageImpl(msg);

            assertNotNull(welcome);
            assertTrue(welcome.isWelcomeMessage());
            assertTrue(welcome.getSession() == SESSION_ID);
            assertTrue(welcome.getDetails() == role);
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testAbnormal() {
        JSONArray msg = new JSONArray();
        WampWelcomeMessageImpl welcome = new WampWelcomeMessageImpl(msg);

        // no session id
        try {
            welcome.getSession();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no details
        try {
            welcome.getDetails();
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIllegalMessageType() {
        JSONArray msg = new JSONArray();
        msg.put(-1);
        try {
            new WampWelcomeMessageImpl(msg);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
