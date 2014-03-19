
package com.sonycsl.wamp;

import org.json.JSONArray;
import org.json.JSONObject;

public class WampMessageFactory {

    public static WampMessage createHello(String realm, JSONObject details) {
        return new WampMessage.Builder(WampMessage.HELLO).addUri(realm).addDict(details).build();
    }

    public static WampMessage createWelcome(int session, JSONObject details) {
        return new WampMessage.Builder(WampMessage.WELCOME).addId(session).addDict(details).build();
    }

    public static WampMessage createAbort(JSONObject details, String reason) {
        return new WampMessage.Builder(WampMessage.ABORT).addDict(details).addUri(reason).build();
    }

    public static WampMessage createGoodbye(JSONObject details, String reason) {
        return new WampMessage.Builder(WampMessage.GOODBYE).addDict(details).addUri(reason).build();
    }

    public static WampMessage createError(int requestType, int requestId, JSONObject details,
            String error) {
        return new WampMessage.Builder(WampMessage.ERROR).addInteger(requestType).addId(requestId)
                .addDict(details).addUri(error).build();
    }

    public static WampMessage createError(int requestType, int requestId, JSONObject details,
            String error, JSONArray arguments) {
        return new WampMessage.Builder(WampMessage.ERROR).addInteger(requestType).addId(requestId)
                .addDict(details).addUri(error).addList(arguments).build();
    }

    public static WampMessage createError(int requestType, int requestId, JSONObject details,
            String error, JSONArray arguments, JSONObject argumentsKw) {
        return new WampMessage.Builder(WampMessage.ERROR).addInteger(requestType).addId(requestId)
                .addDict(details).addUri(error).addList(arguments).addDict(argumentsKw).build();
    }

    public static WampMessage createPublish(int requestId, JSONObject options, String topic) {
        return new WampMessage.Builder(WampMessage.PUBLISH).addId(requestId).addDict(options)
                .addUri(topic).build();
    }

    public static WampMessage createPublish(int requestId, JSONObject options, String topic,
            JSONArray arguments) {
        return new WampMessage.Builder(WampMessage.PUBLISH).addId(requestId).addDict(options)
                .addUri(topic).addList(arguments).build();
    }

    public static WampMessage createPublish(int requestId, JSONObject options, String topic,
            JSONArray arguments, JSONObject argumentsKw) {
        return new WampMessage.Builder(WampMessage.PUBLISH).addId(requestId).addDict(options)
                .addUri(topic).addList(arguments).addDict(argumentsKw).build();
    }

    public static WampMessage createPublished(int requestId, int publicationId) {
        return new WampMessage.Builder(WampMessage.PUBLISHED).addId(requestId)
                .addId(publicationId).build();
    }

    public static WampMessage createSubscribe(int requestId, JSONObject options, String topic) {
        return new WampMessage.Builder(WampMessage.SUBSCRIBE).addId(requestId).addDict(options)
                .addUri(topic).build();
    }

    public static WampMessage createSubscribed(int requestId, int subscriptionId) {
        return new WampMessage.Builder(WampMessage.SUBSCRIBED).addId(requestId)
                .addId(subscriptionId).build();
    }

    public static WampMessage createEvent(int subscriptionId, int publicationId, JSONObject details) {
        return new WampMessage.Builder(WampMessage.EVENT).addId(subscriptionId)
                .addId(publicationId).addDict(details).build();
    }

    public static WampMessage createEvent(int subscriptionId, int publicationId,
            JSONObject details, JSONArray arguments) {
        return new WampMessage.Builder(WampMessage.EVENT).addId(subscriptionId)
                .addId(publicationId).addDict(details).addList(arguments).build();
    }

    public static WampMessage createEvent(int subscriptionId, int publicationId,
            JSONObject details, JSONArray arguments, JSONObject argumentsKw) {
        return new WampMessage.Builder(WampMessage.EVENT).addId(subscriptionId)
                .addId(publicationId).addDict(details).addList(arguments).addDict(argumentsKw)
                .build();
    }

