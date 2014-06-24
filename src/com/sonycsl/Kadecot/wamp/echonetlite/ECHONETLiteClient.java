/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp.echonetlite;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import com.sonycsl.Kadecot.core.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.device.AccessException;
import com.sonycsl.Kadecot.device.DeviceProperty;
import com.sonycsl.Kadecot.device.echo.EchoDeviceDatabase;
import com.sonycsl.Kadecot.wamp.KadecotProperty;
import com.sonycsl.Kadecot.wamp.KadecotWampClient;
import com.sonycsl.Kadecot.wamp.KadecotWampTopic;
import com.sonycsl.Kadecot.wamp.echonetlite.ECHONETLiteClient.ECHONETLiteWampSubscriber.OnTopicListener;
import com.sonycsl.Kadecot.wamp.provider.KadecotProviderClient;
import com.sonycsl.echo.Echo;
import com.sonycsl.echo.eoj.EchoObject;
import com.sonycsl.echo.node.EchoNode;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampInvocationMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampResultMessage;
import com.sonycsl.wamp.role.WampCallee;
import com.sonycsl.wamp.role.WampCaller;
import com.sonycsl.wamp.role.WampPublisher;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.role.WampSubscriber;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ECHONETLiteClient extends KadecotWampClient {

    public static final String BASE_URI = "com.sonycsl.kadecot.echonetlite";

    private ECHONETLiteWampCallee mCallee;

    private ECHONETLiteWampSubscriber mSubscriber;

    private ECHONETLiteManager mManager;

    // <deviceId, deviceData>
    private Map<Long, ECHONETLiteDeviceData> mDeviceMap;

    private Map<String, JSONObject> mTemporaryDeviceMap;

    private final Handler mHandler;

    public ECHONETLiteClient(Context context) {
        super();

        mDeviceMap = new ConcurrentHashMap<Long, ECHONETLiteDeviceData>();
        mTemporaryDeviceMap = new HashMap<String, JSONObject>();

        ECHONETLiteManager.ECHONETLiteWampDevicePropertyChangedListener pListener = createPropetyChangedListener();
        ECHONETLiteDiscovery.OnEchoDeviceInfoListener dListener = createDeviceInfoListener();
        mManager = ECHONETLiteManager.getInstance();
        mManager.setClient(this);
        mManager.setListener(pListener, dListener);
        mHandler = new Handler();
    }

    private ECHONETLiteManager.ECHONETLiteWampDevicePropertyChangedListener createPropetyChangedListener() {
        return new ECHONETLiteManager.ECHONETLiteWampDevicePropertyChangedListener() {

            @Override
            public void OnPropertyChanged(ECHONETLiteDeviceData data, List<DeviceProperty> list) {
                publishOnPropertyChanged(data, list);
            }
        };
    }

    private ECHONETLiteDiscovery.OnEchoDeviceInfoListener createDeviceInfoListener() {
        return new ECHONETLiteDiscovery.OnEchoDeviceInfoListener() {

            @Override
            public void onDeviceStateChanged(JSONObject data) {
                putDeviceInfo(data);
            }

            @Override
            public void onDeviceAdded(JSONObject data) {
                putDeviceInfo(data);
            }
        };
    }

    @Override
    protected Set<WampRole> getClientRoleSet() {
        mSubscriber = new ECHONETLiteWampSubscriber(mManager, new OnTopicListener() {

            // TODO: 実験用の値なので変更可能にする
            private static final int DELAY_MILLIS = 5000;

            private Map<String, Runnable> mRunnables = new HashMap<String, Runnable>();

            @Override
            public void onTopicSubscribed(String topic) {
                final String propertyName = topic.split("\\.", 7)[6];

                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                synchronized (mDeviceMap) {
                                    for (Entry<Long, ECHONETLiteDeviceData> entry : mDeviceMap
                                            .entrySet()) {

                                        JSONObject property = new JSONObject();
                                        List<DeviceProperty> response;
                                        try {
                                            property.put("propertyName", propertyName);
                                            response = mCallee.callGet(entry.getValue(), property);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            continue;
                                        } catch (AccessException e) {
                                            e.printStackTrace();
                                            continue;
                                        }

                                        JSONObject argsKw;
                                        try {
                                            argsKw = mCallee
                                                    .createResponseArgumentsKw(response);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            continue;
                                        }

                                        if (argsKw == null) {
                                            continue;
                                        }

                                        try {
                                            transmit(WampMessageFactory.createPublish(
                                                    WampRequestIdGenerator.getId(),
                                                    new JSONObject()
                                                            .put(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID,
                                                                    entry.getKey()),
                                                    ECHONETLiteTopicGenerator.getTopic(entry
                                                            .getValue()
                                                            .getClassCode(),
                                                            ECHONETLitePropertyName
                                                                    .translate(propertyName)),
                                                    new JSONArray(), argsKw));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            continue;
                                        }
                                    }
                                }
                                return null;
                            }
                        }.execute();
                        mHandler.postDelayed(this, DELAY_MILLIS);
                    }
                };
                mRunnables.put(topic, r);
                mHandler.post(r);
            }

            @Override
            public void onTopicUnsubscribed(String topic) {
                mHandler.removeCallbacks(mRunnables.remove(topic));
            }

        });
        mCallee = new ECHONETLiteWampCallee();

        Set<WampRole> roleSet = new HashSet<WampRole>();
        roleSet.add(new WampCaller());
        roleSet.add(new WampPublisher());
        roleSet.add(mSubscriber);
        roleSet.add(mCallee);
        return roleSet;
    }

    @Override
    protected void onConnected(WampPeer peer) {
    }

    @Override
    protected void onTransmitted(WampPeer peer, WampMessage msg) {
        if (msg.isGoodbyeMessage()) {
            if (mIdHolder < 0) {
                return;
            }
            transmit(WampMessageFactory
                    .createUnsubscribe(WampRequestIdGenerator.getId(), mIdHolder));
        }
    }

    @Override
    public Set<String> getSubscribableTopics() {
        Set<String> topics = new HashSet<String>();
        topics.add(KadecotWampTopic.TOPIC_PRIVATE_SEARCH);
        topics.add(KadecotProviderClient.Topic.START.getUri());
        topics.add(KadecotProviderClient.Topic.STOP.getUri());
        return topics;
    }

    @Override
    public Set<String> getRegisterableProcedures() {
        Set<String> procs = new HashSet<String>();
        for (ECHONETLiteProcedure procedure : ECHONETLiteProcedure.values()) {
            procs.add(procedure.toString());
        }
        return procs;
    }

    // TODO: 2014/6 release 向けの暫定措置
    private int mIdHolder = -1;

    @Override
    protected void onReceived(WampMessage msg) {
        if (msg.isWelcomeMessage()) {
            mManager.start();
            putTopics();

            mIdHolder = WampRequestIdGenerator.getId();
            transmit(WampMessageFactory.createSubscribe(mIdHolder,
                    new JSONObject(), ECHONETLiteTopicGenerator.getTopic((short) 304, "0x85")));
        }

        if (msg.isSubscribedMessage()) {
            if (mIdHolder == msg.asSubscribedMessage().getRequestId()) {
                mIdHolder = msg.asSubscribedMessage().getSubscriptionId();
            }
        }

        if (msg.isGoodbyeMessage()) {
            removeTopics();
            mManager.stop();
        }

        if (msg.isResultMessage()) {
            WampResultMessage result = msg.asResultMessage();
            if (!result.hasArgumentsKw()) {
                return;
            }
            JSONObject device = result.getArgumentsKw();
            try {
                // add device data to data list
                ECHONETLiteDeviceData data = new ECHONETLiteDeviceData(device);
                // data.rename(echoData.nickname);
                mDeviceMap.put(data.getDeviceId(), data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void startDiscovery() {
        mManager.start();
    }

    public void stopDiscovery() {
        mManager.stop();
    }

    private void putTopics() {
        try {
            // TODO: fix this workaround
            JSONObject topic = new JSONObject();
            topic.put(KadecotCoreStore.Topics.TopicColumns.NAME,
                    ECHONETLiteTopicGenerator.getTopic((short) 304, "0x85"));
            topic.put(KadecotCoreStore.Topics.TopicColumns.DESCRIPTION, "test");

            transmit(WampMessageFactory.createCall(WampRequestIdGenerator.getId(),
                    new JSONObject(),
                    KadecotProviderClient.Procedure.PUT_TOPIC.getUri(), new JSONArray(), topic));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void removeTopics() {
        try {
            // TODO: fix this workaround
            JSONObject topic = new JSONObject();
            topic.put(KadecotCoreStore.Topics.TopicColumns.NAME,
                    ECHONETLiteTopicGenerator.getTopic((short) 304, "0x85"));
            transmit(WampMessageFactory.createCall(WampRequestIdGenerator.getId(),
                    new JSONObject(),
                    KadecotProviderClient.Procedure.REMOVE_TOPIC.getUri(), new JSONArray(), topic));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    protected void putDeviceInfo(JSONObject data) {
        try {
            mTemporaryDeviceMap.put(data.getString(KadecotCoreStore.Devices.DeviceColumns.UUID),
                    data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        transmit(WampMessageFactory.createCall(WampRequestIdGenerator.getId(),
                new JSONObject(),
                KadecotProviderClient.Procedure.PUT_DEVICE.getUri(),
                new JSONArray(), data));
    }

    protected void publishOnPropertyChanged(ECHONETLiteDeviceData data, List<DeviceProperty> list) {
        try {
            for (DeviceProperty dp : list) {
                JSONObject options = new JSONObject();
                options.put("deviceId", data.getDeviceId());

                String topic = ECHONETLiteTopicGenerator.getTopic(data.getClassCode(), dp.name);
                if (topic == null) {
                    return;
                }
                JSONArray arguments = new JSONArray();
                arguments.put(dp.value);
                transmit(WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), options,
                        topic, arguments));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class ECHONETLiteWampCallee extends WampCallee {

        @Override
        protected WampMessage invocation(String procedure, WampMessage msg) {
            ECHONETLiteProcedure enumProcedure = ECHONETLiteProcedure
                    .getEnum(procedure);
            WampInvocationMessage invMsg = msg.asInvocationMessage();

            if (enumProcedure == ECHONETLiteProcedure.NOT_PROCEDURE) {
                return WampMessageFactory.createError(msg.getMessageType(), invMsg.getRequestId(),
                        new JSONObject(), WampError.NO_SUCH_PROCEDURE);
            }

            JSONObject argumentKw = new JSONObject();
            try {
                argumentKw = resolveInvocationMsg(enumProcedure, invMsg);
            } catch (JSONException e) {
                e.printStackTrace();
                return WampMessageFactory.createError(msg.getMessageType(), invMsg.getRequestId(),
                        new JSONObject(), WampError.INVALID_ARGUMENT);
            } catch (AccessException e) {
                e.printStackTrace();
                return WampMessageFactory.createError(msg.getMessageType(), invMsg.getRequestId(),
                        new JSONObject(), e.getClass().getName());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return WampMessageFactory.createError(msg.getMessageType(), invMsg.getRequestId(),
                        new JSONObject(), WampError.INVALID_ARGUMENT);
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
                return WampMessageFactory.createError(msg.getMessageType(), invMsg.getRequestId(),
                        new JSONObject(), WampError.NO_SUCH_PROCEDURE);
            }

            return WampMessageFactory.createYield(invMsg.getRequestId(), new JSONObject(),
                    new JSONArray(), argumentKw);
        }

        private JSONObject resolveInvocationMsg(ECHONETLiteProcedure procedure,
                WampInvocationMessage msg) throws JSONException, AccessException,
                IllegalArgumentException, UnsupportedOperationException {
            long deviceId = msg.getDetails().getLong(
                    KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID);
            JSONObject params = msg.getArgumentsKw();
            List<DeviceProperty> response = new ArrayList<DeviceProperty>();
            ECHONETLiteDeviceData data = mDeviceMap.get(deviceId);
            if (data == null) {
                throw new IllegalArgumentException("no such device : deviceId " + deviceId);
            }
            switch (procedure) {
                case GET:
                    response = callGet(data, params);
                    break;
                case SET:
                    response = callSet(data, params);
                    break;
                default:
                    throw new UnsupportedOperationException(procedure.toString());
            }

            return createResponseArgumentsKw(response);
        }

        /**
         * @param list size must be one.
         * @return
         * @throws JSONException
         */
        private JSONObject createResponseArgumentsKw(List<DeviceProperty> list)
                throws JSONException {

            JSONObject json = new JSONObject();
            DeviceProperty dp = list.get(0);
            if (dp.value == null) {
                return null;
            }
            String propName = ECHONETLitePropertyName.translate(dp.name);
            json.put(KadecotProperty.PROPERTY_NAME_KEY, propName);
            json.put(KadecotProperty.PROPERTY_VALUE_KEY, dp.value);
            return json;
        }

        /**
         * @param data
         * @param params [propName1, propName2, ...]
         * @return
         */
        public List<DeviceProperty> callGet(ECHONETLiteDeviceData data, JSONObject params)
                throws JSONException, AccessException {
            List<DeviceProperty> propList = makePropertyList(params);
            EchoObject obj = null;
            try {
                obj = getEchoObject(data.getDeviceId());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            return mManager.get(obj, propList);
        }

        /**
         * @param data
         * @param params [[propName1, propValue1], [propName2, propValue2], ...]
         * @return
         */
        private List<DeviceProperty> callSet(ECHONETLiteDeviceData data, JSONObject params)
                throws JSONException, AccessException {
            List<DeviceProperty> propertyList = makePropertyList(params);
            EchoObject obj = null;
            try {
                obj = getEchoObject(data.getDeviceId());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            return mManager.set(obj, propertyList);
        }

        /**
         * @param data
         * @param param {"propertyName" : name}
         * @return
         */
        private List<DeviceProperty> makePropertyList(JSONObject param)
                throws JSONException {
            ArrayList<DeviceProperty> propertyList = new ArrayList<DeviceProperty>();

            String propName = ECHONETLitePropertyName.translate(param
                    .getString(KadecotProperty.PROPERTY_NAME_KEY));

            Object propValue = null;

            // for set
            if (param.has(KadecotProperty.PROPERTY_VALUE_KEY)) {
                String paramValue = param.getString(KadecotProperty.PROPERTY_VALUE_KEY);
                JSONArray jarray = new JSONArray();
                String val = ECHONETLitePropertyValue.getPropertyValue(paramValue).toString();
                jarray.put(Integer.decode(val));
                propValue = jarray;
            }

            DeviceProperty dp = new DeviceProperty(propName, propValue);
            propertyList.add(dp);

            return propertyList;
        }
    }

    protected static final class ECHONETLiteWampSubscriber extends WampSubscriber {

        public interface OnTopicListener {
            public void onTopicSubscribed(String topic);

            public void onTopicUnsubscribed(String topic);
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
                        mListener.onTopicSubscribed(msg.asEventMessage().getArgumentsKw()
                                .getString("topic"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (topic.equals(KadecotProviderClient.Topic.STOP.getUri())) {
                if (mListener != null) {
                    try {
                        mListener.onTopicUnsubscribed(msg.asEventMessage().getArgumentsKw()
                                .getString("topic"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

    }

    public EchoObject getEchoObject(long deviceId) throws UnknownHostException {
        ECHONETLiteDeviceData data = mDeviceMap.get(deviceId);
        if (data == null) {
            return null;
        }

        String address = null;

        if (data.getAddress().equals(EchoDeviceDatabase.LOCAL_ADDRESS)) {
            // local
            address = Echo.getSelfNode().getAddressStr();
        } else {
            // remote
            address = data.getAddress();
        }

        EchoNode ne = Echo.getNode(address);
        if (ne == null)
            return null;
        EchoObject eoj = ne.getInstance(data.getClassCode(), data.getInstanceCode());
        return eoj;
    }

    public Map<Long, ECHONETLiteDeviceData> getDeviceMap() {
        return mDeviceMap;
    }
}
