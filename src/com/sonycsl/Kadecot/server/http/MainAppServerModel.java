/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.server.http;

import android.content.Context;
import android.util.Log;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MainAppServerModel implements ServerResponseModel {

    private static final String TAG = MainAppServerModel.class.getSimpleName();

    public static final String MAIN_BASE_URI = "html";

    private Context mContext;

    private static final Map<String, String> MIME_MAP;

    static {
        Map<String, String> map = new HashMap<String, String>();
        map.put("txt", "text/plain");
        map.put("html", NanoHTTPD.MIME_HTML);
        map.put("htm", NanoHTTPD.MIME_HTML);
        map.put("xml", "text/xml");
        map.put("js", "text/javascript");
        map.put("vbs", "text/vbscript");
        map.put("css", "text/css");
        map.put("gif", "image/gif");
        map.put("jpg", "image/jpeg");
        map.put("jpeg", "image/jpeg");
        map.put("png", "image/png");
        map.put("cgi", "application/x-httpd-cgi");
        map.put("doc", "application/msword");
        map.put("pdf", "application/pdf");
        MIME_MAP = Collections.unmodifiableMap(map);
    }

    public MainAppServerModel(Context context) {
        mContext = context;
    }

    @Override
    public Response createResponse(Method method, String uri, Map<String, String> params) {
        if (method != Method.GET) {
            return new Response(Response.Status.METHOD_NOT_ALLOWED, NanoHTTPD.MIME_PLAINTEXT,
                    Response.Status.METHOD_NOT_ALLOWED.toString());
        }

        String[] directories = uri.split("/", 3);
        int length = directories.length;
        if (length < 3) {
            return new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                    Response.Status.BAD_REQUEST.toString());
        }

        if (!MAIN_BASE_URI.equals(directories[1])) {
            return new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                    Response.Status.BAD_REQUEST.toString());
        }

        String[] assetsSplit = directories[2].split("\\.");
        String mimeType = MIME_MAP.get(assetsSplit[assetsSplit.length - 1]);
        if (mimeType == null) {
            Log.i(TAG, "Unable to expect a mime type from a file extension. file name="
                    + Arrays.toString(assetsSplit));
            return new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                    Response.Status.BAD_REQUEST.toString());
        }

        try {
            return new Response(Response.Status.OK, mimeType, mContext.getAssets().open(
                    directories[1] + "/" + directories[2]));
        } catch (IOException e) {
            return new Response(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                    Response.Status.INTERNAL_ERROR.toString());
        }
    }
}
