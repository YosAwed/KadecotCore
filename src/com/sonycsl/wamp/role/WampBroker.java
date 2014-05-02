
package com.sonycsl.wamp.role;

import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampPublishMessage;
import com.sonycsl.wamp.message.WampSubscribeMessage;
import com.sonycsl.wamp.message.WampUnsubscribeMessage;

import org.json.JSONObject;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class WampBroker extends WampRole {

    private final Map<String, Map<WampPeer, Integer>> mSubscriberMaps = new ConcurrentHashMap<String, Map<WampPeer, Integer>>();

    private int mPublicationId = 0;

    private int mSubscriptionId = 0;

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
                    return true;
                }
            }
        }

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
