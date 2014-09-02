/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.server.http;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.sonycsl.Kadecot.net.ConnectivityManagerUtil;
import com.sonycsl.Kadecot.service.ServerManager;
import com.sonycsl.Kadecot.wamp.client.KadecotAppClientWrapper;
import com.sonycsl.Kadecot.wamp.client.KadecotAppClientWrapper.WampCallListener;
import com.sonycsl.Kadecot.wamp.client.provider.WampProviderAccessHelper;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SnapServerModel implements ServerResponseModel {

    private static final String TAG = SnapServerModel.class.getSimpleName();

    public static final String SNAP_BASE_URI = "snap";

    private static final String ACCESS_ORIGIN = "http://snap.berkeley.edu";

    private static final String REQ_BLOCK = "block.xml";
    private static final String REQ_LIST = "list";
    private static final String REQ_GET = "get";
    private static final String REQ_SET = "set";

    private static final String BLOCK_XML = "snap/block.xml";
    private static final String IP_ADDRESS_PLACEHOLDER = "localhost";
    private static final String PORT_NUMBER_PLACEHOLDER = "31338";

    private Context mContext;
    private KadecotAppClientWrapper mClient;

    private Map<String, Integer> deviceList = new HashMap<String, Integer>();

    private static class ResultHolder {
        public boolean success;
        public JSONObject argumentsKw;
        public String error;
    }

    public SnapServerModel(Context context, KadecotAppClientWrapper client) {
        mContext = context;
        mClient = client;
    }

    private static class SnapResponse extends Response {
        public SnapResponse(Status status, String callback, String txt) {
            super(status, callback, txt);
            addHeader("Access-Control-Allow-Origin", ACCESS_ORIGIN);
        }
    };

    @Override
    public Response createResponse(Method method, String uri, Map<String, String> params) {
        if (method != Method.GET) {
            return new SnapResponse(Response.Status.METHOD_NOT_ALLOWED, NanoHTTPD.MIME_PLAINTEXT,
                    Response.Status.METHOD_NOT_ALLOWED.toString());
        }

        String[] directories = uri.split("/", 3);
        int length = directories.length;
        if (length < 3) {
            return new SnapResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                    Response.Status.BAD_REQUEST.toString());
        }

        if (!SNAP_BASE_URI.equals(directories[1])) {
            return new SnapResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                    Response.Status.BAD_REQUEST.toString());
        }

        if (REQ_BLOCK.equals(directories[2])) {
            // ///////////////////////
            // read block.xml here. should buffer?
            // replace localhost with IP Address.
            // also port number.default is 31338.
            try {
                AssetManager am = mContext.getAssets();
                InputStream is = am.open(BLOCK_XML);
                BufferedReader in = new BufferedReader(new InputStreamReader(is));

                StringBuffer buf = new StringBuffer();
                String str;
                while ((str = in.readLine()) != null) {
                    buf.append(str);
                }
                in.close();
                str = buf.toString();
                str = str.replaceAll(IP_ADDRESS_PLACEHOLDER, ConnectivityManagerUtil.getIPAddress(mContext));
                str = str.replaceAll(PORT_NUMBER_PLACEHOLDER, ServerManager.JSONP_PORT_NO + "");
                return new SnapResponse(Response.Status.OK, "text/xml", str);

            } catch (IOException e) {
                e.printStackTrace();
                return new SnapResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                        "fail;can't open block.xml");
            }

        } else if (REQ_LIST.equals(directories[2])) {
            ResultHolder getDeviceListResult = syncCall(mClient,
                    WampProviderAccessHelper.Procedure.GET_DEVICE_LIST.getUri(),
                    new JSONObject(), new JSONObject());

            try {
                JSONObject response = getDeviceListResult.argumentsKw;
                JSONArray ja = response.getJSONArray("deviceList");

                ArrayList<String> nicknames = new ArrayList<String>();
                ArrayList<String> devtype = new ArrayList<String>();

                deviceList.clear();
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject dev = ja.getJSONObject(i);

                    String nickname = dev.getString("nickname");
                    int id = Integer.parseInt(dev.getString("deviceId"));

                    nicknames.add(nickname);
                    devtype.add(dev.getString("deviceType"));

                    deviceList.put(nickname, id);
                }
                String ret = string_join(":", nicknames) + ";" + string_join(":", devtype);

                return new SnapResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT,
                        ret);

            } catch (JSONException e) {
                e.printStackTrace();
                return new SnapResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                        "fail;can't get list(internal)");
            }

        } else if (REQ_SET.equals(directories[2])) {
            if (params.containsKey("nickname") && params.containsKey("epc")) {
                try {
                    int deviceID = deviceList.get(params.get("nickname"));
                    String edt = params.get("edt");
                    if (!edt.startsWith("0x")) {
                        edt = "0x" + Integer.toHexString(Integer.parseInt(edt));
                    }

                    String paramsKwStr = "{\"propertyName\":\"" + params.get("epc") + "\", " +
                            "\"propertyValue\":\"" + edt + "\"}";
                    String procedure = "com.sonycsl.kadecot.echonetlite.procedure.set";

                    return invokeWampCall(mClient, deviceID, procedure, paramsKwStr);
                } catch (Exception e) {
                    return new SnapResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                            "fail;device not found");
                }
            } else {
                return new SnapResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                        "fail;nickname or epc not found");
            }

        } else if (REQ_GET.equals(directories[2])) {
            if (params.containsKey("nickname") && params.containsKey("epc")) {
                try {
                    int deviceID = deviceList.get(params.get("nickname"));
                    String paramsKwStr = "{\"propertyName\":\"" + params.get("epc") + "\"}";
                    String procedure = "com.sonycsl.kadecot.echonetlite.procedure.get";

                    return invokeWampCall(mClient, deviceID, procedure, paramsKwStr);
                } catch (Exception e) {
                    return new SnapResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                            "fail;device not found");
                }
            } else {
                return new SnapResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                        "fail;nickname or epc not found");
            }
        }

        return new SnapResponse(Response.Status.INTERNAL_ERROR,
                NanoHTTPD.MIME_PLAINTEXT, Response.Status.INTERNAL_ERROR.toString());
    }

    private Response invokeWampCall(KadecotAppClientWrapper client, int deviceID, String procedure,
            String paramsKwStr) {

        JSONObject options;
        try {
            options = new JSONObject().put("deviceId", deviceID);
        } catch (JSONException e) {
            return new SnapResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                    "fail;illefal Device ID");
        }

        JSONObject paramsKw;
        try {
            paramsKw = new JSONObject(paramsKwStr);
        } catch (JSONException e) {
            return new SnapResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                    "fail;illegal param");
        }

        ResultHolder result = syncCall(client, procedure, options, paramsKw);
        if (!result.success) {
            return new SnapResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                    "fail;" + result.error);
        }

        try {
            JSONArray ja = result.argumentsKw.getJSONArray("propertyValue");
            ArrayList<String> edt = new ArrayList<String>();
            for (int i = 0; i < ja.length(); i++) {
                edt.add(String.valueOf(ja.getInt(i)));
            }
            return new SnapResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT,
                    "success;" + string_join(":", edt));

        } catch (Exception e) {
            return new SnapResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                    "fail;illegal response");
        }
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

    private static String string_join(String delimiter, ArrayList<String> strs) {
        StringBuffer sb = new StringBuffer();
        for (String s : strs) {
            if (sb.length() > 0) {
                sb.append(delimiter);
            }
            sb.append(s);
        }
        return sb.toString();
    }
}
