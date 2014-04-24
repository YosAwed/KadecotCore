
package com.sonycsl.test.Kadecot.server;

import android.content.Context;
import android.test.mock.MockContext;

import com.sonycsl.Kadecot.core.KadecotCoreApplication;
import com.sonycsl.Kadecot.server.KadecotWebSocketServer;

import junit.framework.TestCase;

public class KadecotWebSocketServerTestCase extends TestCase {

    private static class TestMockContext extends MockContext {

        @Override
        public Context getApplicationContext() {
            return new KadecotCoreApplication() {

                @Override
                public Context getApplicationContext() {
                    return this;
                }

            };
        }
    }

    private KadecotWebSocketServer mServer;

    protected void setUp() throws Exception {
        mServer = KadecotWebSocketServer.getInstance(new TestMockContext());
    }

    @Override
    protected void tearDown() throws Exception {
        if (mServer.isStarted()) {
            mServer.stop();
        }
    }

    public void testCtor() {
        assertNotNull(mServer);
    }

    public void testSingleton() {
        assertEquals(mServer, KadecotWebSocketServer.getInstance(new TestMockContext()));
    }

    public void testStart() {
        assertFalse(mServer.isStarted());
        mServer.start();
        assertTrue(mServer.isStarted());
    }

    public void testStop() {
        assertFalse(mServer.isStarted());
        mServer.start();
        assertTrue(mServer.isStarted());
        mServer.stop();
        assertFalse(mServer.isStarted());
    }
}
