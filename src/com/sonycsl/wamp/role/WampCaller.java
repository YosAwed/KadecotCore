
package com.sonycsl.wamp.role;

import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;

public class WampCaller extends WampRole {

    public WampCaller() {
        super();
    }

    public WampCaller(WampRole next) {
        super(next);
    }

    @Override
    public final boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg) {
        return msg.isCallMessage();
    }

    @Override
    public final boolean resolveRxMessageImpl(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {
        return msg.isResultMessage();
    }
}
