/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.server;

import android.os.Build;
import android.util.Log;

import com.sonycsl.Kadecot.wamp.KadecotProtocolSetupCallback;
import com.sonycsl.Kadecot.wamp.KadecotWampClient;
import com.sonycsl.Kadecot.wamp.KadecotWampPeerLocator;
import com.sonycsl.Kadecot.wamp.KadecotWampRouter;
import com.sonycsl.Kadecot.wamp.KadecotWampSetupCallback;
import com.sonycsl.Kadecot.wamp.KadecotWebSocketClient;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampRouter;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.drafts.Draft_75;
import org.java_websocket.drafts.Draft_76;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotWebSocketServer {

    private static final String TAG = KadecotWebSocketServer.class.getSimpleName();

    private static final String WAMP_PROTOCOL = "wamp.2.json";

    private static final int portno = 41314;

    protected static KadecotWebSocketServer sInstance = null;

    private WebSocketServerImpl mWebSocketServer = null;

    private boolean mStarted = false;

    private KadecotWampRouter mRouter;

    private CountDownLatch mSystemSetup;

    private CountDownLatch mWampSetup;

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

        mRouter = KadecotWampPeerLocator.getRouter();

        mSystemSetup = new CountDownLatch(KadecotWampPeerLocator.getSystemClients().length);
        for (KadecotWampClient client : KadecotWampPeerLocator.getSystemClients()) {
            client.connect(mRouter);
            client.setCallback(new KadecotWampSetupCallback(
                    client.getTopicsToSubscribe(), client.getRegisterableProcedures()
                            .keySet(),
                    new KadecotWampSetupCallback.OnCompletionListener() {
                        @Override
                        public void onCompletion() {
                            mSystemSetup.countDown();
                        }
                    }));
        }

        mWampSetup = new CountDownLatch(KadecotWampPeerLocator.getProtocolClients().length);
        for (KadecotWampClient client : KadecotWampPeerLocator.getProtocolClients()) {
            client.connect(mRouter);
            client.setCallback(new KadecotProtocolSetupCallback(client.getSubscribableTopics(),
                    client.getRegisterableProcedures(),
                    new KadecotProtocolSetupCallback.OnCompletionListener() {

                        @Override
                        public void onCompletion() {
                        }
                    }));
            client.setCallback(new KadecotWampSetupCallback(client.getTopicsToSubscribe(), client
                    .getRegisterableProcedures().keySet(),
                    new KadecotWampSetupCallback.OnCompletionListener() {
                        @Override
                        public void onCompletion() {
                            mWampSetup.countDown();
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
        List<Draft> draftList = new ArrayList<Draft>();
        draftList.add(new Draft_17_Protocol(WAMP_PROTOCOL));
        draftList.add(new Draft_17());
        draftList.add(new Draft_10_Protocol(WAMP_PROTOCOL));
        draftList.add(new Draft_10());
        draftList.add(new Draft_76_Protocol(WAMP_PROTOCOL));
        draftList.add(new Draft_76());
        draftList.add(new Draft_75_Protocol(WAMP_PROTOCOL));
        draftList.add(new Draft_75());

        mWebSocketServer = new WebSocketServerImpl(new InetSocketAddress(portno), draftList);
        mWebSocketServer.start();

        for (KadecotWampClient client : KadecotWampPeerLocator.getSystemClients()) {
            client.transmit(WampMessageFactory.createHello(KadecotWampRouter.REALM,
                    new JSONObject()));
        }

        try {
            mSystemSetup.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "unable to setup KadecotWampClient");
            return;
        }

        for (KadecotWampClient client : KadecotWampPeerLocator.getProtocolClients()) {
            client.transmit(WampMessageFactory.createHello(KadecotWampRouter.REALM,
                    new JSONObject()));
        }

        try {
            mWampSetup.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "unable to setup Wamp");
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

        for (KadecotWampClient client : KadecotWampPeerLocator.getProtocolClients()) {
            client.transmit(WampMessageFactory.createGoodbye(new JSONObject(),
                    WampError.CLOSE_REALM));
        }

        for (KadecotWampClient client : KadecotWampPeerLocator.getSystemClients()) {
            client.transmit(WampMessageFactory.createGoodbye(new JSONObject(),
                    WampError.CLOSE_REALM));
        }

        mWebSocketServer = null;
        mStarted = false;
    }

    public class WebSocketServerImpl extends WebSocketServer {

        private Map<WebSocket, KadecotWebSocketClient> mClients = new ConcurrentHashMap<WebSocket, KadecotWebSocketClient>();

        public WebSocketServerImpl(InetSocketAddress address, List<Draft> draftList) {
            super(address, draftList);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            KadecotWebSocketClient client = new KadecotWebSocketClient(conn);
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
