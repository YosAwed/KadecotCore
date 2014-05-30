/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.server;

import android.os.Build;
import android.util.Log;

import com.sonycsl.kadecot.wamp.KadecotWampClient;
import com.sonycsl.kadecot.wamp.KadecotWampClientLocator;
import com.sonycsl.kadecot.wamp.KadecotWampClientSetupCallback;
import com.sonycsl.kadecot.wamp.KadecotWampClientSetupCallback.OnCompletionListener;
import com.sonycsl.kadecot.wamp.KadecotWampRouter;
import com.sonycsl.kadecot.wamp.KadecotWebSocketClient;
import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampRouter;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotWebSocketServer {

    private static final String TAG = KadecotWebSocketServer.class.getSimpleName();

    private static final int portno = 41314;

    protected static KadecotWebSocketServer sInstance = null;

    private WebSocketServerImpl mWebSocketServer = null;

    private boolean mStarted = false;

    private KadecotWampRouter mRouter;

    private CountDownLatch mSetupLatch;

    public synchronized static KadecotWebSocketServer getInstance() {
        if (sInstance == null) {
            sInstance = new KadecotWebSocketServer();
        }
        return sInstance;
    }

    private KadecotWebSocketServer() {
        if (Build.PRODUCT.startsWith("sdk")) {
            java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
            java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
        }

        mRouter = new KadecotWampRouter();

        mSetupLatch = new CountDownLatch(KadecotWampClientLocator.getClients().length);
        for (KadecotWampClient client : KadecotWampClientLocator.getClients()) {
            client.connect(mRouter);
            client.setCallback(new KadecotWampClientSetupCallback(
                    client.getSubscribableTopics(), client.getRegisterableProcedures(),
                    new OnCompletionListener() {
                        @Override
                        public void onCompletion() {
                            mSetupLatch.countDown();
                        }
                    }));
        }

        WebSocketImpl.DEBUG = false;// true;
    }

    public WampRouter getWampRouter() {
        return mRouter;
    }

    public synchronized void start() {
        if (isStarted()) {
            return;
        }
        stop();
        mWebSocketServer = new WebSocketServerImpl(new InetSocketAddress(portno));
        mWebSocketServer.start();

        for (KadecotWampClient client : KadecotWampClientLocator.getClients()) {
            client.transmit(WampMessageFactory.createHello(KadecotWampRouter.REALM,
                    new JSONObject()));
        }

        try {
            mSetupLatch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "unable to setup KadecotWampClient");
            return;
        }

        mStarted = true;
    }

    public synchronized boolean isStarted() {
        return mStarted;
    }

    public synchronized void stop() {
        if (!isStarted()) {
            return;
        }
        try {
            if (mWebSocketServer != null) {
                mWebSocketServer.stop();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (KadecotWampClient client : KadecotWampClientLocator.getClients()) {
            client.transmit(WampMessageFactory.createGoodbye(new JSONObject(),
                    WampError.CLOSE_REALM));
        }

        mWebSocketServer = null;
        mStarted = false;
    }

    public class WebSocketServerImpl extends WebSocketServer {

        private Map<WebSocket, WampClient> mClients = new ConcurrentHashMap<WebSocket, WampClient>();

        public WebSocketServerImpl(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            WampClient client = new KadecotWebSocketClient(conn);
            client.connect(mRouter);
            mClients.put(conn, client);
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            mClients.remove(conn);
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            if (mClients.containsKey(conn)) {
                try {
                    WampMessage msg = WampMessageFactory.create(new JSONArray(message));
                    mClients.get(conn).transmit(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
        }

        @Override
        public synchronized void stop(int timeout) throws IOException, InterruptedException {
            super.stop(timeout);

            mClients.clear();

            KadecotWebSocketServer.this.mWebSocketServer = null;

            if (KadecotWebSocketServer.this.isStarted()) {
                KadecotWebSocketServer.this.start();
            }

        }
    }
}
