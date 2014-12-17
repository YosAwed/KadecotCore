/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.test.server.http;

import com.sonycsl.Kadecot.server.http.HttpServer;
import com.sonycsl.Kadecot.server.http.mock.MockHTTPSession;
import com.sonycsl.Kadecot.server.http.mock.MockResponseFactory;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;

import junit.framework.TestCase;

import java.io.IOException;

public class HttpServerTestCase extends TestCase {

    private static final int PORT = 31413;

    private MockResponseFactory mFactory;
    private HttpServer mServer;
    private MockResponseFactory mOkFactory;
    private MockResponseFactory mForbiddenFactory;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mFactory = new MockResponseFactory(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_HTML, "");
        mServer = new HttpServer(PORT, mFactory);
        mOkFactory = new MockResponseFactory(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "");
        mForbiddenFactory = new MockResponseFactory(Response.Status.FORBIDDEN,
                NanoHTTPD.MIME_PLAINTEXT, "");
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

    public void testPutResponseFactory() {
        assertNull(mServer.putResponseFactory("test", mOkFactory));
        assertEquals(mOkFactory, mServer.putResponseFactory("test", mForbiddenFactory));
    }

    public void testRemove() {
        assertNull(mServer.putResponseFactory("test", mOkFactory));
        assertEquals(mOkFactory, mServer.putResponseFactory("test", mForbiddenFactory));
        assertEquals(mForbiddenFactory, mServer.removeResponseFactory("test"));
        assertNull(mServer.removeResponseFactory("test"));
    }

    public void testCreateResponse() {
        mServer.putResponseFactory("/model1", mOkFactory);
        mServer.putResponseFactory("/model2", mForbiddenFactory);

        MockHTTPSession session;

        session = new MockHTTPSession(Method.GET, "/model1");
        assertEquals(Response.Status.OK, mServer.serve(session).getStatus());

        session = new MockHTTPSession(Method.GET, "/model2");
        assertEquals(Response.Status.FORBIDDEN, mServer.serve(session).getStatus());

        mServer.removeResponseFactory("/model1");
        session = new MockHTTPSession(Method.GET, "/model1");
        assertEquals(Response.Status.BAD_REQUEST, mServer.serve(session).getStatus());
    }
}
