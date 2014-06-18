/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp.role;

import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.WampPublishMessage;
import com.sonycsl.wamp.message.WampSubscribeMessage;
import com.sonycsl.wamp.message.WampUnsubscribeMessage;

import org.json.JSONObject;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class WampBroker extends WampRole {

    public interface PubSubMessageHandler {
        public void onSubscribe(String topic);

        public void onUnsubscribe(String topic);
    }

    private final Map<String, Map<WampPeer, Integer>> mSubscriberMaps = new ConcurrentHashMap<String, Map<WampPeer, Integer>>();

    private int mPublicationId = 0;

    private int mSubscriptionId = 0;

    private final PubSubMessageHandler mPubSubMessageHandler;

    @Override
    public final String getRoleName() {
        return "broker";
    }

    public WampBroker() {
        super();
        mPubSubMessageHandler = null;
    }

    public WampBroker(PubSubMessageHandler handler) {
        super();
        mPubSubMessageHandler = handler;
    }

    @Override
    protected final boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected final boolean resolveRxMessageImpl(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {

        if (msg.isSubscribeMessage()) {
            return resolveSubscribeMessage(transmitter, msg, listener);
        }

        if (msg.isUnsubscribeMessage()) {
            return resolveUnsubscribeMessage(transmitter, msg, listener);
        }

        if (msg.isPublishMessage()) {
            return resolvePublishMessage(transmitter, msg, listener);
        }

        return false;
    }

    private boolean resolveSubscribeMessage(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {

        WampSubscribeMessage sub = msg.asSubscribeMessage();
        String topic = sub.getTopic();
        if (!mSubscriberMaps.containsKey(topic)) {
            mSubscriberMaps.put(topic, new ConcurrentHashMap<WampPeer, Integer>());
        }
        Map<WampPeer, Integer> subMap = mSubscriberMaps.get(topic);
        int subscriptionId = ++mSubscriptionId;
        subMap.put(transmitter, subscriptionId);

        listener.onReply(transmitter,
                WampMessageFactory.createSubscribed(sub.getRequestId(), subscriptionId));

        if (mPubSubMessageHandler != null) {
            mPubSubMessageHandler.onSubscribe(topic);
        }

        return true;
    }

    private boolean resolveUnsubscribeMessage(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {

        /*
         * TODO improve performance
         */
        WampUnsubscribeMessage unsub = msg.asUnsubscribeMessage();
        int subId = unsub.getSubscriptionId();
        for (Entry<String, Map<WampPeer, Integer>> topics : mSubscriberMaps.entrySet()) {
            Map<WampPeer, Integer> subMap = topics.getValue();
            for (Entry<WampPeer, Integer> subIds : subMap.entrySet()) {
                WampPeer target = subIds.getKey();
                if (target != transmitter) {
                    continue;
                }
                if (subIds.getValue() == subId) {
                    subMap.remove(target);
                    listener.onReply(transmitter,
                            WampMessageFactory.createUnsubscribed(unsub.getRequestId()));
                    if (mPubSubMessageHandler != null) {
                        mPubSubMessageHandler.onUnsubscribe(topics.getKey());
                    }
                    return true;
                }
            }
        }
        listener.onReply(transmitter, WampMessageFactory.createError(WampMessageType.UNSUBSCRIBE,
                unsub.getRequestId(), new JSONObject(), WampError.NO_SUCH_SUBSCRIPTION));
        return false;
    }

    private boolean resolvePublishMessage(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {

        WampPublishMessage pub = msg.asPublishMessage();

        int publicationId = ++mPublicationId;
        listener.onReply(transmitter,
                WampMessageFactory.createPublished(pub.getRequestId(), publicationId));

        if (!mSubscriberMaps.containsKey(pub.getTopic())) {
            return true;
        }

        Map<WampPeer, Integer> subMap = mSubscriberMaps.get(pub.getTopic());
        for (Entry<WampPeer, Integer> entry : subMap.entrySet()) {
            listener.onReply(entry.getKey(),
                    createEventMessage(entry.getValue(), publicationId, msg.asPublishMessage()));
        }
        return true;
    }

    private WampMessage createEventMessage(int subscriptionId, int publicationId,
            WampPublishMessage msg) {

        if (msg.hasArgumentsKw()) {
            return WampMessageFactory.createEvent(subscriptionId, publicationId, new JSONObject(),
                    msg.getArguments(), msg.getArgumentsKw());
        }

        if (msg.hasArguments()) {
            return WampMessageFactory.createEvent(subscriptionId, publicationId, new JSONObject(),
                    msg.getArguments());
        }

        return WampMessageFactory.createEvent(subscriptionId, publicationId, new JSONObject());
    }
}
