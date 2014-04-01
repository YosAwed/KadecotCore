
package com.sonycsl.wamp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class WampBroker extends WampRouter {

    private final Map<String, Map<WampMessenger, Set<Integer>>> mTopicSubscriptionMap = new ConcurrentHashMap<String, Map<WampMessenger, Set<Integer>>>();

    private int mPublicationId = 0;

    private int mSubscriptionId = 0;

    private static final String NO_SUCH_SUBSCRIPTION = "wamp.error.no_such_subscription";

    private static final String NO_SUCH_SUBSCRIPTER = "wamp.error.no_such_subscripter";

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
            handlePublishMessage(friend, msg.asPublishMessage());
            return true;
        }

        if (msg.isSubscribeMessage()) {
            handleSubscribeMessage(friend, msg.asSubscribeMessage());
            return true;
        }

        if (msg.isUnsubscribeMessage()) {
            handleUnsubscribeMessage(friend, msg.asUnsubscribeMessage());
            return true;
        }

        return false;
    }

    private void handlePublishMessage(WampMessenger friend, WampPublishMessage message) {
        int requestId = message.getRequestId();
        JSONObject options = message.getOptions();
        String topic = message.getTopic();
        JSONArray arguments = message.getArguments();
        JSONObject argumentKw = message.getArgumentsKw();

        publish(friend, requestId, options, topic, arguments, argumentKw);
    }

    private void publish(WampMessenger publisher, int requestId, JSONObject options, String topic,
            JSONArray arguments, JSONObject argumentKw) {
        if (topic == null || "".equals(topic)) {
            throw new IllegalArgumentException("topic should not be null");
        }

        int publicationId = ++mPublicationId;

        synchronized (mTopicSubscriptionMap) {
            Map<WampMessenger, Set<Integer>> subscriberSubscriptionIdsMap = mTopicSubscriptionMap
                    .get(topic);

            if (subscriberSubscriptionIdsMap != null) {
                for (WampMessenger subscriber : subscriberSubscriptionIdsMap.keySet()) {
                    for (int subscriptionId : subscriberSubscriptionIdsMap.get(subscriber)) {
                        subscriber.send(WampMessageFactory.createEvent(subscriptionId,
                                publicationId, createEventDetails(options, arguments, argumentKw),
                                arguments, argumentKw));
                    }
                }
            }
        }
        publisher.send(WampMessageFactory.createPublished(requestId, publicationId));
    }

    private void handleSubscribeMessage(WampMessenger subscriber, WampSubscribeMessage message) {

        int requestId = message.getRequestId();
        JSONObject options = message.getOptions();
        String topic = message.getTopic();

        subscribe(subscriber, requestId, options, topic);
    }

    protected abstract JSONObject createEventDetails(JSONObject options, JSONArray arguments,
            JSONObject argumentKw);

    private void subscribe(WampMessenger friend, int requestId, JSONObject options, String topic) {
        int subscriptionId;
        synchronized (mTopicSubscriptionMap) {
            Map<WampMessenger, Set<Integer>> subscriberSubscriptionIdsMap = mTopicSubscriptionMap
                    .get(topic);
            if (subscriberSubscriptionIdsMap == null) {
                subscriberSubscriptionIdsMap = new ConcurrentHashMap<WampMessenger, Set<Integer>>();
                mTopicSubscriptionMap.put(topic, subscriberSubscriptionIdsMap);
            }

            Set<Integer> subscriptionIds = subscriberSubscriptionIdsMap.get(friend);
            if (subscriptionIds == null) {
                subscriptionIds = new HashSet<Integer>();
                subscriberSubscriptionIdsMap.put(friend, subscriptionIds);
            }

            subscriptionId = ++mSubscriptionId;
            subscriptionIds.add(subscriptionId);
        }

        friend.send(WampMessageFactory.createSubscribed(requestId, subscriptionId));
    }

    private void handleUnsubscribeMessage(WampMessenger unsubscriber, WampUnsubscribeMessage message) {

        int requestId = message.getRequestId();
        int subscriptionId = message.getSubscriptionId();

        unsubscribe(unsubscriber, requestId, subscriptionId);
    }

    private void unsubscribe(WampMessenger unsubscriber, int requestId, int subscriptionId) {
        synchronized (mTopicSubscriptionMap) {
            for (Map<WampMessenger, Set<Integer>> subscriberSubscriptionIdsMap : mTopicSubscriptionMap
                    .values()) {
                Set<Integer> subscriptionIds = subscriberSubscriptionIdsMap.get(unsubscriber);
                if (subscriptionIds == null) {
                    unsubscriber.send(WampMessageFactory.createError(WampMessageType.UNSUBSCRIBE,
                            requestId, new JSONObject(), NO_SUCH_SUBSCRIPTER));
                    return;
                }

                if (!subscriptionIds.remove(Integer.valueOf(subscriptionId))) {
                    unsubscriber.send(WampMessageFactory.createError(WampMessageType.UNSUBSCRIBE,
                            requestId, new JSONObject(), NO_SUCH_SUBSCRIPTION));
                    return;
                }

                unsubscriber.send(WampMessageFactory.createUnsubscribed(requestId));
            }
        }
    }

}
