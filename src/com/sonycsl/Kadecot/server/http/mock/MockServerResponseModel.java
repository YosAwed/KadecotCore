/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.server.http.mock;

import com.sonycsl.Kadecot.server.http.ServerResponseModel;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;

import java.util.Map;

public class MockServerResponseModel implements ServerResponseModel {

    private Status mStatus;
    private String mMimeType;
    private String mContents;

    public MockServerResponseModel() {
        mStatus = Response.Status.OK;
        mMimeType = NanoHTTPD.MIME_PLAINTEXT;
        mContents = "";
    }

    public MockServerResponseModel(Status status, String mimeType, String contents) {
        mStatus = status;
        mMimeType = mimeType;
        mContents = contents;
    }

    @Override
    public Response createResponse(Method method, String uri, Map<String, String> params) {
        return new Response(mStatus, mMimeType, mContents);
    }

}
