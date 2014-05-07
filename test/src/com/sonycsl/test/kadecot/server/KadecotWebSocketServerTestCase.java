/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.kadecot.server;

import android.content.Context;
import android.test.mock.MockContext;

import com.sonycsl.kadecot.core.KadecotCoreApplication;
import com.sonycsl.kadecot.server.KadecotWebSocketServer;

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
