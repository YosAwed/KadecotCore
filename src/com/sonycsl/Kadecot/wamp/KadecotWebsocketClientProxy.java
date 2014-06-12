
package com.sonycsl.Kadecot.wamp;

import android.util.Log;

import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampRole;

import org.java_websocket.WebSocket.READYSTATE;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class KadecotWebsocketClientProxy extends WampPeer {

    private static final String TAG = KadecotWebsocketClientProxy.class.getSimpleName();

    private static final String PROTOCOL = "ws://";

    private WebSocketClient mWsClient;

    @Override
    protected Set<WampRole> getRoleSet() {
        Set<WampRole> roleSet = new HashSet<WampRole>();
        roleSet.add(new WampWebSocketClientProxy());
        return roleSet;
    }

    @Override
    protected void onConnected(WampPeer peer) {
    }

    public void open(String ipaddress, String port) throws InterruptedException, TimeoutException {
        if (mWsClient != null && mWsClient.getReadyState() == READYSTATE.OPEN) {
            Log.i(TAG, "WebSocket is already opened");
            return;
        }

        URI uri;
        try {
            uri = new URI(PROTOCOL + ipaddress + ":" + port);
        } catch (URISyntaxException e) {
            Log.e(TAG, "URI Syntax Exception" + ipaddress);
            return;
        }

        final CountDownLatch latch = new CountDownLatch(1);
        mWsClient = new WebSocketClient(uri) {

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.d(TAG, "onOpen : " + handshakedata.getHttpStatusMessage());
                latch.countDown();
            }

            @Override
            public void onMessage(String message) {
                Log.d(TAG, "onMessage : " + message);
                try {
                    transmit(WampMessageFactory.create(new JSONArray(message)));
                } catch (JSONException e) {
                    Log.e(TAG, "message is not WAMP format: " + message);
                }
            }

            @Override
            public void onError(Exception ex) {
                Log.d(TAG, "onError : " + ex);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.d(TAG, "onClose : code : " + code + ", reason : " + reason +
                        ", remote : " + remote);
            }
        };
        mWsClient.connect();

        if (!latch.await(1, TimeUnit.SECONDS)) {
            throw new TimeoutException("WebSocket connect timeout");
        }

    }

    public void close() {
        if (mWsClient == null || mWsClient.getReadyState() != READYSTATE.OPEN) {
            Log.i(TAG, "WebSocket is already closed");
            return;
        }

        mWsClient.close();
    }

    public boolean isOpen() {
        if (mWsClient == null) {
            return false;
        }
        return mWsClient.getReadyState() == READYSTATE.OPEN;
    }

    @Override
    protected void onTransmitted(WampPeer peer, WampMessage msg) {
    }

    @Override
    protected void onReceived(WampMessage msg) {
        if (mWsClient == null) {
            Log.e(TAG, "msg: " + msg);
            return;
        }

        if (mWsClient.getReadyState() == READYSTATE.OPEN) {
            mWsClient.send(msg.toString());
        }
    }

    private static class WampWebSocketClientProxy extends WampRole {

        @Override
        public String getRoleName() {
            return "proxy";
        }

        @Override
        protected boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg) {
            return true;
        }

        @Override
        protected boolean resolveRxMessageImpl(WampPeer transmitter, WampMessage msg,
                OnReplyListener listener) {
            return true;
        }

    }

}
