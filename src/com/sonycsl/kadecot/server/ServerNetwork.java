/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;

public class ServerNetwork {

    private static ServerNetwork sInstance = null;

    private final Context mContext;

    private final BroadcastReceiver mConnectionReceiver;

    private int mConnectedHomeNetwork = UNDEFINED;

    public static final int CONNECTED = 0;

    public static final int UNCONNECTED = 1;

    public static final int UNDEFINED = 2;

    private ServerManager mServerManager;

    private final ConnectivityManager mConnectivityManager;

    private final WifiManager mWifiManager;

    private boolean mWatchingConnection = false;

    private ServerNetwork(Context context) {
        mContext = context.getApplicationContext();

        mConnectivityManager =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        mConnectionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                watchConnection();
            }
        };

    }

    public static synchronized ServerNetwork getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ServerNetwork(context);
        }

        return sInstance;
    }

    public void startWatchingConnection() {
        mWatchingConnection = true;
        mConnectedHomeNetwork = UNDEFINED;

        watchConnection();

        mContext.registerReceiver(mConnectionReceiver, new IntentFilter(
                "android.net.conn.CONNECTIVITY_CHANGE"));
    }

    public void stopWatchingConnection() {
        mWatchingConnection = false;
        mContext.unregisterReceiver(mConnectionReceiver);
        mConnectedHomeNetwork = UNDEFINED;
        onNetworkChanged();
    }

    public void watchConnection() {
        if (mWatchingConnection == false) {
            return;
        }
        int connected = isConnectedHomeNetwork();
        // Dbg.print("checkConnection:"+connected);
        if (mConnectedHomeNetwork != connected) {
            mConnectedHomeNetwork = connected;
            onNetworkChanged();
        }
    }

    private void onNetworkChanged() {
        if (mConnectedHomeNetwork == CONNECTED) {
            getServerManager().startHomeNetwork();
        } else {
            getServerManager().stopHomeNetwork();
        }
    }

    public int isConnectedHomeNetwork() {
        if (Build.PRODUCT.startsWith("sdk")) {
            return CONNECTED;
        }

        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
        if (info == null) {
            return UNCONNECTED;
        }

        switch (info.getType()) {
            case ConnectivityManager.TYPE_ETHERNET:
                return CONNECTED;
            case ConnectivityManager.TYPE_WIFI:
                if (info.isConnected()) {
                    return CONNECTED;
                } else {
                    return UNCONNECTED;
                }
        }

        return UNCONNECTED;
    }

    public String getCurrentConnectionBSSID() {

        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();

        if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {

            WifiInfo wInfo = mWifiManager.getConnectionInfo();
            return wInfo.getBSSID();
        } else {
            return null;
        }
    }

    public JSONObject getNetworkInfoAsJSON() {

        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
        JSONObject value = new JSONObject();
        try {
            if (info == null) {
                value.put("isConnected", false);
            } else {
                value.put("isConnected", info.isConnected());
                value.put("type", info.getTypeName());
                if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                    WifiInfo wInfo = mWifiManager.getConnectionInfo();
                    if (wInfo != null) {
                        // from android 4.2,there is extra quotation.
                        String ssid = wInfo.getSSID();
                        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                            ssid = ssid.substring(1, ssid.length() - 1);
                        }
                        value.put("SSID", wInfo.getSSID());
                    }
                }
                value.put("ipv4", getIPAddress());

                value.put("isDeviceAccessible", (isConnectedHomeNetwork() == CONNECTED));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return value;
    }

    public String getIPAddress() {
        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            WifiInfo wInfo = mWifiManager.getConnectionInfo();
            int ipAddress = wInfo.getIpAddress();
            return String.format(Locale.getDefault(), "%01d.%01d.%01d.%01d",
                    (ipAddress >> 0) & 0xff,
                    (ipAddress >> 8) & 0xff,
                    (ipAddress >> 16) & 0xff,
                    (ipAddress >> 24) & 0xff);
        } else {
            InetAddress address = null;
            try {
                address = getLocalIpAddress();
            } catch (SocketException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (address == null) {
                return "";
            } else {
                return address.getHostAddress();
            }
        }
    }

    public static InetAddress getLocalIpAddress() throws SocketException {
        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface intf = en.nextElement();
            Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
            while (enumIpAddr.hasMoreElements()) {
                InetAddress inetAddress = enumIpAddr.nextElement();
                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                    return inetAddress;
                }
            }
        }
        return null;
    }

    private ServerManager getServerManager() {
        if (mServerManager == null) {
            mServerManager = ServerManager.getInstance(mContext);
        }
        return mServerManager;
    }
}
