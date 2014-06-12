
package com.sonycsl.Kadecot.server;

import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_76;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.HandshakeBuilder;
import org.java_websocket.handshake.ServerHandshakeBuilder;

public class Draft_76_Protocol extends Draft_76 {

    private static final String SEC_WS_PROTOCOL = "Sec-WebSocket-Protocol";

    private String mSupportProtocol;

    public Draft_76_Protocol(String protocol) {
        mSupportProtocol = protocol;
    }

    @Override
    public HandshakeState acceptHandshakeAsServer(ClientHandshake handshakedata) {
        if (super.acceptHandshakeAsServer(handshakedata) == HandshakeState.MATCHED) {
            String protocol = handshakedata.getFieldValue(SEC_WS_PROTOCOL);
            if (mSupportProtocol.equals(protocol)) {
                return HandshakeState.MATCHED;
            }
        }

        return HandshakeState.NOT_MATCHED;
    }

    @Override
    public HandshakeBuilder postProcessHandshakeResponseAsServer(ClientHandshake request,
            ServerHandshakeBuilder response) throws InvalidHandshakeException {
        HandshakeBuilder builder = super.postProcessHandshakeResponseAsServer(request,
                response);
        builder.put(SEC_WS_PROTOCOL, mSupportProtocol);
        return builder;
    }

    @Override
    public Draft copyInstance() {
        return new Draft_76_Protocol(mSupportProtocol);
    }
}
