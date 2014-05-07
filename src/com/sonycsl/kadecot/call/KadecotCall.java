/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.call;

import android.content.Context;

import com.sonycsl.kadecot.device.DeviceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.concurrent.Executors;

/**
 * JSON-RPC 2.0 をベースとしている(paramsは配列のみ)
 */
public abstract class KadecotCall {
    @SuppressWarnings("unused")
    private static final String TAG = KadecotCall.class.getSimpleName();

    private final KadecotCall self = this;

    private static final String VERSION = "3.0";

    private final Context mContext;

    private final RequestProcessor mRequestProcessor;

    private final NotificationProcessor mNotificationProcessor;

    protected static HashSet<KadecotCall> mKadecotCalls = new HashSet<KadecotCall>();

    private final static long REQUEST_TIMEOUT = 1000 * 60 * 5;

    private final int mPermissionLevel;

    public KadecotCall(Context context, int permissionLevel, RequestProcessor request,
            NotificationProcessor notification) {
        mContext = context.getApplicationContext();
        mRequestProcessor = request;
        mNotificationProcessor = notification;
        mPermissionLevel = permissionLevel;

    }

    public abstract void send(JSONObject obj);

    public final void sendRequest(String id, String method, JSONArray params) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("version", VERSION);
            obj.put("id", id);
            obj.put("method", method);
            if (params != null) {
                obj.put("params", params);
            }
            send(obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public final void sendNotification(String method, JSONObject params) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("version", VERSION);
            obj.put("method", method);
            if (params != null) {
                obj.put("params", params);
            }
            send(obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public final void sendResponse(int id, Response response) {
        JSONObject obj;
        try {
            obj = response.toJSON();
            obj.put("version", VERSION);
            obj.put("id", id);
            send(obj);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void receive(JSONObject obj) {
        // check version
        if (obj.isNull("version")) {
            if (!obj.isNull("id")) {
                try {
                    sendResponse(obj.getInt("id"), new ErrorResponse(
                            ErrorResponse.INTERNAL_ERROR_CODE, "not found version"));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return;
        }
        String version = VERSION;
        try {
            version = obj.getString("version");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!VERSION.equals(version)) {
            if (!obj.isNull("id")) {
                try {
                    sendResponse(obj.getInt("id"), new ErrorResponse(
                            ErrorResponse.INTERNAL_ERROR_CODE, "version:" + version
                                    + " is not latest"));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
            return;
        }

        if (!obj.isNull("method") && !obj.isNull("id")) {
            // request
            try {
                int id = obj.getInt("id");
                String method = "";
                JSONObject params = null;
                try {
                    method = obj.getString("method");
                    if (!obj.isNull("params")) {
                        params = obj.getJSONObject("params");
                    }
                } catch (Exception e) {
                    // error
                    sendResponse(id, new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e));
                    return;
                }
                receiveRequest(id, method, params);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (!obj.isNull("method")) {
            // notification
            try {
                String method = obj.getString("method");
                JSONObject params = obj.getJSONObject("params");

                receiveNotification(method, params);
            } catch (Exception e) {
                // error
            }
        } else {
            // response
            try {
                String id = obj.getString("id");
                Object result = obj.get("result");
                Object error = obj.get("error");

                receiveResponse(id, result, error);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void receiveRequest(final int id, final String method, final JSONObject params) {

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                /*
                 * final Thread currentThread = Thread.currentThread(); Thread t
                 * = new Thread(new Runnable(){
                 * @Override public void run() { try {
                 * Thread.sleep(REQUEST_TIMEOUT); } catch (InterruptedException
                 * e) { e.printStackTrace(); } currentThread.interrupt(); } });
                 * t.start();
                 */
                Response res = mRequestProcessor.process(method, params);

                // t.interrupt();

                sendResponse(id, res);
            }

        });
    }

    public void receiveNotification(final String method, final JSONObject params) {

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {

                mNotificationProcessor.process(method, params);
            }

        });
    }

    public void receiveResponse(String id, Object result, Object error) {

    }

    public void start() {
        // onNotifyServerSettings
        // onDeviceFound
        mKadecotCalls.add(this);
        this.sendNotification(Notification.ON_SERVER_STATUS_UPDATED, Notification
                .getParamsOnNotifyServerSettings(mContext));
        this.sendNotification(Notification.ON_DEVICE_LIST_UPDATED,
                Notification.getParamsOnUpdateList(
                        mContext, mPermissionLevel));

    }

    public void stop() {
        mKadecotCalls.remove(this);
    }

    protected static void informAll(final String method, final JSONObject params) {
        for (KadecotCall kc : mKadecotCalls) {
            kc.sendNotification(method, params);
        }
    }

    public int getPermissionLevel() {
        return mPermissionLevel;
    }

    protected static void informAll(final String method, final JSONObject params,
            int protocolPermissionLevel) {
        for (KadecotCall kc : mKadecotCalls) {
            if (DeviceManager.isAllowedPermission(kc.getPermissionLevel(), protocolPermissionLevel)) {
                kc.sendNotification(method, params);
            }
        }
    }

    protected static HashSet<KadecotCall> getKadecotCalls() {
        return mKadecotCalls;
    }

}
