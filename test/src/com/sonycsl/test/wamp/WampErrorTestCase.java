
package com.sonycsl.test.wamp;

import com.sonycsl.wamp.WampError;

import junit.framework.TestCase;

public class WampErrorTestCase extends TestCase {
    public void testCtor() {
        assertNotNull(new WampError());
    }

    public void testPredefinedErrors() {
        assertEquals("wamp.error.not_authorized", WampError.NOT_AUTHORIZED);
        assertEquals("wamp.error.no_such_realm", WampError.NO_SUCH_REALM);
        assertEquals("wamp.error.system_shutdown", WampError.SYSTEM_SHUTDOWN);
        assertEquals("wamp.error.close_realm", WampError.CLOSE_REALM);
        assertEquals("wamp.error.goodbye_and_out", WampError.GOODBYE_AND_OUT);
        assertEquals("wamp.error.no_such_procedure", WampError.NO_SUCH_PROCEDURE);
        assertEquals("wamp.error.no_such_subscription", WampError.NO_SUCH_SUBSCRIPTION);
        assertEquals("wamp.error.no_such_registration", WampError.NO_SUCH_REGISTRATION);
        assertEquals("wamp.error.invalid_argument", WampError.INVALID_ARGUMENT);
        assertEquals("wamp.error.invalid_topic", WampError.INVALID_TOPIC);
        assertEquals("wamp.error.procedure_already_exists", WampError.PROCEDURE_ALREADY_EXISTS);
    }
}
