
package com.sonycsl.Kadecot.wamp;

import android.util.Log;

import com.sonycsl.Kadecot.core.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.wamp.provider.KadecotProviderClient;
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
    private static final String TAG = KadecotProtocolSetupCallback.class.getSimpleName();

    public KadecotProtocolSetupCallback(Map<String, String> topics,
            Map<String, String> procedures, OnCompletionListener listener) {
        mTopics = topics;
        mProcedures = procedures;
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

            for (String topic : mTopics.keySet()) {
                try {
                    transmitter.transmit(WampMessageFactory.createCall(
                            WampRequestIdGenerator.getId(),
                            new JSONObject(),
                            KadecotProviderClient.Procedure.REMOVE_TOPIC.getUri(),
                            new JSONArray(),
                            new JSONObject().put(KadecotCoreStore.Topics.TopicColumns.NAME,
                                    topic)));
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException");
                }
            }

            for (String procedure : mProcedures.keySet()) {
                try {
                    transmitter.transmit(WampMessageFactory.createCall(
                            WampRequestIdGenerator.getId(),
                            new JSONObject(),
                            KadecotProviderClient.Procedure.REMOVE_PROCEDURE.getUri(),
                            new JSONArray(),
                            new JSONObject().put(KadecotCoreStore.Procedures.ProcedureColumns.NAME,
                                    procedure)));
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

            for (String topic : mTopics.keySet()) {
                String description = mTopics.get(topic);
                try {
                    receiver.transmit(WampMessageFactory.createCall(
                            WampRequestIdGenerator.getId(),
                            new JSONObject(),
                            KadecotProviderClient.Procedure.PUT_TOPIC.getUri(),
                            new JSONArray(),
                            new JSONObject().put(KadecotCoreStore.Topics.TopicColumns.NAME,
                                    topic).put(
                                    KadecotCoreStore.Topics.TopicColumns.DESCRIPTION,
                                    description)));
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException");
                }
            }

            for (String procedure : mProcedures.keySet()) {
                String description = mProcedures.get(procedure);
                try {
                    receiver.transmit(WampMessageFactory.createCall(
                            WampRequestIdGenerator.getId(),
                            new JSONObject(),
                            KadecotProviderClient.Procedure.PUT_PROCEDURE.getUri(),
                            new JSONArray(),
                            new JSONObject().put(KadecotCoreStore.Procedures.ProcedureColumns.NAME,
                                    procedure).put(
                                    KadecotCoreStore.Procedures.ProcedureColumns.DESCRIPTION,
                                    description)));
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException");
                }
            }
            notifyCompletion();
            return;
        }
    }
}
