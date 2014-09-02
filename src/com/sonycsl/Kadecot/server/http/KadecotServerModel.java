/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.server.http;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KadecotServerModel implements ServerResponseModel {

    private Map<String, ServerResponseModel> mModels = new ConcurrentHashMap<String, ServerResponseModel>();

    public ServerResponseModel addModel(String key, ServerResponseModel model) {
        return mModels.put(key, model);
    }

    public ServerResponseModel removeModel(String key) {
        return mModels.remove(key);
    }

    @Override
    public Response createResponse(Method method, String uri, Map<String, String> params) {
        String[] directories = uri.split("/", 3);

        if (directories.length < 2) {
            return new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                    Response.Status.BAD_REQUEST.toString());
        }

        ServerResponseModel model = mModels.get(directories[1]);
        if (model == null) {
            return new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                    Response.Status.BAD_REQUEST.toString());
        }

        return model.createResponse(method, uri, params);
    }
}
