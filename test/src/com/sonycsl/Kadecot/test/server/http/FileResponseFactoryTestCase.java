/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.test.server.http;

import android.content.Context;
import android.test.InstrumentationTestCase;

import com.sonycsl.Kadecot.server.http.mock.MockHTTPSession;
import com.sonycsl.Kadecot.server.http.response.FileResponseFactory;

import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;

public class FileResponseFactoryTestCase extends InstrumentationTestCase {

    private Context mContext;

    private FileResponseFactory mFactory;

    @Override
    public void setUp() {
        mContext = getInstrumentation().getContext();
        mFactory = new FileResponseFactory(mContext);
    }

    public void testCreateResponse() {
        MockHTTPSession session;
        session = new MockHTTPSession(Method.GET, "/html/index.html");
        assertEquals(Response.Status.OK, mFactory.create(session, "/").getStatus());

        session = new MockHTTPSession(Method.GET, "/html/badfile");
        assertEquals(Response.Status.NOT_FOUND, mFactory.create(session, "/").getStatus());

        session = new MockHTTPSession(Method.GET, "/html/nofile.html");
        assertEquals(Response.Status.NOT_FOUND, mFactory.create(session, "/").getStatus());
    }
}
