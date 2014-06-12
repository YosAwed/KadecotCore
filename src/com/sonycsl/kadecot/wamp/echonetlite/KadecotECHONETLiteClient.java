/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.wamp.echonetlite;

import android.content.Context;
import android.util.Log;

import com.sonycsl.kadecot.device.AccessException;
import com.sonycsl.kadecot.device.DeviceProperty;
import com.sonycsl.kadecot.device.echo.EchoDeviceData;
import com.sonycsl.kadecot.device.echo.EchoDiscovery;
import com.sonycsl.kadecot.device.echo.EchoManager;
import com.sonycsl.kadecot.wamp.KadecotWampClient;
import com.sonycsl.kadecot.wamp.KadecotWampTopic;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampInvocationMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampCallee;
import com.sonycsl.wamp.role.WampPublisher;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.role.WampSubscriber;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KadecotECHONETLiteClient extends KadecotWampClient {

    private final String TAG = KadecotECHONETLiteClient.class.getSimpleName();

    private ECHONETLiteWampCallee mCallee;
    private ECHONETLiteWampSubscriber mSubscriber;
    private EchoManager mManager;

    public KadecotECHONETLiteClient(Context context) {
        super();

        EchoManager.EchoDevicePropertyChangedListener pListener = createPropetyChangedListener();
        EchoDiscovery.OnEchoDeviceInfoListener dListener = createDeviceInfoListener();
        mManager = EchoManager.getInstance(context);
        mManager.setListener(pListener, dListener);
    }

    private EchoManager.EchoDevicePropertyChangedListener createPropetyChangedListener() {
        return new EchoManager.EchoDevicePropertyChangedListener() {
            @Override
            public void OnPropertyChanged(EchoDeviceData data, List<DeviceProperty> list) {
                publishOnPropertyChanged(data, list);
            }
        };

    }

    private EchoDiscovery.OnEchoDeviceInfoListener createDeviceInfoListener() {
        return new EchoDiscovery.OnEchoDeviceInfoListener() {
            @Override
            public void onDeviceStateChanged(EchoDeviceData deviceInfo) {
                publishDeviceInfo(deviceInfo);
            }

            @Override
            public void onDeviceAdded(EchoDeviceData deviceInfo) {
                publishDeviceInfo(deviceInfo);
            }
        };
    }

    @Override
    protected Set<WampRole> getClientRoleSet() {
        WampRole publisher = new WampPublisher();
        mSubscriber = new ECHONETLiteWampSubscriber();
        mCallee = new ECHONETLiteWampCallee();

        Set<WampRole> roleSet = new HashSet<WampRole>();
        roleSet.add(publisher);
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
        for (KadecotECHONETLiteProcedure procedure : KadecotECHONETLiteProcedure.values()) {
            procs.add(procedure.toString());
        }
        return procs;
    }

    @Override
    protected void onReceived(WampMessage msg) {
        Log.d(TAG, "OnReceived : " + msg.toString());
        if (msg.isWelcomeMessage()) {
            // TODO : remove this comment out and stop comment out after remove
            // DeviceManager.
            // startDiscovery();
        }

        if (msg.isGoodbyeMessage()) {
            // TODO : remove this comment out and stop comment out after remove
            // DeviceManager.
            // stopDiscovery();
        }
    }

    public void startDiscovery() {
        mManager.start();
    }

    public void stopDiscovery() {
        mManager.stop();
    }

    protected void publishDeviceInfo(EchoDeviceData deviceInfo) {
        Log.i(TAG, "publish deviceinfo : " + deviceInfo.nickname);
    }

    protected void publishOnPropertyChanged(EchoDeviceData data, List<DeviceProperty> list) {

        try {
            for (DeviceProperty dp : list) {
                JSONObject options = new JSONObject();
                options.put("nickname", data.nickname);
                /**
                 * exmaple : com.sonycsl.kadecot.echonetlite.aircon.power. TODO
                 * : now com.sonycsl.kadecot.echonetlite.80.power
                 */
                String topic = "com.sonycsl.kadecot.echonetlite." + data.echoClassCode + "."
                        + data.nickname + "." + dp.name;
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
            KadecotECHONETLiteProcedure enumProcedure = KadecotECHONETLiteProcedure
                    .getEnum(procedure);
            WampInvocationMessage invMsg = msg.asInvocationMessage();

            if (enumProcedure == KadecotECHONETLiteProcedure.NOT_PROCEDURE) {
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
            }

            return WampMessageFactory.createYield(invMsg.getRequestId(), new JSONObject(),
                    new JSONArray(), argumentKw);
        }

        private JSONObject resolveInvocationMsg(KadecotECHONETLiteProcedure procedure,
                WampInvocationMessage msg) throws JSONException, AccessException {
            String nickname = msg.getDetails().getString("nickname");
            EchoDeviceData data = mManager.getDeviceData(nickname);
            JSONObject params = msg.getArgumentsKw();
            List<DeviceProperty> response = new ArrayList<DeviceProperty>();

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
            json.put(KadecotWampECHONETLiteUtil.PROPERTY_NAME_KEY, propName);
            json.put(KadecotWampECHONETLiteUtil.PROPERTY_VALUE_KEY, val);
            Log.i(TAG, "return property : " + json.toString());
            return json;
        }

        /**
         * @param data
         * @param params [propName1, propName2, ...]
         * @return
         */
        private List<DeviceProperty> callGet(EchoDeviceData data, JSONObject params)
                throws JSONException, AccessException {
            List<DeviceProperty> propList = makePropertyList(data, params);
            return mManager.get(data.deviceId, propList);
        }

        /**
         * @param data
         * @param params [[propName1, propValue1], [propName2, propValue2], ...]
         * @return
         */
        private List<DeviceProperty> callSet(EchoDeviceData data, JSONObject params)
                throws JSONException, AccessException {
            List<DeviceProperty> propertyList = makePropertyList(data, params);
            return mManager.set(data.deviceId, propertyList);
        }

        /**
         * This method is temporary. After DeviceProtocol delete its get method,
         * this method must be deleted.
         * 
         * @param data
         * @param param {"propertyName" : name}
         * @return
         */
        private List<DeviceProperty> makePropertyList(EchoDeviceData data, JSONObject param)
                throws JSONException {
            ArrayList<DeviceProperty> propertyList = new ArrayList<DeviceProperty>();

            String propName = ECHONETLitePropertyName.translate(param
                    .getString(KadecotWampECHONETLiteUtil.PROPERTY_NAME_KEY));

            Object propValue = null;

            // for set
            if (param.has(KadecotWampECHONETLiteUtil.PROPERTY_VALUE_KEY)) {
                String paramValue = param.getString(KadecotWampECHONETLiteUtil.PROPERTY_VALUE_KEY);
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
        protected void onEvent(String topic, WampMessage msg) {
            if (topic.equals(KadecotWampTopic.TOPIC_PRIVATE_SEARCH)) {
                mManager.refreshDeviceList();
            }
        }
    }
}
