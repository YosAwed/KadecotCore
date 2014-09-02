/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.test.server.http;

import android.content.Context;
import android.test.InstrumentationTestCase;

import com.sonycsl.Kadecot.server.http.MainAppServerModel;

import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;

import java.util.HashMap;

public class MainAppServerModelTestCase extends InstrumentationTestCase {

    private Context mContext;

    private MainAppServerModel mModel;

    @Override
    public void setUp() {
        mContext = getInstrumentation().getContext();
        mModel = new MainAppServerModel(mContext);
    }

    public void testCreateResponse() {
        assertEquals(
                Response.Status.OK,
                mModel.createResponse(Method.GET, "/html/index.html", new HashMap<String, String>())
                        .getStatus());
        assertEquals(
                Response.Status.BAD_REQUEST,
                mModel.createResponse(Method.GET, "/html/badfile",
                        new HashMap<String, String>())
                        .getStatus());
        assertEquals(
                Response.Status.INTERNAL_ERROR,
                mModel.createResponse(Method.GET, "/html/nofile.html",
                        new HashMap<String, String>())
                        .getStatus());
        assertEquals(
                Response.Status.BAD_REQUEST,
                mModel.createResponse(Method.GET, "/badclass.html",
                        new HashMap<String, String>())
                        .getStatus());
    }

}
