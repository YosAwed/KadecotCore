
package com.sonycsl.test.wamp.message;

import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.message.WampAbortMessage;
import com.sonycsl.wamp.message.WampCallMessage;
import com.sonycsl.wamp.message.WampErrorMessage;
import com.sonycsl.wamp.message.WampEventMessage;
import com.sonycsl.wamp.message.WampGoodbyeMessage;
import com.sonycsl.wamp.message.WampHelloMessage;
import com.sonycsl.wamp.message.WampInvocationMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.WampPublishMessage;
import com.sonycsl.wamp.message.WampPublishedMessage;
import com.sonycsl.wamp.message.WampRegisterMessage;
import com.sonycsl.wamp.message.WampRegisteredMessage;
import com.sonycsl.wamp.message.WampResultMessage;
import com.sonycsl.wamp.message.WampSubscribeMessage;
import com.sonycsl.wamp.message.WampSubscribedMessage;
import com.sonycsl.wamp.message.WampUnregisterMessage;
import com.sonycsl.wamp.message.WampUnregisteredMessage;
import com.sonycsl.wamp.message.WampUnsubscribeMessage;
import com.sonycsl.wamp.message.WampUnsubscribedMessage;
import com.sonycsl.wamp.message.WampWelcomeMessage;
import com.sonycsl.wamp.message.WampYieldMessage;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WampMessageFactoryTestCase extends TestCase {
    private static final String REALM = "realm";
    private static final String TOPIC = "topic.test";
    private static final String PROCEDURE = "procedure.test";

    public void testCtor() {
        assertNotNull(new WampMessageFactory());
    }

    public void testHelloCreate() {
        try {
            // create hello message
            JSONArray json = new JSONArray();
            json.put(WampMessageType.HELLO);
            json.put(REALM);
            JSONObject role = new JSONObject("{\"roles\":{\"caller\":{}}}");
            json.put(role);
            WampMessage msg = WampMessageFactory.create(json);

            assertTrue(msg.isHelloMessage());

            WampHelloMessage hello = msg.asHelloMessage();

            // check content
            assertTrue(hello.getRealm().equals(REALM));
            assertTrue(hello.getDetails() == role);
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testWelcomeCreate() {
        try {
            JSONArray json = new JSONArray();
            json.put(WampMessageType.WELCOME);
            int sessionId = 1;
            json.put(sessionId);
            JSONObject role = new JSONObject("{\"roles\":{\"dealer\":{}}}");
            json.put(role);

            WampMessage msg = WampMessageFactory.create(json);

            assertTrue(msg.isWelcomeMessage());

            WampWelcomeMessage welcome = msg.asWelcomeMessage();
            assertTrue(welcome.getSession() == sessionId);
            assertTrue(welcome.getDetails() == role);
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testAbortCreate() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.ABORT);
        JSONObject details = new JSONObject();
        json.put(details);
        String reason = WampError.NO_SUCH_REALM;
        json.put(reason);

        WampMessage msg = WampMessageFactory.create(json);

        assertTrue(msg.isAbortMessage());

        WampAbortMessage abort = msg.asAbortMessage();
        assertTrue(abort.getDetails() == details);
        assertTrue(abort.getReason().equals(reason));
    }

    public void testGoodbyeCreate() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.GOODBYE);
        JSONObject details = new JSONObject();
        json.put(details);
        String reason = WampError.GOODBYE_AND_OUT;
        json.put(reason);

        WampMessage msg = WampMessageFactory.create(json);

        assertTrue(msg.isGoodbyeMessage());

        WampGoodbyeMessage goodbye = msg.asGoodbyeMessage();
        assertTrue(goodbye.getDetails() == details);
        assertTrue(goodbye.getReason().equals(reason));
    }

    public void testErrorCreate() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.ERROR);
        int requestType = WampMessageType.CALL;
        json.put(requestType);
        int requestId = 1;
        json.put(requestId);
        JSONObject details = new JSONObject();
        json.put(details);
        String error = WampError.NO_SUCH_PROCEDURE;
        json.put(error);

        WampMessage msg = WampMessageFactory.create(json);

        assertTrue(msg.isErrorMessage());

        WampErrorMessage errorMsg = msg.asErrorMessage();
        assertTrue(errorMsg.getRequestType() == requestType);
        assertTrue(errorMsg.getRequestId() == requestId);
        assertTrue(errorMsg.getDetails() == details);
        assertTrue(errorMsg.getUri().equals(error));
    }

    public void testPublishCreate() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.PUBLISH);
        int requestId = 1;
        json.put(requestId);
        JSONObject options = new JSONObject();
        json.put(options);
        json.put(TOPIC);

        WampMessage msg = WampMessageFactory.create(json);

        assertTrue(msg.isPublishMessage());

        WampPublishMessage publish = msg.asPublishMessage();
        assertTrue(publish.getRequestId() == requestId);
        assertTrue(publish.getOptions() == options);
        assertTrue(publish.getTopic().equals(TOPIC));
    }

    public void testPublishedCreate() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.PUBLISHED);
        int requestId = 1;
        json.put(requestId);
        int publicationId = 2;
        json.put(publicationId);

        WampMessage msg = WampMessageFactory.create(json);

        assertTrue(msg.isPublishedMessage());

        WampPublishedMessage published = msg.asPublishedMessage();
        assertTrue(published.getRequestId() == requestId);
        assertTrue(published.getPublicationId() == publicationId);
    }

    public void testSubscribeCreate() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.SUBSCRIBE);
        int requestId = 1;
        json.put(requestId);
        JSONObject options = new JSONObject();
        json.put(options);
        json.put(TOPIC);

        WampMessage msg = WampMessageFactory.create(json);

        assertTrue(msg.isSubscribeMessage());

        WampSubscribeMessage subscribe = msg.asSubscribeMessage();
        assertTrue(subscribe.getRequestId() == requestId);
        assertTrue(subscribe.getOptions() == options);
        assertTrue(subscribe.getTopic().equals(TOPIC));
    }

    public void testSubscribedCreate() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.SUBSCRIBED);
        int requestId = 1;
        json.put(requestId);
        int subscriptionId = 2;
        json.put(subscriptionId);

        WampMessage msg = WampMessageFactory.create(json);

        assertTrue(msg.isSubscribedMessage());

        WampSubscribedMessage subscribed = msg.asSubscribedMessage();
        assertTrue(subscribed.getRequestId() == requestId);
        assertTrue(subscribed.getSubscriptionId() == subscriptionId);
    }

    public void testUnsubscribeCreate() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.UNSUBSCRIBE);
        int requestId = 1;
        json.put(requestId);
        int subscriptionId = 2;
        json.put(subscriptionId);

        WampMessage msg = WampMessageFactory.create(json);

        assertTrue(msg.isUnsubscribeMessage());

        WampUnsubscribeMessage unsubscribe = msg.asUnsubscribeMessage();
        assertTrue(unsubscribe.getRequestId() == requestId);
        assertTrue(unsubscribe.getSubscriptionId() == subscriptionId);
    }

    public void testUnsubscribedCreate() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.UNSUBSCRIBED);
        int requestId = 1;
        json.put(requestId);

        WampMessage msg = WampMessageFactory.create(json);

        assertTrue(msg.isUnsubscribedMessage());

        WampUnsubscribedMessage unsubscribed = msg.asUnsubscribedMessage();
        assertTrue(unsubscribed.getRequestId() == requestId);
    }

    public void testEventCreate() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.EVENT);
        int subscriptionId = 1;
        json.put(subscriptionId);
        int publicationId = 2;
        json.put(publicationId);
        JSONObject details = new JSONObject();
        json.put(details);

        WampMessage msg = WampMessageFactory.create(json);

        assertTrue(msg.isEventMessage());

        WampEventMessage event = msg.asEventMessage();
        assertTrue(event.getSubscriptionId() == subscriptionId);
        assertTrue(event.getPublicationId() == publicationId);
        assertTrue(event.getDetails() == details);
    }

    public void testCallCreate() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.CALL);
        int requestId = 1;
        json.put(requestId);
        JSONObject options = new JSONObject();
        json.put(options);
        json.put(PROCEDURE);

        WampMessage msg = WampMessageFactory.create(json);

        assertTrue(msg.isCallMessage());

        WampCallMessage call = msg.asCallMessage();
        assertTrue(call.getRequestId() == requestId);
        assertTrue(call.getOptions() == options);
        assertTrue(call.getProcedure().equals(PROCEDURE));
    }

    public void testResultCreate() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.RESULT);
        int requestId = 1;
        json.put(requestId);
        JSONObject details = new JSONObject();
        json.put(details);

        WampMessage msg = WampMessageFactory.create(json);

        assertTrue(msg.isResultMessage());

        WampResultMessage result = msg.asResultMessage();
        assertTrue(result.getRequestId() == requestId);
        assertTrue(result.getDetails() == details);
    }

    public void testRegisterCreate() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.REGISTER);
        int requestId = 1;
        json.put(requestId);
        JSONObject options = new JSONObject();
        json.put(options);
        json.put(PROCEDURE);

        WampMessage msg = WampMessageFactory.create(json);

        assertTrue(msg.isRegisterMessage());

        WampRegisterMessage register = msg.asRegisterMessage();
        assertTrue(register.getRequestId() == requestId);
        assertTrue(register.getOptions() == options);
        assertTrue(register.getProcedure().equals(PROCEDURE));
    }

    public void testRegisteredCreate() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.REGISTERED);
        int requestId = 1;
        json.put(requestId);
        int registrationId = 2;
        json.put(registrationId);

        WampMessage msg = WampMessageFactory.create(json);

        assertTrue(msg.isRegisteredMessage());

        WampRegisteredMessage registered = msg.asRegisteredMessage();
        assertTrue(registered.getRequestId() == requestId);
        assertTrue(registered.getRegistrationId() == registrationId);
    }

    public void testUnregisterCreate() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.UNREGISTER);
        int requestId = 1;
        json.put(requestId);
        int registrationId = 2;
        json.put(registrationId);

        WampMessage msg = WampMessageFactory.create(json);

        assertTrue(msg.isUnregisterMessage());

        WampUnregisterMessage unregister = msg.asUnregisterMessage();
        assertTrue(unregister.getRequestId() == requestId);
        assertTrue(unregister.getRegistrationId() == registrationId);
    }

    public void testUnregsiteredCreate() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.UNREGISTERED);
        int requestId = 1;
        json.put(requestId);

        WampMessage msg = WampMessageFactory.create(json);

        assertTrue(msg.isUnregisteredMessage());

        WampUnregisteredMessage unregistered = msg.asUnregisteredMessage();
        assertTrue(unregistered.getRequestId() == requestId);
    }

    public void testInvocationCreate() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.INVOCATION);
        int requestId = 1;
        json.put(requestId);
        int registrationId = 2;
        json.put(registrationId);
        JSONObject details = new JSONObject();
        json.put(details);

        WampMessage msg = WampMessageFactory.create(json);

        assertTrue(msg.isInvocationMessage());

        WampInvocationMessage invocation = msg.asInvocationMessage();
        assertTrue(invocation.getRequestId() == requestId);
        assertTrue(invocation.getRegistrationId() == registrationId);
        assertTrue(invocation.getDetails() == details);
    }

    public void testYieldCreate() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.YIELD);
        int requestId = 1;
        json.put(requestId);
        JSONObject options = new JSONObject();
        json.put(options);

        WampMessage msg = WampMessageFactory.create(json);

        assertTrue(msg.isYieldMessage());

        WampYieldMessage yield = msg.asYieldMessage();
        assertTrue(yield.getRequestId() == requestId);
        assertTrue(yield.getOptions() == options);
    }

    public void testCreateAbnormal() {
        // no message type
        try {
            JSONArray json = new JSONArray();
            WampMessageFactory.create(json);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCreateHello() {
        try {
            JSONObject role = new JSONObject("{\"roles\":{\"caller\":{}}}");
            WampMessage msg = WampMessageFactory.createHello(REALM, role);

            assertTrue(msg.isHelloMessage());

            WampHelloMessage hello = msg.asHelloMessage();
            assertTrue(hello.getRealm().equals(REALM));
            assertTrue(hello.getDetails() == role);
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testCreateHelloAbnormal() {
        // details null check
        try {
            WampMessageFactory.createHello(REALM, null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // realm null check
        try {
            JSONObject role = new JSONObject("{\"roles\":{\"caller\":{}}}");
            WampMessageFactory.createHello(null, role);
            fail();
        } catch (IllegalArgumentException e) {
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testCreateWelcome() {
        try {
            JSONObject role = new JSONObject("{\"roles\":{\"dealer\":{}}}");
            int sessionId = 1;
            WampMessage msg = WampMessageFactory.createWelcome(sessionId, role);

            assertTrue(msg.isWelcomeMessage());

            WampWelcomeMessage welcome = msg.asWelcomeMessage();
            assertTrue(welcome.getSession() == sessionId);
            assertTrue(welcome.getDetails() == role);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void testCreateWelcomeAbnormal() {
        // details null check
        try {
            int sessionId = 1;
            WampMessageFactory.createWelcome(sessionId, null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCreateAbort() {
        JSONObject details = new JSONObject();
        WampMessage msg = WampMessageFactory.createAbort(details, WampError.NO_SUCH_REALM);

        assertTrue(msg.isAbortMessage());

        WampAbortMessage abort = msg.asAbortMessage();
        assertTrue(abort.getDetails() == details);
        assertTrue(abort.getReason().equals(WampError.NO_SUCH_REALM));
    }

    public void testCreateAbortAbnormal() {
        // details null check
        try {
            WampMessageFactory.createAbort(null, WampError.NO_SUCH_REALM);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // reason null check
        try {
            WampMessageFactory.createAbort(new JSONObject(), null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCreateGoodbye() {
        JSONObject details = new JSONObject();
        WampMessage msg = WampMessageFactory.createGoodbye(details, WampError.GOODBYE_AND_OUT);

        assertTrue(msg.isGoodbyeMessage());

        WampGoodbyeMessage goodbye = msg.asGoodbyeMessage();
        assertTrue(goodbye.getDetails() == details);
        assertTrue(goodbye.getReason().equals(WampError.GOODBYE_AND_OUT));
    }

    public void testCreateGoodbyeAbnormal() {
        // details null check
        try {
            WampMessageFactory.createGoodbye(null, WampError.GOODBYE_AND_OUT);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // reason null check
        try {
            WampMessageFactory.createAbort(new JSONObject(), null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCreateError() {
        int requestType = WampMessageType.CALL;
        int requestId = 1;
        JSONObject details = new JSONObject();
        String error = WampError.NO_SUCH_PROCEDURE;
        WampMessage msg;
        WampErrorMessage errorMsg;

        // no arguments and argumentsKw
        msg = WampMessageFactory.createError(requestType, requestId, details, error);

        assertTrue(msg.isErrorMessage());

        errorMsg = msg.asErrorMessage();
        assertTrue(errorMsg.getRequestType() == requestType);
        assertTrue(errorMsg.getRequestId() == requestId);
        assertTrue(errorMsg.getDetails() == details);
        assertTrue(errorMsg.getUri().equals(error));

        // arguments
        JSONArray arguments = new JSONArray();
        msg = WampMessageFactory.createError(requestType, requestId, details, error, arguments);

        assertTrue(msg.isErrorMessage());

        errorMsg = msg.asErrorMessage();
        assertTrue(errorMsg.getRequestType() == requestType);
        assertTrue(errorMsg.getRequestId() == requestId);
        assertTrue(errorMsg.getDetails() == details);
        assertTrue(errorMsg.getUri().equals(error));
        assertTrue(errorMsg.getArguments() == arguments);

        // argumentsKw
        JSONObject argumentsKw = new JSONObject();
        msg = WampMessageFactory.createError(requestType, requestId, details, error, arguments,
                argumentsKw);

        assertTrue(msg.isErrorMessage());

        errorMsg = msg.asErrorMessage();
        assertTrue(errorMsg.getRequestType() == requestType);
        assertTrue(errorMsg.getRequestId() == requestId);
        assertTrue(errorMsg.getDetails() == details);
        assertTrue(errorMsg.getUri().equals(error));
        assertTrue(errorMsg.getArguments() == arguments);
        assertTrue(errorMsg.getArgumentsKw() == argumentsKw);
    }

    public void testCreateErrorAbnormal() {
        int requestType = WampMessageType.CALL;
        String error = WampError.NO_SUCH_PROCEDURE;
        JSONObject options = new JSONObject();

        // options null
        try {
            WampMessageFactory.createError(requestType, 1, null, error);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // error null
        try {
            WampMessageFactory.createError(requestType, 1, options, null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // arguments null
        try {
            WampMessageFactory.createError(requestType, 1, options, error, null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // argumentsKw null
        try {
            WampMessageFactory.createError(requestType, 1, options, error,
                    new JSONArray(), null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCreateSubscribe() {
        int requestId = 1;
        JSONObject options = new JSONObject();
        WampMessage msg = WampMessageFactory.createSubscribe(requestId, options, TOPIC);

        assertTrue(msg.isSubscribeMessage());

        WampSubscribeMessage subscribe = msg.asSubscribeMessage();
        assertTrue(subscribe.getRequestId() == requestId);
        assertTrue(subscribe.getOptions() == options);
        assertTrue(subscribe.getTopic().equals(TOPIC));
    }

    public void testCreateSubscribeAbnormal() {
        // options null check
        try {
            WampMessageFactory.createSubscribe(1, null, TOPIC);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // topic null check
        try {
            WampMessageFactory.createSubscribe(1, new JSONObject(), null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCreateSubscribed() {
        int requestId = 1;
        int subscriptionId = 2;
        WampMessage msg = WampMessageFactory.createSubscribed(requestId, subscriptionId);

        assertTrue(msg.isSubscribedMessage());

        WampSubscribedMessage subscribed = msg.asSubscribedMessage();
        assertTrue(subscribed.getRequestId() == requestId);
        assertTrue(subscribed.getSubscriptionId() == subscriptionId);
    }

    public void testCreateUnsubscribe() {
        int requestId = 1;
        int subscriptionId = 2;
        WampMessage msg = WampMessageFactory.createUnsubscribe(requestId, subscriptionId);

        assertTrue(msg.isUnsubscribeMessage());

        WampUnsubscribeMessage unsubscribe = msg.asUnsubscribeMessage();
        assertTrue(unsubscribe.getRequestId() == requestId);
        assertTrue(unsubscribe.getSubscriptionId() == subscriptionId);
    }

    public void testCreateUnsubscribed() {
        int requestId = 1;
        WampMessage msg = WampMessageFactory.createUnsubscribed(requestId);

        assertTrue(msg.isUnsubscribedMessage());

        WampUnsubscribedMessage unsubscribed = msg.asUnsubscribedMessage();
        assertTrue(unsubscribed.getRequestId() == requestId);
    }

    public void testCreatePublish() {
        // has no arguments and argumentsKw
        int requestId = 1;
        JSONObject options = new JSONObject();
        WampMessage msg = WampMessageFactory.createPublish(requestId, options, TOPIC);

        assertTrue(msg.isPublishMessage());

        WampPublishMessage publish = msg.asPublishMessage();
        assertTrue(publish.getRequestId() == requestId);
        assertTrue(publish.getTopic().equals(TOPIC));

        // has arguments
        JSONArray arguments = new JSONArray();
        msg = WampMessageFactory.createPublish(requestId, options, TOPIC, arguments);

        assertTrue(msg.isPublishMessage());

        publish = msg.asPublishMessage();
        assertTrue(publish.getRequestId() == requestId);
        assertTrue(publish.getTopic().equals(TOPIC));
        assertTrue(publish.getArguments() == arguments);

        // has argumentsKw
        JSONObject argumentsKw = new JSONObject();
        msg = WampMessageFactory.createPublish(requestId, options, TOPIC, arguments, argumentsKw);

        assertTrue(msg.isPublishMessage());

        publish = msg.asPublishMessage();
        assertTrue(publish.getRequestId() == requestId);
        assertTrue(publish.getTopic().equals(TOPIC));
        assertTrue(publish.getArguments() == arguments);
        assertTrue(publish.getArgumentsKw() == argumentsKw);
    }

    public void testCreatePublishAbnormal() {
        JSONObject options = new JSONObject();

        // options null
        try {
            WampMessageFactory.createPublish(1, null, TOPIC);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // topic null
        try {
            WampMessageFactory.createPublish(1, options, null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // arguments null
        try {
            WampMessageFactory.createPublish(1, options, TOPIC, null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // argumentsKw null
        try {
            WampMessageFactory.createPublish(1, options, TOPIC, new JSONArray(), null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCreatePublished() {
        int requestId = 1;
        int publicationId = 2;
        WampMessage msg = WampMessageFactory.createPublished(requestId, publicationId);

        assertTrue(msg.isPublishedMessage());

        WampPublishedMessage published = msg.asPublishedMessage();

        assertTrue(published.getRequestId() == requestId);
        assertTrue(published.getPublicationId() == publicationId);
    }

    public void testCreateEvent() {
        int subscriptionId = 1;
        int publicationId = 2;
        JSONObject details = new JSONObject();
        WampMessage msg;
        WampEventMessage event;

        // no arguments and argumentsKw
        msg = WampMessageFactory.createEvent(subscriptionId, publicationId, details);

        assertTrue(msg.isEventMessage());

        event = msg.asEventMessage();
        assertTrue(event.getSubscriptionId() == subscriptionId);
        assertTrue(event.getPublicationId() == publicationId);
        assertTrue(event.getDetails() == details);

        // arguments
        JSONArray arguments = new JSONArray();
        msg = WampMessageFactory.createEvent(subscriptionId, publicationId, details, arguments);

        assertTrue(msg.isEventMessage());

        event = msg.asEventMessage();
        assertTrue(event.getSubscriptionId() == subscriptionId);
        assertTrue(event.getPublicationId() == publicationId);
        assertTrue(event.getDetails() == details);
        assertTrue(event.getArguments() == arguments);

        // argumentsKw
        JSONObject argumentsKw = new JSONObject();
        msg = WampMessageFactory.createEvent(subscriptionId, publicationId, details, arguments,
                argumentsKw);

        assertTrue(msg.isEventMessage());

        event = msg.asEventMessage();
        assertTrue(event.getSubscriptionId() == subscriptionId);
        assertTrue(event.getPublicationId() == publicationId);
        assertTrue(event.getDetails() == details);
        assertTrue(event.getArguments() == arguments);
        assertTrue(event.getArgumentsKw() == argumentsKw);
    }

    public void createEventAbnormal() {
        JSONObject details = new JSONObject();

        // details null
        try {
            WampMessageFactory.createEvent(1, 2, null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // arguments null
        try {
            WampMessageFactory.createEvent(1, 2, details, null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // arguments null
        try {
            WampMessageFactory.createEvent(1, 2, details, new JSONArray(), null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCreateRegister() {
        int requestId = 1;
        JSONObject options = new JSONObject();
        WampMessage msg = WampMessageFactory.createRegister(requestId, options, PROCEDURE);

        assertTrue(msg.isRegisterMessage());

        WampRegisterMessage register = msg.asRegisterMessage();
        assertTrue(register.getRequestId() == requestId);
        assertTrue(register.getOptions() == options);
        assertTrue(register.getProcedure().equals(PROCEDURE));
    }

    public void testCreateRegisterAbnormal() {
        // options null
        try {
            WampMessageFactory.createRegister(1, null, PROCEDURE);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // procedure null
        try {
            WampMessageFactory.createRegister(1, new JSONObject(), null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCreateRegistered() {
        int requestId = 1;
        int registrationId = 2;
        WampMessage msg = WampMessageFactory.createRegistered(requestId, registrationId);

        assertTrue(msg.isRegisteredMessage());

        WampRegisteredMessage registered = msg.asRegisteredMessage();
        assertTrue(registered.getRequestId() == requestId);
        assertTrue(registered.getRegistrationId() == registrationId);
    }

    public void testCreateUnregister() {
        int requestId = 1;
        int registrationId = 2;
        WampMessage msg = WampMessageFactory.createUnregister(requestId, registrationId);

        assertTrue(msg.isUnregisterMessage());

        WampUnregisterMessage unregister = msg.asUnregisterMessage();
        assertTrue(unregister.getRequestId() == requestId);
        assertTrue(unregister.getRegistrationId() == registrationId);
    }

    public void testCreateUnregistered() {
        int requestId = 1;
        WampMessage msg = WampMessageFactory.createUnregistered(requestId);

        assertTrue(msg.isUnregisteredMessage());

        WampUnregisteredMessage unregistered = msg.asUnregisteredMessage();
        assertTrue(unregistered.getRequestId() == requestId);
    }

    public void testCreateInvocation() {
        int requestId = 1;
        int registrationId = 2;
        JSONObject details = new JSONObject();

        // no arguments and argumentsKw
        WampMessage msg = WampMessageFactory.createInvocation(requestId, registrationId, details);

        assertTrue(msg.isInvocationMessage());
        WampInvocationMessage invocation = msg.asInvocationMessage();

        assertTrue(invocation.getRequestId() == requestId);
        assertTrue(invocation.getRegistrationId() == registrationId);
        assertTrue(invocation.getDetails() == details);

        // arguments
        JSONArray arguments = new JSONArray();
        msg = WampMessageFactory.createInvocation(requestId, registrationId, details, arguments);

        assertTrue(msg.isInvocationMessage());
        invocation = msg.asInvocationMessage();

        assertTrue(invocation.getRequestId() == requestId);
        assertTrue(invocation.getRegistrationId() == registrationId);
        assertTrue(invocation.getDetails() == details);
        assertTrue(invocation.getArguments() == arguments);

        // argumentsKw
        JSONObject argumentsKw = new JSONObject();
        msg = WampMessageFactory.createInvocation(requestId, registrationId, details, arguments,
                argumentsKw);

        assertTrue(msg.isInvocationMessage());
        invocation = msg.asInvocationMessage();

        assertTrue(invocation.getRequestId() == requestId);
        assertTrue(invocation.getRegistrationId() == registrationId);
        assertTrue(invocation.getDetails() == details);
        assertTrue(invocation.getArguments() == arguments);
        assertTrue(invocation.getArgumentsKw() == argumentsKw);
    }

    public void testCreateInvocationAbnormal() {
        // details null
        try {
            WampMessageFactory.createInvocation(1, 1, null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // arguments null
        try {
            WampMessageFactory.createInvocation(1, 1, new JSONObject(), null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // argumentsKw null
        try {
            WampMessageFactory.createInvocation(1, 1, new JSONObject(),
                    new JSONArray(), null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCreateYield() {
        int requestId = 1;
        JSONObject options = new JSONObject();

        // no arguments and argumentsKw
        WampMessage msg = WampMessageFactory.createYield(requestId, options);

        assertTrue(msg.isYieldMessage());

        WampYieldMessage yield = msg.asYieldMessage();
        assertTrue(yield.getRequestId() == requestId);
        assertTrue(yield.getOptions() == options);

        // arguments
        JSONArray arguments = new JSONArray();
        msg = WampMessageFactory.createYield(requestId, options, arguments);

        assertTrue(msg.isYieldMessage());

        yield = msg.asYieldMessage();
        assertTrue(yield.getRequestId() == requestId);
        assertTrue(yield.getOptions() == options);
        assertTrue(yield.getArguments() == arguments);

        // argumentsKw
        JSONObject argumentsKw = new JSONObject();
        msg = WampMessageFactory.createYield(requestId, options, arguments, argumentsKw);

        assertTrue(msg.isYieldMessage());

        yield = msg.asYieldMessage();
        assertTrue(yield.getRequestId() == requestId);
        assertTrue(yield.getOptions() == options);
        assertTrue(yield.getArguments() == arguments);
        assertTrue(yield.getArgumentsKw() == argumentsKw);
    }

    public void testCreateYieldAbnormal() {
        // options null
        try {
            WampMessageFactory.createYield(1, null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // arguments null
        try {
            WampMessageFactory.createYield(1, new JSONObject(), null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        // argumentsKw null
        try {
            WampMessageFactory.createYield(1, new JSONObject(), new JSONArray(), null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}