    public static WampMessage createCall(int requestId, JSONObject options, String procedure) {
        return new WampMessage.Builder(WampMessage.CALL).addId(requestId).addDict(options)
                .addUri(procedure).build();
    }

    public static WampMessage createCall(int requestId, JSONObject options, String procedure,
            JSONArray arguments) {
        return new WampMessage.Builder(WampMessage.CALL).addId(requestId).addDict(options)
                .addUri(procedure).addList(arguments).build();
    }

    public static WampMessage createCall(int requestId, JSONObject options, String procedure,
            JSONArray arguments, JSONObject argumentsKw) {
        return new WampMessage.Builder(WampMessage.CALL).addId(requestId).addDict(options)
                .addUri(procedure).addList(arguments).addDict(argumentsKw).build();
    }

    public static WampMessage createResult(int requestId, JSONObject details) {
        return new WampMessage.Builder(WampMessage.RESULT).addId(requestId).addDict(details)
                .build();
    }

    public static WampMessage createResult(int requestId, JSONObject details, JSONArray arguments) {
        return new WampMessage.Builder(WampMessage.RESULT).addId(requestId).addDict(details)
                .addList(arguments).build();
    }

    public static WampMessage createResult(int requestId, JSONObject details, JSONArray arguments,
            JSONObject argumentsKw) {
        return new WampMessage.Builder(WampMessage.RESULT).addId(requestId).addDict(details)
                .addList(arguments).addDict(argumentsKw).build();
    }

    public static WampMessage createRegister(int requestId, JSONObject options, String procedure) {
        return new WampMessage.Builder(WampMessage.REGISTER).addId(requestId).addDict(options)
                .addUri(procedure).build();
    }

    public static WampMessage createRegistered(int requestId, int registrationId) {
        return new WampMessage.Builder(WampMessage.REGISTERED).addId(requestId)
                .addId(registrationId).build();
    }

    public static WampMessage createUnregister(int requestId, int registrationid) {
        return new WampMessage.Builder(WampMessage.UNREGISTER).addId(requestId)
                .addId(registrationid).build();
    }

    public static WampMessage createUnregistered(int requestId) {
        return new WampMessage.Builder(WampMessage.UNREGISTERED).addId(requestId).build();
    }

    public static WampMessage createInvocation(int requestId, int registrationId, JSONObject details) {
        return new WampMessage.Builder(WampMessage.INTERRUPT).addId(requestId)
                .addId(registrationId).addDict(details).build();
    }

    public static WampMessage createInvocation(int requestId, int registrationId,
            JSONObject details, JSONArray arguments) {
        return new WampMessage.Builder(WampMessage.INTERRUPT).addId(requestId)
                .addId(registrationId).addDict(details).addList(arguments).build();
    }

    public static WampMessage createInvocation(int requestId, int registrationId,
            JSONObject details, JSONArray arguments, JSONObject argumentsKw) {
        return new WampMessage.Builder(WampMessage.INTERRUPT).addId(requestId)
                .addId(registrationId).addDict(details).addList(arguments).addDict(argumentsKw)
                .build();
    }

    public static WampMessage createYield(int requestId, JSONObject details) {
        return new WampMessage.Builder(WampMessage.YIELD).addId(requestId).addDict(details).build();
    }

    public static WampMessage createYield(int requestId, JSONObject details, JSONArray arguments) {
        return new WampMessage.Builder(WampMessage.YIELD).addId(requestId).addDict(details)
                .addList(arguments).build();
    }

    public static WampMessage createYield(int requestId, JSONObject details, JSONArray arguments,
            JSONObject argumentsKw) {
        return new WampMessage.Builder(WampMessage.YIELD).addId(requestId).addDict(details)
                .addList(arguments).addDict(argumentsKw).build();
    }
}
