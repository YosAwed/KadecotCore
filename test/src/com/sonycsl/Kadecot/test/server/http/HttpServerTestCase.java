/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.test.server.http;

import com.sonycsl.Kadecot.server.http.HttpServer;
import com.sonycsl.Kadecot.server.http.mock.MockServerResponseModel;

import junit.framework.TestCase;

import java.io.IOException;

public class HttpServerTestCase extends TestCase {

    private static final int PORT = 31413;

    private MockServerResponseModel mModel;
    private HttpServer mServer;

    @Override
    public void setUp() {
        mModel = new MockServerResponseModel();
        mServer = new HttpServer(PORT, mModel);
    }

    public void testConstructor() {
        assertNotNull(mServer);
    }

    public void testStartStop() {
        try {
            mServer.start();
            mServer.stop();
        } catch (IOException e) {
            fail();
        }
    }
}
