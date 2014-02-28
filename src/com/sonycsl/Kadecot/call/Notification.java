
package com.sonycsl.Kadecot.call;

import com.sonycsl.Kadecot.device.DeviceManager;
import com.sonycsl.Kadecot.server.ServerNetwork;
import com.sonycsl.Kadecot.server.ServerSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import java.util.HashSet;

public class Notification {
    @SuppressWarnings("unused")
    private static final String TAG = Notification.class.getSimpleName();

    public static final String ON_DEVICE_FOUND = "onDeviceFound";

    public static final String ON_PROPERTY_CHANGED = "onPropertyChanged";

    public static final String ON_SERVER_STATUS_UPDATED = "onServerStatusUpdated";

    public static final String ON_NICKNAME_CHANGED = "onNicknameChanged";

    public static final String ON_DEVICE_DELETED = "onDeviceDeleted";

    public static final String ON_DEVICE_LIST_UPDATED = "onDeviceListUpdated";

    private Notification() {
    }

    public static void informAllOnDeviceFound(JSONObject device, int protocolPermissionLevel) {
        JSONObject params = new JSONObject();
        JSONArray deviceArray = new JSONArray();
        deviceArray.put(device);
        try {
            params.put("device", deviceArray);
        } catch (JSONException e) {
            // Never happens
            e.printStackTrace();
        }
        KadecotCall.informAll(ON_DEVICE_FOUND, params, protocolPermissionLevel);
    }

    public static JSONObject getParamsOnUpdateList(Context context, int clientPermissionLevel) {
        return DeviceManager.getInstance(context).getDeviceList(clientPermissionLevel);
    }

    public static void informAllOnUpdateList(Context context) {
        JSONObject params0 = getParamsOnUpdateList(context, Permission.ALL);
        JSONObject params1 = getParamsOnUpdateList(context, Permission.LIMITED);
        HashSet<KadecotCall> calls = KadecotCall.getKadecotCalls();
        for (KadecotCall kc : calls) {
            if (kc.getPermissionLevel() == Permission.ALL) {
                kc.sendNotification(ON_DEVICE_LIST_UPDATED, params0);
            } else {
                kc.sendNotification(ON_DEVICE_LIST_UPDATED, params1);
            }
        }
    }

    public static void informAllInactiveDeviceList(Context context) {
        JSONObject params0 = getParamsOnUpdateList(context, 0);
        JSONObject params1 = getParamsOnUpdateList(context, 1);
        try {
            params0.put("active", false);
            params1.put("active", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HashSet<KadecotCall> calls = KadecotCall.getKadecotCalls();
        for (KadecotCall kc : calls) {
            if (kc.getPermissionLevel() == Permission.ALL) {
                kc.sendNotification(ON_DEVICE_LIST_UPDATED, params0);
            } else {
                kc.sendNotification(ON_DEVICE_LIST_UPDATED, params1);
            }
        }
    }

    public static void informAllOnPropertyChanged(JSONObject params, int protocolPermissionLevel) {
        KadecotCall.informAll(ON_PROPERTY_CHANGED, params, protocolPermissionLevel);
    }

    public static JSONObject getParamsOnNotifyServerSettings(Context context) {
        JSONObject ret = new JSONObject();
        try {
            ret.put("networkInfo", ServerNetwork.getInstance(context).getNetworkInfoAsJSON());
            ServerSettings settings = ServerSettings.getInstance(context);
            ret.put("location", settings.getLocationJSONObject());
            JSONObject serverMode = new JSONObject();
            serverMode.put("persistent", settings.isEnabledPersistentMode());
            serverMode.put("jsonpServer", settings.isEnabledJSONPServer());
            serverMode.put("websocketServer", settings.isEnabledWebSocketServer());
            serverMode.put("snapServer", settings.isEnabledSnapServer());
            ret.put("serverMode", serverMode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void informAllOnNotifyServerSettings(Context context) {
        JSONObject params = getParamsOnNotifyServerSettings(context);
        KadecotCall.informAll(ON_SERVER_STATUS_UPDATED, params);
    }

    public static void informAllOnNicknameChanged(String currentName, String newName,
            int protocolPermissionLevel) {
        JSONObject notificationParam = new JSONObject();
        try {
            notificationParam.put("oldName", currentName);
            notificationParam.put("currentName", newName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        KadecotCall.informAll(ON_NICKNAME_CHANGED, notificationParam, protocolPermissionLevel);
    }

    public static void informAllOnDeviceDeleted(String nickname, int protocolPermissionLevel) {
        JSONObject notificationParam = new JSONObject();
        try {
            notificationParam.put("targetName", nickname);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        KadecotCall.informAll(ON_DEVICE_DELETED, notificationParam, protocolPermissionLevel);

    }

}
