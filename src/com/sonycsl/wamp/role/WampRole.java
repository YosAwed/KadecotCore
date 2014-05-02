
package com.sonycsl.wamp.role;

import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;

abstract public class WampRole {

    abstract public String getRoleName();

    public interface OnReplyListener {
        void onReply(WampPeer receiver, WampMessage reply);
    }

    public final boolean resolveTxMessage(WampPeer receiver, WampMessage msg) {
        return resolveTxMessageImpl(receiver, msg);
    }

    public final boolean resolveRxMessage(WampPeer receiver, WampMessage msg,
            OnReplyListener listener) {
        return resolveRxMessageImpl(receiver, msg, listener);
    }

    abstract protected boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg);

    abstract protected boolean resolveRxMessageImpl(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener);
}
