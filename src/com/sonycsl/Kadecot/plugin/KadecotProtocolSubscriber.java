/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.plugin;

import com.sonycsl.Kadecot.wamp.KadecotWampTopic;
import com.sonycsl.Kadecot.wamp.client.provider.WampProviderAccessHelper;
import com.sonycsl.wamp.message.WampEventMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampSubscriber;

public class KadecotProtocolSubscriber extends WampSubscriber {
    private ProtocolSearchEventListener mSearchEventListener;
    private TopicSubscriptionListener mSubscribeListener;
    private EventListener mEventListener;

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
    }

    void setSubscribeListener(TopicSubscriptionListener listener) {
        mSubscribeListener = listener;
    }

    void setEventListener(EventListener listener) {
        mEventListener = listener;
    }

    @Override
    protected final void onEvent(String topic, WampMessage msg) {
        if (KadecotWampTopic.TOPIC_PRIVATE_SEARCH.equals(topic)) {
            mSearchEventListener.onSearchEvent(msg.asEventMessage());
            return;
        }

        if (WampProviderAccessHelper.Topic.START.getUri().equals(topic)) {
            if (mSubscribeListener != null) {
                mSubscribeListener.onSubscribedEvent(topic, msg.asEventMessage());
            }
            return;
        }

        if (WampProviderAccessHelper.Topic.STOP.getUri().equals(topic)) {
            if (mSubscribeListener != null) {
                mSubscribeListener.onUnsubscribedEvent(topic, msg.asEventMessage());
            }
            return;
        }

        if (mEventListener != null) {
            mEventListener.onEvent(topic, msg.asEventMessage());
        }
    }
}
