
package com.sonycsl.Kadecot.call;

import com.sonycsl.Kadecot.server.ServerSettings;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;

public class LocalRequestProcessor extends RequestProcessor {

    protected ServerSettings mServerSettings;

    public LocalRequestProcessor(Context context) {
        super(context, PERMISSION_ALL);
        mServerSettings = ServerSettings.getInstance(mContext);
    }

    // server settings
    public Response fullInitialize(JSONArray params) {
        mServerSettings.fullInitialize();
        return new Response(null);
    }

    public Response setServerLocation(JSONArray params) {
        if (params == null || params.length() < 2) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            String lat = params.getString(0);
            String lng = params.getString(1);
            mServerSettings.setLocation(lat, lng);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new Response(null);
    }

    public Response enableServerNetwork(JSONArray params) {
        if (params == null || params.length() < 1) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            boolean enabled = params.getBoolean(0);
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

    public Response enableWebSocketServer(JSONArray params) {
        if (params == null || params.length() < 1) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            boolean enabled = params.getBoolean(0);
            mServerSettings.enableWebSocketServer(enabled);
            return new Response(true);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e);
        }
    }

    public Response enableJSONPServer(JSONArray params) {
        if (params == null || params.length() < 1) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            boolean enabled = params.getBoolean(0);
            mServerSettings.enableJSONPServer(enabled);
            return new Response(true);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e);
        }
    }

    public Response enableSnapServer(JSONArray params) {
        if (params == null || params.length() < 1) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            boolean enabled = params.getBoolean(0);
            mServerSettings.enableSnapServer(enabled);
            return new Response(true);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e);
        }
    }

    public Response enablePersistentMode(JSONArray params) {
        if (params == null || params.length() < 1) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            boolean enabled = params.getBoolean(0);
            mServerSettings.enablePersistentMode(enabled);
            return new Response(true);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e);
        }
    }

}
