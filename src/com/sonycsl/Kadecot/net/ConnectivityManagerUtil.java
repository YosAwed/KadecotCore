/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class ConnectivityManagerUtil {

    private ConnectivityManagerUtil() {
        super();
    }

    public static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return null;
        }

        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null) {
            return null;
        }

        switch (info.getType()) {
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_ETHERNET:
                return info;
            default:
                return null;
        }
    }

    public static boolean isConnected(Context context) {
        if (Build.PRODUCT.startsWith("sdk")) {
            return true;
        }

        NetworkInfo info = getActiveNetworkInfo(context);
        if (info == null) {
            return false;
        }

        return info.isConnected();
    }

    public static NetworkID getCurrentNetworkID(Context context) {
        NetworkInfo info = getActiveNetworkInfo(context);
        NetworkID networkID;

        if (info == null) {
            return null;
        }

        switch (info.getType()) {
            case ConnectivityManager.TYPE_ETHERNET:
                networkID = new NetworkID(NetworkID.ETHER_SSID, NetworkID.ETHER_BSSID);
                break;

            case ConnectivityManager.TYPE_WIFI:
                WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                if (manager == null) {
                    return null;
                }
                WifiInfo wifiInfo = manager.getConnectionInfo();
                if (wifiInfo == null || wifiInfo.getBSSID() == null || wifiInfo.getSSID() == null
                        || wifiInfo.getBSSID().equals("") || wifiInfo.getSSID().equals("")) {
                    return null;
                }
                networkID = new NetworkID(wifiInfo.getSSID().replace("\"", ""), wifiInfo.getBSSID());
                break;

            default:
                return null;
        }

        return networkID;
    }

    public static String getIPAddress(Context context) {

        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface
                    .getNetworkInterfaces());

            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());

                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase(Locale.getDefault());
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        if (isIPv4) {
                            /* Exclude 3G/LTE network */
                            if (!intf.getName().startsWith("rmnet")) {
                                return sAddr;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        }
        return "";
    }
}
