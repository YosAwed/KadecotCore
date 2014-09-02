/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp.client.provider;

import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;

import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.wamp.client.KadecotWampClient;
import com.sonycsl.Kadecot.wamp.client.provider.DeviceObserver.OnDeviceChangedListener;
import com.sonycsl.Kadecot.wamp.client.provider.WampProviderAccessHelper.Procedure;
import com.sonycsl.Kadecot.wamp.client.provider.WampProviderAccessHelper.Topic;
import com.sonycsl.Kadecot.wamp.client.provider.TopicObserver.OnSubscriberListener;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampPublisher;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class KadecotProviderClient extends KadecotWampClient {

    private final Context mContext;
    private final DeviceObserver mDeviceObserver;
    private final TopicObserver mTopicObserver;

    public KadecotProviderClient(Context context, Handler handler) {
        super();
        mContext = context;
        mDeviceObserver = new DeviceObserver(mContext.getContentResolver(), handler);
        mTopicObserver = new TopicObserver(mContext.getContentResolver(), handler);
    }

    @Override
    protected Set<WampRole> getClientRoleSet() {
        Set<WampRole> roles = new HashSet<WampRole>();
        roles.add(new WampPublisher());
        try {
            roles.add(new ProviderCallee(mContext));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return roles;
    }

    @Override
    protected void onConnected(WampPeer peer) {
    }

    @Override
    protected void onTransmitted(WampPeer peer, WampMessage msg) {
    }

    @Override
    protected void onReceived(WampMessage msg) {
        if (msg.isWelcomeMessage()) {
            mDeviceObserver.setOnDeviceChangedListener(new OnDeviceChangedListener() {
                @Override
                public void onDeviceFound(JSONObject device) {
                    JSONObject options = new JSONObject();
                    try {
                        options.put(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID,
                                device.get(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                    transmit(WampMessageFactory.createPublish(WampRequestIdGenerator.getId(),
                            options, WampProviderAccessHelper.Topic.DEVICE.getUri(),
                            new JSONArray(), device));
                }
            });

            mTopicObserver.setOnSubscriberListener(new OnSubscriberListener() {

                @Override
                public void onAppeared(String topic) {
                    try {
                        transmit(WampMessageFactory.createPublish(WampRequestIdGenerator.getId(),
                                new JSONObject(), WampProviderAccessHelper.Topic.START.getUri(),
                                new JSONArray(), new JSONObject().put("topic", topic)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                }

                @Override
                public void onDisappeared(String topic) {
                    try {
                        transmit(WampMessageFactory.createPublish(WampRequestIdGenerator.getId(),
                                new JSONObject(), WampProviderAccessHelper.Topic.STOP.getUri(),
                                new JSONArray(), new JSONObject().put("topic", topic)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            });
        }

        if (msg.isGoodbyeMessage()) {
            mDeviceObserver.setOnDeviceChangedListener(null);
            mTopicObserver.setOnSubscriberListener(null);
        }
    }

    @Override
    public Map<String, String> getSubscribableTopics() {
        Map<String, String> topics = new HashMap<String, String>();
        for (Topic t : Topic.values()) {
            topics.put(t.getUri(), "");
        }
        return topics;
    }

    @Override
    public Map<String, String> getRegisterableProcedures() {
        Map<String, String> procs = new HashMap<String, String>();

        for (Procedure p : WampProviderAccessHelper.Procedure.values()) {
            procs.put(p.getUri(), "");
        }
        return procs;
    }

    @Override
    public Set<String> getTopicsToSubscribe() {
        return new HashSet<String>();
    }
}
