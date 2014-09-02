/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.plugin;

import com.sonycsl.Kadecot.plugin.KadecotProtocolClient.ProtocolSearchEventListener;
import com.sonycsl.Kadecot.wamp.KadecotWampTopic;
import com.sonycsl.Kadecot.wamp.client.provider.WampProviderAccessHelper;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampSubscriber;

public class KadecotProtocolSubscriber extends WampSubscriber {
    private ProtocolSearchEventListener mSearchEventListener;

    private OnTopicListener mTopicListener;

    public interface OnTopicListener {
        public void onTopicStarted(String topic);

        public void onTopicStopped(String topic);
    }

    public KadecotProtocolSubscriber(ProtocolSearchEventListener searchEventListener,
            OnTopicListener topicListener) {
        this.mSearchEventListener = searchEventListener;
        this.mTopicListener = topicListener;
    }

    @Override
    protected void onEvent(String topic, WampMessage msg) {
        if (topic.equals(KadecotWampTopic.TOPIC_PRIVATE_SEARCH)) {
            if (mSearchEventListener != null) {
                mSearchEventListener.search();
            }
        }

        if (topic.equals(WampProviderAccessHelper.Topic.START.getUri())) {
            if (mTopicListener != null) {
                mTopicListener.onTopicStarted(topic);
            }
        }

        if (topic.equals(WampProviderAccessHelper.Topic.STOP.getUri())) {
            if (mTopicListener != null) {
                mTopicListener.onTopicStopped(topic);
            }
        }
    }
}
