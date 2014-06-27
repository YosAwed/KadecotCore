
package com.sonycsl.Kadecot.wamp.echonetlite;

import com.sonycsl.Kadecot.wamp.KadecotWampTopic;
import com.sonycsl.Kadecot.wamp.provider.KadecotProviderClient;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampSubscriber;

import org.json.JSONException;

public class ECHONETLiteWampSubscriber extends WampSubscriber {

    public interface OnTopicListener {
        public void onTopicStarted(String topic);

        public void onTopicStopped(String topic);
    }

    private final ECHONETLiteManager mManager;
    private final OnTopicListener mListener;

    public ECHONETLiteWampSubscriber(ECHONETLiteManager manager, OnTopicListener listener) {
        mManager = manager;
        mListener = listener;
    }

    @Override
    protected void onEvent(String topic, WampMessage msg) {
        if (topic.equals(KadecotWampTopic.TOPIC_PRIVATE_SEARCH)) {
            mManager.refreshDeviceList();
        }

        if (topic.equals(KadecotProviderClient.Topic.START.getUri())) {
            if (mListener != null) {
                try {
                    mListener.onTopicStarted(msg.asEventMessage().getArgumentsKw()
                            .getString("topic"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        if (topic.equals(KadecotProviderClient.Topic.STOP.getUri())) {
            if (mListener != null) {
                try {
                    mListener.onTopicStopped(msg.asEventMessage().getArgumentsKw()
                            .getString("topic"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
