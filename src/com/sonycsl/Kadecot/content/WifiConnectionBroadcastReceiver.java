/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

abstract public class WifiConnectionBroadcastReceiver extends BroadcastReceiver {

    private final Context mContext;

    public WifiConnectionBroadcastReceiver(Context context) {
        mContext = context;
    }

    private boolean isConnected(Intent intent) {
        ConnectivityManager manager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }

        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null) {
            return false;
        }

        if (info.getType() != ConnectivityManager.TYPE_WIFI) {
            return false;
        }

        return info.isConnected();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            return;
        }

        if (!intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            return;
        }

        if (isConnected(intent)) {
            onConnected();
        } else {
            onDisconnected();
        }
    }

    abstract public void onConnected();

    abstract public void onDisconnected();
}
