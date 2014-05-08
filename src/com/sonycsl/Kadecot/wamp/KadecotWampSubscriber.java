
package com.sonycsl.Kadecot.wamp;

import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.WampSubscriber;
import com.sonycsl.wamp.message.WampEventMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampSubscribedMessage;
import com.sonycsl.wamp.message.WampUnsubscribedMessage;

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
    }

    @Override
    protected void unsubscribed(WampUnsubscribedMessage msg) {
    }

    @Override
    protected void event(WampEventMessage msg) {
    }

    @Override
    protected void onConsumed(WampMessage msg) {
        mWs.send(msg.toString());
    }
}
