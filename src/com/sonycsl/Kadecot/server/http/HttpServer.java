/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.server.http;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.HashMap;

public class HttpServer extends NanoHTTPD {

    private boolean mIsStarted;
    private ServerResponseModel mModel;

    public HttpServer(int port, ServerResponseModel model) {
        super(port);

        mModel = model;
    }

    @Override
    public synchronized void start() throws IOException {
        if (mIsStarted) {
            return;
        }
        super.start();

        mIsStarted = true;
    }

    @Override
    public synchronized void stop() {
        if (!mIsStarted) {
            return;
        }
        mIsStarted = false;

        super.stop();
    }

    @Override
    public Response serve(IHTTPSession session) {

        // Parse URI with "/"
        String uri = session.getUri();
        try {
            session.parseBody(new HashMap<String, String>());
        } catch (IOException ioe) {
            return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT,
                    "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
        } catch (ResponseException re) {
            return new Response(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
        }

        return mModel.createResponse(session.getMethod(), uri, session.getParms());
    }
}
