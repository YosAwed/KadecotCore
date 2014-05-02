
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

import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract public class WampSubscriber extends WampRole {

    private final Map<WampPeer, WampMessage> mSubs = new ConcurrentHashMap<WampPeer, WampMessage>();
    private final Map<WampPeer, WampMessage> mUnsubs = new ConcurrentHashMap<WampPeer, WampMessage>();
    private final Map<WampPeer, Map<Integer, String>> mTopicMaps = new ConcurrentHashMap<WampPeer, Map<Integer, String>>();

    @Override
    public final String getRoleName() {
        return "subscriber";
    }

    @Override
    public boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg) {
        if (msg.isSubscribeMessage()) {
            mSubs.put(receiver, msg);
            if (!mTopicMaps.containsKey(receiver)) {
                mTopicMaps.put(receiver, new ConcurrentHashMap<Integer, String>());
            }
            return true;
        }
        if (msg.isUnsubscribeMessage()) {
            mUnsubs.put(receiver, msg);
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
        if (!mSubs.containsKey(transmitter)) {
            return false;
        }

        WampSubscribeMessage request = mSubs.get(transmitter).asSubscribeMessage();
        WampSubscribedMessage response = msg.asSubscribedMessage();

        if (request.getRequestId() != response.getRequestId()) {
            return false;
        }

        Map<Integer, String> topicMap = mTopicMaps.get(transmitter);
        if (topicMap == null) {
            return false;
        }
        topicMap.put(response.getSubscriptionId(), request.getTopic());

        mSubs.remove(transmitter);

        return true;
    }

    private boolean resolveUnsubscribedMessage(WampPeer transmitter, WampMessage msg) {
        if (!mUnsubs.containsKey(transmitter)) {
            return false;
        }

        WampUnsubscribeMessage request = mUnsubs.get(transmitter).asUnsubscribeMessage();
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

        mUnsubs.remove(transmitter);
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

        event(topicMap.get(subId), msg);

        return true;
    }

    abstract protected void event(String topic, WampMessage msg);
}
