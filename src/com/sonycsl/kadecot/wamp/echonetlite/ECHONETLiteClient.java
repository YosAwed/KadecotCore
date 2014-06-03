/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.wamp.echonetlite;

import android.content.Context;
import android.util.Log;

import com.sonycsl.echo.Echo;
import com.sonycsl.echo.eoj.EchoObject;
import com.sonycsl.echo.node.EchoNode;
import com.sonycsl.kadecot.database.KadecotDAO;
import com.sonycsl.kadecot.device.AccessException;
import com.sonycsl.kadecot.device.DeviceProperty;
import com.sonycsl.kadecot.device.echo.EchoDeviceDatabase;
import com.sonycsl.kadecot.wamp.KadecotProperty;
import com.sonycsl.kadecot.wamp.KadecotProviderWampClient;
import com.sonycsl.kadecot.wamp.KadecotWampClient;
import com.sonycsl.kadecot.wamp.KadecotWampTopic;
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
import java.util.Set;

public class ECHONETLiteClient extends KadecotWampClient {

    private final String TAG = ECHONETLiteClient.class.getSimpleName();

    private final String LOCALE = "jp";

    private ECHONETLiteWampCallee mCallee;

    private ECHONETLiteWampSubscriber mSubscriber;

    private ECHONETLiteManager mManager;

    // <deviceId, deviceData>
    private Map<Long, ECHONETLiteDeviceData> mDeviceMap;

    private Map<String, JSONObject> mTemporaryDeviceMap;

    public ECHONETLiteClient(Context context) {
        super();

        mDeviceMap = new HashMap<Long, ECHONETLiteDeviceData>();
        mTemporaryDeviceMap = new HashMap<String, JSONObject>();

        ECHONETLiteManager.ECHONETLiteWampDevicePropertyChangedListener pListener = createPropetyChangedListener();
        ECHONETLiteDiscovery.OnEchoDeviceInfoListener dListener = createDeviceInfoListener();
        mManager = ECHONETLiteManager.getInstance();
        mManager.setClient(this);
        mManager.setListener(pListener, dListener);
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
        mSubscriber = new ECHONETLiteWampSubscriber();
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
    }

    @Override
    public Set<String> getSubscribableTopics() {
        Set<String> topics = new HashSet<String>();
        topics.add(KadecotWampTopic.TOPIC_PRIVATE_SEARCH);
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

    @Override
    protected void onReceived(WampMessage msg) {
        Log.d(TAG, "OnReceived : " + msg.toString());
        if (msg.isWelcomeMessage()) {
            mManager.start();
        }

        if (msg.isGoodbyeMessage()) {
            mManager.stop();
        }

        if (msg.isResultMessage()) {
            WampResultMessage result = msg.asResultMessage();
            if (!result.hasArgumentsKw()) {
                return;
            }
            JSONObject device = result.getArgumentKw();
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

    protected void putDeviceInfo(JSONObject data) {
        Log.i(TAG, "publish deviceinfo : " + data.toString());
        try {
            mTemporaryDeviceMap.put(data.getString(KadecotDAO.DEVICE_UUID), data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        transmit(WampMessageFactory.createCall(WampRequestIdGenerator.getId(),
                new JSONObject(), KadecotProviderWampClient.Procedure.PUT_DEVICE.getUri(),
                new JSONArray(), data));
    }

    protected void publishOnPropertyChanged(ECHONETLiteDeviceData data, List<DeviceProperty> list) {

        try {
            for (DeviceProperty dp : list) {
                JSONObject options = new JSONObject();
                options.put("nickname", data.getNickname());
                /**
                 * exmaple : com.sonycsl.kadecot.echonetlite.aircon.power. TODO
                 * : now com.sonycsl.kadecot.echonetlite.80.power
                 */
                String topic = "com.sonycsl.kadecot.echonetlite." + data.getClassCode() + "."
                        + data.getNickname() + "." + dp.name;
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
            long deviceId = msg.getDetails().getLong(KadecotDAO.DEVICE_ID);
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
            String propName = ECHONETLitePropertyName.translate(dp.name);
            int val = ((JSONArray) dp.value).getInt(0);
            json.put(KadecotProperty.PROPERTY_NAME_KEY, propName);
            json.put(KadecotProperty.PROPERTY_VALUE_KEY, val);
            Log.i(TAG, "return property : " + json.toString());
            return json;
        }

        /**
         * @param data
         * @param params [propName1, propName2, ...]
         * @return
         */
        private List<DeviceProperty> callGet(ECHONETLiteDeviceData data, JSONObject params)
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
         * This method is temporary. After DeviceProtocol delete its get method,
         * this method must be deleted.
         * 
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

    private class ECHONETLiteWampSubscriber extends WampSubscriber {

        @Override
        protected void event(String topic, WampMessage msg) {
            if (topic.equals(KadecotWampTopic.TOPIC_PRIVATE_SEARCH)) {
                mManager.refreshDeviceList();
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
