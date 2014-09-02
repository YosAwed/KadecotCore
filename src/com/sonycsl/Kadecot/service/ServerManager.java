/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.service;

import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.RemoteException;

import com.sonycsl.Kadecot.core.R;
import com.sonycsl.Kadecot.preference.JsonpServerPreference;
import com.sonycsl.Kadecot.preference.SnapServerPreference;
import com.sonycsl.Kadecot.preference.WebSocketServerPreference;
import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.server.http.HttpServer;
import com.sonycsl.Kadecot.server.http.JsonpServerModel;
import com.sonycsl.Kadecot.server.http.KadecotServerModel;
import com.sonycsl.Kadecot.server.http.MainAppServerModel;
import com.sonycsl.Kadecot.server.http.SnapServerModel;
import com.sonycsl.Kadecot.server.websocket.ClientAuthCallback;
import com.sonycsl.Kadecot.server.websocket.OpeningHandshake;
import com.sonycsl.Kadecot.server.websocket.WebSocketServer;
import com.sonycsl.Kadecot.wamp.client.KadecotAppClientWrapper;
import com.sonycsl.Kadecot.wamp.client.KadecotAppClientWrapper.WampGoodbyeListener;
import com.sonycsl.Kadecot.wamp.client.KadecotAppClientWrapper.WampWelcomeListener;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.transport.ProxyPeer;
import com.sonycsl.wamp.transport.WampWebSocketTransport;
import com.sonycsl.wamp.transport.WampWebSocketTransport.OnWampMessageListener;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ServerManager {

    private interface ServerSettingChanger {
        public void change();
    }

    public static final int WS_PORT_NO = 41314;

    public static final String WAMP_PROTOCOL = "wamp.2.json";

    public static final int JSONP_PORT_NO = 31413;

    private static final String LOCALHOST = "localhost";

    public static final String WS_STARTED_INTENT = "com.sonycsl.kadecot.intent.SERVER_STARTED";

    public static final String EXTRA_ACCEPTED_ORIGIN = "acceptedOrigin";

    private final Map<String, ServerSettingChanger> mChangers;

    private final Context mContext;

    private final WebSocketServer mWebSocketServer;

    private boolean mStatus = false;

    private KadecotAppClientWrapper mClientJsonp;
    private KadecotAppClientWrapper mClientSnap;

    private HttpServer mHttpServer;

    private KadecotServerModel mModels;

    private ProxyPeer mProxyJsonp;
    private ProxyPeer mProxySnap;

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

    private final String mAcceptedOrigin = UUID.randomUUID().toString();

    private final ClientAuthCallback mAuthCallbackAdapter = new ClientAuthCallback() {

        @Override
        public boolean authenticate(OpeningHandshake handShake) {
            if (mAcceptedOrigin.equals(handShake.getOrigin())) {
                return true;
            }

            if (mAuthCallback == null) {
                return false;
            }

            return mAuthCallback.authenticate(handShake);
        }
    };

    private WampWebSocketTransport mTransportJsonp;

    private WampWebSocketTransport mTransportSnap;

    private BroadcastReceiver mWsReceiverJsonp;

    private BroadcastReceiver mWsReceiverSnap;

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
                            Intent intent = new Intent(WS_STARTED_INTENT);
                            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            intent.putExtra(EXTRA_ACCEPTED_ORIGIN, mAcceptedOrigin);
                            mContext.sendBroadcast(intent);
                        } else {
                            mWebSocketServer.stop();
                            disableAllDevices();
                        }
                    }
                });

        mChangers.put(mContext.getString(R.string.jsonp_preference_key),
                new ServerSettingChanger() {
                    @Override
                    public void change() {
                        if (JsonpServerPreference.isEnabled(mContext)) {
                            if (mWsReceiverJsonp == null) {
                                mWsReceiverJsonp = new BroadcastReceiver() {

                                    @Override
                                    public void onReceive(Context context, Intent intent) {
                                        mTransportJsonp
                                                .open(LOCALHOST, WS_PORT_NO, mAcceptedOrigin);
                                        mClientJsonp.hello("realm", new WampWelcomeListener() {

                                            @Override
                                            public void onWelcome(int session, JSONObject details) {
                                            }

                                        });
                                    }
                                };
                            }

                            mContext.registerReceiver(mWsReceiverJsonp, new IntentFilter(
                                    WS_STARTED_INTENT));
                            mTransportJsonp.open(LOCALHOST, WS_PORT_NO, mAcceptedOrigin);
                            mClientJsonp.hello("realm", new WampWelcomeListener() {

                                @Override
                                public void onWelcome(int session, JSONObject details) {
                                }

                            });
                            mModels.addModel(JsonpServerModel.JSONP_BASE_URI,
                                    new JsonpServerModel(
                                            mClientJsonp));
                        } else {
                            if (mWsReceiverJsonp != null) {
                                mContext.unregisterReceiver(mWsReceiverJsonp);
                                mWsReceiverJsonp = null;
                            }
                            mTransportJsonp.close();
                            mModels.removeModel(JsonpServerModel.JSONP_BASE_URI);
                            mClientJsonp.goodbye("exit.jsonp.server", new WampGoodbyeListener() {

                                @Override
                                public void onGoodbye(JSONObject details, String reason) {
                                }
                            });
                        }
                    }
                });

        mChangers.put(mContext.getString(R.string.snap_preference_key), new ServerSettingChanger() {
            @Override
            public void change() {
                if (SnapServerPreference.isEnabled(mContext)) {
                    if (mWsReceiverSnap == null) {
                        mWsReceiverSnap = new BroadcastReceiver() {

                            @Override
                            public void onReceive(Context context, Intent intent) {
                                mTransportSnap.open(LOCALHOST, WS_PORT_NO, mAcceptedOrigin);
                                mClientSnap.hello("realm", new WampWelcomeListener() {

                                    @Override
                                    public void onWelcome(int session, JSONObject details) {
                                    }

                                });
                            }
                        };
                    }

                    mContext.registerReceiver(mWsReceiverSnap, new IntentFilter(
                            WS_STARTED_INTENT));
                    mTransportSnap.open(LOCALHOST, WS_PORT_NO, mAcceptedOrigin);
                    mClientSnap.hello("realm", new WampWelcomeListener() {

                        @Override
                        public void onWelcome(int session, JSONObject details) {
                        }

                    });
                    mModels.addModel(SnapServerModel.SNAP_BASE_URI,
                            new SnapServerModel(mContext, mClientSnap));
                } else {
                    if (mWsReceiverSnap != null) {
                        mContext.unregisterReceiver(mWsReceiverSnap);
                        mWsReceiverSnap = null;
                    }
                    mTransportSnap.close();
                    mModels.removeModel(SnapServerModel.SNAP_BASE_URI);
                    mClientSnap.goodbye("exit.snap.server", new WampGoodbyeListener() {

                        @Override
                        public void onGoodbye(JSONObject details, String reason) {
                        }
                    });
                }
            }
        });
    }

    public ServerManager(Context context) {
        mContext = context.getApplicationContext();
        mChangers = new HashMap<String, ServerManager.ServerSettingChanger>();
        mWebSocketServer = new WebSocketServer(WS_PORT_NO, WAMP_PROTOCOL);
        mWebSocketServer.setClientAuthCallback(mAuthCallbackAdapter);
        mTransportJsonp = new WampWebSocketTransport();
        mProxyJsonp = new ProxyPeer(mTransportJsonp);
        mTransportJsonp.setOnWampMessageListener(new OnWampMessageListener() {

            @Override
            public void onMessage(WampMessage msg) {
                mProxyJsonp.transmit(msg);
            }
        });

        mClientJsonp = new KadecotAppClientWrapper();
        mClientJsonp.connect(mProxyJsonp);

        mTransportSnap = new WampWebSocketTransport();
        mProxySnap = new ProxyPeer(mTransportSnap);
        mTransportSnap.setOnWampMessageListener(new OnWampMessageListener() {
            @Override
            public void onMessage(WampMessage msg) {
                mProxySnap.transmit(msg);
            }
        });

        mClientSnap = new KadecotAppClientWrapper();
        mClientSnap.connect(mProxySnap);

        mModels = new KadecotServerModel();
        mHttpServer = new HttpServer(JSONP_PORT_NO, mModels);

        setupChangers();
    }

    public void setClientAuthCallback(ClientAuthCallback callback) {
        mAuthCallback = callback;
    }

    public void startServers() {
        mModels.addModel(MainAppServerModel.MAIN_BASE_URI, new MainAppServerModel(mContext));
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

        mWebSocketServer.stop();

        mModels.removeModel(JsonpServerModel.JSONP_BASE_URI);
        mClientJsonp.goodbye("exit.jsonp.server", new WampGoodbyeListener() {

            @Override
            public void onGoodbye(JSONObject details, String reason) {
            }
        });

        mClientSnap.goodbye("exit.snap.server", new WampGoodbyeListener() {
            @Override
            public void onGoodbye(JSONObject details, String reason) {
            }
        });

        mModels.removeModel(MainAppServerModel.MAIN_BASE_URI);
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
        if (mWsReceiverSnap != null) {
            mContext.unregisterReceiver(mWsReceiverSnap);
            mWsReceiverSnap = null;
        }
        mContext.getSharedPreferences(mContext.getString(R.string.preferences_file_name),
                Context.MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(mListener);
        stopServers();
        disableAllDevices();

        mStatus = false;
    }

}
