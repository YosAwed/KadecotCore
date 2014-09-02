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
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.SparseArray;

import com.sonycsl.Kadecot.core.R;
import com.sonycsl.Kadecot.net.ConnectivityManagerUtil;
import com.sonycsl.Kadecot.preference.JsonpServerPreference;
import com.sonycsl.Kadecot.preference.KadecotServicePreference;
import com.sonycsl.Kadecot.preference.SnapServerPreference;
import com.sonycsl.Kadecot.preference.WebSocketServerPreference;
import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.server.websocket.ClientAuthCallback;
import com.sonycsl.Kadecot.server.websocket.OpeningHandshake;
import com.sonycsl.Kadecot.service.IPublisher.OnPublishedListener;
import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampPublishedMessage;
import com.sonycsl.wamp.role.WampPublisher;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.transport.ProxyPeer;
import com.sonycsl.wamp.transport.WampWebSocketTransport;
import com.sonycsl.wamp.transport.WampWebSocketTransport.OnWampMessageListener;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public final class KadecotService extends Service implements ClientAuthCallback {

    private static final int FOREGROUND_ID = 100;

    private static final Set<String> SUPPORTED_KEYS;

    static {
        SUPPORTED_KEYS = new HashSet<String>();
    }

    private ServerManager mServerManager;

    private SharedPreferences mPreferences;

    private IWampClientImpl mIWampClient;

    private OnSharedPreferenceChangeListener mListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
            if (!SUPPORTED_KEYS.contains(key)) {
                return;
            }
            changeNotification(sp);
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null) {
                return;
            }

            if (intent.getAction().equals(ServerManager.WS_STARTED_INTENT)) {
                String origin = "";
                if (intent.hasExtra(ServerManager.EXTRA_ACCEPTED_ORIGIN)) {
                    origin = intent.getStringExtra(ServerManager.EXTRA_ACCEPTED_ORIGIN);
                }
                mIWampClient.connect(origin);
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

    public KadecotService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SUPPORTED_KEYS.add(getString(R.string.persistent_mode_preference_key));
        SUPPORTED_KEYS.add(getString(R.string.websocket_preference_key));
        SUPPORTED_KEYS.add(getString(R.string.jsonp_preference_key));
        SUPPORTED_KEYS.add(getString(R.string.snap_preference_key));

        mServerManager = new ServerManager(this);
        mServerManager.setClientAuthCallback(this);
        mPreferences = getSharedPreferences(getString(R.string.preferences_file_name),
                MODE_PRIVATE);
        mPreferences.registerOnSharedPreferenceChangeListener(mListener);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(ServerManager.WS_STARTED_INTENT);
        registerReceiver(mReceiver, filter);
        mIWampClient = new IWampClientImpl(KadecotService.this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mIWampClient.disconnect();
        mServerManager.stop();

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
        return new LocalBinder();
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
        contentText += "Http:" + (JsonpServerPreference.isEnabled(this) ? "ON\n" : "OFF\n");
        contentText += "Snap:" + (SnapServerPreference.isEnabled(this) ? "ON\n" : "OFF\n");

        Notification notice = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notify_kadecot)
                .setTicker("Kadecot Server").setWhen(System.currentTimeMillis())
                .setContentTitle("Kadecot Server").setContentText(contentText)
                .setContentIntent(pendIntent).build();
        notice.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(FOREGROUND_ID, notice);
    }

    public class LocalBinder extends Binder {
        public IWampClient getWampClient() {
            return mIWampClient;
        }
    }

    @Override
    public boolean authenticate(OpeningHandshake handShake) {
        final String origin = handShake.getOrigin();

        if (origin.equals("")) {
            return false;
        }

        ContentProviderClient client = getContentResolver()
                .acquireContentProviderClient(
                        KadecotCoreStore.Handshakes.CONTENT_URI);
        Cursor cursor;
        try {
            cursor = client.query(KadecotCoreStore.Handshakes.CONTENT_URI,
                    new String[] {
                            KadecotCoreStore.Handshakes.HandshakeColumns.ORIGIN,
                            KadecotCoreStore.Handshakes.HandshakeColumns.STATUS
                    }
                    , KadecotCoreStore.Handshakes.HandshakeColumns.ORIGIN + "=?", new String[] {
                        origin
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

        client = getContentResolver().acquireContentProviderClient(
                KadecotCoreStore.Handshakes.CONTENT_URI);
        ContentValues values = new ContentValues();
        values.put(KadecotCoreStore.Handshakes.HandshakeColumns.ORIGIN, origin);
        values.put(KadecotCoreStore.Handshakes.HandshakeColumns.STATUS, 0);
        try {
            client.insert(KadecotCoreStore.Handshakes.CONTENT_URI, values);
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            client.release();
        }

        return false;
    }

    private static final class IWampClientImpl implements IWampClient {

        private final Context mContext;
        private final WampWebSocketTransport mTransport;
        private final KadecotServiceClient mClient;

        public IWampClientImpl(Context context) {
            mContext = context;
            mTransport = new WampWebSocketTransport();
            final ProxyPeer proxy = new ProxyPeer(mTransport);
            mTransport.setOnWampMessageListener(new OnWampMessageListener() {
                @Override
                public void onMessage(WampMessage msg) {
                    proxy.transmit(msg);
                }
            });

            mClient = new KadecotServiceClient();
            mClient.connect(proxy);
        }

        public void connect(String origin) {
            mTransport.open(ConnectivityManagerUtil.getIPAddress(mContext),
                    ServerManager.WS_PORT_NO, origin);
            mClient.transmit(WampMessageFactory.createHello("realm", new JSONObject()));
        }

        public void disconnect() {
            mClient.transmit(WampMessageFactory.createGoodbye(new JSONObject(),
                    WampError.GOODBYE_AND_OUT));
            mTransport.close();
        }

        @Override
        public IPublisher asPublisher() {
            return new IPublisher() {
                @Override
                public void publish(String topic, JSONObject argsKw, OnPublishedListener listener) {
                    mClient.publish(topic, argsKw, listener);
                }
            };
        }
    }

    private static class KadecotServiceClient extends WampClient {

        private SparseArray<OnPublishedListener> mRequestIdMap;

        public KadecotServiceClient() {
            mRequestIdMap = new SparseArray<IPublisher.OnPublishedListener>();
        }

        @Override
        protected Set<WampRole> getClientRoleSet() {
            Set<WampRole> role = new HashSet<WampRole>();
            role.add(new WampPublisher());
            return role;
        }

        public void publish(String topic, JSONObject argsKw, OnPublishedListener listener) {
            int requestId = WampRequestIdGenerator.getId();
            if (listener != null) {
                mRequestIdMap.put(requestId, listener);
            }
            transmit(WampMessageFactory.createPublish(requestId, new JSONObject(), topic,
                    new JSONArray(), argsKw));
        }

        @Override
        protected void onConnected(WampPeer peer) {
        }

        @Override
        protected void onTransmitted(WampPeer peer, WampMessage msg) {
        }

        @Override
        protected void onReceived(WampMessage msg) {
            if (msg.isPublishedMessage()) {
                WampPublishedMessage pubMsg = msg.asPublishedMessage();
                OnPublishedListener listener = mRequestIdMap.get(pubMsg.getRequestId());
                if (listener != null) {
                    mRequestIdMap.remove(pubMsg.getRequestId());
                    listener.onPublished(pubMsg.getPublicationId());
                }
            }
        }
    }
}
