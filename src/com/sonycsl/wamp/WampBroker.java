
package com.sonycsl.wamp;

import com.sonycsl.wamp.message.MessageCreater;
import com.sonycsl.wamp.message.MessageType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class WampBroker extends WampRouter {

    private final Map<String, Map<WampMessenger, Set<Integer>>> mTopicSubscriptionMap = new ConcurrentHashMap<String, Map<WampMessenger, Set<Integer>>>();

    private int mPublicationId = 0;

    private int mSubscriptionId = 0;

    private static final int REQUESTID_IDX = 1;

    private static final int OPTION_IDX = 2;

    private static final int SUBSCRIPTIONID_IDX = 2;

    private static final int TOPIC_IDX = 3;

    private static final int ARGUMENTS_IDX = 4;

    private static final int ARGUMENTKW_IDX = 5;

    private static final String NO_SUCH_SUBSCRIPTION = "wamp.error.no_such_subscription";

    private static final String NO_SUCH_SUBSCRIPTER = "wamp.error.no_such_subscripter";

    public WampBroker() {
    }

    public WampBroker(WampRouter next) {
        super(next);
    }

    @Override
    protected final boolean consumeRoleMessage(WampMessenger friend, JSONArray msg) {
        try {
            int messageType = MessageType.getMessageType(msg);
            switch (messageType) {
                case MessageType.PUBLISH:
                    handlePublishMessage(friend, msg);
                    return true;
                case MessageType.SUBSCRIBE:
                    handleSubscribeMessage(friend, msg);
                    return true;
                case MessageType.UNSUBSCRIBED:
                    handleUnsubscribeMessage(friend, msg);
                    return true;
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Illegal Message, message: " + msg + ", from: "
                    + friend);
        }

        return false;
    }

    private void handlePublishMessage(WampMessenger friend, JSONArray message) throws JSONException {
        if (message.length() > 6) {
            throw new IllegalArgumentException("PUBLISH message is Illegal, message: "
                    + message);
        }

        int requestId = message.getInt(REQUESTID_IDX);
        JSONObject options = message.getJSONObject(OPTION_IDX);
        String topic = message.getString(TOPIC_IDX);
        JSONArray arguments = message.optJSONArray(ARGUMENTS_IDX);
        JSONObject argumentKw = message.optJSONObject(ARGUMENTKW_IDX);

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
            for (WampMessenger subscriber : subscriberSubscriptionIdsMap.keySet()) {
                for (int subscriptionId : subscriberSubscriptionIdsMap.get(subscriber)) {
                    subscriber.send(MessageCreater.createEventMessage(subscriptionId,
                            publicationId, createEventDetails(options, arguments, argumentKw),
                            arguments, argumentKw));
                }
            }
        }
        publisher.send(MessageCreater.createPublishedMessage(requestId, publicationId));
    }

    private void handleSubscribeMessage(WampMessenger subscriber, JSONArray message)
            throws JSONException {
        if (message.length() != 4) {
            throw new IllegalArgumentException("SUBSCRIBE message is Illegal, message: " + message);
        }

        int requestId = message.getInt(REQUESTID_IDX);
        JSONObject options = message.getJSONObject(OPTION_IDX);
        String topic = message.getString(TOPIC_IDX);

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

        friend.send(MessageCreater.createSubscribedMessage(subscriptionId));
    }

    private void handleUnsubscribeMessage(WampMessenger unsubscriber, JSONArray message)
            throws JSONException {
        if (message.length() != 3) {
            throw new IllegalArgumentException("SUBSCRIBE message is Illegal, message: " + message);
        }

        int requestId = message.getInt(REQUESTID_IDX);
        int subscriptionId = message.getInt(SUBSCRIPTIONID_IDX);

        unsubscribe(unsubscriber, requestId, subscriptionId);
    }

    private void unsubscribe(WampMessenger unsubscriber, int requestId, int subscriptionId) {
        synchronized (mTopicSubscriptionMap) {
            for (Map<WampMessenger, Set<Integer>> subscriberSubscriptionIdsMap : mTopicSubscriptionMap
                    .values()) {
                Set<Integer> subscriptionIds = subscriberSubscriptionIdsMap.get(unsubscriber);
                if (subscriptionIds == null) {
                    unsubscriber.send(MessageCreater.createErrorMessage(MessageType.UNSUBSCRIBE,
                            requestId, new JSONObject(), NO_SUCH_SUBSCRIPTER));
                    return;
                }

                if (!subscriptionIds.remove(Integer.valueOf(subscriptionId))) {
                    unsubscriber.send(MessageCreater.createErrorMessage(MessageType.UNSUBSCRIBE,
                            requestId, new JSONObject(), NO_SUCH_SUBSCRIPTION));
                    return;
                }

                unsubscriber.send(MessageCreater.createUnsubscribedMessage(requestId));
            }
        }
    }

}
