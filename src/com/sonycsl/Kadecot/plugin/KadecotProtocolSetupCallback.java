/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.plugin;

import android.util.Log;

import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.wamp.client.provider.WampProviderAccessHelper;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.WampPeer.Callback;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class KadecotProtocolSetupCallback implements Callback {

    public interface OnCompletionListener {
        public void onCompletion();
    }

    private final Map<String, String> mTopics;
    private final Map<String, String> mProcedures;
    private final OnCompletionListener mListener;
    private final String mProtocol;
    private static final String TAG = KadecotProtocolSetupCallback.class.getSimpleName();

    public KadecotProtocolSetupCallback(Map<String, String> topics,
            Map<String, String> procedures, OnCompletionListener listener) {
        mTopics = topics;
        mProcedures = procedures;
        mListener = listener;

        String protocol = null;
        for (String topic : mTopics.keySet()) {
            if (protocol == null) {
                protocol = topic.split("\\.", 5)[3];
                continue;
            }
            if (!protocol.equals(topic.split("\\.", 5)[3])) {
                throw new IllegalArgumentException(topic.split("\\.", 5)[3] + " is invalid");
            }
        }

        for (String proc : mProcedures.keySet()) {
            if (protocol == null) {
                protocol = proc.split("\\.", 5)[3];
                continue;
            }
            if (!protocol.equals(proc.split("\\.", 5)[3])) {
                throw new IllegalArgumentException(proc.split("\\.", 5)[3] + " is invalid");
            }
        }
        mProtocol = protocol;
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

            if (mProtocol != null) {
                try {
                    transmitter.transmit(WampMessageFactory.createCall(
                            WampRequestIdGenerator.getId(),
                            new JSONObject(),
                            WampProviderAccessHelper.Procedure.REMOVE_TOPICS.getUri(),
                            new JSONArray(),
                            new JSONObject().put(KadecotCoreStore.Topics.TopicColumns.PROTOCOL,
                                    mProtocol)));
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException");
                }

                try {
                    transmitter.transmit(WampMessageFactory.createCall(
                            WampRequestIdGenerator.getId(),
                            new JSONObject(),
                            WampProviderAccessHelper.Procedure.REMOVE_PROCEDURES.getUri(),
                            new JSONArray(),
                            new JSONObject().put(
                                    KadecotCoreStore.Procedures.ProcedureColumns.PROTOCOL,
                                    mProtocol)));
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException");
                }
            }
            notifyCompletion();
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
        mListener.onCompletion();
    }

    @Override
    public void postReceive(WampPeer receiver, WampMessage msg) {
        if (msg.isWelcomeMessage()) {

            JSONObject topics = new JSONObject();
            for (String topic : mTopics.keySet()) {
                try {
                    topics.put(topic, mTopics.get(topic));
                } catch (JSONException e) {
                    e.printStackTrace();
                    continue;
                }
            }

            if (topics.length() != 0) {
                try {
                    receiver.transmit(WampMessageFactory.createCall(
                            WampRequestIdGenerator.getId(),
                            new JSONObject(),
                            WampProviderAccessHelper.Procedure.PUT_TOPICS.getUri(),
                            new JSONArray(),
                            new JSONObject().put(mProtocol, topics)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            JSONObject procs = new JSONObject();
            for (String proc : mProcedures.keySet()) {
                try {
                    procs.put(proc, mProcedures.get(proc));
                } catch (JSONException e) {
                    e.printStackTrace();
                    continue;
                }
            }

            if (procs.length() != 0) {
                try {
                    receiver.transmit(WampMessageFactory.createCall(
                            WampRequestIdGenerator.getId(),
                            new JSONObject(),
                            WampProviderAccessHelper.Procedure.PUT_PROCEDURES.getUri(),
                            new JSONArray(),
                            new JSONObject().put(mProtocol, procs)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            notifyCompletion();
            return;
        }
    }
}
