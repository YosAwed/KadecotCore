
package com.sonycsl.wamp;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampRole;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class WampRouter extends WampPeer {

    @Override
    protected final Set<WampRole> getRoleSet() {
        Set<WampRole> roleSet = new HashSet<WampRole>();
        roleSet.add(new WampRouterSession());
        roleSet.addAll(getRouterRoleSet());
        return roleSet;
    }

    abstract protected Set<WampRole> getRouterRoleSet();

    @Override
    public final void transmit(WampMessage msg) {
        if (!msg.isGoodbyeMessage()) {
            throw new UnsupportedOperationException();
        }
        super.transmit(msg);
    }

    private static final class WampRouterSession extends WampRole {

        private int mSessionId = 0;

        private final Map<WampPeer, Integer> mSessions = new ConcurrentHashMap<WampPeer, Integer>();

        @Override
        public boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg) {
            if (!msg.isGoodbyeMessage()) {
                throw new UnsupportedOperationException();
            }
            return true;
        }

        @Override
        public boolean resolveRxMessageImpl(WampPeer transmitter, WampMessage msg,
                OnReplyListener listener) {
            if (msg.isHelloMessage()) {
                return resolveHelloMessage(transmitter, msg, listener);
            }

            if (msg.isGoodbyeMessage()) {
                return resolveGoodByeMessage(transmitter, msg, listener);
            }

            if (!mSessions.containsKey(transmitter)) {
                listener.onReply(transmitter,
                        WampMessageFactory.createError(msg.getMessageType(), -1, null,
                                WampError.NOT_AUTHORIZED));
                return true;
            }

            return false;
        }

        private boolean resolveHelloMessage(WampPeer transmitter, WampMessage msg,
                OnReplyListener listener) {
            int sessionId = ++mSessionId;
            mSessions.put(transmitter, sessionId);
            try {
                JSONObject roles = new JSONObject().put("broker", new JSONObject()).put("dealer",
                        new JSONObject());
                JSONObject details = new JSONObject().put("roles", roles);
                listener.onReply(transmitter, WampMessageFactory.createWelcome(sessionId, details));
            } catch (JSONException e) {
                throw new IllegalStateException("JSONException");
            }
            return true;
        }

        private boolean resolveGoodByeMessage(WampPeer transmitter, WampMessage msg,
                OnReplyListener listener) {

            if (msg.asGoodbyeMessage().getReason().equals(WampError.GOODBYE_AND_OUT)) {
                return true;
            }

            if (!msg.asGoodbyeMessage().getReason().equals(WampError.CLOSE_REALM)) {
                listener.onReply(transmitter,
                        WampMessageFactory.createError(msg.getMessageType(), -1, null,
                                WampError.NOT_AUTHORIZED));
                return true;
            }

            listener.onReply(transmitter, WampMessageFactory.createGoodbye(new JSONObject(),
                    WampError.GOODBYE_AND_OUT));
            mSessions.remove(transmitter);
            return true;
        }
    }
}
