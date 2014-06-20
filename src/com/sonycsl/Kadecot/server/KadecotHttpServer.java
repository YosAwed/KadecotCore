
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

    private static final KadecotHttpServer sInstance = new KadecotHttpServer(JSONP_PORT);

    private static class ResultHolder {
        public JSONObject argumentsKw;
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
        if (!(session.getMethod() == Method.GET || session.getMethod() == Method.POST)) {
            return new Response(Response.Status.METHOD_NOT_ALLOWED.toString());
        }
        if (!mProxy.isOpen()) {
            try {
                mProxy.open(LOCALHOST, WEBSOCKET_PORT);
            } catch (InterruptedException e) {
                return new Response(Response.Status.INTERNAL_ERROR.toString());
            } catch (TimeoutException e) {
                return new Response(Response.Status.INTERNAL_ERROR.toString());
            }

            if (!mProxy.isOpen()) {
                return new Response(Response.Status.INTERNAL_ERROR.toString());
            }
        }

        // Parse URI with "/"
        String uri = session.getUri();
        String[] directories = uri.split("/");
        try {
            session.parseBody(new HashMap<String, String>());
        } catch (IOException ioe) {
            return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT,
                    "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
        } catch (ResponseException re) {
            return new Response(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
        }

        if (session.getMethod() == Method.POST) {
            if (directories.length == 4) {
                JSONObject options;
                try {
                    long deviceId = Long.parseLong(directories[2]);
                    options = new JSONObject().put("deviceId", deviceId);
                } catch (NumberFormatException e) {
                    return new Response(Response.Status.BAD_REQUEST.toString());
                } catch (JSONException e) {
                    return new Response(Response.Status.BAD_REQUEST.toString());
                }
                String procedure = directories[3];

                Map<String, String> params = session.getParms();
                if (params == null) {
                    return new Response(Response.Status.BAD_REQUEST.toString());
                }
                JSONObject paramsKw;
                try {
                    paramsKw = new JSONObject(session.getQueryParameterString());
                } catch (JSONException e) {
                    return new Response(Response.Status.BAD_REQUEST.toString());
                }
                ResultHolder result = syncCall(mAppClient, procedure, options, paramsKw);
                if (result == null) {
                    return new Response(Response.Status.INTERNAL_ERROR.toString());
                }
                return new Response(result.argumentsKw.toString());
            }

            return new Response(Response.Status.BAD_REQUEST.toString());
        }

        if (directories.length == 2) {
            if (DEVICES.equals(directories[1])) {
                ResultHolder getDeviceListResult = syncCall(mAppClient,
                        KadecotProviderClient.Procedure.GET_DEVICE_LIST.getUri(),
                        new JSONObject(), new JSONObject());
                return new Response(getDeviceListResult.argumentsKw.toString());
            }
            return new Response(Response.Status.BAD_REQUEST.toString());
        }

        if (directories.length == 3) {
            long deviceId;
            try {
                deviceId = Long.parseLong(directories[2]);
            } catch (NumberFormatException e) {
                return new Response(Response.Status.BAD_REQUEST.toString());
            }
            ResultHolder getDeviceListResult = syncCall(mAppClient,
                    KadecotProviderClient.Procedure.GET_DEVICE_LIST.getUri(),
                    new JSONObject(), new JSONObject());
            if (getDeviceListResult == null) {
                return new Response(Response.Status.INTERNAL_ERROR.toString());
            }
            JSONArray deviceList;
            try {
                deviceList = getDeviceListResult.argumentsKw.getJSONArray("deviceList");
            } catch (JSONException e) {
                return new Response(Response.Status.INTERNAL_ERROR.toString());
            }

            try {
                for (int devi = 0; devi < deviceList.length(); devi++) {
                    JSONObject device = deviceList.getJSONObject(devi);
                    if (device.getLong("deviceId") == deviceId) {
                        String protocol = deviceList.getJSONObject(devi).getString("protocol");
                        ResultHolder getProcListResult = syncCall(mAppClient,
                                KadecotProviderClient.Procedure.GET_PROCEDURE_LIST.getUri(),
                                new JSONObject(), new JSONObject().put(
                                        KadecotCoreStore.Procedures.ProcedureColumns.PROTOCOL,
                                        protocol));
                        if (getProcListResult == null) {
                            return new Response(Response.Status.INTERNAL_ERROR.toString());
                        }

                        return new Response(getProcListResult.argumentsKw.toString());
                    }
                }

            } catch (JSONException e) {
                return new Response(Response.Status.BAD_REQUEST.toString());
            }
        }

        if (directories.length == 4) {
            long deviceId;
            try {
                deviceId = Long.parseLong(directories[2]);
            } catch (NumberFormatException e) {
                return new Response(Response.Status.BAD_REQUEST.toString());
            }
            ResultHolder getDeviceListResult = syncCall(mAppClient,
                    KadecotProviderClient.Procedure.GET_DEVICE_LIST.getUri(),
                    new JSONObject(), new JSONObject());
            if (getDeviceListResult == null) {
                return new Response(Response.Status.INTERNAL_ERROR.toString());
            }
            JSONArray deviceList;
            try {
                deviceList = getDeviceListResult.argumentsKw.getJSONArray("deviceList");
            } catch (JSONException e) {
                return new Response(Response.Status.INTERNAL_ERROR.toString());
            }

            try {
                for (int devi = 0; devi < deviceList.length(); devi++) {
                    JSONObject device = deviceList.getJSONObject(devi);
                    if (device.getLong("deviceId") == deviceId) {
                        String description = deviceList.getJSONObject(devi)
                                .getString("description");
                        return new Response(description);
                    }
                }

            } catch (JSONException e) {
                return new Response(Response.Status.BAD_REQUEST.toString());
            }
        }
        return new Response(Response.Status.BAD_REQUEST.toString());
    }

    private static ResultHolder syncCall(KadecotAppClientWrapper appClient, String procedure,
            JSONObject options, JSONObject paramsKw) {
        final CountDownLatch latch = new CountDownLatch(1);
        final ResultHolder holder = new ResultHolder();
        appClient.call(procedure, options, paramsKw,
                new WampCallListener() {

                    @Override
                    public void onResult(JSONObject details, JSONObject argumentsKw) {
                        holder.argumentsKw = argumentsKw;
                        latch.countDown();
                    }

                    @Override
                    public void onError(JSONObject details, String error) {
                        Log.e(TAG, error);
                    }
                });

        try {
            if (!latch.await(1, TimeUnit.SECONDS)) {
                return null;
            }
        } catch (InterruptedException e) {
            return null;
        }

        return holder;
    }
}
