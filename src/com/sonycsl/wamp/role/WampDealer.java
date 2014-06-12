/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp.role;

import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampLog;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampCallMessage;
import com.sonycsl.wamp.message.WampErrorMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.WampRegisterMessage;
import com.sonycsl.wamp.message.WampUnregisterMessage;
import com.sonycsl.wamp.message.WampYieldMessage;

import org.json.JSONObject;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

abstract public class WampDealer extends WampRole {

    private static final String TAG = WampDealer.class.getSimpleName();

    private final Map<String, RegisterInfo> mCallees = new ConcurrentHashMap<String, WampDealer.RegisterInfo>();
    private final Map<Integer, CallInfo> mCallers = new ConcurrentHashMap<Integer, WampDealer.CallInfo>();
    private int mRegistraionId = 0;
    private int mInvocationRequestId = 0;

    @Override
    public final String getRoleName() {
        return "dealer";
    }

    @Override
    public final boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean resolveRxMessageImpl(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {

        if (msg.isRegisterMessage()) {
            return resolveRegisterMessage(transmitter, msg, listener);
        }

        if (msg.isUnregisterMessage()) {
            return resolveUnregisterMessage(transmitter, msg, listener);
        }

        if (msg.isCallMessage()) {
            return resolveCallMessage(transmitter, msg, listener);
        }

        if (msg.isYieldMessage()) {
            return resolveYieldMessage(transmitter, msg, listener);
        }

        if (msg.isErrorMessage()) {
            return resolveErrorMessage(transmitter, msg, listener);
        }

        return false;
    }

    private static final class RegisterInfo {
        private final WampPeer mCallee;
        private final int mRegistraionId;

        public RegisterInfo(WampPeer callee, int registrationId) {
            mCallee = callee;
            mRegistraionId = registrationId;
        }

        public WampPeer getCallee() {
            return mCallee;
        }

        public int getRegistraionId() {
            return mRegistraionId;
        }
    }

    private boolean resolveRegisterMessage(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {

        WampRegisterMessage reg = msg.asRegisterMessage();

        if (mCallees.containsKey(reg.getProcedure())) {
            listener.onReply(transmitter,
                    WampMessageFactory.createError(msg.getMessageType(), -1, new JSONObject(),
                            WampError.PROCEDURE_ALREADY_EXISTS));
            return true;
        }

        int registrationId = ++mRegistraionId;
        mCallees.put(reg.getProcedure(), new RegisterInfo(transmitter, registrationId));
        listener.onReply(transmitter,
                WampMessageFactory.createRegistered(reg.getRequestId(), registrationId));

        return true;
    }

    private boolean resolveUnregisterMessage(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {

        WampUnregisterMessage unreg = msg.asUnregisterMessage();
        int registraionId = unreg.getRegistrationId();

        for (Entry<String, RegisterInfo> entry : mCallees.entrySet()) {
            if (entry.getValue().getRegistraionId() == registraionId) {
                listener.onReply(entry.getValue().getCallee(),
                        WampMessageFactory.createUnregistered(unreg.getRequestId()));
                mCallees.remove(entry.getKey());
                break;
            }
        }
        return true;
    }

    private static final class CallInfo {
        private final WampPeer mCaller;
        private final int mRequestId;

        public CallInfo(WampPeer caller, int requestId) {
            mCaller = caller;
            mRequestId = requestId;
        }

        public WampPeer getCaller() {
            return mCaller;
        }

        public int getRequestId() {
            return mRequestId;
        }
    }

    private boolean resolveCallMessage(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {

        WampCallMessage call = msg.asCallMessage();

        if (!mCallees.containsKey(call.getProcedure())) {
            WampLog.e(TAG, "No such procedure: " + call.getProcedure());
            listener.onReply(transmitter,
                    WampMessageFactory.createError(msg.getMessageType(), call.getRequestId(),
                            new JSONObject(), WampError.NO_SUCH_PROCEDURE));
            return true;
        }

        RegisterInfo info = mCallees.get(call.getProcedure());
        int requestId = ++mInvocationRequestId;
        mCallers.put(requestId, new CallInfo(transmitter, call.getRequestId()));
        listener.onReply(info.getCallee(),
                createInvocationMessage(requestId, info.getRegistraionId(), call));

        return true;
    }

    private WampMessage createInvocationMessage(int requestId, int registrationId,
            WampCallMessage msg) {

        if (msg.hasArgumentsKw()) {
            return WampMessageFactory.createInvocation(requestId, registrationId,
                    createInvocationDetails(msg.getOptions()), msg.getArguments(),
                    msg.getArgumentsKw());
        }

        if (msg.hasArguments()) {
            return WampMessageFactory.createInvocation(requestId, registrationId,
                    createInvocationDetails(msg.getOptions()), msg.getArguments());
        }

        return WampMessageFactory.createInvocation(requestId, registrationId,
                createInvocationDetails(msg.getOptions()));
    }

    abstract protected JSONObject createInvocationDetails(JSONObject callOptions);

    private boolean resolveYieldMessage(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {

        WampYieldMessage yield = msg.asYieldMessage();
        int invocationRequestId = yield.getRequestId();

        if (!mCallers.containsKey(invocationRequestId)) {
            return false;
        }

        CallInfo info = mCallers.get(invocationRequestId);
        listener.onReply(info.getCaller(), createResultMessage(info.getRequestId(), yield));
        mCallers.remove(invocationRequestId);
        return true;
    }

    private WampMessage createResultMessage(int requestId, WampYieldMessage msg) {
        if (msg.hasArgumentsKw()) {
            return WampMessageFactory.createResult(requestId, new JSONObject(), msg.getArguments(),
                    msg.getArgumentsKw());
        }

        if (msg.hasArguments()) {
            return WampMessageFactory.createResult(requestId, new JSONObject(), msg.getArguments());
        }

        return WampMessageFactory.createResult(requestId, new JSONObject());
    }

    private boolean resolveErrorMessage(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {
        WampErrorMessage errorMsg = msg.asErrorMessage();
        int callRequestId = errorMsg.getRequestId();

        if (!mCallers.containsKey(callRequestId)) {
            return false;
        }

        if (errorMsg.getRequestType() != WampMessageType.INVOCATION) {
            return false;
        }

        CallInfo info = mCallers.get(callRequestId);
        listener.onReply(info.getCaller(), createErrorMessage(info.getRequestId(), errorMsg));
        mCallers.remove(callRequestId);

        return true;
    }

    private WampMessage createErrorMessage(int requestId, WampErrorMessage msg) {
        if (msg.hasArgumentsKw()) {
            return WampMessageFactory.createError(WampMessageType.CALL, requestId,
                    new JSONObject(), msg.getUri(), msg.getArguments(), msg.getArgumentsKw());
        }

        if (msg.hasArguments()) {
            return WampMessageFactory.createError(WampMessageType.CALL, requestId,
                    new JSONObject(), msg.getUri(), msg.getArguments());
        }

        return WampMessageFactory.createError(WampMessageType.CALL, requestId,
                new JSONObject(), msg.getUri());
    }
}
