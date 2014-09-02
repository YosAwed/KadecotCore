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

import java.util.Locale;

public final class ConnectivityManagerUtil {

    private ConnectivityManagerUtil() {
        super();
    }

    private static NetworkInfo getActiveNetworkInfoOnWifi(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return null;
        }

        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null) {
            return null;
        }

        if (info.getType() != ConnectivityManager.TYPE_WIFI) {
            return null;
        }

        return info;
    }

    public static boolean isConnected(Context context) {
        if (Build.PRODUCT.startsWith("sdk")) {
            return true;
        }

        NetworkInfo info = getActiveNetworkInfoOnWifi(context);
        if (info == null) {
            return false;
        }

        return info.isConnected();
    }

    public static WifiInfo getWifiInfo(Context context) {
        NetworkInfo info = getActiveNetworkInfoOnWifi(context);
        if (info == null) {
            return null;
        }

        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (manager == null) {
            return null;
        }

        return manager.getConnectionInfo();
    }

    public static String getIPAddress(Context context) {
        WifiInfo wInfo = getWifiInfo(context);
        if (wInfo == null) {
            return null;
        }

        int ipAddress = wInfo.getIpAddress();
        return String.format(Locale.getDefault(), "%01d.%01d.%01d.%01d",
                (ipAddress >> 0) & 0xff,
                (ipAddress >> 8) & 0xff,
                (ipAddress >> 16) & 0xff,
                (ipAddress >> 24) & 0xff);
    }
}
