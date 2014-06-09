/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.wamp;

import android.util.Log;

import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampRole;

import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

import java.util.HashSet;
import java.util.Set;

public class KadecotWebSocketClient extends WampPeer {

    private static final String TAG = KadecotWebSocketClient.class.getSimpleName();

    private final WebSocket mWs;

    public KadecotWebSocketClient(WebSocket webSocket) {
        super();
        mWs = webSocket;
    }

    @Override
    protected Set<WampRole> getRoleSet() {
        Set<WampRole> roleSet = new HashSet<WampRole>();
        roleSet.add(new WampRole() {

            @Override
            protected boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg) {
                return true;
            }

            @Override
            protected boolean resolveRxMessageImpl(WampPeer transmitter, WampMessage msg,
                    OnReplyListener listener) {
                return true;
            }

            @Override
            public String getRoleName() {
                return null;
            }
        });
        return roleSet;
    }

    @Override
    protected void onConnected(WampPeer peer) {
    }

    @Override
    protected void onTransmitted(WampPeer peer, WampMessage msg) {
    }

    @Override
    protected void onReceived(WampMessage msg) {
        if (!mWs.isOpen()) {
            Log.i(TAG, "OnReceived: WebSocket is already closed. msg=" + msg.toString());
            return;
        }

        try {
            mWs.send(msg.toString());
        } catch (WebsocketNotConnectedException e) {
            Log.i(TAG, "OnReceived: WebSocket is already closed. msg=" + msg.toString());
        }
    }

}
