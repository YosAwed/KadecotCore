
package com.sonycsl.Kadecot.wamp;

import com.sonycsl.wamp.WampCaller;
import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampResultMessage;

import org.java_websocket.WebSocket;

public class KadecotWampCaller extends WampCaller {

    private WebSocket mWs;

    public KadecotWampCaller(WebSocket ws) {
        mWs = ws;
    }

    public KadecotWampCaller(WebSocket ws, WampClient next) {
        super(next);
        mWs = ws;
    }

    @Override
    protected void result(WampResultMessage msg) {
    }

    @Override
    protected void onConsumed(WampMessage msg) {
        mWs.send(msg.toString());
    }
}
