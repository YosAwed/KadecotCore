
package com.sonycsl.wamp;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class WampBroker extends WampRouter {

    private final Map<String, Map<WampMessenger, Set<Integer>>> mTopicSubscriptionMap = new ConcurrentHashMap<String, Map<WampMessenger, Set<Integer>>>();

    private final Map<String, Set<Integer>> mTopicSubscriptionIdsMap = new ConcurrentHashMap<String, Set<Integer>>();

    private final Map<Integer, AccessInfo<WampSubscribeMessage>> mSubscriptionIdAccessInfoMap = new ConcurrentHashMap<Integer, AccessInfo<WampSubscribeMessage>>();

    private int mPublicationId = 0;

    private int mSubscriptionId = 0;

    private static final String NO_SUCH_SUBSCRIBER = "wamp.error.no_such_subscriber";

    public WampBroker() {
    }

    public WampBroker(WampRouter next) {
        super(next);
    }

    @Override
    protected boolean consumeRoleBroadcast(WampMessage msg) {
        return false;
    }

    @Override
    protected final boolean consumeRoleMessage(WampMessenger friend, WampMessage msg) {
        if (msg.isPublishMessage()) {
            publish(friend, msg.asPublishMessage());
            return true;
        }

        if (msg.isSubscribeMessage()) {
            subscribe(friend, msg.asSubscribeMessage());
            return true;
        }

        if (msg.isUnsubscribeMessage()) {
            unsubscribe(friend, msg.asUnsubscribeMessage());
            return true;
        }

        return false;
    }

    private void publish(WampMessenger publisher, WampPublishMessage msg) {
        String topic = msg.getTopic();

        if (topic == null || "".equals(topic)) {
            throw new IllegalArgumentException("topic should not be null");
        }

        int publicationId = ++mPublicationId;

        synchronized (topic) {
            Set<Integer> subscriptionIds = mTopicSubscriptionIdsMap.get(topic);
            if (subscriptionIds == null) {
                publisher.send(WampMessageFactory.createPublished(
                        msg.getRequestId(), publicationId));
                return;
            }

            for (Integer subscriptionId : subscriptionIds) {
                AccessInfo<WampSubscribeMessage> accessInfo = mSubscriptionIdAccessInfoMap
                        .get(subscriptionId);

                if (accessInfo == null) {
                    publisher.send(WampMessageFactory.createPublished(
                            msg.getRequestId(), publicationId));
                    return;
                }

                accessInfo.getMessenger().send(
                        createEventMessage(subscriptionId, publicationId, new JSONObject(), msg));
            }

        }

        publisher.send(WampMessageFactory.createPublished(msg.getRequestId(), publicationId));
    }

    private void subscribe(WampMessenger friend, WampSubscribeMessage message) {
        int subscriptionId = ++mSubscriptionId;

        synchronized (mTopicSubscriptionIdsMap) {
            Set<Integer> subscriptionIds = mTopicSubscriptionIdsMap.get(message.getTopic());
            if (subscriptionIds == null) {
                subscriptionIds = new HashSet<Integer>();
                mTopicSubscriptionIdsMap.put(message.getTopic(), subscriptionIds);
            }
            subscriptionIds.add(subscriptionId);
        }

        synchronized (mSubscriptionIdAccessInfoMap) {
            mSubscriptionIdAccessInfoMap.put(Integer.valueOf(subscriptionId),
                    new AccessInfo<WampSubscribeMessage>(friend, message));
        }

        friend.send(WampMessageFactory.createSubscribed(message.getRequestId(), subscriptionId));
    }

    private void unsubscribe(WampMessenger unsubscriber, WampUnsubscribeMessage message) {
        synchronized (mSubscriptionIdAccessInfoMap) {
            AccessInfo<WampSubscribeMessage> msg = mSubscriptionIdAccessInfoMap.get(Integer
                    .valueOf(message.getSubscriptionId()));

            if (msg == null) {
                unsubscriber.send(WampMessageFactory.createError(WampMessageType.UNSUBSCRIBE,
                        message.getRequestId(), new JSONObject(), WampError.NO_SUCH_SUBSCRIPTION));
                return;
            }
            if (msg.getMessenger() != unsubscriber) {
                unsubscriber.send(WampMessageFactory.createError(WampMessageType.UNSUBSCRIBE,
                        message.getRequestId(), new JSONObject(), NO_SUCH_SUBSCRIBER));
                return;
            }
            mSubscriptionIdAccessInfoMap.remove(Integer.valueOf(message.getSubscriptionId()));

            synchronized (mTopicSubscriptionIdsMap) {
                Set<Integer> subscriptionIds = mTopicSubscriptionIdsMap.get(msg
                        .getReceivedMessage().getTopic());
                if (subscriptionIds == null) {
                    throw new IllegalStateException("Topic reference error");
                }
                if (!subscriptionIds.remove(Integer.valueOf(message.getSubscriptionId()))) {
                    throw new IllegalStateException("Topic reference error");
                }
            }
        }

        unsubscriber.send(WampMessageFactory.createUnsubscribed(message.getRequestId()));
    }

    private static WampMessage createEventMessage(int subscriptionId, int publicationId,
            JSONObject details, WampPublishMessage msg) {
        if (msg.hasArguments() && msg.hasArgumentsKw()) {
            return WampMessageFactory.createEvent(subscriptionId, publicationId, details,
                    msg.getArguments(), msg.getArgumentsKw());
        }

        if (msg.hasArguments()) {
            return WampMessageFactory.createEvent(subscriptionId, publicationId, details,
                    msg.getArguments());
        }

        return WampMessageFactory.createEvent(subscriptionId, publicationId, details);
    }

}
