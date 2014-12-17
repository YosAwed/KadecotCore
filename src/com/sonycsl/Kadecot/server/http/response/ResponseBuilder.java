/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.server.http.response;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public final class ResponseBuilder {

    private final Response mResponse;

    public ResponseBuilder(Status status, String mimeType, String txt) {
        mResponse = new Response(status, mimeType, txt);
    }

    public ResponseBuilder(Status status) {
        mResponse = new Response(status, NanoHTTPD.MIME_PLAINTEXT, status.toString());
    }

    public ResponseBuilder addHeader(String name, String value) {
        mResponse.addHeader(name, value);
        return this;
    }

    public Response build() {
        return mResponse;
    }
}
