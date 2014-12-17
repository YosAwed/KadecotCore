/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;

import com.sonycsl.Kadecot.content.WifiConnectionBroadcastReceiver;
import com.sonycsl.Kadecot.core.R;
import com.sonycsl.Kadecot.net.ConnectivityManagerUtil;
import com.sonycsl.Kadecot.preference.DeveloperModePreference;
import com.sonycsl.Kadecot.preference.KadecotServicePreference;
import com.sonycsl.Kadecot.preference.WebSocketServerPreference;
import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.server.websocket.ClientAuthCallback;
import com.sonycsl.Kadecot.server.websocket.OpeningHandshake;
import com.sonycsl.Kadecot.wamp.WampTopology;
import com.sonycsl.Kadecot.wamp.util.WampLocatorCallback;
import com.sonycsl.wamp.WampPeer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

public final class KadecotService extends Service implements ClientAuthCallback,
        WampLocatorCallback {

    public static final String CUSTOM_URL_SCHEME = "kadecot:";

    private static final int FOREGROUND_ID = 100;
    private static final String ACCESS_TOKEN_PARAM_KEY = "access_token";

    public static final int MSGR_INTERFACE_VERSION = 1;

    public static final String MSGR_KEY_REQ_WAMP = "requestWamp";

    public static final String MSGR_KEY_CONNECT = "connect";

    public static final String MSGR_KEY_ENABLE_WS = "enableWebsocket";
    public static final String MSGR_KEY_GET_WS_STATUS = "getWebsocketStatus";
    public static final String MSGR_KEY_WS_STATUS = "websocketStatus";

    public static final String MSGR_KEY_ENABLE_DEVMODE = "enableDevMode";
    public static final String MSGR_KEY_GET_DEVMODE_STATUS = "getDevModeStatus";
    public static final String MSGR_KEY_DEVMODE_STATUS = "devModeStatus";

    private Map<String, OnChangeListener> mSupportedPrefs;

    private ServerManager mServerManager;

    private SharedPreferences mPreferences;

    private RequestHandler mHandler;
    private Messenger mMessenger;

    public interface OnChangeListener {
        public void onChange(String key);
    }

    private OnSharedPreferenceChangeListener mListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
            if (!mSupportedPrefs.keySet().contains(key)) {
                return;
            }
            mSupportedPrefs.get(key).onChange(key);
            changeNotification(sp);
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null) {
                return;
            }

            if (!intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                return;
            }

            Bundle extra = intent.getExtras();
            if (extra == null) {
                mServerManager.stop();
                return;
            }

            if (extra.getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
                mServerManager.stop();
                return;
            }

            if (extra.getBoolean(ConnectivityManager.EXTRA_IS_FAILOVER, false)) {
                mServerManager.stop();
                return;
            }

            mServerManager.start();
        }
    };

    private MulticastDnsTrigger mTrigger;
    private WampTopology mTopology;

    public KadecotService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mTopology = new WampTopology();
        mHandler = new RequestHandler(this);
        mHandler.setWampLocatorCallback(this);
        mMessenger = new Messenger(mHandler);
        mServerManager = new ServerManager(this, mTopology);
        mServerManager.setClientAuthCallback(this);
        mPreferences = getSharedPreferences(getString(R.string.preferences_file_name),
                MODE_PRIVATE);

        mSupportedPrefs = new HashMap<String, OnChangeListener>();
        mSupportedPrefs.put(getString(R.string.persistent_mode_preference_key),
                new OnChangeListener() {

                    @Override
                    public void onChange(String key) {
                    }
                });
        mSupportedPrefs.put(getString(R.string.websocket_preference_key), new OnChangeListener() {

            @Override
            public void onChange(String key) {
                mHandler.sendWebsocketStatus(WebSocketServerPreference
                        .isEnabled(getApplicationContext()));
            }
        });
        mSupportedPrefs.put(getString(R.string.developer_mode_preference_key),
                new OnChangeListener() {

                    @Override
                    public void onChange(String key) {
                        mHandler.sendDeveloperModeStatus(DeveloperModePreference
                                .isEnabled(getApplicationContext()));
                    }
                });
        mPreferences.registerOnSharedPreferenceChangeListener(mListener);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(ServerManager.ACTION_APP);
        registerReceiver(mReceiver, filter);
        mTrigger = new MulticastDnsTrigger(KadecotService.this);
        registerReceiver(mTrigger, filter);
        try {
            mTopology.start();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Topology does not work normally.");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mTopology.stop();
        mServerManager.stop();

        unregisterReceiver(mTrigger);
        mTrigger.onDisconnected();
        unregisterReceiver(mReceiver);
        mPreferences.unregisterOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ConnectivityManagerUtil.isConnected(this)) {
            mServerManager.start();
        }

        if (KadecotServicePreference.isPersistentModeEnabled(this)) {
            changeNotification(mPreferences);
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    private void changeNotification(SharedPreferences sp) {
        if (!KadecotServicePreference.isPersistentModeEnabled(this)) {
            stopForeground(true);
            return;
        }

        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(getPackageName());
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent, 0);
        String contentText;
        if (ConnectivityManagerUtil.isConnected(this)) {
            contentText = ConnectivityManagerUtil.getIPAddress(this) + "\n";
        } else {
            contentText = "Network Error\n";
        }
        contentText += "HomeNet:" + (ConnectivityManagerUtil.isConnected(this) ? "ON\n" : "OFF\n");
        contentText += "WebSocket:"
                + (WebSocketServerPreference.isEnabled(this) ? "ON\n" : "OFF\n");
        contentText += "Http:" + (DeveloperModePreference.isEnabled(this) ? "ON\n" : "OFF\n");

        Notification notice = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notify_kadecot)
                .setTicker("Kadecot Server").setWhen(System.currentTimeMillis())
                .setContentTitle("Kadecot Server").setContentText(contentText)
                .setContentIntent(pendIntent).setColor(Color.BLACK).build();
        notice.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(FOREGROUND_ID, notice);
    }

    @Override
    public void locate(WampPeer peer) {
        peer.connect(mTopology.getRouter());
    }

    @Override
    public boolean isAuthenticated(OpeningHandshake handshake) {
        final String origin = handshake.getOrigin();
        final String token = handshake.getParameter(ACCESS_TOKEN_PARAM_KEY);

        if (origin.equals("")) {
            return false;
        }

        if (token.equals("")) {
            return false;
        }

        try {
            new URL(origin);
        } catch (MalformedURLException e) {
            if (!origin.startsWith(CUSTOM_URL_SCHEME)) {
                return false;
            }
        }

        ContentProviderClient client = getContentResolver()
                .acquireContentProviderClient(
                        KadecotCoreStore.Handshakes.CONTENT_URI);
        Cursor cursor;
        try {
            cursor = client.query(KadecotCoreStore.Handshakes.CONTENT_URI,
                    new String[] {
                            KadecotCoreStore.Handshakes.HandshakeColumns.ORIGIN,
                            KadecotCoreStore.Handshakes.HandshakeColumns.TOKEN,
                            KadecotCoreStore.Handshakes.HandshakeColumns.STATUS
                    }
                    , KadecotCoreStore.Handshakes.HandshakeColumns.ORIGIN + "=? AND "
                            + KadecotCoreStore.Handshakes.HandshakeColumns.TOKEN + "=?"
                    , new String[] {
                            origin, token
                    }, null);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        } finally {
            client.release();
        }

        try {
            int count = cursor.getCount();
            if (count > 1) {
                throw new IllegalStateException();
            }
            if (count == 1) {
                cursor.moveToFirst();
                int status = cursor.getInt(cursor
                        .getColumnIndex(KadecotCoreStore.Handshakes.HandshakeColumns.STATUS));
                return (status == 1);
            }
        } finally {
            cursor.close();
        }

        return false;
    }

    @Override
    public Set<String> getScopeSet(OpeningHandshake handshake) {
        if (!isAuthenticated(handshake)) {
            return new HashSet<String>();
        }
        ContentProviderClient client = getContentResolver()
                .acquireContentProviderClient(
                        KadecotCoreStore.Handshakes.CONTENT_URI);
        Cursor cursor;
        try {
            cursor = client.query(KadecotCoreStore.Handshakes.CONTENT_URI,
                    new String[] {
                        KadecotCoreStore.Handshakes.HandshakeColumns.SCOPE
                    },
                    KadecotCoreStore.Handshakes.HandshakeColumns.TOKEN + "=?",
                    new String[] {
                        handshake.getParameter(ACCESS_TOKEN_PARAM_KEY)
                    }, null);
        } catch (RemoteException e) {
            return new HashSet<String>();
        } finally {
            client.release();
        }
        if (cursor.getCount() != 1) {
            cursor.close();
            return new HashSet<String>();
        }

        cursor.moveToFirst();
        String[] scopes = cursor.getString(cursor
                .getColumnIndex(KadecotCoreStore.Handshakes.HandshakeColumns.SCOPE)).split(",");
        cursor.close();
        return new HashSet<String>(Arrays.asList(scopes));
    }

    private static final class MulticastDnsTrigger extends WifiConnectionBroadcastReceiver {

        private static final String SERVICE_TYPE = "_kadecot._tcp.local.";
        private static final String SERVICE_NAME_PREFIX = "kadecot-";

        private final Context mContext;

        private JmDNS mDNS;
        private MulticastLock mLock;

        public MulticastDnsTrigger(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        public void onConnected() {

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected void onPreExecute() {
                    WifiManager wifi = (WifiManager) mContext
                            .getSystemService(Context.WIFI_SERVICE);
                    mLock = wifi.createMulticastLock(MulticastDnsTrigger.class.getSimpleName());
                    mLock.setReferenceCounted(true);
                    mLock.acquire();
                }

                @Override
                protected Void doInBackground(Void... params) {
                    final String serviceName = SERVICE_NAME_PREFIX
                            + ConnectivityManagerUtil.getIPAddress(mContext).replace(".", "-");
                    ServiceInfo info = ServiceInfo.create(SERVICE_TYPE, serviceName,
                            ServerManager.WS_PORT_NO, "");
                    try {
                        mDNS = JmDNS.create(InetAddress.getByName(ConnectivityManagerUtil
                                .getIPAddress(mContext)), serviceName);
                        mDNS.registerService(info);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        return null;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }

                    return null;
                }
            }.execute();
        }

        @Override
        public void onDisconnected() {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    if (mDNS == null) {
                        return null;
                    }

                    mDNS.unregisterAllServices();
                    try {
                        mDNS.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mDNS = null;
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    if (mLock == null) {
                        return;
                    }
                    mLock.release();
                    mLock = null;
                }
            }.execute();
        }
    }
}
