/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.server.http.response;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

abstract public class ResponseFactory {

    public Response create(IHTTPSession session, String rootPath) {
        return createResponse(session, rootPath);
    }

    abstract protected Response createResponse(IHTTPSession session, String rootPath);
}
