
package com.sonycsl.Kadecot.wamp;

import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.WampEventMessage;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampSubscribedMessage;
import com.sonycsl.wamp.WampSubscriber;
import com.sonycsl.wamp.WampUnsubscribedMessage;

import org.java_websocket.WebSocket;

public class KadecotWampSubscriber extends WampSubscriber {

    private WebSocket mWs;

    public KadecotWampSubscriber(WebSocket ws) {
        mWs = ws;
    }

    public KadecotWampSubscriber(WebSocket ws, WampClient next) {
        super(next);
        mWs = ws;
    }

    @Override
    protected void subscribed(WampSubscribedMessage msg) {
        mWs.send(msg.toString());
    }

    @Override
    protected void unsubscribed(WampUnsubscribedMessage msg) {
        mWs.send(msg.toString());
    }

    @Override
    protected void event(WampEventMessage msg) {
        mWs.send(msg.toString());
    }

    @Override
    protected void onConsumed(WampMessage msg) {
    }

    @Override
    protected boolean consumeRoleBroadcast(WampMessage msg) {
        return false;
    }

}
