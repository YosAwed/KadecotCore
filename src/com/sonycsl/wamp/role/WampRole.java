
package com.sonycsl.wamp.role;

import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;

abstract public class WampRole {

    private final WampRole mNext;

    public interface OnReplyListener {
        void onReply(WampPeer receiver, WampMessage reply);
    }

    public WampRole() {
        mNext = null;
    }

    public WampRole(WampRole next) {
        mNext = next;
    }

    public final boolean resolveTxMessage(WampPeer receiver, WampMessage msg) {
        if (resolveTxMessageImpl(receiver, msg)) {
            return true;
        }
        if (mNext == null) {
            return false;
        }
        return mNext.resolveTxMessage(receiver, msg);
    }

    public final boolean resolveRxMessage(WampPeer receiver, WampMessage msg,
            OnReplyListener listener) {
        if (resolveRxMessageImpl(receiver, msg, listener)) {
            return true;
        }
        if (mNext == null) {
            return false;
        }
        return mNext.resolveRxMessage(receiver, msg, listener);
    }

    abstract protected boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg);

    abstract protected boolean resolveRxMessageImpl(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener);
}
