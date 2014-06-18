/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp.role;

import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampEventMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampSubscribeMessage;
import com.sonycsl.wamp.message.WampSubscribedMessage;
import com.sonycsl.wamp.message.WampUnsubscribeMessage;
import com.sonycsl.wamp.message.WampUnsubscribedMessage;
import com.sonycsl.wamp.util.DoubleKeyMap;

import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract public class WampSubscriber extends WampRole {

    private final DoubleKeyMap<WampPeer, Integer, WampMessage> mSubs = new DoubleKeyMap<WampPeer, Integer, WampMessage>();
    private final DoubleKeyMap<WampPeer, Integer, WampMessage> mUnsubs = new DoubleKeyMap<WampPeer, Integer, WampMessage>();
    private final Map<WampPeer, Map<Integer, String>> mTopicMaps = new ConcurrentHashMap<WampPeer, Map<Integer, String>>();

    @Override
    public final String getRoleName() {
        return "subscriber";
    }

    @Override
    public boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg) {
        if (msg.isSubscribeMessage()) {
            mSubs.put(receiver, msg.asSubscribeMessage().getRequestId(), msg);
            if (!mTopicMaps.containsKey(receiver)) {
                mTopicMaps.put(receiver, new ConcurrentHashMap<Integer, String>());
            }
            return true;
        }
        if (msg.isUnsubscribeMessage()) {
            mUnsubs.put(receiver, msg.asUnsubscribeMessage().getRequestId(), msg);
            return true;
        }
        return false;
    }

    @Override
    public boolean resolveRxMessageImpl(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {
        if (msg.isSubscribedMessage()) {
            return resolveSubscribedMessage(transmitter, msg);
        }

        if (msg.isUnsubscribedMessage()) {
            return resolveUnsubscribedMessage(transmitter, msg);
        }
        if (msg.isEventMessage()) {
            return resolveEventMessage(transmitter, msg, listener);
        }

        return false;
    }

    private boolean resolveSubscribedMessage(WampPeer transmitter, WampMessage msg) {
        WampSubscribedMessage subscribedMsg = msg.asSubscribedMessage();
        if (!mSubs.containsKey(transmitter, subscribedMsg.getRequestId())) {
            return false;
        }

        WampSubscribeMessage request = mSubs.get(transmitter, subscribedMsg.getRequestId())
                .asSubscribeMessage();
        WampSubscribedMessage response = msg.asSubscribedMessage();

        if (request.getRequestId() != response.getRequestId()) {
            return false;
        }

        Map<Integer, String> topicMap = mTopicMaps.get(transmitter);
        if (topicMap == null) {
            return false;
        }
        topicMap.put(response.getSubscriptionId(), request.getTopic());

        mSubs.remove(transmitter, subscribedMsg.getRequestId());

        return true;
    }

    private boolean resolveUnsubscribedMessage(WampPeer transmitter, WampMessage msg) {
        WampUnsubscribedMessage unsubscribedMsg = msg.asUnsubscribedMessage();
        if (!mUnsubs.containsKey(transmitter, unsubscribedMsg.getRequestId())) {
            return false;
        }

        WampUnsubscribeMessage request = mUnsubs.get(transmitter, unsubscribedMsg.getRequestId())
                .asUnsubscribeMessage();
        WampUnsubscribedMessage response = msg.asUnsubscribedMessage();

        if (request.getRequestId() != response.getRequestId()) {
            return false;
        }

        Map<Integer, String> topicMap = mTopicMaps.get(transmitter);
        if (topicMap == null) {
            return false;
        }
        if (!topicMap.containsKey(request.getSubscriptionId())) {
            return false;
        }
        topicMap.remove(request.getSubscriptionId());

        mUnsubs.remove(transmitter, unsubscribedMsg.getRequestId());
        return true;
    }

    private boolean resolveEventMessage(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {

        WampEventMessage event = msg.asEventMessage();

        Map<Integer, String> topicMap = mTopicMaps.get(transmitter);
        if (topicMap == null) {
            return false;
        }

        final int subId = event.getSubscriptionId();
        if (!topicMap.containsKey(subId)) {
            listener.onReply(transmitter, WampMessageFactory.createError(msg.getMessageType(), -1,
                    new JSONObject(), WampError.NO_SUCH_SUBSCRIPTION));
            return true;
        }

        onEvent(topicMap.get(subId), msg);

        return true;
    }

    abstract protected void onEvent(String topic, WampMessage msg);
}
