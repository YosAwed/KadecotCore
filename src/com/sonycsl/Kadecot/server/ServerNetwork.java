package com.sonycsl.Kadecot.server;

import java.net.SocketException;

import org.json.JSONException;
import org.json.JSONObject;

import com.sonycsl.echo.EchoUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class ServerNetwork {
	@SuppressWarnings("unused")
	private static final String TAG = ServerNetwork.class.getSimpleName();
	private final ServerNetwork self = this;

	private static ServerNetwork sInstance = null;
	
	private final Context mContext;
	private final BroadcastReceiver mConnectionReceiver;
	private boolean mConnectedHomeNetwork;
	
	private ServerManager mServerManager;
	private ServerSettings mServerSettings;
	
	private ServerNetwork(Context context) {
		mContext = context.getApplicationContext();
		
		mConnectionReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				checkConnection();
			}
		};
	}
	
	public static synchronized ServerNetwork getInstance(Context context) {
		if(sInstance == null) {
			sInstance = new ServerNetwork(context);
			sInstance.init();
		}
		
		return sInstance;
	}
	
	protected void init() {
		mServerManager = ServerManager.getInstance(mContext);
		mServerSettings = ServerSettings.getInstance(mContext);
		
		mConnectedHomeNetwork = isConnectedHomeNetwork();

		onNetworkChanged();
	}
	public void startConnectionReceiver() {
		checkConnection();
		
		mContext.registerReceiver(mConnectionReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
	}
	//TODO
	public void stopConnectionReceiver() {
		mContext.unregisterReceiver(mConnectionReceiver);
		//mServerManager.stopHomeNetwork();
	}
	
	public void checkConnection() {
		boolean connected = isConnectedHomeNetwork();
		
		if(mConnectedHomeNetwork != connected) {
			mConnectedHomeNetwork = isConnectedHomeNetwork();
			onNetworkChanged();
		}
	}
	

	protected void onNetworkChanged() {
		if(mConnectedHomeNetwork) {
			mServerManager.startHomeNetwork();
		} else {
			mServerManager.stopHomeNetwork();
		}
	}
	

	public boolean isConnectedHomeNetwork() {

		ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		
		if(info != null && info.getType() == ConnectivityManager.TYPE_WIFI && info.isConnected()) {
			WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wInfo = wifiManager.getConnectionInfo();
			if(wInfo != null && mServerSettings.getWifiBSSID().equalsIgnoreCase(wInfo.getBSSID())) {
				return true;
			}
		}
		return false;
	}
	
	public String getCurrentConnectionBSSID() {

		ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();

		if(info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {

			WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wInfo = wifiManager.getConnectionInfo();
			return wInfo.getBSSID();
		} else {
			return null;
		}
	}
	
	public JSONObject getNetworkInfoAsJSON() {
		ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		JSONObject value = new JSONObject();
		try {
			if(info == null) {
				value.put("isConnected", false);
			} else {
				value.put("isConnected", info.isConnected());
				value.put("type", info.getTypeName());
				if(info.getType() == ConnectivityManager.TYPE_WIFI) {
					WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
					WifiInfo wInfo = wifiManager.getConnectionInfo();
					if(wInfo != null) {
						// from android 4.2,there is extra quotation.
						String ssid = wInfo.getSSID();
						if (ssid.startsWith("\"") && ssid.endsWith("\"")){
							ssid = ssid.substring(1, ssid.length()-1);
						}
						value.put("SSID", wInfo.getSSID());
					}
				}
				value.put("ip",getIPAddress());

				value.put("isDeviceAccessible", isConnectedHomeNetwork());
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}
	
	public String getIPAddress(){
		WifiManager wManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = wManager.getConnectionInfo();
		int ipAddress = wInfo.getIpAddress();
		return String.format("%01d.%01d.%01d.%01d",(ipAddress>>0)&0xff, (ipAddress>>8)&0xff,
												(ipAddress>>16)&0xff, (ipAddress>>24)&0xff);
	}
}
