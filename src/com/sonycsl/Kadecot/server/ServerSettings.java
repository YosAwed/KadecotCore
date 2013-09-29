package com.sonycsl.Kadecot.server;

import java.lang.reflect.Method;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.sonycsl.Kadecot.call.ErrorResponse;
import com.sonycsl.Kadecot.call.Response;
import com.sonycsl.Kadecot.device.DeviceManager;

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

	public enum ExecutionMode {
		APPLICATION,
		APPLICATION_BACKGROUND,
		WEBSOCKET_SERVER,
		WEBSOCKET_HTTP_SERVER,
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
		if(sInstance == null) {
			sInstance = new ServerSettings(context);
			sInstance.init();
		}
		return sInstance;
	}
	
	protected void init() {
		mServerManager = ServerManager.getInstance(mContext);
		mDeviceManager = DeviceManager.getInstance(mContext);
		mServerNetwork = ServerNetwork.getInstance(mContext);

	}
	
	public void fullInitialize() {
		removeWifiBSSID();
		
		mDeviceManager.deleteAllDeviceData();
		
		mServerManager.onChangedServerSettings();
	}
	
	public String[] getLocation() {
		String[] ret = new String[2];
		ret[0] = mPreferences.getString(KEY_LOCATION_LAT, "35.681183");
		ret[1] = mPreferences.getString(KEY_LOCATION_LNG, "139.765931");
		return ret;
	}
	
	public void setLocation(String lat, String lng) {
		mPreferences.edit().putString(KEY_LOCATION_LAT, lat).putString(KEY_LOCATION_LNG, lng).commit();
		
		mServerManager.onChangedServerSettings();
	}
	
	
	public double[] getLocationDouble() {
		String[] loc = getLocation();
		double[] ret = {Double.parseDouble(loc[0]),Double.parseDouble(loc[1])};
		return ret;
	}

	public JSONArray getLocationJSONArray() {
		JSONArray location = new JSONArray();
		double[] dLocation = getLocationDouble();
		try {
			location.put(dLocation[0]);
			location.put(dLocation[1]);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return location;
	}
	
	// network
	public String getWifiBSSID() {
		return mPreferences.getString(KEY_WIFI_BSSID, "");
	}
	
	private void setWifiBSSID(String bssid) {
		mPreferences.edit().putString(KEY_WIFI_BSSID, bssid).commit();

		mServerNetwork.checkConnection();
		
		mServerManager.onChangedServerSettings();
	}
	
	public void removeWifiBSSID() {
		mPreferences.edit().remove(KEY_WIFI_BSSID).commit();

		mServerNetwork.checkConnection();
		
		mServerManager.onChangedServerSettings();
	}

	public Response registerNetwork() {
		String bssid = mServerNetwork.getCurrentConnectionBSSID();
		if(bssid != null) {
			setWifiBSSID(bssid);
			return new Response(null);
		} else {
			return new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, "cannot register this network");
		}
	}
	
	public Response unregisterNetwork() {
		removeWifiBSSID();
		return new Response(null);
	}
	
	
	
	public void enableWebSocketServer(boolean enabled) {
		mPreferences.edit().putBoolean(KEY_WEBSOCKET_SERVER, enabled).commit();
		
		mServerManager.onChangedServerSettings();
	}
	
	public boolean isEnabledWebSocketServer() {
		return mPreferences.getBoolean(KEY_WEBSOCKET_SERVER, false);
	}
	
	public void enableJSONPServer(boolean enabled) {
		mPreferences.edit().putBoolean(KEY_JSONP_SERVER, enabled).commit();
		
		mServerManager.onChangedServerSettings();
	}
	
	public boolean isEnabledJSONPServer() {
		return mPreferences.getBoolean(KEY_JSONP_SERVER, false);
	}
	
	public void enablePersistentMode(boolean enabled) {
		mPreferences.edit().putBoolean(KEY_PERSISTENT_MODE, enabled).commit();
		
		mServerManager.onChangedServerSettings();
	}
	
	public boolean isEnabledPersistentMode() {
		return mPreferences.getBoolean(KEY_PERSISTENT_MODE, false);
	}
}
