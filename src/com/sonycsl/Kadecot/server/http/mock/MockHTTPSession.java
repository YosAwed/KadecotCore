/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.server.http.mock;

import fi.iki.elonen.NanoHTTPD.CookieHandler;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.ResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MockHTTPSession implements IHTTPSession {

    private final Method mMethod;
    private final String mUri;
    private Map<String, String> mParams;

    public MockHTTPSession(Method method, String uri) {
        mMethod = method;
        mUri = uri;
    }

    @Override
    public void execute() throws IOException {
    }

    @Override
    public Map<String, String> getParms() {
        return (mParams != null) ? mParams : new HashMap<String, String>();
    }

    public void setParams(Map<String, String> params) {
        mParams = params;
    }

    @Override
    public Map<String, String> getHeaders() {
        return null;
    }

    @Override
    public String getUri() {
        return mUri;
    }

    @Override
    public String getQueryParameterString() {
        return null;
    }

    @Override
    public Method getMethod() {
        return mMethod;
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public CookieHandler getCookies() {
        return null;
    }

    @Override
    public void parseBody(Map<String, String> files) throws IOException, ResponseException {
    }

}
