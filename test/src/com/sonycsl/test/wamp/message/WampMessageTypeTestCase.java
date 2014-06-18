
package com.sonycsl.test.wamp.message;

import com.sonycsl.wamp.message.WampMessageType;

import junit.framework.TestCase;

public class WampMessageTypeTestCase extends TestCase {
    public void testCtor() {
        WampMessageType type = new WampMessageType();
        assertNotNull(type);
    }

    public void testMessageCode() {
        assertEquals(1, WampMessageType.HELLO);
        assertEquals(2, WampMessageType.WELCOME);
        assertEquals(3, WampMessageType.ABORT);
        assertEquals(4, WampMessageType.CHALLENGE);
        assertEquals(5, WampMessageType.AUTHENTICATE);
        assertEquals(6, WampMessageType.GOODBYE);
        assertEquals(7, WampMessageType.HEARTBEAT);
        assertEquals(8, WampMessageType.ERROR);

        assertEquals(16, WampMessageType.PUBLISH);
        assertEquals(17, WampMessageType.PUBLISHED);

        assertEquals(32, WampMessageType.SUBSCRIBE);
        assertEquals(33, WampMessageType.SUBSCRIBED);
        assertEquals(34, WampMessageType.UNSUBSCRIBE);
        assertEquals(35, WampMessageType.UNSUBSCRIBED);
        assertEquals(36, WampMessageType.EVENT);

        assertEquals(48, WampMessageType.CALL);
        assertEquals(49, WampMessageType.CANCEL);
        assertEquals(50, WampMessageType.RESULT);

        assertEquals(64, WampMessageType.REGISTER);
        assertEquals(65, WampMessageType.REGISTERED);
        assertEquals(66, WampMessageType.UNREGISTER);
        assertEquals(67, WampMessageType.UNREGISTERED);
        assertEquals(68, WampMessageType.INVOCATION);
        assertEquals(69, WampMessageType.INTERRUPT);
        assertEquals(70, WampMessageType.YIELD);
    }
}
