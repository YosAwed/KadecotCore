
package com.sonycsl.Kadecot.server;

import android.content.Context;
import android.content.SharedPreferences;

import com.sonycsl.Kadecot.call.ErrorResponse;
import com.sonycsl.Kadecot.call.Response;
import com.sonycsl.Kadecot.device.DeviceManager;

import org.json.JSONException;
import org.json.JSONObject;

public class ServerSettings {
    @SuppressWarnings("unused")
    private static final String TAG = ServerSettings.class.getSimpleName();

    private final ServerSettings self = this;

    private static String PREFERENCES_NAME = "Settings";

    private static String KEY_LOCATION_LAT = "location_lat";

    private static String KEY_LOCATION_LNG = "location_lng";

    private static String KEY_WIFI_BSSID = "wifi_bssid";

    private static String KEY_WEBSOCKET_SERVER = "websocket_server";

    private static String KEY_JSONP_SERVER = "jsonp_server";

    private static String KEY_PERSISTENT_MODE = "persistent_mode";

    private static String KEY_SNAP_SERVER = "snap_server";

    private static final String KEY_LATITUDE = "latitude";

    private static final String KEY_LONGITUDE = "longitude";

    public enum ExecutionMode {
        APPLICATION, APPLICATION_BACKGROUND, WEBSOCKET_SERVER, WEBSOCKET_HTTP_SERVER,
    };

    private static ServerSettings sInstance = null;

    private final Context mContext;

    private final SharedPreferences mPreferences;

    private ServerManager mServerManager;

    private ServerNetwork mServerNetwork;

    private DeviceManager mDeviceManager;

    private ServerSettings(Context context) {
        mContext = context;
        mPreferences = mContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized ServerSettings getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ServerSettings(context);
        }
        return sInstance;
    }

    public void fullInitialize() {
        mPreferences.edit().clear().commit();

        getDeviceManager().deleteAllDeviceData();

        getServerManager().onChangedServerSettings();
    }

    public String[] getLocation() {
        String[] ret = new String[2];
        ret[0] = mPreferences.getString(KEY_LOCATION_LAT, "35.681183");
        ret[1] = mPreferences.getString(KEY_LOCATION_LNG, "139.765931");
        return ret;
    }

    public void setLocation(String lat, String lng) {
        mPreferences.edit().putString(KEY_LOCATION_LAT, lat).putString(KEY_LOCATION_LNG, lng)
                .commit();

        getServerManager().onChangedServerSettings();
    }

    public double[] getLocationDouble() {
        String[] loc = getLocation();
        double[] ret = {
                Double.parseDouble(loc[0]), Double.parseDouble(loc[1])
        };
        return ret;
    }

    public JSONObject getLocationJSONObject() {
        JSONObject ret = new JSONObject();

        double[] dLocation = getLocationDouble();

        try {
            ret.put(KEY_LATITUDE, dLocation[0]);
            ret.put(KEY_LONGITUDE, dLocation[1]);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    /*
     * public JSONArray getLocationJSONArray() { JSONArray location = new
     * JSONArray(); double[] dLocation = getLocationDouble(); try {
     * location.put(dLocation[0]); location.put(dLocation[1]); } catch
     * (JSONException e) { // TODO Auto-generated catch block
     * e.printStackTrace(); } return location; }
     */
    // network
    public String getWifiBSSID() {
        return mPreferences.getString(KEY_WIFI_BSSID, "");
    }

    private void setWifiBSSID(String bssid) {
        mPreferences.edit().putString(KEY_WIFI_BSSID, bssid).commit();

        // getNetwork().checkConnection();
        getServerManager().onChangedServerSettings();
    }

    public void removeWifiBSSID() {
        mPreferences.edit().remove(KEY_WIFI_BSSID).commit();

        // getNetwork().checkConnection();

        getServerManager().onChangedServerSettings();
    }

    public Response registerNetwork() {
        String bssid = getNetwork().getCurrentConnectionBSSID();
        if (bssid != null) {
            setWifiBSSID(bssid);
            return new Response(null);
        } else {
            return new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE,
                    "cannot register this network");
        }
    }

    public Response unregisterNetwork() {
        removeWifiBSSID();
        return new Response(null);
    }

    public void enableWebSocketServer(boolean enabled) {
        mPreferences.edit().putBoolean(KEY_WEBSOCKET_SERVER, enabled).commit();

        getServerManager().onChangedServerSettings();
    }

    public boolean isEnabledWebSocketServer() {
        return mPreferences.getBoolean(KEY_WEBSOCKET_SERVER, false);
    }

    public void enableJSONPServer(boolean enabled) {
        mPreferences.edit().putBoolean(KEY_JSONP_SERVER, enabled).commit();

        getServerManager().onChangedServerSettings();
    }

    public boolean isEnabledJSONPServer() {
        return mPreferences.getBoolean(KEY_JSONP_SERVER, false);
    }

    public void enableSnapServer(boolean enabled) {
        mPreferences.edit().putBoolean(KEY_SNAP_SERVER, enabled).commit();

        getServerManager().onChangedServerSettings();
    }

    public boolean isEnabledSnapServer() {
        return mPreferences.getBoolean(KEY_SNAP_SERVER, false);
    }

    public void enablePersistentMode(boolean enabled) {
        mPreferences.edit().putBoolean(KEY_PERSISTENT_MODE, enabled).commit();

        getServerManager().onChangedServerSettings();
    }

    public boolean isEnabledPersistentMode() {
        return mPreferences.getBoolean(KEY_PERSISTENT_MODE, false);
    }

    private ServerManager getServerManager() {
        if (mServerManager == null) {
            mServerManager = ServerManager.getInstance(mContext);
        }
        return mServerManager;
    }

    private DeviceManager getDeviceManager() {
        if (mDeviceManager == null) {
            mDeviceManager = DeviceManager.getInstance(mContext);
        }
        return mDeviceManager;
    }

    private ServerNetwork getNetwork() {
        if (mServerNetwork == null) {
            mServerNetwork = ServerNetwork.getInstance(mContext);
        }
        return mServerNetwork;
    }
}
