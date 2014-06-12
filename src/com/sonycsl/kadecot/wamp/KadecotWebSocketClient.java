/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.wamp;

import android.util.Log;

import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampCaller;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.role.WampSubscriber;

import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

import java.util.HashSet;
import java.util.Set;

public class KadecotWebSocketClient extends WampClient {

    private static final String TAG = KadecotWebSocketClient.class.getSimpleName();

    private final WebSocket mWs;

    public KadecotWebSocketClient(WebSocket webSocket) {
        super();
        mWs = webSocket;
    }

    @Override
    protected Set<WampRole> getClientRoleSet() {
        Set<WampRole> roleSet = new HashSet<WampRole>();
        roleSet.add(new WampCaller());
        roleSet.add(new WampSubscriber() {
            @Override
            protected void onEvent(String topic, WampMessage msg) {
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
