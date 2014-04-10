
package com.sonycsl.wamp;

import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class WampDealer extends WampRouter {

    private Map<String, Integer> mProcedureRegistrationIdMap = new ConcurrentHashMap<String, Integer>();

    private Map<Integer, AccessInfo<WampRegisterMessage>> mRegistrationIdAccessInfoMap = new ConcurrentHashMap<Integer, AccessInfo<WampRegisterMessage>>();

    private Map<Integer, AccessInfo<WampCallMessage>> mInvocRequestIdAccessInfoMap = new ConcurrentHashMap<Integer, AccessInfo<WampCallMessage>>();

    private int mRegistrationId = 0;

    private int mInvocRequestId = 0;

    private static final String ILLEGAL_UNREGISTERER = "wamp.error.no_such_unregisterer";

    private static final String NO_SUCH_REGISTRATION = "wamp.error.no_such_registration";

    private static final String NO_SUCH_PROCEDURE = "wamp.error.no_such_procedure";

    public WampDealer() {
    }

    public WampDealer(WampRouter next) {
        super(next);
    }

    @Override
    protected boolean consumeRoleBroadcast(WampMessage msg) {
        return false;
    }

    @Override
    protected final boolean consumeRoleMessage(WampMessenger friend, WampMessage msg) {
        if (consumeMyRoleMessage(friend, msg)) {
            onConsumed(msg);
            return true;
        }

        return false;
    }

    private boolean consumeMyRoleMessage(WampMessenger friend, WampMessage msg) {
        if (msg.isRegisterMessage()) {
            register(friend, msg.asRegisterMessage());
            return true;
        }

        if (msg.isUnregisterMessage()) {
            unregister(friend, msg.asUnregisterMessage());
            return true;
        }

        if (msg.isCallMessage()) {
            call(friend, msg.asCallMessage());
            return true;
        }

        if (msg.isYieldMessage()) {
            yield(friend, msg.asYieldMessage());
            return true;
        }

        return false;
    }

    private void register(WampMessenger registerer, WampRegisterMessage message) {
        int requestId = message.getRequestId();
        String procedure = message.getProcedure();

        int registrationId = ++mRegistrationId;

        mProcedureRegistrationIdMap.put(procedure, registrationId);
        mRegistrationIdAccessInfoMap.put(registrationId, new AccessInfo<WampRegisterMessage>(
                registerer, message));

        registerer.send(WampMessageFactory.createRegistered(requestId, registrationId));
    }

    private void unregister(WampMessenger unregisterer, WampUnregisterMessage message) {
        int requestId = message.getRequestId();
        int registrationId = message.getRegistrationId();

        synchronized (mRegistrationIdAccessInfoMap) {
            AccessInfo<WampRegisterMessage> regInfo = mRegistrationIdAccessInfoMap.get(Integer
                    .valueOf(registrationId));

            if (regInfo == null) {
                unregisterer.send(WampMessageFactory.createError(WampMessageType.UNREGISTER,
                        requestId, new JSONObject(), NO_SUCH_REGISTRATION));
                return;
            }

            if (unregisterer != regInfo.getMessenger()) {
                unregisterer.send(WampMessageFactory.createError(WampMessageType.UNREGISTER,
                        requestId, new JSONObject(), ILLEGAL_UNREGISTERER));
                return;
            }

            mRegistrationIdAccessInfoMap.remove(Integer.valueOf(registrationId));
        }

        unregisterer.send(WampMessageFactory.createUnregistered(requestId));
    }

    private void call(WampMessenger caller, WampCallMessage message) {
        Integer registrationId = mProcedureRegistrationIdMap.get(message.getProcedure());

        if (registrationId == null) {
            caller.send(createWampCallErrorMessage(message, NO_SUCH_PROCEDURE));
            return;
        }

        AccessInfo<WampRegisterMessage> regInfo = mRegistrationIdAccessInfoMap.get(registrationId);
        if (regInfo == null) {
            throw new IllegalStateException("Access Info is null");
        }

        int invocRequestId = ++mInvocRequestId;

        mInvocRequestIdAccessInfoMap.put(invocRequestId, new AccessInfo<WampCallMessage>(caller,
                message));

        WampMessenger registererMessenger = regInfo.getMessenger();

        registererMessenger.send(createWampInvocationMessage(invocRequestId, registrationId,
                new JSONObject(), message));
    }

    private static WampMessage createWampCallErrorMessage(WampCallMessage msg, String errorUri) {
        if (msg.hasArguments() && msg.hasArgumentsKw()) {
            return WampMessageFactory.createError(WampMessageType.CALL, msg.getRequestId(),
                    new JSONObject(), errorUri, msg.getArguments(),
                    msg.getArgumentsKw());
        }

        if (msg.hasArguments()) {
            return WampMessageFactory.createError(WampMessageType.CALL, msg.getRequestId(),
                    new JSONObject(), errorUri, msg.getArguments());
        }

        return WampMessageFactory.createError(WampMessageType.CALL, msg.getRequestId(),
                new JSONObject(), errorUri);
    }

    private static WampMessage createWampInvocationMessage(int requestId, int registrationId,
            JSONObject details, WampCallMessage msg) {
        if (msg.hasArguments() && msg.hasArgumentsKw()) {
            return WampMessageFactory.createInvocation(requestId, registrationId, details,
                    msg.getArguments(), msg.getArgumentsKw());
        }

        if (msg.hasArguments()) {
            return WampMessageFactory.createInvocation(requestId, registrationId, details,
                    msg.getArguments());
        }

        return WampMessageFactory.createInvocation(requestId, registrationId, details);
    }

    private void yield(WampMessenger friend, WampYieldMessage message) {
        AccessInfo<WampCallMessage> callInfo = mInvocRequestIdAccessInfoMap.remove(Integer
                .valueOf(message.getRequestId()));

        WampMessenger caller = callInfo.getMessenger();
        caller.send(createWampResultMessage(callInfo.getReceivedMessage().getRequestId(), message));
    }

    private static WampMessage createWampResultMessage(int callRequestId, WampYieldMessage msg) {
        if (msg.hasArguments() && msg.hasArgumentsKw()) {
            return WampMessageFactory.createResult(callRequestId, new JSONObject(),
                    msg.getArguments(), msg.getArgumentsKw());
        }

        if (msg.hasArguments()) {
            return WampMessageFactory.createResult(callRequestId, new JSONObject(),
                    msg.getArguments());
        }

        return WampMessageFactory.createResult(callRequestId, new JSONObject());
    }

}
