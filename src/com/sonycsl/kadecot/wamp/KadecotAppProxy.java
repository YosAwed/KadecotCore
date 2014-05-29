
package com.sonycsl.kadecot.wamp;

import android.util.Log;

import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampRole;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class KadecotAppProxy extends WampPeer {

    private static final String TAG = KadecotAppProxy.class.getSimpleName();

    private static final String PROTOCOL = "ws://";

    // private static final String PORT = "41314";

    private WebSocketClient mWsClient;

    @Override
    protected Set<WampRole> getRoleSet() {
        Set<WampRole> roleSet = new HashSet<WampRole>();
        roleSet.add(new WampWebSocketClientProxy());
        return roleSet;
    }

    @Override
    protected void OnConnected(WampPeer peer) {
    }

    void connect(String ipaddress, String port) {
        URI uri;
        try {
            uri = new URI(PROTOCOL + ipaddress + ":" + port);
        } catch (URISyntaxException e) {
            Log.e(TAG, "URI Syntax Exception" + ipaddress);
            return;
        }

        mWsClient = new WebSocketClient(uri) {

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.d(TAG, "onOpen : " + handshakedata.getHttpStatusMessage());
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
    }

    void disconnect() {
        mWsClient.close();
    }

    @Override
    protected void OnTransmitted(WampPeer peer, WampMessage msg) {
    }

    @Override
    protected void OnReceived(WampMessage msg) {
        if (mWsClient == null) {
            Log.e(TAG, "msg: " + msg);
        }

        mWsClient.send(msg.toString());
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
