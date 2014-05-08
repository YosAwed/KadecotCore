
package com.sonycsl.wamp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract public class WampCallee extends WampClient {

    private final Map<Integer, String> mRegisteringProcedures;
    private final Map<Integer, String> mRegisteredProcedures;

    public WampCallee() {
        mRegisteringProcedures = new ConcurrentHashMap<Integer, String>();
        mRegisteredProcedures = new ConcurrentHashMap<Integer, String>();
    }

    public WampCallee(WampClient next) {
        super(next);
        mRegisteringProcedures = new ConcurrentHashMap<Integer, String>();
        mRegisteredProcedures = new ConcurrentHashMap<Integer, String>();
    }

    @Override
    protected final void onBroadcast(WampMessage msg) {
        if (!msg.isRegisterMessage()) {
            return;
        }
        WampRegisterMessage reg = msg.asRegisterMessage();
        mRegisteringProcedures.put(Integer.valueOf(reg.getRequestId()), reg.getProcedure());
    }

    private void registered(WampRegisteredMessage msg) {
        final int requestId = msg.getRequestId();
        final String procedure = mRegisteringProcedures.get(requestId);
        mRegisteringProcedures.remove(requestId);
        mRegisteredProcedures.put(msg.getRegistrationId(), procedure);
    }

    private void unregistered(WampUnregisteredMessage msg) {
    }

    private void invocation(WampMessenger friend, WampInvocationMessage msg) {
        friend.send(onInvocation(mRegisteredProcedures.get(msg.getRegistrationId()), msg));
    }

    abstract protected WampMessage onInvocation(String procedure, WampInvocationMessage msg);

    private boolean consumeMyMessage(WampMessenger friend, WampMessage msg) {
        if (msg.isRegisteredMessage()) {
            registered(msg.asRegisteredMessage());
            return true;
        }

        if (msg.isUnregisteredMessage()) {
            unregistered(msg.asUnregisteredMessage());
            return true;
        }

        if (msg.isInvocationMessage()) {
            invocation(friend, msg.asInvocationMessage());
            return true;
        }

        if (msg.isInterruptMessage()) {
            /*
             * TODO future support
             */
            return false;
        }

        return false;
    }

    @Override
    protected boolean consumeRoleMessage(WampMessenger friend, WampMessage msg) {

        if (consumeMyMessage(friend, msg)) {
            onConsumed(msg);
            return true;
        }

        return false;
    }

}
