/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.server.http;

import android.util.Log;

import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.wamp.client.KadecotAppClientWrapper;
import com.sonycsl.Kadecot.wamp.client.KadecotAppClientWrapper.WampCallListener;
import com.sonycsl.Kadecot.wamp.client.provider.WampProviderAccessHelper;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class JsonpServerModel implements ServerResponseModel {

    private static final String TAG = JsonpServerModel.class.getSimpleName();

    public static final String JSONP_SERVICE_VERSION = "v1";

    public static final String JSONP_BASE_URI = "jsonp";

    private static final String DEVICES = "devices";
    private static final String PARAMS = "params";
    private static final String CALLBACK = "callback";

    private static final String DEVICE_ID = "deviceId";
    private static final String PROTOCOL = "protocol";
    private static final String PROCEDURE = "procedure";
    private static final String DESCRIPTION = "description";

    private static final String PROCEDURE_LIST = "procedureList";

    private static final String PROCEDURE_HEAD = "com.sonycsl.kadecot.";

    private static final String BDR_PROTOCOL = "sonyblurayrecorder";

    private KadecotAppClientWrapper mClient;

    private static class JsonpResponse extends Response {

        public static final String MIME_JSONP = "application/javascript";

        public JsonpResponse(Status status, String callback) {
            super(status, getMime(callback), createJsonpString(callback,
                    generateErrorJson(status.toString()).toString()));
        }

        public JsonpResponse(Status status, String callback, String txt) {
            super(status, getMime(callback), createJsonpString(callback, txt));
        }

        public static JSONObject generateErrorJson(String error) {
            try {
                return new JSONObject().put("error", error);
            } catch (JSONException e) {
                return null;
            }
        }

        private static String getMime(String callback) {
            if (callback == null || "".equals(callback)) {
                return NanoHTTPD.MIME_PLAINTEXT;
            }
            return MIME_JSONP;
        }

        private static String createJsonpString(String callback, String msg) {
            if (callback == null || "".equals(callback)) {
                return msg;
            }
            StringBuilder builder = new StringBuilder();
            builder.append(callback);
            builder.append("(");
            builder.append(msg);
            builder.append(")");
            return builder.toString();
        }
    }

    private static class ResultHolder {
        public boolean success;
        public JSONObject argumentsKw;
        public String error;
    }

    public JsonpServerModel(KadecotAppClientWrapper client) {
        mClient = client;
    }

    @Override
    public Response createResponse(Method method, String uri, Map<String, String> params) {
        String callback = params.remove(JsonpServerModel.CALLBACK);

        if (method != Method.GET) {
            return new JsonpResponse(Response.Status.METHOD_NOT_ALLOWED, callback,
                    Response.Status.METHOD_NOT_ALLOWED.toString());
        }

        String[] directories = uri.split("/");

        if (directories.length < 3) {
            return new JsonpResponse(Response.Status.BAD_REQUEST, callback,
                    Response.Status.BAD_REQUEST.toString());
        }

        if (!JSONP_BASE_URI.equals(directories[1])) {
            return new JsonpResponse(Response.Status.BAD_REQUEST, callback,
                    Response.Status.BAD_REQUEST.toString());
        }

        if (directories[2].equals("v1")) {
            return createResponseV1(directories, params, callback);

            // Add version here
        } else {
            return new JsonpResponse(Response.Status.BAD_REQUEST, callback,
                    Response.Status.BAD_REQUEST.toString());
        }
    }

    private Response createResponseV1(String[] directories, Map<String, String> params,
            String callback) {

        if (directories.length == 4) {
            if (DEVICES.equals(directories[3])) {
                ResultHolder getDeviceListResult = syncCall(mClient,
                        WampProviderAccessHelper.Procedure.GET_DEVICE_LIST.getUri(),
                        new JSONObject(), new JSONObject());
                if (!getDeviceListResult.success) {
                    return new JsonpResponse(Response.Status.INTERNAL_ERROR, callback,
                            JsonpResponse.generateErrorJson(getDeviceListResult.error)
                                    .toString());
                }

                try {
                    JSONArray newArray = new JSONArray();
                    JSONArray orgArray = getDeviceListResult.argumentsKw.getJSONArray("deviceList");

                    for (int index = 0; index < orgArray.length(); index++) {
                        JSONObject obj = orgArray.getJSONObject(index);
                        if (!obj.getString("protocol").equals(BDR_PROTOCOL)) {
                            newArray.put(obj);
                        }
                    }
                    getDeviceListResult.argumentsKw.put("deviceList", newArray);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return new JsonpResponse(Response.Status.OK, callback,
                        getDeviceListResult.argumentsKw.toString());
            }
            return new JsonpResponse(Response.Status.BAD_REQUEST, callback);
        }

        if (directories.length == 5) {
            if (!DEVICES.equals(directories[3])) {
                return new JsonpResponse(Response.Status.BAD_REQUEST, callback);
            }
            long deviceId;
            try {
                deviceId = Long.parseLong(directories[4]);
            } catch (NumberFormatException e) {
                return new JsonpResponse(Response.Status.BAD_REQUEST, callback);
            }

            JSONObject device = fetchDevice(mClient, deviceId);
            if (device == null) {
                return new JsonpResponse(Response.Status.INTERNAL_ERROR, callback);
            }

            if (params.isEmpty()) {
                return getProcedureDetail(mClient, callback, device);
            }

            return invokeWampCall(mClient, callback, device, params);
        }

        return new JsonpResponse(Response.Status.BAD_REQUEST, callback);
    }

    private static JSONObject fetchDevice(KadecotAppClientWrapper client, long deviceId) {
        ResultHolder getDeviceListResult = syncCall(client,
                WampProviderAccessHelper.Procedure.GET_DEVICE_LIST.getUri(),
                new JSONObject(), new JSONObject());
        if (!getDeviceListResult.success) {
            return null;
        }
        JSONArray deviceList;
        try {
            deviceList = getDeviceListResult.argumentsKw.getJSONArray("deviceList");
        } catch (JSONException e) {
            return null;
        }

        try {
            for (int devi = 0; devi < deviceList.length(); devi++) {
                JSONObject device = deviceList.getJSONObject(devi);
                if (device.getLong(DEVICE_ID) == deviceId) {
                    if (device.getString("protocol").equals(BDR_PROTOCOL)) {
                        return null;
                    }
                    return device;
                }
            }

        } catch (JSONException e) {
            return null;
        }

        return null;
    }

    private static Response getProcedureDetail(KadecotAppClientWrapper client, String callback,
            JSONObject device) {
        try {
            String protocol = device.getString(PROTOCOL);
            ResultHolder getProcListResult = syncCall(client,
                    WampProviderAccessHelper.Procedure.GET_PROCEDURE_LIST.getUri(),
                    new JSONObject(), new JSONObject().put(
                            KadecotCoreStore.Procedures.ProcedureColumns.PROTOCOL,
                            protocol));
            if (!getProcListResult.success) {
                return new JsonpResponse(Response.Status.INTERNAL_ERROR, callback,
                        JsonpResponse.generateErrorJson(getProcListResult.error).toString());
            }

            JSONArray returnProcArray = new JSONArray();
            JSONArray procListArray = getProcListResult.argumentsKw.getJSONArray(PROCEDURE_LIST);
            for (int proci = 0; proci < procListArray.length(); proci++) {
                JSONObject wampProcedure = procListArray.getJSONObject(proci);

                String[] splitProc = wampProcedure.getString(PROCEDURE).split("\\.", 6);

                JSONObject retProc = new JSONObject().put(PROCEDURE, splitProc[5]).put(DESCRIPTION,
                        wampProcedure.getString(DESCRIPTION));
                returnProcArray.put(retProc);
            }

            JSONObject returnProcList = new JSONObject();
            returnProcList.put(PROCEDURE_LIST, returnProcArray);

            return new JsonpResponse(Response.Status.OK, callback,
                    returnProcList.toString());

        } catch (JSONException e) {
            return new JsonpResponse(Response.Status.BAD_REQUEST, callback);
        }
    }

    private static Response invokeWampCall(KadecotAppClientWrapper client, String callback,
            JSONObject device,
            Map<String, String> params) {
        String procedureFoot = params.get(PROCEDURE);
        if (procedureFoot == null) {
            return new JsonpResponse(Response.Status.BAD_REQUEST, callback);
        }
        String paramsKwStr = params.get(PARAMS);
        if (paramsKwStr == null) {
            return new JsonpResponse(Response.Status.BAD_REQUEST, callback);
        }

        String procedure;
        try {
            StringBuilder procBuilder = new StringBuilder();
            procBuilder.append(PROCEDURE_HEAD);
            procBuilder.append(device.getString(PROTOCOL));
            procBuilder.append(".");
            procBuilder.append(PROCEDURE);
            procBuilder.append(".");
            procBuilder.append(procedureFoot);

            procedure = procBuilder.toString();
        } catch (JSONException e1) {
            return new JsonpResponse(Response.Status.INTERNAL_ERROR, callback);
        }

        JSONObject options;
        try {
            options = new JSONObject().put(DEVICE_ID, device.getLong(DEVICE_ID));
        } catch (JSONException e) {
            return new JsonpResponse(Response.Status.BAD_REQUEST, callback);
        }
        JSONObject paramsKw;
        try {
            paramsKw = new JSONObject(paramsKwStr);
        } catch (JSONException e) {
            return new JsonpResponse(Response.Status.BAD_REQUEST, callback);
        }
        ResultHolder result = syncCall(client, procedure, options, paramsKw);
        if (!result.success) {
            return new JsonpResponse(Response.Status.INTERNAL_ERROR, callback, JsonpResponse
                    .generateErrorJson(result.error).toString());
        }
        return new JsonpResponse(Response.Status.OK, callback, result.argumentsKw.toString());
    }

    private static ResultHolder syncCall(KadecotAppClientWrapper appClient, String procedure,
            JSONObject options, JSONObject paramsKw) {
        final CountDownLatch latch = new CountDownLatch(1);
        final ResultHolder holder = new ResultHolder();

        appClient.call(procedure, options, paramsKw,
                new WampCallListener() {

                    @Override
                    public void onResult(JSONObject details, JSONObject argumentsKw) {
                        holder.success = true;
                        holder.argumentsKw = argumentsKw;
                        latch.countDown();
                    }

                    @Override
                    public void onError(JSONObject details, String error) {
                        Log.e(TAG, error);
                        holder.success = false;
                        holder.error = error;
                        latch.countDown();
                    }
                });

        try {
            if (!latch.await(1, TimeUnit.SECONDS)) {
                holder.success = false;
                holder.error = "Request Timeout Error";
                Log.e(TAG, holder.error, new Throwable());
            }
        } catch (InterruptedException e) {
            holder.success = false;
            holder.error = "Request Interupted";
        }

        return holder;
    }
}
