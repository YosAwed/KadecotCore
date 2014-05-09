/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampRole;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

abstract public class WampClient extends WampPeer {

    @Override
    protected final Set<WampRole> getRoleSet() {
        Set<WampRole> roleSet = new HashSet<WampRole>();
        Set<WampRole> clientRole = getClientRoleSet();
        roleSet.add(new WampClientSession());
        roleSet.addAll(clientRole);
        return roleSet;
    }

    abstract protected Set<WampRole> getClientRoleSet();

    @Override
    public final void transmit(WampMessage msg) {
        super.transmit(msg);
    }

    private static final class WampClientSession extends WampRole {

        public WampClientSession() {
        }

        @Override
        public final String getRoleName() {
            return "sessionClient";
        }

        @Override
        protected final boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg) {
            if (msg.isHelloMessage()) {
                return true;
            }
            if (msg.isGoodbyeMessage()) {
                return true;
            }
            return false;
        }

        @Override
        protected final boolean resolveRxMessageImpl(WampPeer transmitter, WampMessage msg,
                OnReplyListener listener) {

            if (msg.isWelcomeMessage()) {
                return true;
            }

            if (msg.isAbortMessage() || msg.isErrorMessage()) {
                return true;
            }

            if (msg.isGoodbyeMessage()) {
                return resolveGoodbyeMessage(transmitter, msg, listener);
            }

            if (msg.isChallengeMessage()) {
                return resolveInvalidMessage(transmitter, msg, listener);
            }

            if (msg.isHeartbeatMessage()) {
                return resolveInvalidMessage(transmitter, msg, listener);
            }

            return false;
        }

        private boolean resolveGoodbyeMessage(WampPeer transmitter, WampMessage msg,
                OnReplyListener listener) {
            String reason = msg.asGoodbyeMessage().getReason();

            if (reason.equals(WampError.GOODBYE_AND_OUT)) {
                return true;
            }

            if (reason.equals(WampError.SYSTEM_SHUTDOWN)
                    || reason.equals(WampError.CLOSE_REALM)) {
                listener.onReply(transmitter, WampMessageFactory.createGoodbye(new JSONObject(),
                        WampError.GOODBYE_AND_OUT));
                return true;
            }

            return resolveInvalidMessage(transmitter, msg, listener);
        }

        private boolean resolveInvalidMessage(WampPeer transmitter, WampMessage msg,
                OnReplyListener listener) {
            listener.onReply(transmitter,
                    WampMessageFactory.createError(msg.getMessageType(), -1, null,
                            WampError.NOT_AUTHORIZED));
            return true;
        }
    }
}
