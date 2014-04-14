
package com.sonycsl.wamp.message;

import com.sonycsl.wamp.message.impl.WampAbortMessageImpl;
import com.sonycsl.wamp.message.impl.WampCallMessageImpl;
import com.sonycsl.wamp.message.impl.WampErrorMessageImpl;
import com.sonycsl.wamp.message.impl.WampEventMessageImpl;
import com.sonycsl.wamp.message.impl.WampGoodbyeMessageImpl;
import com.sonycsl.wamp.message.impl.WampHelloMessageImpl;
import com.sonycsl.wamp.message.impl.WampInvocationMessageImpl;
import com.sonycsl.wamp.message.impl.WampPublishMessageImpl;
import com.sonycsl.wamp.message.impl.WampPublishedMessageImpl;
import com.sonycsl.wamp.message.impl.WampRegisterMessageImpl;
import com.sonycsl.wamp.message.impl.WampRegisteredMessageImpl;
import com.sonycsl.wamp.message.impl.WampResultMessageImpl;
import com.sonycsl.wamp.message.impl.WampSubscribeMessageImpl;
import com.sonycsl.wamp.message.impl.WampSubscribedMessageImpl;
import com.sonycsl.wamp.message.impl.WampUnregisterMessageImpl;
import com.sonycsl.wamp.message.impl.WampUnregisteredMessageImpl;
import com.sonycsl.wamp.message.impl.WampUnsubscribeMessageImpl;
import com.sonycsl.wamp.message.impl.WampUnsubscribedMessageImpl;
import com.sonycsl.wamp.message.impl.WampWelcomeMessageImpl;
import com.sonycsl.wamp.message.impl.WampYieldMessageImpl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WampMessageFactory {

    private static int getMessageType(JSONArray msg) {
        try {
            return msg.getInt(WampMessageType.MESSAGE_TYPE_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no messagetype");
        }
    }

    public static WampMessage create(JSONArray msg) {
        switch (getMessageType(msg)) {
            case WampMessageType.HELLO:
                return new WampHelloMessageImpl(msg);
            case WampMessageType.WELCOME:
                return new WampWelcomeMessageImpl(msg);
            case WampMessageType.ABORT:
                return new WampAbortMessageImpl(msg);
            case WampMessageType.GOODBYE:
                return new WampGoodbyeMessageImpl(msg);
            case WampMessageType.ERROR:
                return new WampErrorMessageImpl(msg);
            case WampMessageType.PUBLISH:
                return new WampPublishMessageImpl(msg);
            case WampMessageType.PUBLISHED:
                return new WampPublishedMessageImpl(msg);
            case WampMessageType.SUBSCRIBE:
                return new WampSubscribeMessageImpl(msg);
            case WampMessageType.SUBSCRIBED:
                return new WampSubscribedMessageImpl(msg);
            case WampMessageType.UNSUBSCRIBE:
                return new WampUnsubscribeMessageImpl(msg);
            case WampMessageType.UNSUBSCRIBED:
                return new WampUnsubscribedMessageImpl(msg);
            case WampMessageType.EVENT:
                return new WampEventMessageImpl(msg);
            case WampMessageType.CALL:
                return new WampCallMessageImpl(msg);
            case WampMessageType.RESULT:
                return new WampResultMessageImpl(msg);
            case WampMessageType.REGISTER:
                return new WampRegisterMessageImpl(msg);
            case WampMessageType.REGISTERED:
                return new WampRegisteredMessageImpl(msg);
            case WampMessageType.UNREGISTER:
                return new WampUnregisterMessageImpl(msg);
            case WampMessageType.UNREGISTERED:
                return new WampUnregisteredMessageImpl(msg);
            case WampMessageType.INVOCATION:
                return new WampInvocationMessageImpl(msg);
            case WampMessageType.YIELD:
                return new WampYieldMessageImpl(msg);
            default:
                return null;
        }
    }

    public static WampMessage createHello(String realm, JSONObject details) {
        return WampHelloMessageImpl.create(realm, details);
    }

    public static WampMessage createWelcome(int session, JSONObject details) {
        return WampWelcomeMessageImpl.create(session, details);
    }

    public static WampMessage createAbort(JSONObject details, String reason) {
        return WampAbortMessageImpl.create(details, reason);
    }

    public static WampMessage createGoodbye(JSONObject details, String reason) {
        return WampGoodbyeMessageImpl.create(details, reason);
    }

    public static WampMessage createError(int requestType, int requestId, JSONObject details,
            String error) {
        return WampErrorMessageImpl.create(requestType, requestId, details, error);
    }

    public static WampMessage createError(int requestType, int requestId, JSONObject details,
            String error, JSONArray arguments) {
        return WampErrorMessageImpl.create(requestType, requestId, details, error, arguments);
    }

    public static WampMessage createError(int requestType, int requestId, JSONObject details,
            String error, JSONArray arguments, JSONObject argumentsKw) {
        return WampErrorMessageImpl.create(requestType, requestId, details, error, arguments,
                argumentsKw);
    }

    public static WampMessage createPublish(int requestId, JSONObject options, String topic) {
        return WampPublishMessageImpl.create(requestId, options, topic);
    }

    public static WampMessage createPublish(int requestId, JSONObject options, String topic,
            JSONArray arguments) {
        return WampPublishMessageImpl.create(requestId, options, topic, arguments);
    }

    public static WampMessage createPublish(int requestId, JSONObject options, String topic,
            JSONArray arguments, JSONObject argumentsKw) {
        return WampPublishMessageImpl.create(requestId, options, topic, arguments, argumentsKw);
    }

    public static WampMessage createPublished(int requestId, int publicationId) {
        return WampPublishedMessageImpl.create(requestId, publicationId);
    }

    public static WampMessage createSubscribe(int requestId, JSONObject options, String topic) {
        return WampSubscribeMessageImpl.create(requestId, options, topic);
    }

    public static WampMessage createSubscribed(int requestId, int subscriptionId) {
        return WampSubscribedMessageImpl.create(requestId, subscriptionId);
    }

    public static WampMessage createUnsubscribe(int requestId, int subscriptionId) {
        return WampUnsubscribeMessageImpl.create(requestId, subscriptionId);
    }

    public static WampMessage createUnsubscribed(int requestId) {
        return WampUnsubscribedMessageImpl.create(requestId);
    }

    public static WampMessage createEvent(int subscriptionId, int publicationId,
            JSONObject details) {
        return WampEventMessageImpl.create(subscriptionId, publicationId, details);
    }

    public static WampMessage createEvent(int subscriptionId, int publicationId,
            JSONObject details, JSONArray arguments) {
        return WampEventMessageImpl.create(subscriptionId, publicationId, details, arguments);
    }

    public static WampMessage createEvent(int subscriptionId, int publicationId,
            JSONObject details, JSONArray arguments, JSONObject argumentsKw) {
        return WampEventMessageImpl.create(subscriptionId, publicationId, details, arguments,
                argumentsKw);
    }

    public static WampMessage createCall(int requestId, JSONObject options, String procedure) {
        return WampCallMessageImpl.create(requestId, options, procedure);
    }

    public static WampMessage createCall(int requestId, JSONObject options, String procedure,
            JSONArray arguments) {
        return WampCallMessageImpl.create(requestId, options, procedure, arguments);
    }

    public static WampMessage createCall(int requestId, JSONObject options, String procedure,
            JSONArray arguments, JSONObject argumentsKw) {
        return WampCallMessageImpl.create(requestId, options, procedure, arguments, argumentsKw);
    }

    public static WampMessage createResult(int requestId, JSONObject details) {
        return WampResultMessageImpl.create(requestId, details);
    }

    public static WampMessage createResult(int requestId, JSONObject details,
            JSONArray arguments) {
        return WampResultMessageImpl.create(requestId, details, arguments);
    }

    public static WampMessage createResult(int requestId, JSONObject details,
            JSONArray arguments, JSONObject argumentsKw) {
        return WampResultMessageImpl.create(requestId, details, arguments, argumentsKw);

    }

    public static WampMessage createRegister(int requestId, JSONObject options, String procedure) {
        return WampRegisterMessageImpl.create(requestId, options, procedure);

    }

    public static WampMessage createRegistered(int requestId, int registrationId) {
        return WampRegisteredMessageImpl.create(requestId, registrationId);
    }

    public static WampMessage createUnregister(int requestId, int registrationId) {
        return WampUnregisterMessageImpl.create(requestId, registrationId);
    }

    public static WampMessage createUnregistered(int requestId) {
        return WampUnregisteredMessageImpl.create(requestId);
    }

    public static WampMessage createInvocation(int requestId, int registrationId, JSONObject details) {
        return WampInvocationMessageImpl.create(requestId, registrationId, details);
    }

    public static WampMessage createInvocation(int requestId, int registrationId,
            JSONObject details, JSONArray arguments) {
        return WampInvocationMessageImpl.create(requestId, registrationId, details, arguments);
    }

    public static WampMessage createInvocation(int requestId, int registrationId,
            JSONObject details, JSONArray arguments, JSONObject argumentsKw) {
        return WampInvocationMessageImpl.create(requestId, registrationId, details, arguments,
                argumentsKw);
    }

    public static WampMessage createYield(int requestId, JSONObject options) {
        return WampYieldMessageImpl.create(requestId, options);
    }

    public static WampMessage createYield(int requestId, JSONObject options, JSONArray arguments) {
        return WampYieldMessageImpl.create(requestId, options, arguments);
    }

    public static WampMessage createYield(int requestId, JSONObject options, JSONArray arguments,
            JSONObject argumentsKw) {
        return WampYieldMessageImpl.create(requestId, options, arguments, argumentsKw);
    }
}
