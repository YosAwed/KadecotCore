
package com.sonycsl.test.wamp.util;

import com.sonycsl.wamp.util.WampRequestIdGenerator;

import junit.framework.TestCase;

public class WampRequestIdGeneratorTestCase extends TestCase {

    public void testCtor() {
        assertNotNull(new WampRequestIdGenerator());
    }

    public void testGetId() {
        int id1 = WampRequestIdGenerator.getId();
        int id2 = WampRequestIdGenerator.getId();
        assertTrue(id1 != id2);
    }
}
