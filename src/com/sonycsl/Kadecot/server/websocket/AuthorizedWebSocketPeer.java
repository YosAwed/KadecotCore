/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.server.websocket;

import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.message.WampCallMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.WampSubscribeMessage;
import com.sonycsl.wamp.transport.WebSocketPeer;

import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.util.Set;

public class AuthorizedWebSocketPeer extends WebSocketPeer {

    private final WebSocket mWebSocket;
    private final Set<String> mScopeSet;

    public AuthorizedWebSocketPeer(WebSocket webSocket, Set<String> scopeSet) {
        super(webSocket);
        mWebSocket = webSocket;
        mScopeSet = scopeSet;
    }

    private boolean isAuthorized(String uri) {
        for (String scope : mScopeSet) {
            if (uri.startsWith(scope)) {
                return true;
            }
        }
        return false;
    }

    private void authorizedCall(WampMessage msg) {
        WampCallMessage call = msg.asCallMessage();
        if (isAuthorized(call.getProcedure())) {
            super.transmit(msg);
            return;
        }

        WampMessage reply = WampMessageFactory.createError(msg.getMessageType(),
                call.getRequestId(), new JSONObject(), WampError.NOT_AUTHORIZED);
        mWebSocket.send(reply.toString());
    }

    private void authorizedSubscribe(WampMessage msg) {
        WampSubscribeMessage sub = msg.asSubscribeMessage();
        if (isAuthorized(sub.getTopic())) {
            super.transmit(msg);
            return;
        }

        WampMessage reply = WampMessageFactory.createError(msg.getMessageType(),
                sub.getRequestId(), new JSONObject(), WampError.NOT_AUTHORIZED);
        mWebSocket.send(reply.toString());
    }

    @Override
    public void transmit(WampMessage msg) {
        switch (msg.getMessageType()) {
            case WampMessageType.CALL:
                authorizedCall(msg);
                return;
            case WampMessageType.SUBSCRIBE:
                authorizedSubscribe(msg);
                return;
            default:
                super.transmit(msg);
                return;
        }

    }
}
