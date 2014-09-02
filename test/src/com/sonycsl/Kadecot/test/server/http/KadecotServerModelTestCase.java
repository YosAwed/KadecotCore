/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.test.server.http;

import com.sonycsl.Kadecot.server.http.KadecotServerModel;
import com.sonycsl.Kadecot.server.http.mock.MockServerResponseModel;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;

import junit.framework.TestCase;

import java.util.HashMap;

public class KadecotServerModelTestCase extends TestCase {

    private KadecotServerModel mModel;
    private MockServerResponseModel mModel1;
    private MockServerResponseModel mModel2;

    @Override
    public void setUp() {
        mModel1 = new MockServerResponseModel(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "");
        mModel2 = new MockServerResponseModel(Response.Status.FORBIDDEN,
                NanoHTTPD.MIME_PLAINTEXT, "");
        mModel = new KadecotServerModel();
    }

    public void testAddModel() {
        assertNull(mModel.addModel("test", mModel1));
        assertEquals(mModel1, mModel.addModel("test", mModel2));
    }

    public void testRemove() {
        assertNull(mModel.addModel("test", mModel1));
        assertEquals(mModel1, mModel.addModel("test", mModel2));
        assertEquals(mModel2, mModel.removeModel("test"));
        assertNull(mModel.removeModel("test"));
    }

    public void testCreateResponse() {
        mModel.addModel("model1", mModel1);
        mModel.addModel("model2", mModel2);
        assertEquals(Response.Status.OK,
                mModel.createResponse(Method.GET, "/model1", new HashMap<String, String>())
                        .getStatus());
        assertEquals(Response.Status.FORBIDDEN,
                mModel.createResponse(Method.GET, "/model2", new HashMap<String, String>())
                        .getStatus());

        mModel.removeModel("model1");
        assertEquals(Response.Status.BAD_REQUEST,
                mModel.createResponse(Method.GET, "/model1", new HashMap<String, String>())
                        .getStatus());
    }
}
