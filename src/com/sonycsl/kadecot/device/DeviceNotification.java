/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.device;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.sonycsl.kadecotcore.R;

import org.json.JSONArray;
import org.json.JSONException;

public class DeviceNotification {

    protected static final int ERROR_NOTIFICATION_ID = 137;

    public static final String INTENT_ACTION_LAUNCH_FROM_DEVICE_ERROR_NOTIFICATION =
            "com.sonycsl.kadecot.LAUNCH_FROM_DEVICE_ERROR_NOTIFICATION";

    protected final Context mContext;

    protected Notification mNotification = null;

    protected int mNotificationId = 0;

    public DeviceNotification(Context context) {
        mContext = context;
    }

    public DeviceNotification buildEchoErrorNotification(String nickname, JSONArray errorValue)
            throws JSONException {

        // Intent intent =
        // pm.getLaunchIntentForPackage("com.sonycsl.ARMoekaden");
        Intent intent = new Intent(INTENT_ACTION_LAUNCH_FROM_DEVICE_ERROR_NOTIFICATION);
        // intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        byte val0 = (byte) errorValue.getInt(0);
        byte val1 = (byte) errorValue.getInt(1);
        String errorInfo = "0x" + String.format("%02x", val0) + String.format("%02x", val1);
        String contentText = "[" + nickname + "]" + errorInfo;

        mNotification = new NotificationCompat.Builder(mContext).setSmallIcon(R.drawable.icon)
                .setTicker("ERROR").setWhen(System.currentTimeMillis()).setContentText(contentText)
                .setContentTitle("ERROR").setContentIntent(pendIntent).build();

        mNotificationId = ERROR_NOTIFICATION_ID;

        return this;
    }

    public void show() {
        if (mNotification != null) {
            NotificationManager nm =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(mNotificationId, mNotification);
        }
    }

}
