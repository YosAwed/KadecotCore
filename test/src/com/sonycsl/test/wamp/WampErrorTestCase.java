
package com.sonycsl.test.wamp;

import com.sonycsl.wamp.WampError;

import junit.framework.TestCase;

public class WampErrorTestCase extends TestCase {
    public void testCtor() {
        assertNotNull(new WampError());
    }
}
