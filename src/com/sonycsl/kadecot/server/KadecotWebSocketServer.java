/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.server;

import android.os.Build;

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

public class KadecotWebSocketServer {

    private final KadecotWebSocketServer self = this;

    private static final int portno = 41314;

    protected static KadecotWebSocketServer sInstance = null;

    private WebSocketServerImpl mWebSocketServer = null;

    private boolean mStarted = false;

    private KadecotWampRouter mRouter;

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

        for (WampClient client : KadecotWampClientLocator.getClients()) {
            client.connect(mRouter);
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

        for (WampClient client : KadecotWampClientLocator.getClients()) {
            client.transmit(WampMessageFactory.createHello(KadecotWampRouter.REALM,
                    new JSONObject()));
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

        for (WampClient client : KadecotWampClientLocator.getClients()) {
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

            self.mWebSocketServer = null;

            if (self.isStarted()) {
                self.start();
            }

        }
    }
}
