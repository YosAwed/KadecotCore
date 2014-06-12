
package com.sonycsl.Kadecot.server;

import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.exceptions.InvalidHandshakeException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.HandshakeBuilder;
import org.java_websocket.handshake.ServerHandshakeBuilder;

public class Draft_10_Protocol extends Draft_10 {

    private static final String SEC_WS_PROTOCOL = "Sec-WebSocket-Protocol";

    private String mSupportProtocol;

    public Draft_10_Protocol(String protocol) {
        mSupportProtocol = protocol;
    }

    @Override
    public HandshakeState acceptHandshakeAsServer(ClientHandshake handshakedata)
            throws InvalidHandshakeException {
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
        return new Draft_10_Protocol(mSupportProtocol);
    }
}
