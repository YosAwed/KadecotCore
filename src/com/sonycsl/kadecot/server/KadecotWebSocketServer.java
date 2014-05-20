/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.server;

import android.content.Context;
import android.os.Build;

import com.sonycsl.kadecot.call.KadecotCall;
import com.sonycsl.kadecot.call.NotificationProcessor;
import com.sonycsl.kadecot.call.RequestProcessor;
import com.sonycsl.kadecot.core.Dbg;
import com.sonycsl.kadecot.core.KadecotCoreApplication;
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
import java.util.HashMap;
import java.util.Map;

public class KadecotWebSocketServer {
    @SuppressWarnings("unused")
    private static final String TAG = KadecotWebSocketServer.class.getSimpleName();

    private final KadecotWebSocketServer self = this;

    private static final int portno = 41314;

    private Context mContext;

    protected static KadecotWebSocketServer sInstance = null;

    private WebSocketServerImpl mWebSocketServer = null;

    private boolean mStarted = false;

    private KadecotWampRouter mRouter;

    public synchronized static KadecotWebSocketServer getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new KadecotWebSocketServer(context);
        }
        return sInstance;
    }

    private KadecotWebSocketServer(Context context) {
        if (Build.PRODUCT.startsWith("sdk")) {
            java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
            java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
        }

        mContext = context.getApplicationContext();

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

        private Map<WebSocket, KadecotCall> mKadecotCalls = new HashMap<WebSocket, KadecotCall>();

        private Map<WebSocket, WampClient> mClients = new HashMap<WebSocket, WampClient>();

        public WebSocketServerImpl(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {

            // origin
            String origin = handshake.getFieldValue("origin");
            KadecotCoreApplication app = (KadecotCoreApplication) mContext.getApplicationContext();
            if (app.getModifiableObject().acceptWebSocketOrigin(origin)) {
                /** KadecotCall **/

                /** WAMP **/
                WampClient client = new KadecotWebSocketClient(conn);
                client.connect(mRouter);
                mClients.put(conn, client);

            } else {
                conn.close();
            }

        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            /** KadecotCall **/
            if (mKadecotCalls.containsKey(conn)) {
                KadecotCall kc = mKadecotCalls.get(conn);
                mKadecotCalls.remove(conn);
                kc.stop();
            }

            /** WAMP has no method for onClose **/
        }

        @Override
        public void onMessage(WebSocket conn, String message) {

            /** KadecotCall **/
            Dbg.print(message);
            if (mKadecotCalls.containsKey(conn)) {
                try {
                    mKadecotCalls.get(conn).receive(new JSONObject(message));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            /** WAMP **/
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
            // if (mKadecotCalls.containsKey(conn)) {
            // KadecotCall kc = mKadecotCalls.get(conn);
            // kc.stop();
            // mKadecotCalls.remove(conn);
            // }
        }

        @Override
        public synchronized void stop(int timeout) throws IOException, InterruptedException {
            super.stop(timeout);

            /** KadecotCalls **/
            for (WebSocket ws : mKadecotCalls.keySet()) {
                mKadecotCalls.get(ws).stop();
            }
            mKadecotCalls.clear();

            /** WAMP **/
            mClients.clear();

            self.mWebSocketServer = null;

            if (self.isStarted()) {
                self.start();
            }

        }
    }

    public class WebSocketCall extends KadecotCall {

        private WebSocket ws;

        public WebSocketCall(Context context, WebSocket ws) {
            super(context, 1, new RequestProcessor(context, 1), new NotificationProcessor(context,
                    1));
            // TODO Auto-generated constructor stub
            this.ws = ws;
        }

        @Override
        public void send(JSONObject obj) {
            ws.send(obj.toString());
        }

    }
}
