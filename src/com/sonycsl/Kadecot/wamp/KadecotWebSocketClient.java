
package com.sonycsl.Kadecot.wamp;

import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampCaller;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.role.WampSubscriber;

import org.java_websocket.WebSocket;

public class KadecotWebSocketClient extends WampClient {

    private final WebSocket mWs;

    public KadecotWebSocketClient(WebSocket webSocket) {
        super();
        mWs = webSocket;
    }

    @Override
    protected WampRole getClientRole() {
        WampSubscriber subscriber = new WampSubscriber() {
            @Override
            protected void event(String topic, WampMessage msg) {
            }
        };
        return new WampCaller(subscriber);
    }

    @Override
    protected void OnConnected(WampPeer peer) {
    }

    @Override
    protected void OnReceived(WampMessage msg) {
        mWs.send(msg.toString());
    }

}
