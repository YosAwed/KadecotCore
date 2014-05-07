/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.call;

import android.content.Context;

import com.sonycsl.kadecot.server.ServerSettings;

import org.json.JSONException;
import org.json.JSONObject;

public class LocalRequestProcessor extends RequestProcessor {

    protected ServerSettings mServerSettings;

    private static final String KEY_LATITUDE = "latitude";

    private static final String KEY_LONGITUDE = "longitude";

    private static final String KEY_ENABLE = "enable";

    public LocalRequestProcessor(Context context) {
        super(context, Permission.ALL);
        mServerSettings = ServerSettings.getInstance(mContext);
    }

    // server settings
    public Response fullInitialize(JSONObject params) {
        mServerSettings.fullInitialize();
        return new Response(null);
    }

    public Response setServerLocation(JSONObject params) {
        if (params == null || params.length() == 0) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            String lat = params.getString(KEY_LATITUDE);
            String lng = params.getString(KEY_LONGITUDE);
            mServerSettings.setLocation(lat, lng);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new Response(null);
    }

    public Response enableServerNetwork(JSONObject params) {
        if (params == null || params.length() < 1) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            boolean enabled = params.getBoolean(KEY_ENABLE);
            if (enabled) {
                return mServerSettings.registerNetwork();
            } else {
                return mServerSettings.unregisterNetwork();
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e);
        }

    }

    public Response enableWebSocketServer(JSONObject params) {
        if (params == null || params.length() < 1) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            boolean enabled = params.getBoolean(KEY_ENABLE);
            mServerSettings.enableWebSocketServer(enabled);
            return new Response(true);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e);
        }
    }

    public Response enableJSONPServer(JSONObject params) {
        if (params == null || params.length() < 1) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            boolean enabled = params.getBoolean(KEY_ENABLE);
            mServerSettings.enableJSONPServer(enabled);
            return new Response(true);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e);
        }
    }

    public Response enableSnapServer(JSONObject params) {
        if (params == null || params.length() < 1) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            boolean enabled = params.getBoolean(KEY_ENABLE);
            mServerSettings.enableSnapServer(enabled);
            return new Response(true);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e);
        }
    }

    public Response enablePersistentMode(JSONObject params) {
        if (params == null || params.length() < 1) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            boolean enabled = params.getBoolean(KEY_ENABLE);
            mServerSettings.enablePersistentMode(enabled);
            return new Response(true);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e);
        }
    }

}
