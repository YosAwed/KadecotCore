
package com.sonycsl.test.wamp;

import com.sonycsl.wamp.WampLog;

import junit.framework.TestCase;

public class WampLogTestCase extends TestCase {
    private static final String TAG = "WampLogTestCase";

    public void testCtor() {
    }

    public void testE() {
        WampLog.e(TAG, "WampLog error test");
    }

    public void testD() {
        WampLog.d(TAG, "WampLog debug test");
    }

    public void testNullTag() {
        WampLog.d(null, "test null tag");
    }

    public void testNullMsg() {
        WampLog.d(TAG, null);
    }
}
