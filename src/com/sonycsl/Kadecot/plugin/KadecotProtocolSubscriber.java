/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.plugin;

import com.sonycsl.Kadecot.wamp.KadecotWampTopic;
import com.sonycsl.wamp.message.WampEventMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampSubscriber;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class KadecotProtocolSubscriber extends WampSubscriber {
    private static final String META_TOPIC = "metatopic";
    private static final String META_TOPICS_ADD = "wamp.metatopic.subscriber.add";
    private static final String META_TOPICS_REMOVE = "wamp.metatopic.subscriber.remove";
    private static final String TOPIC = "topic";

    private ProtocolSearchEventListener mSearchEventListener;
    private TopicSubscriptionListener mSubscribeListener;
    private EventListener mEventListener;

    private HashMap<String, Integer> mSubscribedTopicMap;

    public interface ProtocolSearchEventListener {
        public void onSearchEvent(WampEventMessage msg);
    }

    public interface TopicSubscriptionListener {
        public void onSubscribedEvent(String topic, WampEventMessage msg);

        public void onUnsubscribedEvent(String topic, WampEventMessage msg);
    }

    public interface EventListener {
        public void onEvent(String topic, WampEventMessage msg);
    }

    KadecotProtocolSubscriber(ProtocolSearchEventListener protocolSearchEventListener) {
        mSearchEventListener = protocolSearchEventListener;
        mSubscribedTopicMap = new HashMap<String, Integer>();
    }

    void setSubscribeListener(TopicSubscriptionListener listener) {
        mSubscribeListener = listener;
    }

    void setEventListener(EventListener listener) {
        mEventListener = listener;
    }

    public boolean isSubscribed(String topic) {
        return mSubscribedTopicMap.get(topic) != null;
    }

    @Override
    protected final void onEvent(String topic, WampMessage msg) {
        if (KadecotWampTopic.TOPIC_PRIVATE_SEARCH.equals(topic)) {
            mSearchEventListener.onSearchEvent(msg.asEventMessage());
            return;
        }
        try {
            JSONObject details = msg.asEventMessage().getDetails();
            String metaTopic = details.getString(META_TOPIC);
            String targetTopic = details.getString(TOPIC);

            if (META_TOPICS_ADD.equals(metaTopic)) {
                Integer value = mSubscribedTopicMap.get(targetTopic);
                if (value != null) {
                    mSubscribedTopicMap.put(targetTopic, value.intValue() + 1);
                } else {
                    mSubscribedTopicMap.put(targetTopic, 1);
                    if (mSubscribeListener != null) {
                        mSubscribeListener.onSubscribedEvent(targetTopic, msg.asEventMessage());
                    }
                }
                return;

            } else if (META_TOPICS_REMOVE.equals(metaTopic)) {
                Integer value = mSubscribedTopicMap.get(targetTopic);
                if (value != null) {
                    if (value.intValue() == 1) {
                        mSubscribedTopicMap.remove(targetTopic);
                        if (mSubscribeListener != null) {
                            mSubscribeListener.onUnsubscribedEvent(targetTopic,
                                    msg.asEventMessage());
                        }
                    } else {
                        mSubscribedTopicMap.put(targetTopic, value.intValue() - 1);
                    }
                }
                return;
            }

        } catch (JSONException e) {
            // not META_TOPIC
        }

        if (mEventListener != null) {
            mEventListener.onEvent(topic, msg.asEventMessage());
        }
    }
}
