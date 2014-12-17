/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;

import com.sonycsl.Kadecot.core.R;
import com.sonycsl.Kadecot.preference.DeveloperModePreference;
import com.sonycsl.Kadecot.preference.WebSocketServerPreference;
import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.server.http.HttpServer;
import com.sonycsl.Kadecot.server.http.response.FileResponseFactory;
import com.sonycsl.Kadecot.server.http.response.JsonpReponseFactory;
import com.sonycsl.Kadecot.server.http.response.OauthResponseFactory;
import com.sonycsl.Kadecot.server.websocket.ClientAuthCallback;
import com.sonycsl.Kadecot.server.websocket.OpeningHandshake;
import com.sonycsl.Kadecot.server.websocket.WebSocketServer;
import com.sonycsl.Kadecot.wamp.WampTopology;
import com.sonycsl.Kadecot.wamp.client.KadecotAppClientWrapper;
import com.sonycsl.Kadecot.wamp.client.KadecotAppClientWrapper.WampGoodbyeListener;
import com.sonycsl.Kadecot.wamp.client.KadecotAppClientWrapper.WampWelcomeListener;
import com.sonycsl.Kadecot.wamp.util.WampLocatorCallback;
import com.sonycsl.wamp.WampPeer;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ServerManager {

    private interface ServerSettingChanger {
        public void change();
    }

    public static final int WS_PORT_NO = 41314;

    public static final String WAMP_PROTOCOL = "wamp.2.json";

    public static final int JSONP_PORT_NO = 31413;

    private static final String JSONP_ROOT_PATH = "/jsonp";

    private static final String OAUTH_ROOT_PATH = "/apps";

    public static final String ACTION_APP = "com.sonycsl.kadecot.app";

    private static final String ACTION_PLUGIN = "com.sonycsl.kadecot.plugin";

    private static final String PERMISSION_WEBSOCKET = "com.sonycsl.kadecot.permission.ACCESS_WEBSOCKET_SERVER";

    public static final String EXTRA_ACCEPTED_ORIGIN = "acceptedOrigin";

    public static final String EXTRA_ACCEPTED_TOKEN = "acceptedToken";

    private static final String ANDROID_APP_ORIGIN = "http://app.kadecot.net";

    private static final String ANDROID_APP_TOKEN = UUID.randomUUID().toString();

    private static final String ANDROID_PLUGIN_ORIGIN = UUID.randomUUID().toString();

    private static final String ANDROID_PLUGIN_TOKEN = UUID.randomUUID().toString();

    private static final Set<String> ANDROID_APP_SCOPE;

    private static final Set<String> ANDROID_PLUGIN_SCOPE;

    static {
        Set<String> scopes = new HashSet<String>();
        scopes.add("com.sonycsl.kadecot");
        ANDROID_APP_SCOPE = Collections.unmodifiableSet(scopes);
        ANDROID_PLUGIN_SCOPE = Collections.unmodifiableSet(scopes);
    }

    private final Map<String, ServerSettingChanger> mChangers;

    private final Context mContext;

    private final WebSocketServer mWebSocketServer;

    private boolean mStatus = false;

    private KadecotAppClientWrapper mClientJsonp;

    private HttpServer mHttpServer;

    private PackageManager mPackageManager;
    private Collection<PluginConnection> mPluginConnections;

    private static class PluginConnection {

        private Intent mIntent;
        private ServiceConnection mConn;

        PluginConnection(Intent intent, ServiceConnection conn) {
            mIntent = intent;
            mConn = conn;
        }

        Intent getIntent() {
            return mIntent;
        }

        ServiceConnection getServiceConnection() {
            return mConn;
        }
    }

    private OnSharedPreferenceChangeListener mListener = new OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
            if (!mChangers.containsKey(key)) {
                return;
            }
            mChangers.get(key).change();
        }

    };

    private ClientAuthCallback mAuthCallback;

    private final ClientAuthCallback mAuthCallbackAdapter = new ClientAuthCallback() {

        private static final String ACCESS_TOKEN = "access_token";

        private boolean fromAndroidApp(OpeningHandshake handshake) {
            return ANDROID_APP_ORIGIN.equals(handshake.getOrigin())
                    && ANDROID_APP_TOKEN.equals(handshake.getParameter(ACCESS_TOKEN));
        }

        private boolean fromAndroidPlugin(OpeningHandshake handshake) {
            return ANDROID_PLUGIN_ORIGIN.equals(handshake.getOrigin())
                    && ANDROID_PLUGIN_TOKEN.equals(handshake.getParameter(ACCESS_TOKEN));
        }

        @Override
        public boolean isAuthenticated(OpeningHandshake handshake) {
            if (fromAndroidApp(handshake)) {
                return true;
            }

            if (fromAndroidPlugin(handshake)) {
                return true;
            }

            if (mAuthCallback == null) {
                return false;
            }

            return mAuthCallback.isAuthenticated(handshake);
        }

        @Override
        public Set<String> getScopeSet(OpeningHandshake handshake) {
            if (fromAndroidApp(handshake)) {
                return ANDROID_APP_SCOPE;
            }

            if (fromAndroidPlugin(handshake)) {
                return ANDROID_PLUGIN_SCOPE;
            }

            if (mAuthCallback == null) {
                return new HashSet<String>();
            }

            return mAuthCallback.getScopeSet(handshake);
        }
    };

    private BroadcastReceiver mWsReceiverJsonp;

    private WampTopology mTopology;

    private void disableAllDevices() {
        ContentValues values = new ContentValues();
        values.put(KadecotCoreStore.Devices.DeviceColumns.STATUS, false);
        ContentProviderClient provider = mContext.getContentResolver()
                .acquireContentProviderClient(KadecotCoreStore.Devices.CONTENT_URI);
        try {
            provider.update(KadecotCoreStore.Devices.CONTENT_URI, values, null, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            provider.release();
        }
    }

    private void setupChangers() {
        mChangers.put(mContext.getString(R.string.websocket_preference_key),
                new ServerSettingChanger() {

                    @Override
                    public void change() {
                        if (WebSocketServerPreference.isEnabled(mContext)) {
                            mWebSocketServer.start();

                            Intent intent = new Intent(ACTION_APP);
                            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            intent.putExtra(EXTRA_ACCEPTED_ORIGIN, ANDROID_APP_ORIGIN);
                            intent.putExtra(EXTRA_ACCEPTED_TOKEN, ANDROID_APP_TOKEN);
                            mContext.sendBroadcast(intent, PERMISSION_WEBSOCKET);

                            /**
                             * Get some plug-in information.
                             */
                            List<ResolveInfo> plugins = mPackageManager.queryIntentServices(
                                    new Intent(ServerManager.ACTION_PLUGIN), 0);

                            for (ResolveInfo plugin : plugins) {
                                ComponentName component = new ComponentName(
                                        plugin.serviceInfo.packageName,
                                        plugin.serviceInfo.name);
                                final Intent startPluginIntent = new Intent();
                                startPluginIntent.setComponent(component);
                                startPluginIntent.putExtra(EXTRA_ACCEPTED_ORIGIN,
                                        ANDROID_PLUGIN_ORIGIN);
                                startPluginIntent.putExtra(EXTRA_ACCEPTED_TOKEN,
                                        ANDROID_PLUGIN_TOKEN);
                                final ServiceConnection serviceConn = new ServiceConnection() {

                                    @Override
                                    public void onServiceConnected(ComponentName name,
                                            IBinder service) {
                                    }

                                    @Override
                                    public void onServiceDisconnected(ComponentName name) {
                                        mContext.startService(startPluginIntent);
                                        mContext.bindService(startPluginIntent, this,
                                                Context.BIND_AUTO_CREATE);
                                    }

                                };
                                mPluginConnections.add(new PluginConnection(startPluginIntent,
                                        serviceConn));
                                mContext.startService(startPluginIntent);
                                mContext.bindService(startPluginIntent, serviceConn,
                                        Context.BIND_AUTO_CREATE);
                            }
                        } else {
                            for (PluginConnection conn : mPluginConnections) {
                                mContext.unbindService(conn.getServiceConnection());
                                mContext.stopService(conn.getIntent());
                            }
                            mPluginConnections.clear();
                            mWebSocketServer.stop();

                            disableAllDevices();
                        }
                    }
                });

        mChangers.put(mContext.getString(R.string.developer_mode_preference_key),
                new ServerSettingChanger() {
                    @Override
                    public void change() {
                        if (DeveloperModePreference.isEnabled(mContext)) {
                            if (mWsReceiverJsonp == null) {
                                mWsReceiverJsonp = new BroadcastReceiver() {

                                    @Override
                                    public void onReceive(Context context, Intent intent) {
                                        mClientJsonp.hello("realm", new WampWelcomeListener() {

                                            @Override
                                            public void onWelcome(int session, JSONObject details) {
                                            }

                                        });
                                    }
                                };
                            }

                            mContext.registerReceiver(mWsReceiverJsonp, new IntentFilter(
                                    ACTION_PLUGIN));
                            mClientJsonp.hello("realm", new WampWelcomeListener() {

                                @Override
                                public void onWelcome(int session, JSONObject details) {
                                }

                            });
                            mHttpServer.putResponseFactory(JSONP_ROOT_PATH,
                                    new JsonpReponseFactory(mClientJsonp));
                        } else {
                            if (mWsReceiverJsonp != null) {
                                mContext.unregisterReceiver(mWsReceiverJsonp);
                                mWsReceiverJsonp = null;
                            }
                            mHttpServer.removeResponseFactory(JSONP_ROOT_PATH);
                            mClientJsonp.goodbye("exit.jsonp.server", new WampGoodbyeListener() {

                                @Override
                                public void onGoodbye(JSONObject details, String reason) {
                                }
                            });
                        }
                    }
                });
    }

    public ServerManager(Context context, WampTopology topology) {
        mContext = context.getApplicationContext();
        mTopology = topology;
        mChangers = new HashMap<String, ServerManager.ServerSettingChanger>();
        mWebSocketServer = new WebSocketServer(WS_PORT_NO, WAMP_PROTOCOL);
        mWebSocketServer.setClientAuthCallback(mAuthCallbackAdapter);
        mWebSocketServer.setWampLocatorCallback(new WampLocatorCallback() {

            @Override
            public void locate(WampPeer peer) {
                peer.connect(mTopology.getRouter());
            }
        });

        mClientJsonp = new KadecotAppClientWrapper();
        mClientJsonp.connect(mTopology.getRouter());

        mHttpServer = new HttpServer(JSONP_PORT_NO, new FileResponseFactory(mContext));
        mHttpServer.putResponseFactory(OAUTH_ROOT_PATH, new OauthResponseFactory(mContext));

        mPluginConnections = new ArrayList<PluginConnection>();
        mPackageManager = mContext.getPackageManager();

        setupChangers();
    }

    public void setClientAuthCallback(ClientAuthCallback callback) {
        mAuthCallback = callback;
    }

    public void startServers() {
        try {
            mHttpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (ServerSettingChanger changer : mChangers.values()) {
            changer.change();
        }
    }

    public void start() {
        if (mStatus) {
            return;
        }
        startServers();
        mContext.getSharedPreferences(mContext.getString(R.string.preferences_file_name),
                Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(mListener);
        mStatus = true;
    }

    private void stopServers() {

        for (PluginConnection conn : mPluginConnections) {
            mContext.unbindService(conn.getServiceConnection());
            mContext.stopService(conn.getIntent());
        }
        mPluginConnections.clear();
        mWebSocketServer.stop();

        mHttpServer.removeResponseFactory(JSONP_ROOT_PATH);
        mHttpServer.removeResponseFactory(OAUTH_ROOT_PATH);
        mClientJsonp.goodbye("exit.jsonp.server", new WampGoodbyeListener() {

            @Override
            public void onGoodbye(JSONObject details, String reason) {
            }
        });
        mHttpServer.stop();
    }

    public void stop() {
        if (!mStatus) {
            return;
        }

        if (mWsReceiverJsonp != null) {
            mContext.unregisterReceiver(mWsReceiverJsonp);
            mWsReceiverJsonp = null;
        }
        mContext.getSharedPreferences(mContext.getString(R.string.preferences_file_name),
                Context.MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(mListener);
        stopServers();
        disableAllDevices();

        mStatus = false;
    }

}
