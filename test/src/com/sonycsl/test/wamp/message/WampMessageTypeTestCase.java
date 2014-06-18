
package com.sonycsl.test.wamp.message;

import com.sonycsl.wamp.message.WampMessageType;

import junit.framework.TestCase;

public class WampMessageTypeTestCase extends TestCase {
    public void testCtor() {
        WampMessageType type = new WampMessageType();
        assertNotNull(type);
    }

    public void testMessageCode() {
        assertTrue(WampMessageType.HELLO == 1);
        assertTrue(WampMessageType.WELCOME == 2);
        assertTrue(WampMessageType.ABORT == 3);
        assertTrue(WampMessageType.CHALLENGE == 4);
        assertTrue(WampMessageType.AUTHENTICATE == 5);
        assertTrue(WampMessageType.GOODBYE == 6);
        assertTrue(WampMessageType.HEARTBEAT == 7);
        assertTrue(WampMessageType.ERROR == 8);

        assertTrue(WampMessageType.PUBLISH == 16);
        assertTrue(WampMessageType.PUBLISHED == 17);

        assertTrue(WampMessageType.SUBSCRIBE == 32);
        assertTrue(WampMessageType.SUBSCRIBED == 33);
        assertTrue(WampMessageType.UNSUBSCRIBE == 34);
        assertTrue(WampMessageType.UNSUBSCRIBED == 35);
        assertTrue(WampMessageType.EVENT == 36);

        assertTrue(WampMessageType.CALL == 48);
        assertTrue(WampMessageType.CANCEL == 49);
        assertTrue(WampMessageType.RESULT == 50);

        assertTrue(WampMessageType.REGISTER == 64);
        assertTrue(WampMessageType.REGISTERED == 65);
        assertTrue(WampMessageType.UNREGISTER == 66);
        assertTrue(WampMessageType.UNREGISTERED == 67);
        assertTrue(WampMessageType.INVOCATION == 68);
        assertTrue(WampMessageType.INTERRUPT == 69);
        assertTrue(WampMessageType.YIELD == 70);
    }
}
