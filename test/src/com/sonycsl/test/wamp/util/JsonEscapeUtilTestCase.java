
package com.sonycsl.test.wamp.util;

import com.sonycsl.wamp.util.JsonEscapeUtil;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonEscapeUtilTestCase extends TestCase {
    public void testEscapeSlash() {
        JSONObject json = new JSONObject();
        try {
            json.put("key", "value");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String str = JsonEscapeUtil.escapeSlash(json);
        // expect : "{\"key\":\"value\"}"
        assertTrue(str.equals("\"{\\\"key\\\":\\\"value\\\"}\""));
    }
}
