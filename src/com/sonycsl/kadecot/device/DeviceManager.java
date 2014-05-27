/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.device;

import android.content.Context;
import android.util.Log;

import com.sonycsl.kadecot.call.CannotProcessRequestException;
import com.sonycsl.kadecot.call.ErrorResponse;
import com.sonycsl.kadecot.call.Notification;
import com.sonycsl.kadecot.call.Response;
import com.sonycsl.kadecot.core.KadecotCoreApplication;
import com.sonycsl.kadecot.device.echo.EchoManager;
import com.sonycsl.kadecot.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Deviceを管理するクラス
 */
public class DeviceManager {
    private static final String TAG = DeviceManager.class.getSimpleName();

    private static DeviceManager sInstance = null;

    private final Context mContext;

    private final HashMap<String, DeviceProtocol> mDeviceProtocols;

    private boolean mStarted = false;

    private DeviceDatabase mDeviceDatabase;

    private Logger mLogger;

    private static final String KEY_DEVICE = "device";

    private static final String KEY_NICKNAME = "nickname";

    private static final String KEY_PROTOCOL = "protocol";

    private static final String KEY_DEVICE_NAME = "deviceName";

    private static final String KEY_ACTIVE = "active";

    private static final String KEY_PARENT = "parent";

    private static final String KEY_DEVICE_TYPE = "deviceType";

    private static final String KEY_CURRENT_NAME = "currentName";

    private static final String KEY_NEW_NAME = "newName";

    private static final String KEY_TARGET_NAME = "targetName";

    private static final String KEY_SUCCESS = "success";

    private static final String KEY_PROPERTY_NAME = "propertyName";

    private static final String KEY_PROPERTY_VALUE = "propertyValue";

    private static final String KEY_PROPERTY = "property";

    private KadecotCoreApplication mApp;

    private DeviceManager(Context context) {
        mApp = (KadecotCoreApplication) context.getApplicationContext();
        mContext = mApp;
        mDeviceProtocols = new HashMap<String, DeviceProtocol>();
        registerDeviceProtocol(EchoManager.getInstance(mContext));
    }

