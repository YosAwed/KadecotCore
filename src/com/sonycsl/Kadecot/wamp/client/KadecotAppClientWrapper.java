/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp.client;

import android.util.Log;

import com.sonycsl.Kadecot.wamp.client.KadecotAppClient.MessageListener;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampErrorMessage;
import com.sonycsl.wamp.message.WampEventMessage;
import com.sonycsl.wamp.message.WampGoodbyeMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.WampResultMessage;
import com.sonycsl.wamp.message.WampSubscribedMessage;
import com.sonycsl.wamp.message.WampUnsubscribedMessage;
import com.sonycsl.wamp.message.WampWelcomeMessage;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KadecotAppClientWrapper {

    private static final String TAG = KadecotAppClientWrapper.class.getSimpleName();

    private KadecotAppClient mAppClient;

    private Map<Integer, WampRequestListener> reqIdListenerMap = new ConcurrentHashMap<Integer, WampRequestListener>();

    private Map<Integer, WampSubscribeListener> subIdSubscriberMap = new ConcurrentHashMap<Integer, WampSubscribeListener>();

    private Map<Integer, Integer> reqIdSubIdMap = new ConcurrentHashMap<Integer, Integer>();

    private WampWelcomeListener mHelloListener;
    private WampGoodbyeListener mGoodbyeListener;

    public interface WampRequestListener {
    }

    public interface WampWelcomeListener {
        public void onWelcome(int session, JSONObject details);
    }

    public interface WampGoodbyeListener {
        public void onGoodbye(JSONObject details, String reason);
    }

    public interface WampCallListener extends WampRequestListener {
        public void onResult(JSONObject details, JSONObject argumentsKw);

        public void onError(JSONObject details, String error);
    }

    public interface WampSubscribeListener extends WampRequestListener {
        public void onEvent(JSONObject details, JSONObject argumentsKw);

        public void onSubscribed(int subscriptionId);

        public void onError(JSONObject details, String error);
    }

    public interface WampUnsubscribeListener extends WampRequestListener {
        public void onUnsubscribed();

        public void onError(JSONObject details, String error);
    }

    public KadecotAppClientWrapper() {
        mAppClient = new KadecotAppClient();
        mAppClient.setOnMessageListener(new MessageListener() {

            @Override
            public void onMessage(WampMessage msg) {
                switch (msg.getMessageType()) {
                    case WampMessageType.WELCOME:
                        respondWelcome(msg.asWelcomeMessage());
                        break;
                    case WampMessageType.GOODBYE:
                        respondGoodbye(msg.asGoodbyeMessage());
                        break;
                    case WampMessageType.ERROR:
                        respondError(msg.asErrorMessage());
                        break;
                    case WampMessageType.RESULT:
                        respondResult(msg.asResultMessage());
                        break;
                    case WampMessageType.EVENT:
                        respondEvent(msg.asEventMessage());
                        break;
                    case WampMessageType.SUBSCRIBED:
                        respondSubscribed(msg.asSubscribedMessage());
                        break;
                    case WampMessageType.UNSUBSCRIBED:
                        respondUnsubscribed(msg.asUnsubscribedMessage());
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void respondWelcome(WampWelcomeMessage msg) {
        if (mHelloListener == null) {
            return;
        }

        mHelloListener.onWelcome(msg.getSession(), msg.getDetails());
    }

    private void respondGoodbye(WampGoodbyeMessage msg) {
        if (mGoodbyeListener == null) {
            return;
        }

        mGoodbyeListener.onGoodbye(msg.getDetails(), msg.getReason());
    }

    private void respondError(WampErrorMessage msg) {
        WampRequestListener callback = reqIdListenerMap.remove(msg.getRequestId());
        if (callback == null) {
            Log.e(TAG, "Unknown Response. msg=" + msg);
            return;
        }

        switch (msg.getRequestType()) {
            case WampMessageType.CALL:
                WampCallListener callListener = (WampCallListener) callback;
                callListener.onError(msg.getDetails(), msg.getUri());
                break;
            case WampMessageType.SUBSCRIBE:
                WampSubscribeListener subscribeListener = (WampSubscribeListener) callback;
                subscribeListener.onError(msg.getDetails(), msg.getUri());
                break;
            case WampMessageType.UNSUBSCRIBE:
                WampUnsubscribeListener unsubscribeListener = (WampUnsubscribeListener) callback;
                unsubscribeListener.onError(msg.getDetails(), msg.getUri());
                break;
        }
    }

    private void respondResult(WampResultMessage msg) {
        WampCallListener callback = (WampCallListener) reqIdListenerMap.remove(msg.getRequestId());
        if (callback == null) {
            Log.e(TAG, "Unknown Result message: " + msg);
            return;
        }

        callback.onResult(msg.getDetails(), msg.getArgumentsKw());
    }

    private void respondEvent(WampEventMessage msg) {
        WampSubscribeListener callback = (WampSubscribeListener) subIdSubscriberMap.get(msg
                .getSubscriptionId());
        if (callback == null) {
            Log.e(TAG, "Unknown Event message: " + msg);
            return;
        }

        callback.onEvent(msg.getDetails(), msg.getArgumentsKw());
    }

    private void respondSubscribed(WampSubscribedMessage msg) {
        WampSubscribeListener callback = (WampSubscribeListener) reqIdListenerMap.remove(msg
                .getRequestId());
        if (callback == null) {
            Log.e(TAG, "Unknown Subscribed message: " + msg);
            return;
        }

        subIdSubscriberMap.put(msg.getSubscriptionId(), callback);

        callback.onSubscribed(msg.getSubscriptionId());
    }

    private void respondUnsubscribed(WampUnsubscribedMessage msg) {
        WampUnsubscribeListener callback = (WampUnsubscribeListener) reqIdListenerMap.get(msg
                .getRequestId());
        if (callback == null) {
            Log.e(TAG, "Unknown Unsubscribed message: " + msg);
            return;
        }
        int subscriptionId = reqIdSubIdMap.remove(msg.getRequestId());
        if (subIdSubscriberMap.remove(subscriptionId) == null) {
            Log.e(TAG, "Already unsubscribed: " + msg);
            return;
        }

        callback.onUnsubscribed();
    }

    public void connect(WampPeer peer) {
        mAppClient.connect(peer);
    }

    public void hello(String realm, WampWelcomeListener listener) {
        mHelloListener = listener;
        mAppClient.transmit(WampMessageFactory.createHello(realm, new JSONObject()));
    }

    public void goodbye(String reason, WampGoodbyeListener listener) {
        mGoodbyeListener = listener;
        mAppClient.transmit(WampMessageFactory.createGoodbye(new JSONObject(), reason));
    }

    public void call(String procedure, JSONObject options, JSONObject paramsKw,
            WampCallListener listener) {

        int reqId = WampRequestIdGenerator.getId();
        reqIdListenerMap.put(reqId, listener);

        mAppClient.transmit(WampMessageFactory.createCall(reqId, options, procedure,
                new JSONArray(), paramsKw));
    }

    public void subscribe(String topic, JSONObject options, WampSubscribeListener listener) {
        int reqId = WampRequestIdGenerator.getId();
        reqIdListenerMap.put(reqId, listener);

        mAppClient.transmit(WampMessageFactory.createSubscribe(reqId, options, topic));
    }

    public void unsubscribe(int subscriptionId, WampUnsubscribeListener listener) {
        int reqId = WampRequestIdGenerator.getId();
        reqIdListenerMap.put(reqId, listener);
        reqIdSubIdMap.put(reqId, subscriptionId);

        mAppClient.transmit(WampMessageFactory.createUnsubscribe(reqId, subscriptionId));
    }
}
