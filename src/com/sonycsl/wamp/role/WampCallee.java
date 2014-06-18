/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp.role;

import com.sonycsl.wamp.DoubleKeyMap;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampInvocationMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampRegisterMessage;
import com.sonycsl.wamp.message.WampRegisteredMessage;
import com.sonycsl.wamp.message.WampUnregisterMessage;
import com.sonycsl.wamp.message.WampUnregisteredMessage;

import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract public class WampCallee extends WampRole {

    private final DoubleKeyMap<WampPeer, Integer, WampMessage> mRegs = new DoubleKeyMap<WampPeer, Integer, WampMessage>();
    private final DoubleKeyMap<WampPeer, Integer, WampMessage> mUnregs = new DoubleKeyMap<WampPeer, Integer, WampMessage>();
    private final Map<WampPeer, Map<Integer, String>> mProcMaps = new ConcurrentHashMap<WampPeer, Map<Integer, String>>();

    @Override
    public final String getRoleName() {
        return "callee";
    }

    @Override
    protected final boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg) {
        if (msg.isRegisterMessage()) {
            mRegs.put(receiver, msg.asRegisterMessage().getRequestId(), msg);
            if (mProcMaps.get(receiver) == null) {
                mProcMaps.put(receiver, new ConcurrentHashMap<Integer, String>());
            }
            return true;
        }

        if (msg.isUnregisterMessage()) {
            mUnregs.put(receiver, msg.asUnregisterMessage().getRequestId(), msg);
            return true;
        }

        return false;
    }

    @Override
    protected final boolean resolveRxMessageImpl(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {

        if (msg.isRegisteredMessage()) {
            return resolveRegisteredMessage(transmitter, msg);
        }

        if (msg.isUnregisteredMessage()) {
            return resolveUnregisteredMessage(transmitter, msg);
        }

        if (msg.isInvocationMessage()) {
            return resolveInvocationMessage(transmitter, msg, listener);
        }

        return false;
    }

    private boolean resolveRegisteredMessage(WampPeer transmitter, WampMessage msg) {
        WampRegisteredMessage registeredMsg = msg.asRegisteredMessage();
        if (!mRegs.containsKey(transmitter, registeredMsg.getRequestId())) {
            return false;
        }

        WampRegisterMessage request = mRegs.get(transmitter, registeredMsg.getRequestId())
                .asRegisterMessage();
        WampRegisteredMessage response = msg.asRegisteredMessage();

        if (request.getRequestId() != response.getRequestId()) {
            return false;
        }

        Map<Integer, String> procMap = mProcMaps.get(transmitter);
        if (procMap == null) {
            return false;
        }
        procMap.put(response.getRegistrationId(), request.getProcedure());

        mRegs.remove(transmitter, registeredMsg.getRequestId());
        return true;
    }

    private boolean resolveUnregisteredMessage(WampPeer transmitter, WampMessage msg) {
        WampUnregisteredMessage unregisteredMsg = msg.asUnregisteredMessage();
        if (!mUnregs.containsKey(transmitter, unregisteredMsg.getRequestId())) {
            return false;
        }

        WampUnregisterMessage request = mUnregs.get(transmitter, unregisteredMsg.getRequestId())
                .asUnregisterMessage();
        WampUnregisteredMessage response = msg.asUnregisteredMessage();

        if (request.getRequestId() != response.getRequestId()) {
            return false;
        }
        Map<Integer, String> procMap = mProcMaps.get(transmitter);
        if (procMap == null) {
            return false;
        }
        if (!procMap.containsKey(request.getRegistrationId())) {
            return false;
        }
        procMap.remove(request.getRegistrationId());

        mUnregs.remove(transmitter, unregisteredMsg.getRequestId());
        return true;
    }

    private boolean resolveInvocationMessage(final WampPeer transmitter, final WampMessage msg,
            final OnReplyListener listener) {

        WampInvocationMessage invocation = msg.asInvocationMessage();

        final Map<Integer, String> procMap = mProcMaps.get(transmitter);
        if (procMap == null) {
            return false;
        }

        final int regId = invocation.getRegistrationId();
        if (!procMap.containsKey(regId)) {
            listener.onReply(transmitter, WampMessageFactory.createError(msg.getMessageType(), -1,
                    new JSONObject(), WampError.NO_SUCH_PROCEDURE));
            return true;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                WampMessage reply = invocation(procMap.get(regId), msg);
                if (!reply.isYieldMessage() && !reply.isErrorMessage()) {
                    return;
                }
                listener.onReply(transmitter, reply);
            }
        }).start();

        return true;
    }

    abstract protected WampMessage invocation(String procedure, WampMessage msg);
}
