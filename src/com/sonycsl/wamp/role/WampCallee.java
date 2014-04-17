
package com.sonycsl.wamp.role;

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

    private final Map<WampPeer, WampMessage> mRegs = new ConcurrentHashMap<WampPeer, WampMessage>();
    private final Map<WampPeer, WampMessage> mUnregs = new ConcurrentHashMap<WampPeer, WampMessage>();
    private final Map<WampPeer, Map<Integer, String>> mProcMaps = new ConcurrentHashMap<WampPeer, Map<Integer, String>>();

    public WampCallee() {
        super();
    }

    public WampCallee(WampRole next) {
        super(next);
    }

    @Override
    public final boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg) {
        if (msg.isRegisterMessage()) {
            mRegs.put(receiver, msg);
            if (mProcMaps.get(receiver) == null) {
                mProcMaps.put(receiver, new ConcurrentHashMap<Integer, String>());
            }
            return true;
        }

        if (msg.isUnregisterMessage()) {
            mUnregs.put(receiver, msg);
            return true;
        }

        return false;
    }

    @Override
    public final boolean resolveRxMessageImpl(WampPeer transmitter, WampMessage msg,
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
        if (!mRegs.containsKey(transmitter)) {
            return false;
        }

        WampRegisterMessage request = mRegs.get(transmitter).asRegisterMessage();
        WampRegisteredMessage response = msg.asRegisteredMessage();

        if (request.getRequestId() != response.getRequestId()) {
            return false;
        }

        Map<Integer, String> procMap = mProcMaps.get(transmitter);
        if (procMap == null) {
            return false;
        }
        procMap.put(response.getRegistrationId(), request.getProcedure());

        mRegs.remove(transmitter);
        return true;
    }

    private boolean resolveUnregisteredMessage(WampPeer transmitter, WampMessage msg) {
        if (!mUnregs.containsKey(transmitter)) {
            return false;
        }

        WampUnregisterMessage request = mUnregs.get(transmitter).asUnregisterMessage();
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

        mUnregs.remove(transmitter);
        return true;
    }

    private boolean resolveInvocationMessage(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {

        WampInvocationMessage invocation = msg.asInvocationMessage();

        Map<Integer, String> procMap = mProcMaps.get(transmitter);
        if (procMap == null) {
            return false;
        }

        final int regId = invocation.getRegistrationId();
        if (!procMap.containsKey(regId)) {
            listener.onReply(transmitter, WampMessageFactory.createError(msg.getMessageType(), -1,
                    new JSONObject(), WampError.NO_SUCH_PROCEDURE));
            return true;
        }

        WampMessage reply = invocation(procMap.get(regId), msg);
        if (!reply.isYieldMessage() && !reply.isErrorMessage()) {
            return false;
        }
        listener.onReply(transmitter, reply);

        return true;
    }

    abstract protected WampMessage invocation(String procedure, WampMessage msg);
}
