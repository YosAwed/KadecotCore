/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.plugin;

import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.wamp.KadecotWampTopic;
import com.sonycsl.Kadecot.wamp.client.KadecotWampClient;
import com.sonycsl.Kadecot.wamp.client.provider.WampProviderAccessHelper;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.message.WampInvocationMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.WampResultMessage;
import com.sonycsl.wamp.role.WampCallee;
import com.sonycsl.wamp.role.WampCaller;
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

public abstract class KadecotProtocolClient extends KadecotWampClient {

    private OnResultOfDeviceFoundListner mOnResultOfDeviceFoundListner = null;

    private Map<String, JSONObject> mTempDeviceMap; // Map<uuid, deviceInfo>

    public KadecotProtocolClient() {
        super();
        mTempDeviceMap = new HashMap<String, JSONObject>();
    }

    public void setOnResultOfDeviceFoundListener(OnResultOfDeviceFoundListner listener) {
        mOnResultOfDeviceFoundListner = listener;
    }

    public interface OnResultOfDeviceFoundListner {
        public void onResultOfDeviceFound(WampResultMessage resultMsg, JSONObject deviceInfoOnFound);
    }

    /**
     * if sub class override this method, must call super.onReceived(msg)
     */
    @Override
    protected void onReceived(WampMessage msg) {
        if (msg.isResultMessage()) {
            WampResultMessage resultMsg = msg.asResultMessage();

            if (resultMsg.hasArgumentsKw()) {
                if (mOnResultOfDeviceFoundListner != null) {
                    JSONObject device = resultMsg.getArgumentsKw();
                    String uuid;
                    try {
                        uuid = device.getString(KadecotCoreStore.Devices.DeviceColumns.UUID);
                        mOnResultOfDeviceFoundListner.onResultOfDeviceFound(resultMsg,
                                mTempDeviceMap.get(uuid));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected final Set<WampRole> getClientRoleSet() {
        Set<WampRole> roleSet = new HashSet<WampRole>();
        roleSet.add(getCaller());
        roleSet.add(getCallee());
        roleSet.add(getSubscriber());
        roleSet.add(getPublisher());

        return roleSet;
    }

    protected WampCaller getCaller() {
        return new WampCaller();
    }

    protected WampCallee getCallee() {
        return new KadecotProtocolCallee(new HashSet<String>()) {

            @Override
            protected WampMessage resolveInvocationMsg(String procedure,
                    WampInvocationMessage invocMsg) {
                return WampMessageFactory.createError(WampMessageType.INVOCATION,
                        invocMsg.getRequestId(), new JSONObject(), WampError.INVALID_ARGUMENT);
            }
        };
    }

    protected KadecotProtocolSubscriber getSubscriber() {
        return new KadecotProtocolSubscriber(new ProtocolSearchEventListener() {
            @Override
            public void search() {
                deviceSearch();
            }
        }, getTopicListenter());
    }

    protected WampPublisher getPublisher() {
        return new WampPublisher();
    }

    abstract protected com.sonycsl.Kadecot.plugin.KadecotProtocolSubscriber.OnTopicListener getTopicListenter();

    @Override
    public final Set<String> getTopicsToSubscribe() {
        Set<String> topics = new HashSet<String>();
        topics.add(KadecotWampTopic.TOPIC_PRIVATE_SEARCH);
        topics.add(WampProviderAccessHelper.Topic.START.getUri());
        topics.add(WampProviderAccessHelper.Topic.STOP.getUri());
        return topics;
    }

    abstract protected void deviceSearch();

    public interface ProtocolSearchEventListener {
        public void search();
    }

    /**
     * call this method when device found
     * 
     * @param deviceInfo should be made by createPutDeviceArgsKw method
     */
    public void callPutDevice(String protocol, String uuid, String deviceType,
            String description, boolean status, String ipAddr) {
        try {
            JSONObject deviceInfo = WampProviderAccessHelper.createPutDeviceArgsKw(protocol, uuid,
                    deviceType, description, status, ipAddr);
            mTempDeviceMap.put(uuid, deviceInfo);
            transmit(WampMessageFactory.createCall(WampRequestIdGenerator.getId(),
                    new JSONObject(),
                    WampProviderAccessHelper.Procedure.PUT_DEVICE.getUri(),
                    new JSONArray(),
                    deviceInfo));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
