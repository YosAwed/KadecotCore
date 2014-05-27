
package com.sonycsl.kadecot.wamp;

import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.WampPeer.Callback;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class KadecotWampClientSetupCallback implements Callback {

    public interface OnCompletionListener {
        public void onCompletion();
    }

    private final Set<String> mTopics;
    private final Set<String> mProcedures;
    private final Set<Integer> mSubscriptionIds;
    private final Set<Integer> mRegistrationIds;
    private final OnCompletionListener mListener;

    public KadecotWampClientSetupCallback(Set<String> topics, Set<String> procedures,
            OnCompletionListener listener) {
        mTopics = topics;
        mProcedures = procedures;
        mSubscriptionIds = new HashSet<Integer>(mTopics.size());
        mRegistrationIds = new HashSet<Integer>(mProcedures.size());
        mListener = listener;
    }

    @Override
    public void preConnect(WampPeer connecter, WampPeer connectee) {
    }

    @Override
    public void postConnect(WampPeer connecter, WampPeer connectee) {
    }

    @Override
    public void preTransmit(WampPeer transmitter, WampMessage msg) {
        if (msg.isGoodbyeMessage()) {

            for (int id : mSubscriptionIds) {
                transmitter.transmit(WampMessageFactory.createUnsubscribe(
                        WampRequestIdGenerator.getId(), id));
            }
            mSubscriptionIds.clear();

            for (int id : mRegistrationIds) {
                transmitter.transmit(WampMessageFactory.createUnregister(
                        WampRequestIdGenerator.getId(), id));
            }
            mRegistrationIds.clear();

            return;
        }
    }

    @Override
    public void postTransmit(WampPeer transmitter, WampMessage msg) {
    }

    @Override
    public void preReceive(WampPeer receiver, WampMessage msg) {
    }

    private void notifyCompletion() {
        if (mSubscriptionIds.size() + mRegistrationIds.size()
                == mTopics.size() + mProcedures.size()) {
            mListener.onCompletion();
        }
    }

    @Override
    public void postReceive(WampPeer receiver, WampMessage msg) {
        if (msg.isWelcomeMessage()) {

            for (String topic : mTopics) {
                receiver.transmit(WampMessageFactory.createSubscribe(
                        WampRequestIdGenerator.getId(), new JSONObject(), topic));
            }

            for (String procedure : mProcedures) {
                receiver.transmit(WampMessageFactory.createRegister(
                        WampRequestIdGenerator.getId(), new JSONObject(), procedure));
            }
            notifyCompletion();
            return;
        }

        if (msg.isSubscribedMessage()) {
            mSubscriptionIds.add(msg.asSubscribedMessage().getSubscriptionId());
            notifyCompletion();
            return;

        }

        if (msg.isRegisteredMessage()) {
            mRegistrationIds.add(msg.asRegisteredMessage().getRegistrationId());
            notifyCompletion();
            return;
        }
    }

}
