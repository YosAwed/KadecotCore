/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.server.http.mock;

import com.sonycsl.Kadecot.server.http.response.ResponseFactory;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class MockResponseFactory extends ResponseFactory {

    private Status mStatus;
    private String mMimeType;
    private String mContents;

    public MockResponseFactory() {
        mStatus = Response.Status.OK;
        mMimeType = NanoHTTPD.MIME_PLAINTEXT;
        mContents = "";
    }

    public MockResponseFactory(Status status, String mimeType, String contents) {
        mStatus = status;
        mMimeType = mimeType;
        mContents = contents;
    }

    @Override
    public Response createResponse(IHTTPSession session, String rootPath) {
        return new Response(mStatus, mMimeType, mContents);
    }

}
