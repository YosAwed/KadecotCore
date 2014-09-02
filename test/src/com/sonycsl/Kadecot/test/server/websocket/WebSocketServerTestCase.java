/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.test.server.websocket;

import com.sonycsl.Kadecot.server.websocket.WebSocketServer;
import com.sonycsl.Kadecot.service.ServerManager;

import junit.framework.TestCase;

public class WebSocketServerTestCase extends TestCase {

    private WebSocketServer mServer;

    protected void setUp() throws Exception {
        mServer = new WebSocketServer(ServerManager.WS_PORT_NO, ServerManager.WAMP_PROTOCOL);
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
