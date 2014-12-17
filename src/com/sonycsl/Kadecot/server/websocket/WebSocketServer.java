/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.server.websocket;

import android.os.Build;

import com.sonycsl.Kadecot.wamp.util.WampLocatorCallback;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.transport.WebSocketPeer;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketServer {

    private final int mPortNo;
    private String mSupportProtocol;

    private List<Draft> mDraftList;
    private WampWebSocketServer mWampWebSocketServer;

    private boolean mIsStarted = false;

    private ClientAuthCallback mClientAuthCallback = null;
    private WampLocatorCallback mWampLocatorCallback = null;

    public WebSocketServer(int portno, String supportProtocol) {
        if (Build.PRODUCT.startsWith("sdk")) {
            java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
            java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
        }

        mPortNo = portno;
        mSupportProtocol = supportProtocol;
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
        mWampWebSocketServer = new WampWebSocketServer(new InetSocketAddress(mPortNo), mDraftList);
    }

    public synchronized void start() {
        if (mIsStarted) {
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
            mWampWebSocketServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mWampWebSocketServer = new WampWebSocketServer(new InetSocketAddress(mPortNo), mDraftList);
        mWampWebSocketServer.setClientAuthCallback(mClientAuthCallback);
        mWampWebSocketServer.setWampLocatorCallback(mWampLocatorCallback);
        mIsStarted = false;
    }

    public void setClientAuthCallback(ClientAuthCallback callback) {
        mClientAuthCallback = callback;
        mWampWebSocketServer.setClientAuthCallback(mClientAuthCallback);
    }

    public void setWampLocatorCallback(WampLocatorCallback callback) {
        mWampLocatorCallback = callback;
        mWampWebSocketServer.setWampLocatorCallback(mWampLocatorCallback);
    }

    private static final class WampWebSocketServer extends
            org.java_websocket.server.WebSocketServer {

        private final Map<WebSocket, WebSocketPeer> mClients = new ConcurrentHashMap<WebSocket, WebSocketPeer>();
        private ClientAuthCallback mAuthCallback = null;
        private WampLocatorCallback mWampLocator;

        public WampWebSocketServer(InetSocketAddress address, List<Draft> draftList) {
            super(address, draftList);
        }

        public void setClientAuthCallback(ClientAuthCallback callback) {
            mAuthCallback = callback;
        }

        public void setWampLocatorCallback(WampLocatorCallback callback) {
            mWampLocator = callback;
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            if (mAuthCallback == null) {
                conn.close();
                return;
            }
            if (!mAuthCallback.isAuthenticated(convertTo(handshake))) {
                conn.close();
                return;
            }

            if (mWampLocator == null) {
                return;
            }
            WebSocketPeer client = new AuthorizedWebSocketPeer(conn,
                    mAuthCallback.getScopeSet(convertTo(handshake)));
            mWampLocator.locate(client);
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

                @Override
                public List<NameValuePair> getParameters() {
                    URI uri;
                    try {
                        String dummy = getOrigin();
                        uri = new URI(dummy + src.getResourceDescriptor());
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                        return new LinkedList<NameValuePair>();
                    }
                    return URLEncodedUtils.parse(uri, "UTF-8");
                }

                @Override
                public String getParameter(String name) {
                    Iterator<NameValuePair> itr = getParameters().iterator();
                    while (itr.hasNext()) {
                        NameValuePair pair = itr.next();
                        if (pair.getName().equals(name)) {
                            return pair.getValue();
                        }
                    }
                    return "";
                }
            };
        }
    }

}
