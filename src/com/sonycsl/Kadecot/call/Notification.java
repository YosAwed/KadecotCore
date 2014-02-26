
package com.sonycsl.Kadecot.call;

import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.sonycsl.Kadecot.device.DeviceManager;
import com.sonycsl.Kadecot.server.ServerNetwork;
import com.sonycsl.Kadecot.server.ServerSettings;

public class Notification {
    @SuppressWarnings("unused")
    private static final String TAG = Notification.class.getSimpleName();

    private final Notification self = this;

    public static final String ON_DEVICE_FOUND = "onDeviceFound";

    public static final String ON_PROPERTY_CHANGED = "onPropertyChanged";

    public static final String ON_NOTIFY_SERVER_SETTINGS = "onNotifyServerSettings";

    public static final String ON_NICKNAME_CHANGED = "onNicknameChanged";

    public static final String ON_DEVICE_DELETED = "onDeviceDeleted";

    public static final String ON_UPDATE_LIST = "onUpdateList";

    private Notification() {
    }

    public static void informAllOnDeviceFound(JSONObject device, int protocolPermissionLevel) {
        JSONArray params = new JSONArray();
        params.put(device);
        KadecotCall.informAll(ON_DEVICE_FOUND, params, protocolPermissionLevel);
    }

    public static JSONArray getParamsOnUpdateList(Context context, int clientPermissionLevel) {
        return DeviceManager.getInstance(context).list(clientPermissionLevel);
    }

    public static void informAllOnUpdateList(Context context) {
        JSONArray params0 = getParamsOnUpdateList(context, 0);
        JSONArray params1 = getParamsOnUpdateList(context, 1);
        HashSet<KadecotCall> calls = KadecotCall.getKadecotCalls();
        for (KadecotCall kc : calls) {
            if (kc.getPermissionLevel() == 0) {
                kc.sendNotification(ON_UPDATE_LIST, params0);
            } else {
                kc.sendNotification(ON_UPDATE_LIST, params1);
            }
        }
    }

    public static void informAllInactiveDeviceList(Context context) {
        JSONArray params0 = getParamsOnUpdateList(context, 0);
        JSONArray params1 = getParamsOnUpdateList(context, 1);
        HashSet<KadecotCall> calls = KadecotCall.getKadecotCalls();
        for (int i = 0; i < params0.length(); i++) {
            try {
                JSONObject obj = params0.getJSONObject(i);
                obj.put("active", false);
                params0.put(i, obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < params1.length(); i++) {
            try {
                JSONObject obj = params1.getJSONObject(i);
                obj.put("active", false);
                params1.put(i, obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        for (KadecotCall kc : calls) {
            if (kc.getPermissionLevel() == 0) {
                kc.sendNotification(ON_UPDATE_LIST, params0);
            } else {
                kc.sendNotification(ON_UPDATE_LIST, params1);
            }
        }
    }

    public static void informAllOnPropertyChanged(JSONObject obj, int protocolPermissionLevel) {
        JSONArray params = new JSONArray();
        params.put(obj);
        KadecotCall.informAll(ON_PROPERTY_CHANGED, params, protocolPermissionLevel);
    }

    public static JSONArray getParamsOnNotifyServerSettings(Context context) {
        JSONArray ret = new JSONArray();
        ret.put(ServerNetwork.getInstance(context).getNetworkInfoAsJSON());
        ServerSettings settings = ServerSettings.getInstance(context);
        ret.put(settings.getLocationJSONObject());
        ret.put(settings.isEnabledPersistentMode());
        ret.put(settings.isEnabledJSONPServer());
        ret.put(settings.isEnabledWebSocketServer());
        ret.put(settings.isEnabledSnapServer());
        return ret;
    }

    public static void informAllOnNotifyServerSettings(Context context) {
        JSONArray params = getParamsOnNotifyServerSettings(context);
        KadecotCall.informAll(ON_NOTIFY_SERVER_SETTINGS, params);
    }

    public static void informAllOnNicknameChanged(String oldNickname, String newNickname,
            int protocolPermissionLevel) {
        JSONArray params = new JSONArray();
        params.put(oldNickname);
        params.put(newNickname);

        KadecotCall.informAll(ON_NICKNAME_CHANGED, params, protocolPermissionLevel);
    }

    public static void informAllOnDeviceDeleted(String nickname, int protocolPermissionLevel) {
        JSONArray params = new JSONArray();
        params.put(nickname);

        KadecotCall.informAll(ON_DEVICE_DELETED, params, protocolPermissionLevel);

    }

}
