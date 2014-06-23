
package com.sonycsl.Kadecot.server;

import android.util.Log;

import com.sonycsl.Kadecot.core.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.wamp.KadecotAppClientWrapper;
import com.sonycsl.Kadecot.wamp.KadecotAppClientWrapper.WampCallListener;
import com.sonycsl.Kadecot.wamp.KadecotWampRouter;
import com.sonycsl.Kadecot.wamp.KadecotWebsocketClientProxy;
import com.sonycsl.Kadecot.wamp.provider.KadecotProviderClient;
import com.sonycsl.wamp.WampError;

import fi.iki.elonen.NanoHTTPD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class KadecotHttpServer extends NanoHTTPD {

    private static final String TAG = KadecotHttpServer.class.getSimpleName();

    private KadecotAppClientWrapper mAppClient;
    private KadecotWebsocketClientProxy mProxy;

    private static final String LOCALHOST = "localhost";
    private static final String WEBSOCKET_PORT = "41314";

    private static final int JSONP_PORT = 31413;

    private static final String DEVICES = "devices";
    private static final String PARAMS = "params";
    private static final String CALLBACK = "callback";

    private static final String DEVICE_ID = "deviceId";
    private static final String PROTOCOL = "protocol";
    private static final String PROCEDURE = "procedure";

    private static final String PROCEDURE_HEAD = "com.sonycsl.kadecot.";

    private static final KadecotHttpServer sInstance = new KadecotHttpServer(JSONP_PORT);

    private static class JsonpResponse extends Response {

        public static final String MIME_JSONP = "application/javascript";

        public JsonpResponse(Status status, String callback) {
            super(status, MIME_JSONP, createJsonpString(callback,
                    generateErrorJson(status.toString()).toString()));
        }

        public JsonpResponse(Status status, String callback, String txt) {
            super(status, MIME_JSONP, createJsonpString(callback, txt));
        }

        public static JSONObject generateErrorJson(String error) {
            try {
                return new JSONObject().put("error", error);
            } catch (JSONException e) {
                return null;
            }
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

    public static KadecotHttpServer getInstance() {
        return sInstance;
    }

    private KadecotHttpServer(int port) {
        super(port);

        mAppClient = new KadecotAppClientWrapper();
        mProxy = new KadecotWebsocketClientProxy();

        mAppClient.connect(mProxy);
    }

    private void open() throws InterruptedException, TimeoutException {
        mProxy.open(LOCALHOST, WEBSOCKET_PORT);
        mAppClient.hello(KadecotWampRouter.REALM);
    }

    private void close() {
        mAppClient.goodbye(WampError.CLOSE_REALM);
        mProxy.close();
    }

    @Override
    public void start() throws IOException {
        try {
            open();
        } catch (InterruptedException e) {
            throw new IOException();
        } catch (TimeoutException e) {
            throw new IOException();
        }
        super.start();
    }

    @Override
    public void stop() {
        close();
        super.stop();
    }

    public boolean isRunning() {
        return mProxy.isOpen() && isAlive();
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (session.getMethod() != Method.GET) {
            return new JsonpResponse(Response.Status.METHOD_NOT_ALLOWED, null,
                    Response.Status.METHOD_NOT_ALLOWED.toString());
        }
        if (!mProxy.isOpen()) {
            try {
                mProxy.open(LOCALHOST, WEBSOCKET_PORT);
            } catch (InterruptedException e) {
                return new JsonpResponse(Response.Status.INTERNAL_ERROR, null,
                        Response.Status.INTERNAL_ERROR.toString());
            } catch (TimeoutException e) {
                return new JsonpResponse(Response.Status.INTERNAL_ERROR, null,
                        Response.Status.INTERNAL_ERROR.toString());
            }

            if (!mProxy.isOpen()) {
                return new JsonpResponse(Response.Status.INTERNAL_ERROR, null,
                        Response.Status.INTERNAL_ERROR.toString());
            }
        }

        // Parse URI with "/"
        String uri = session.getUri();
        String[] directories = uri.split("/");
        try {
            session.parseBody(new HashMap<String, String>());
        } catch (IOException ioe) {
            return new JsonpResponse(Response.Status.INTERNAL_ERROR, null,
                    "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
        } catch (ResponseException re) {
            return new JsonpResponse(re.getStatus(), null, re.getMessage());
        }

        String callback = session.getParms().get(CALLBACK);
        // TODO: Escape callback, directories and params

        if (directories.length == 2) {
            if (DEVICES.equals(directories[1])) {
                ResultHolder getDeviceListResult = syncCall(mAppClient,
                        KadecotProviderClient.Procedure.GET_DEVICE_LIST.getUri(),
                        new JSONObject(), new JSONObject());
                if (!getDeviceListResult.success) {
                    return new JsonpResponse(Response.Status.INTERNAL_ERROR, callback,
                            JsonpResponse.generateErrorJson(getDeviceListResult.error).toString());
                }
                return new JsonpResponse(Response.Status.OK, callback,
                        getDeviceListResult.argumentsKw.toString());
            }
            return new JsonpResponse(Response.Status.BAD_REQUEST, callback);
        }

        if (directories.length == 3) {
            if (!DEVICES.equals(directories[1])) {
                return new JsonpResponse(Response.Status.BAD_REQUEST, callback);
            }
            long deviceId;
            try {
                deviceId = Long.parseLong(directories[2]);
            } catch (NumberFormatException e) {
                return new JsonpResponse(Response.Status.BAD_REQUEST, callback);
            }

            JSONObject device = fetchDevice(deviceId);
            if (device == null) {
                return new JsonpResponse(Response.Status.INTERNAL_ERROR, callback);
            }

            Map<String, String> params = session.getParms();
            if (params.isEmpty()) {
                return getProcedureDetail(callback, device);
            }

            return invokeWampCall(callback, device, params);
        }

        return new JsonpResponse(Response.Status.BAD_REQUEST, callback);
    }

    private JSONObject fetchDevice(long deviceId) {
        ResultHolder getDeviceListResult = syncCall(mAppClient,
                KadecotProviderClient.Procedure.GET_DEVICE_LIST.getUri(),
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
                    return device;
                }
            }

        } catch (JSONException e) {
            return null;
        }

        return null;
    }

    private Response getProcedureDetail(String callback, JSONObject device) {
        try {
            String protocol = device.getString(PROTOCOL);
            ResultHolder getProcListResult = syncCall(mAppClient,
                    KadecotProviderClient.Procedure.GET_PROCEDURE_LIST.getUri(),
                    new JSONObject(), new JSONObject().put(
                            KadecotCoreStore.Procedures.ProcedureColumns.PROTOCOL,
                            protocol));
            if (!getProcListResult.success) {
                return new JsonpResponse(Response.Status.INTERNAL_ERROR, callback,
                        JsonpResponse.generateErrorJson(getProcListResult.error).toString());
            }

            return new JsonpResponse(Response.Status.OK, callback,
                    getProcListResult.argumentsKw.toString());

        } catch (JSONException e) {
            return new JsonpResponse(Response.Status.BAD_REQUEST, callback);
        }
    }

    private Response invokeWampCall(String callback, JSONObject device, Map<String, String> params) {
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
        ResultHolder result = syncCall(mAppClient, procedure, options, paramsKw);
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
            }
        } catch (InterruptedException e) {
            holder.success = false;
            holder.error = "Request Interupted";
        }

        return holder;
    }
}
