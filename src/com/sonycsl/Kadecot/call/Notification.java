package com.sonycsl.Kadecot.call;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.sonycsl.Kadecot.server.ServerNetwork;
import com.sonycsl.Kadecot.server.ServerSettings;

public class Notification {
	@SuppressWarnings("unused")
	private static final String TAG = Notification.class
			.getSimpleName();
	private final Notification self = this;
	
	public static final String ON_DEVICE_FOUND = "onDeviceFound";
	public static final String ON_PROPERTY_CHANGED = "onPropertyChanged";
	public static final String ON_NOTIFY_SERVER_SETTINGS = "onNotifyServerSettings";
	
	private Notification(){}
	
	
	
	public static JSONArray onDeviceFound() {
		return new JSONArray();
	}
	
	public static JSONArray onPropertyChanged(JSONObject obj) {
		JSONArray ret = new JSONArray();
		ret.put(obj);
		return ret;
	}
	
	public static JSONArray onNotifyServerSettings(Context context) {
		JSONArray ret = new JSONArray();
		ret.put(ServerNetwork.getInstance(context).getNetworkInfoAsJSON());
		ServerSettings settings = ServerSettings.getInstance(context);
		ret.put(settings.getLocationJSONArray());
		ret.put(settings.isEnabledPersistentMode());
		ret.put(settings.isEnabledJSONPServer());
		ret.put(settings.isEnabledWebSocketServer());
		return ret;
	}

}
