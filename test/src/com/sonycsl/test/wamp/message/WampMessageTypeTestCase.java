
package com.sonycsl.test.wamp.message;

import com.sonycsl.wamp.message.WampMessageType;

import junit.framework.TestCase;

public class WampMessageTypeTestCase extends TestCase {
    public void testCtor() {
        WampMessageType type = new WampMessageType();
        assertNotNull(type);
    }
}
