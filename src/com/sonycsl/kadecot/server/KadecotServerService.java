/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.server;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.sonycsl.kadecotcore.R;

public class KadecotServerService extends Service {

    private final KadecotServerService self = this;

    protected static final int FOREGROUND_ID = 100;

    protected final ServerBinder mBinder;

    protected boolean mForeground = false;

    public KadecotServerService() {
        super();
        mBinder = new ServerBinder(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopServer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startServer();

        return START_STICKY;
    }

    public void changeNotification() {
        if (mForeground == false) {
            return;
        }

        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(getPackageName());
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(self, 0, intent, 0);
        String contentText;
        if (ServerNetwork.getInstance(self).isConnectedHomeNetwork() == ServerNetwork.CONNECTED) {
            contentText = ServerNetwork.getInstance(self).getIPAddress() + "\n";
        } else {
            contentText = "Network Error\n";
        }
        contentText +=
                "HomeNet:"
                        + (ServerNetwork.getInstance(self).isConnectedHomeNetwork() == ServerNetwork.CONNECTED ? "ON\n"
                                : "OFF\n");
        contentText +=
                "WebSocket:"
                        + (ServerManager.getInstance(self).isStartedWebSocketServer() ? "ON\n"
                                : "OFF\n");
        contentText +=
                "Http:"
                        + (ServerManager.getInstance(self).isStartedJSONPServer() ? "ON\n"
                                : "OFF\n");
        contentText +=
                "Snap:"
                        + (ServerManager.getInstance(self).isStartedSnapServer() ? "ON\n" : "OFF\n");

        Notification notice = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.icon)
                .setTicker("Kadecot Server").setWhen(System.currentTimeMillis())
                .setContentTitle("Kadecot Server").setContentText(contentText)
                .setContentIntent(pendIntent).build();
        notice.flags |= Notification.FLAG_NO_CLEAR;
        self.startForeground(FOREGROUND_ID, notice);
    }

    public void startForeground() {
        mForeground = true;
        changeNotification();
    }

    public void stopForeground() {

        self.stopForeground(true);
        mForeground = false;
    }

    public void startServer() {
        ServerManager.getInstance(self).startServer(this);
    }

    public void stopServer() {
        ServerManager.getInstance(self).stopServer(this);
    }

}
