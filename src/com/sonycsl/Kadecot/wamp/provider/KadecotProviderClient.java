/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;

import com.sonycsl.Kadecot.core.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.wamp.KadecotWampClient;
import com.sonycsl.Kadecot.wamp.provider.DeviceObserver.OnDeviceChangedListener;
import com.sonycsl.Kadecot.wamp.provider.TopicObserver.OnSubscriberListener;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampPublisher;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public final class KadecotProviderClient extends KadecotWampClient {

    private static final String PREFIX = "com.sonycsl.kadecot.provider";

    public static enum Topic {
        DEVICE("device"),
        START("start"),
        STOP("stop");

        private final String mUri;

        Topic(String name) {
            mUri = PREFIX + ".topic." + name;
        }

        public String getUri() {
            return mUri;
        }
    }

    public static enum Procedure {
        PUT_DEVICE("putDevice"),
        REMOVE_DEVICE("removeDevice"),
        GET_DEVICE_LIST("getDeviceList"),
        CHANGE_NICKNAME("changeNickname"),
        PUT_TOPIC("putTopic"),
        REMOVE_TOPIC("removeTopic"),
        GET_TOPIC_LIST("getTopicList"),
        PUT_PROCEDURE("putProcedure"),
        REMOVE_PROCEDURE("removeProcedure"),
        GET_PROCEDURE_LIST("getProcedureList");

        private final String mMethod;
        private final String mUri;

        Procedure(String method) {
            mMethod = method;
            mUri = PREFIX + ".procedure." + method;
        }

        public String getUri() {
            return mUri;
        }

        public String getMethod() {
            return mMethod;
        }

        public static Procedure getEnum(String procedure) {
            for (Procedure p : Procedure.values()) {
                if (p.getUri().equals(procedure)) {
                    return p;
                }
            }
            return null;
        }
    }

    public static JSONObject createPutDeviceArgsKw(String protocol, String uuid, String deviceType,
            String description, boolean status) throws JSONException {
        JSONObject info = new JSONObject();
        info.put(KadecotCoreStore.Devices.DeviceColumns.PROTOCOL, protocol);
        info.put(KadecotCoreStore.Devices.DeviceColumns.UUID, uuid);
        info.put(KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE, deviceType);
        info.put(KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION, description);
        info.put(KadecotCoreStore.Devices.DeviceColumns.STATUS, status);
        return info;
    }

    private final ContentResolver mResolver;
    private final DeviceObserver mDeviceObserver;
    private final TopicObserver mTopicObserver;

    public KadecotProviderClient(Context context, Handler handler) {
        super();

        mResolver = context.getContentResolver();
        mDeviceObserver = new DeviceObserver(mResolver, handler);
        mTopicObserver = new TopicObserver(mResolver, handler);
    }

    @Override
    protected Set<WampRole> getClientRoleSet() {
        Set<WampRole> roles = new HashSet<WampRole>();
        roles.add(new WampPublisher());
        try {
            roles.add(new ProviderCallee(mResolver));
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
                            options, KadecotProviderClient.Topic.DEVICE.getUri(),
                            new JSONArray(), device));
                }
            });

            mTopicObserver.setOnSubscriberListener(new OnSubscriberListener() {

                @Override
                public void onAppeared(String topic) {
                    try {
                        transmit(WampMessageFactory.createPublish(WampRequestIdGenerator.getId(),
                                new JSONObject(), KadecotProviderClient.Topic.START.getUri(),
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
                                new JSONObject(), KadecotProviderClient.Topic.STOP.getUri(),
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
    public Set<String> getSubscribableTopics() {
        Set<String> topics = new HashSet<String>();
        return topics;
    }

    @Override
    public Set<String> getRegisterableProcedures() {
        Set<String> procs = new HashSet<String>();

        for (Procedure p : Procedure.values()) {
            procs.add(p.getUri());
        }
        return procs;
    }
}
