/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp;

import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampCaller;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.role.WampSubscriber;

import org.java_websocket.WebSocket;

import java.util.HashSet;
import java.util.Set;

public class KadecotWebSocketClient extends WampClient {

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
            protected void event(String topic, WampMessage msg) {
            }
        });
        return roleSet;
    }

    @Override
    protected void OnConnected(WampPeer peer) {
    }

    @Override
    protected void OnTransmitted(WampPeer peer, WampMessage msg) {
    }

    @Override
    protected void OnReceived(WampMessage msg) {
        mWs.send(msg.toString());
    }

}
