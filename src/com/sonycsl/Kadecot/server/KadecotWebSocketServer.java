
package com.sonycsl.Kadecot.server;

import android.content.Context;

import com.sonycsl.Kadecot.call.KadecotCall;
import com.sonycsl.Kadecot.call.NotificationProcessor;
import com.sonycsl.Kadecot.call.RequestProcessor;
import com.sonycsl.Kadecot.core.Dbg;
import com.sonycsl.Kadecot.core.KadecotCoreApplication;
import com.sonycsl.Kadecot.device.DeviceManager;
import com.sonycsl.Kadecot.wamp.KadecotDeviceObserver;
import com.sonycsl.Kadecot.wamp.KadecotTopicTimer;
import com.sonycsl.Kadecot.wamp.KadecotWampRouter;
import com.sonycsl.Kadecot.wamp.KadecotWampTopic;
import com.sonycsl.Kadecot.wamp.KadecotWebSocketClient;
import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    private KadecotDeviceObserver mDeviceObserver;

    private KadecotTopicTimer mTopicTimer;

    public synchronized static KadecotWebSocketServer getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new KadecotWebSocketServer(context);
        }
        return sInstance;
    }

    private KadecotWebSocketServer(Context context) {
        mContext = context.getApplicationContext();

        mRouter = new KadecotWampRouter();
        mDeviceObserver = new KadecotDeviceObserver();
        mDeviceObserver.connect(mRouter);
        mTopicTimer = new KadecotTopicTimer(KadecotWampTopic.TOPIC_PRIVATE_SEARCH, 5,
                TimeUnit.SECONDS);
        mTopicTimer.connect(mRouter);

        List<WampPeer> peers = DeviceManager.getInstance(context).getWampPeers();
        for (WampPeer peer : peers) {
            peer.connect(mRouter);
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
        mDeviceObserver.transmit(WampMessageFactory.createHello(KadecotWampRouter.REALM,
                new JSONObject()));
        mTopicTimer.transmit(WampMessageFactory.createHello(KadecotWampRouter.REALM,
                new JSONObject()));

        List<WampPeer> peers = DeviceManager.getInstance(mContext).getWampPeers();
        for (WampPeer peer : peers) {
            peer.transmit(WampMessageFactory.createHello(KadecotWampRouter.REALM,
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
        mDeviceObserver.transmit(WampMessageFactory.createGoodbye(new JSONObject(),
                WampError.CLOSE_REALM));
        mTopicTimer.transmit(WampMessageFactory.createGoodbye(new JSONObject(),
                WampError.CLOSE_REALM));
        List<WampPeer> peers = DeviceManager.getInstance(mContext).getWampPeers();
        for (WampPeer peer : peers) {
            peer.transmit(WampMessageFactory.createGoodbye(new JSONObject(),
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
