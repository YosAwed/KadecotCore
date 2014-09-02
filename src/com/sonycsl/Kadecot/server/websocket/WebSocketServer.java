/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.server.websocket;

import android.os.Build;

import com.sonycsl.Kadecot.plugin.KadecotProtocolSetupCallback;
import com.sonycsl.Kadecot.plugin.KadecotWampSetupCallback;
import com.sonycsl.Kadecot.wamp.client.KadecotWampClient;
import com.sonycsl.Kadecot.wamp.router.KadecotWampRouter;
import com.sonycsl.Kadecot.wamp.util.KadecotWampPeerLocator;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampRouter;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.transport.WebSocketPeer;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.drafts.Draft_75;
import org.java_websocket.drafts.Draft_76;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WebSocketServer {

    private final int mPortNo;
    private String mSupportProtocol;

    private List<Draft> mDraftList;
    private WampWebSocketServer mWampWebSocketServer;

    private boolean mIsStarted = false;

    private final WampTopology mTopology;

    private ClientAuthCallback mClientAuthCallback = null;

    public WebSocketServer(int portno, String supportProtocol) {
        if (Build.PRODUCT.startsWith("sdk")) {
            java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
            java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
        }

        mPortNo = portno;
        mSupportProtocol = supportProtocol;
        mTopology = new WampTopology();
        WebSocketImpl.DEBUG = false;// true;
        mDraftList = new ArrayList<Draft>();
        mDraftList.add(new Draft_17_Protocol(mSupportProtocol));
        mDraftList.add(new Draft_17());
        mDraftList.add(new Draft_10_Protocol(mSupportProtocol));
        mDraftList.add(new Draft_10());
        mDraftList.add(new Draft_76_Protocol(mSupportProtocol));
        mDraftList.add(new Draft_76());
        mDraftList.add(new Draft_75_Protocol(mSupportProtocol));
        mDraftList.add(new Draft_75());
        mWampWebSocketServer = new WampWebSocketServer(new InetSocketAddress(mPortNo), mDraftList,
                mTopology);
    }

    public synchronized void start() {
        if (mIsStarted) {
            return;
        }

        try {
            mTopology.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        mWampWebSocketServer.start();
        mIsStarted = true;
    }

    public synchronized boolean isStarted() {
        return mIsStarted;
    }

    public synchronized void stop() {
        if (!mIsStarted) {
            return;
        }

        try {
            mTopology.stop();
            mWampWebSocketServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mWampWebSocketServer = new WampWebSocketServer(new InetSocketAddress(mPortNo),
                mDraftList, mTopology);
        mWampWebSocketServer.setClientAuthCallback(mClientAuthCallback);
        mIsStarted = false;
    }

    public void setClientAuthCallback(ClientAuthCallback callback) {
        mClientAuthCallback = callback;
        mWampWebSocketServer.setClientAuthCallback(mClientAuthCallback);
    }

    private static final class WampTopology {

        private Map<KadecotWampClient, KadecotWampSetupCallback> mWampCallbacks = new HashMap<KadecotWampClient, KadecotWampSetupCallback>();
        private Map<KadecotWampClient, KadecotProtocolSetupCallback> mProtocolCallbacks = new HashMap<KadecotWampClient, KadecotProtocolSetupCallback>();

        public WampTopology() {
            for (KadecotWampClient client : KadecotWampPeerLocator.getSystemClients()) {
                client.connect(KadecotWampPeerLocator.getRouter());
            }
            for (KadecotWampClient client : KadecotWampPeerLocator.getProtocolClients()) {
                client.connect(KadecotWampPeerLocator.getRouter());
            }
        }

        public WampRouter getRouter() {
            return KadecotWampPeerLocator.getRouter();
        }

        public void start() throws InterruptedException {
            final CountDownLatch systemSetup = new CountDownLatch(
                    KadecotWampPeerLocator.getSystemClients().length);
            for (KadecotWampClient client : KadecotWampPeerLocator.getSystemClients()) {
                KadecotWampSetupCallback callback = new KadecotWampSetupCallback(
                        client.getTopicsToSubscribe(), client.getRegisterableProcedures()
                                .keySet(),
                        new KadecotWampSetupCallback.OnCompletionListener() {
                            @Override
                            public void onCompletion() {
                                systemSetup.countDown();
                            }
                        });
                mWampCallbacks.put(client, callback);
                client.setCallback(callback);
            }

            for (KadecotWampClient client : KadecotWampPeerLocator.getSystemClients()) {
                client.transmit(WampMessageFactory.createHello(KadecotWampRouter.REALM,
                        new JSONObject()));
            }

            if (!systemSetup.await(5, TimeUnit.SECONDS)) {
                throw new IllegalArgumentException();
            }

            final CountDownLatch wampSetup = new CountDownLatch(
                    KadecotWampPeerLocator.getProtocolClients().length);
            for (KadecotWampClient client : KadecotWampPeerLocator.getProtocolClients()) {
                KadecotProtocolSetupCallback protocolCallback = new KadecotProtocolSetupCallback(
                        client.getSubscribableTopics(),
                        client.getRegisterableProcedures(),
                        new KadecotProtocolSetupCallback.OnCompletionListener() {
                            @Override
                            public void onCompletion() {
                            }
                        });
                mProtocolCallbacks.put(client, protocolCallback);
                client.setCallback(protocolCallback);

                KadecotWampSetupCallback wampCallback = new KadecotWampSetupCallback(
                        client.getTopicsToSubscribe(), client.getRegisterableProcedures().keySet(),
                        new KadecotWampSetupCallback.OnCompletionListener() {
                            @Override
                            public void onCompletion() {
                                wampSetup.countDown();
                            }
                        });
                mWampCallbacks.put(client, wampCallback);
                client.setCallback(wampCallback);
            }

            for (KadecotWampClient client : KadecotWampPeerLocator.getProtocolClients()) {
                client.transmit(WampMessageFactory.createHello(KadecotWampRouter.REALM,
                        new JSONObject()));
            }

            if (!wampSetup.await(5, TimeUnit.SECONDS)) {
                throw new IllegalArgumentException();
            }

        }

        public void stop() {
            for (KadecotWampClient client : KadecotWampPeerLocator.getProtocolClients()) {
                client.transmit(WampMessageFactory.createGoodbye(new JSONObject(),
                        WampError.CLOSE_REALM));
            }

            for (KadecotWampClient client : KadecotWampPeerLocator.getSystemClients()) {
                client.transmit(WampMessageFactory.createGoodbye(new JSONObject(),
                        WampError.CLOSE_REALM));
            }

            for (Entry<KadecotWampClient, KadecotWampSetupCallback> entry : mWampCallbacks
                    .entrySet()) {
                entry.getKey().removeCallback(entry.getValue());
            }

            for (Entry<KadecotWampClient, KadecotProtocolSetupCallback> entry : mProtocolCallbacks
                    .entrySet()) {
                entry.getKey().removeCallback(entry.getValue());
            }
        }

    }

    private static final class WampWebSocketServer extends
            org.java_websocket.server.WebSocketServer {

        private final Map<WebSocket, WebSocketPeer> mClients = new ConcurrentHashMap<WebSocket, WebSocketPeer>();
        private final WampTopology mTopology;
        private ClientAuthCallback mAuthCallback = null;

        public WampWebSocketServer(InetSocketAddress address, List<Draft> draftList,
                WampTopology topology) {
            super(address, draftList);
            mTopology = topology;
        }

        public void setClientAuthCallback(ClientAuthCallback callback) {
            mAuthCallback = callback;
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            if (mAuthCallback == null) {
                conn.close();
                return;
            }
            if (!mAuthCallback.authenticate(convertTo(handshake))) {
                conn.close();
                return;
            }
            WebSocketPeer client = new WebSocketPeer(conn);
            client.connect(mTopology.getRouter());
            mClients.put(conn, client);
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            WebSocketPeer peer = mClients.remove(conn);
            if (peer != null) {
                peer.close();
            }
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
            for (WebSocket ws : mClients.keySet()) {
                ws.close(CloseFrame.GOING_AWAY);
                WebSocketPeer peer = mClients.remove(ws);
                if (peer != null) {
                    peer.close();
                }
            }
            mClients.clear();
            super.stop(timeout);
        }

        private OpeningHandshake convertTo(final ClientHandshake src) {
            return new OpeningHandshake() {

                @Override
                public String getUpgrade() {
                    return src.getFieldValue("Upgrade");
                }

                @Override
                public String getSecWebSocketVersion() {
                    return src.getFieldValue("Sec-WebSocket-Version");
                }

                @Override
                public String getSecWebSocketProtocol() {
                    return src.getFieldValue("Sec-WebSocket-Protocol");
                }

                @Override
                public String getSecWebSocketKey() {
                    return src.getFieldValue("Sec-WebSocket-Key");
                }

                @Override
                public String getOrigin() {
                    return src.getFieldValue("Origin");
                }

                @Override
                public String getHost() {
                    return src.getFieldValue("Host");
                }

                @Override
                public String getFieldValue(String field) {
                    return src.getFieldValue(field);
                }

                @Override
                public String getConnection() {
                    return src.getFieldValue("Connection");
                }
            };
        }
    }

}
