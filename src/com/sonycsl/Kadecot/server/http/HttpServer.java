/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.server.http;

import com.sonycsl.Kadecot.server.http.response.ResponseFactory;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpServer extends NanoHTTPD {

    private boolean mIsStarted;
    private final Map<String, ResponseFactory> mFactories = Collections
            .synchronizedMap(new HashMap<String, ResponseFactory>());
    private final ResponseFactory mDefaultFactory;
    private final String PATH_DELIMITER = "/";

    public HttpServer(int port, ResponseFactory defaultFactory) {
        super(port);
        mDefaultFactory = defaultFactory;
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

    public ResponseFactory putResponseFactory(String rootPath, ResponseFactory factory) {
        return mFactories.put(rootPath, factory);
    }

    public ResponseFactory removeResponseFactory(String rootPath) {
        return mFactories.remove(rootPath);
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            session.parseBody(new HashMap<String, String>());
        } catch (IOException ioe) {
            return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT,
                    "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
        } catch (ResponseException re) {
            return new Response(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
        }

        final String path = session.getUri();
        final int start = path.indexOf(PATH_DELIMITER);
        final int last = path.lastIndexOf(PATH_DELIMITER);

        int end = start;
        while (end != last) {
            end = path.indexOf(PATH_DELIMITER, end + 1);
            String rootPath = path.substring(start, end);
            if (mFactories.containsKey(rootPath)) {
                return mFactories.get(rootPath).create(session, rootPath);
            }
        }

        if (mFactories.containsKey(path)) {
            return mFactories.get(path).create(session, path);
        }

        return mDefaultFactory.create(session, PATH_DELIMITER);
    }
}
