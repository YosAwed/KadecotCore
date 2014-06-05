/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampRole;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class WampRouter extends WampPeer {

    @Override
    protected final Set<WampRole> getRoleSet() {
        Set<WampRole> roleSet = new HashSet<WampRole>();
        Set<WampRole> routerRole = getRouterRoleSet();
        roleSet.add(new WampRouterSession(routerRole));
        roleSet.addAll(routerRole);
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

        private Set<WampRole> mRoleSet;

        private static final String ROLES_KEY = "roles";

        @Override
        public final String getRoleName() {
            return "sessionRouter";
        }

        public WampRouterSession(Set<WampRole> roleSet) {
            mRoleSet = roleSet;
        }

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

            if (!mSessions.containsKey(transmitter)) {
                listener.onReply(transmitter,
                        WampMessageFactory.createError(msg.getMessageType(), -1, null,
                                WampError.NOT_AUTHORIZED));
                return true;
            }

            if (msg.isGoodbyeMessage()) {
                return resolveGoodByeMessage(transmitter, msg, listener);
            }

            return false;
        }

        private boolean resolveHelloMessage(WampPeer transmitter, WampMessage msg,
                OnReplyListener listener) {
            int sessionId;
            if (mSessions.containsKey(transmitter)) {
                sessionId = mSessions.get(transmitter);
            } else {
                sessionId = ++mSessionId;
            }
            mSessions.put(transmitter, sessionId);

            try {
                JSONObject roles = new JSONObject();
                Iterator<WampRole> ite = mRoleSet.iterator();
                while (ite.hasNext()) {
                    roles.put(ite.next().getRoleName(), new JSONObject());
                }
                JSONObject details = new JSONObject().put(ROLES_KEY, roles);
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
