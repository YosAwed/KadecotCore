
package com.sonycsl.test.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.impl.WampAbortMessageImpl;
import com.sonycsl.wamp.message.impl.WampHelloMessageImpl;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WampHelloMessageImplTestCase extends TestCase {
    private static final String REALM = "realm";

    public void testCtor() {
        try {
            JSONArray msg = new JSONArray();
            msg.put(WampMessageType.HELLO);
            msg.put(REALM);
            JSONObject role = new JSONObject("{\"roles\":{\"caller\":{}}}");
            msg.put(role);
            WampHelloMessageImpl hello = new WampHelloMessageImpl(msg);

            assertNotNull(hello);
            assertTrue(hello.isHelloMessage());
            assertTrue(hello.getRealm().equals(REALM));
            assertTrue(hello.getDetails() == role);
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testAbnormal() {
        JSONArray msg = new JSONArray();
        msg.put(WampMessageType.HELLO);
        WampHelloMessageImpl hello = new WampHelloMessageImpl(msg);

        // no realm
        try {
            String realm = hello.getRealm();
            fail();
        } catch (IllegalArgumentException e) {
        }

        // no details
        try {
            JSONObject details = hello.getDetails();
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