    public static synchronized DeviceManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DeviceManager(context);
        }

        return sInstance;
    }

    public synchronized void start() {
        mStarted = true;

        for (DeviceProtocol protocol : mDeviceProtocols.values()) {
            protocol.start();
        }

        refreshDeviceList(0);
    }

    public synchronized void stop() {
        for (DeviceProtocol protocol : mDeviceProtocols.values()) {
            protocol.stop();
        }
        getLogger().unwatchAll();

        Notification.informAllOnUpdateList(mContext);

        mStarted = false;
    }

    public boolean isStarted() {
        return mStarted;
    }

    public void refreshDeviceList(int permissionLevel) {
        if (isStarted() == false) {
            return;
        }

        // Notification.informAllEmptyOnUpdateList(mContext);
        Notification.informAllInactiveDeviceList(mContext);
        for (DeviceProtocol protocol : mDeviceProtocols.values()) {
            protocol.refreshDeviceList();
        }
        // Notification.informAllOnUpdateList(mContext);

    }

    public synchronized void deleteAllDeviceData() {
        boolean started = mStarted;
        stop();
        for (DeviceProtocol protocol : mDeviceProtocols.values()) {
            protocol.deleteAllDeviceData();
        }
        getDeviceDatabase().deleteAllDeviceData();
        if (started) {
            start();
        }
    }

    /**
     * clientPermission が 0 なら 全ての操作を許可 1 なら 一部の操作の許可 protocolPermission が 0 なら
     * 一部のClientのみ許可 1 なら 全部のClientを許可 clientPermission : {0,1}
     * protocolPermission : {0,1}
     * 
     * @param clientPermissionLevel
     * @param protocolPermissionLevel
     * @return
     */
    public static boolean
            isAllowedPermission(int clientPermissionLevel, int protocolPermissionLevel) {
        return (clientPermissionLevel <= protocolPermissionLevel);
    }

    public JSONObject getDeviceList(int permissionLevel) {
        if (isStarted() == false) {
            return new JSONObject();
        }

        final JSONObject deviceList = new JSONObject();
        JSONArray list = new JSONArray();

        List<DeviceData> dataList = getDeviceDatabase().getDeviceDataList();

        for (DeviceData data : dataList) {
            JSONObject device = getDeviceInfo(data, permissionLevel);
            if (device != null) {
                list.put(device);
            }
        }
        try {
            deviceList.put(KEY_DEVICE, list);
        } catch (JSONException e) {
            // Never happens.
            Log.e(TAG, "JSON exception occurs at getDeviceList");
        }

        return deviceList;
    }

    public JSONObject getDeviceInfo(long deviceId, int permissionLevel) {
        DeviceData data = getDeviceDatabase().getDeviceData(deviceId);
        if (data != null) {
            return getDeviceInfo(data, permissionLevel);
        } else {
            return null;
        }
    }

    public JSONObject getDeviceInfo(DeviceData data, int permissionLevel) {
        JSONObject device = new JSONObject();
        try {
            device.put(KEY_NICKNAME, data.nickname);
            device.put(KEY_PROTOCOL, data.protocolName);

            DeviceProtocol protocol = mDeviceProtocols.get(data.protocolName);

            DeviceInfo info = null;
            if (protocol != null) {
                if (!(isAllowedPermission(permissionLevel, protocol.getAllowedPermissionLevel()))) {
                    return null;
                }
                info = protocol.getDeviceInfo(data.deviceId, "ja");
            }

            if (protocol != null && info != null) {

                device.put(KEY_ACTIVE, info.active);
                device.put(KEY_DEVICE_NAME, info.deviceName);
                device.put(KEY_DEVICE_TYPE, info.deviceType);
                device.put(KEY_PARENT, info.parent);

                return device;

            } else {
                // device.put("active", null);

                return null;

            }
        } catch (JSONException e) {
            return null;
        }
    }

    public void registerDeviceProtocol(DeviceProtocol protocol) {
        mDeviceProtocols.put(protocol.getProtocolName(), protocol);
    }

    public Response
            set(String nickname, ArrayList<DeviceProperty> propertyList, int permissionLevel) {

        if (isStarted() == false) {
            return new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, "Cannot access device");
        }

        DeviceData data = getDeviceDatabase().getDeviceData(nickname);
        if (data == null) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, "nickname not found");
        }

        DeviceProtocol protocol = mDeviceProtocols.get(data.protocolName);

        if (isAllowedPermission(permissionLevel, protocol.getAllowedPermissionLevel())) {
            try {
                List<DeviceProperty> list = protocol.set(data.deviceId, propertyList);

                DeviceInfo info = protocol.getDeviceInfo(data.deviceId, "jp");
                for (DeviceProperty p : list) {
                    completeAccessDeviceProperty(data, info, Logger.ACCESS_TYPE_SET, p);
                }

                return toAccessResponse(nickname, list);

            } catch (AccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return e.getErrorResponse();
            }
        } else {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, "permission denied");
        }

    }

    public Response
            get(String nickname, ArrayList<DeviceProperty> propertyList, int permissionLevel) {

        if (isStarted() == false) {
            return new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, "Cannot access device");
        }

        DeviceData data = getDeviceDatabase().getDeviceData(nickname);
        if (data == null) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, "nickname not found");
        }

        DeviceProtocol protocol = mDeviceProtocols.get(data.protocolName);

        if (isAllowedPermission(permissionLevel, protocol.getAllowedPermissionLevel())) {
            try {

                List<DeviceProperty> list = protocol.get(data.deviceId, propertyList);

                // log
                DeviceInfo info = protocol.getDeviceInfo(data.deviceId, "jp");
                for (DeviceProperty p : list) {
                    completeAccessDeviceProperty(data, info, Logger.ACCESS_TYPE_GET, p);

                }

                return toAccessResponse(nickname, list);
            } catch (AccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return e.getErrorResponse();
            }
        } else {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, "permission denied");
        }
    }

    private Response toAccessResponse(String nickname, List<DeviceProperty> list) {

        JSONObject result = new JSONObject();
        try {
            result.put("nickname", nickname);
            JSONArray array = new JSONArray();
            for (DeviceProperty p : list) {
                JSONObject prop = new JSONObject();

                prop.put("name", p.name);
                prop.put("value", p.value);
                prop.put("success", p.success);
                prop.put("message", p.message);

                array.put(prop);
            }
            result.put("property", array);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new Response(result);
    }

    public synchronized Response deleteDeviceData(JSONObject params) {

        if (isStarted() == false) {
            return new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, "Cannot access device");
        }

        if (params == null || params.length() < 1) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }

        String nickname = null;
        try {
            nickname = params.getString(KEY_TARGET_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }

        DeviceData data = getDeviceDatabase().getDeviceData(nickname);
        if (data == null) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, "nickname not found");
        }

        return deleteDeviceData(data);
    }

    public synchronized Response deleteDeviceData(DeviceData data) {

        DeviceProtocol protocol = mDeviceProtocols.get(data.protocolName);
        CannotProcessRequestException cpre = null;

        try {
            protocol.deleteDeviceData(data.deviceId);
        } catch (CannotProcessRequestException e) {
            cpre = e;
        }

        boolean b = getDeviceDatabase().deleteDeviceData(data.deviceId);
        if (b) {
            Notification.informAllOnDeviceDeleted(data.nickname, protocol
                    .getAllowedPermissionLevel());
        }
        if (cpre != null) {
            // error
            return cpre.getErrorResponse();
        } else {
            return new Response(new JSONObject());
        }
    }

    public synchronized Response deleteInactiveDevices(int permissionLevel) {

        if (isStarted() == false) {
            return new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, "Cannot access device");
        }

        List<DeviceData> dataList = getDeviceDatabase().getDeviceDataList();
        for (DeviceData data : dataList) {
            DeviceInfo info = null;
            DeviceProtocol protocol = mDeviceProtocols.get(data.protocolName);
            if (protocol != null) {
                info = protocol.getDeviceInfo(data.deviceId, "jp");
                if (info != null && !info.active) {
                    protocol.deleteDeviceData(data.deviceId);
                    getDeviceDatabase().deleteDeviceData(data.deviceId);
                }
            }
        }
        Notification.informAllOnUpdateList(mContext);
        return new Response(new JSONObject());
    }

    public synchronized Response changeNickname(JSONObject params) {

        if (isStarted() == false) {
            return new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, "Cannot access device");
        }

        if (params == null || params.length() < 2) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }

        String oldNickname = null;
        String newNickname = null;
        try {
            oldNickname = params.getString(KEY_CURRENT_NAME);
            newNickname = params.getString(KEY_NEW_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        DeviceData data = getDeviceDatabase().getDeviceData(oldNickname);
        if (data == null) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, "nickname not found");
        }
        boolean result = getDeviceDatabase().update(oldNickname, newNickname);
        if (!result) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, "New nickname exists.");
        }
        DeviceProtocol protocol = mDeviceProtocols.get(data.protocolName);

        Notification.informAllOnNicknameChanged(oldNickname, newNickname, protocol
                .getAllowedPermissionLevel());
        return new Response(new JSONObject());
    }

    public void onPropertyChanged(DeviceData data, List<DeviceProperty> list) {
        JSONObject obj = new JSONObject();
        if (data == null) {
            return;
        }
        try {
            obj.put(KEY_NICKNAME, data.nickname);
            JSONArray array = new JSONArray();
            for (DeviceProperty p : list) {
                JSONObject prop = new JSONObject();

                prop.put(KEY_PROPERTY_NAME, p.name);
                prop.put(KEY_PROPERTY_VALUE, p.value);
                prop.put(KEY_SUCCESS, p.success);

                array.put(prop);
            }
            obj.put(KEY_PROPERTY, array);
            Notification.informAllOnPropertyChanged(obj, 1);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // log
        DeviceProtocol protocol = mDeviceProtocols.get(data.protocolName);
        DeviceInfo info = protocol.getDeviceInfo(data.deviceId, "jp");
        for (DeviceProperty p : list) {
            completeAccessDeviceProperty(data, info, Logger.ACCESS_TYPE_GET, p);
        }

    }

    private DeviceDatabase getDeviceDatabase() {
        if (mDeviceDatabase == null) {
            mDeviceDatabase = DeviceDatabase.getInstance(mContext);
        }
        return mDeviceDatabase;
    }

    private Logger getLogger() {
        if (mLogger == null) {
            mLogger = Logger.getInstance(mContext);
        }
        return mLogger;
    }

    private void completeAccessDeviceProperty(final DeviceData data, DeviceInfo info,
            String accessType, DeviceProperty property) {
        getLogger().insertLog(data, info, accessType, property);
        mApp.getModifiableObject().onControlProperty(data, info, accessType, property);
    }
}
