
package com.sonycsl.Kadecot.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.channels.NotYetConnectedException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.sonycsl.Kadecot.call.KadecotCall;
import com.sonycsl.Kadecot.call.NotificationProcessor;
import com.sonycsl.Kadecot.call.RequestProcessor;
import com.sonycsl.Kadecot.core.Dbg;

public class KadecotWebSocketServer {
    @SuppressWarnings("unused")
    private static final String TAG = KadecotWebSocketServer.class.getSimpleName();

    private final KadecotWebSocketServer self = this;

    private static final int portno = 41314;

    private Context mContext;

    protected static KadecotWebSocketServer sInstance = null;

    private WebSocketServerImpl mWebSocketServer = null;

    private boolean mStarted = false;

    public synchronized static KadecotWebSocketServer getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new KadecotWebSocketServer(context);
        }
        return sInstance;
    }

    private KadecotWebSocketServer(Context context) {
        mContext = context.getApplicationContext();

        WebSocketImpl.DEBUG = false;// true;
    }

    public synchronized void start() {
        if (isStarted()) {
            return;
        }
        stop();
        mWebSocketServer = new WebSocketServerImpl(new InetSocketAddress(portno));
        mWebSocketServer.start();
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
        mWebSocketServer = null;
        mStarted = false;
    }

    public class WebSocketServerImpl extends WebSocketServer {

        private Map<WebSocket, KadecotCall> mKadecotCalls = new HashMap<WebSocket, KadecotCall>();

        public WebSocketServerImpl(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {

            // origin
            String origin = handshake.getFieldValue("origin");
            KadecotCall kc = new WebSocketCall(mContext, conn);

            mKadecotCalls.put(conn, kc);
            kc.start();

        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            if (mKadecotCalls.containsKey(conn)) {
                KadecotCall kc = mKadecotCalls.get(conn);
                mKadecotCalls.remove(conn);
                kc.stop();
            }

        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            // TODO Auto-generated method stub

            Dbg.print(message);
            if (mKadecotCalls.containsKey(conn)) {
                try {
                    mKadecotCalls.get(conn).receive(new JSONObject(message));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
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

            for (WebSocket ws : mKadecotCalls.keySet()) {
                mKadecotCalls.get(ws).stop();
            }
            mKadecotCalls.clear();

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
