
package com.sonycsl.Kadecot.wamp;

import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.WampError;
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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KadecotDeviceObserver extends WampClient {

    public static final String DEVICE_LIST_PROCEDURE = "com.sonycsl.Kadecot.procedure.deviceList";

    private int mRequestId = 0;
    private DeviceObserverWampCallee mCallee;
    private DeviceObserverWampSubscriber mSubscriber;
    private int mSubscriptionId;
    private int mRegistrationId;

    public KadecotDeviceObserver() {
        super();
    }

    private void publishDeviceInfo(JSONObject deviceInfo) {
        transmit(WampMessageFactory.
                createPublish(++mRequestId, new JSONObject(),
                        KadecotWampTopic.TOPIC_DEVICE, new JSONArray(), deviceInfo));
    }

    @Override
    protected WampRole getClientRole() {
        WampRole publisher = new WampPublisher();
        mSubscriber = new DeviceObserverWampSubscriber(publisher,
                new DeviceObserverWampSubscriber.OnDeviceInfoListener() {

                    @Override
                    public void onDeviceStateChanged(JSONObject deviceInfo) {
                        publishDeviceInfo(deviceInfo);
                    }

                    @Override
                    public void onDeviceAdded(JSONObject deviceInfo) {
                        publishDeviceInfo(deviceInfo);
                    }
                });
        mCallee = new DeviceObserverWampCallee(mSubscriber,
                new DeviceObserverWampCallee.OnDeviceListRequiredListener() {
                    @Override
                    public JSONArray OnDeviceListRequired() {
                        return mSubscriber.getDeviceList();
                    }
                });
        return mCallee;
    }

    @Override
    protected void onReceived(WampMessage msg) {
        if (msg.isWelcomeMessage()) {
            transmit(WampMessageFactory.createSubscribe(WampRequestIdGenerator.getId(),
                    new JSONObject(), KadecotWampTopic.TOPIC_PRIVATE_DEVICE));
            transmit(WampMessageFactory.createRegister(WampRequestIdGenerator.getId(),
                    new JSONObject(), DEVICE_LIST_PROCEDURE));
            return;
        }

        if (msg.isSubscribedMessage()) {
            mSubscriptionId = msg.asSubscribedMessage().getSubscriptionId();
            return;

        }

        if (msg.isRegisteredMessage()) {
            mRegistrationId = msg.asRegisteredMessage().getRegistrationId();
            return;
        }

        if (msg.isGoodbyeMessage()) {
            transmit(WampMessageFactory.createUnregister(WampRequestIdGenerator.getId(),
                    mRegistrationId));
            transmit(WampMessageFactory.createUnsubscribe(WampRequestIdGenerator.getId(),
                    mSubscriptionId));
            return;
        }
    }

    private static class DeviceObserverWampCallee extends WampCallee {

        public interface OnDeviceListRequiredListener {
            public JSONArray OnDeviceListRequired();
        }

        private final OnDeviceListRequiredListener mListener;

        public DeviceObserverWampCallee(WampRole next, OnDeviceListRequiredListener listener) {
            super(next);
            mListener = listener;
        }

        @Override
        protected WampMessage invocation(String procedure, WampMessage msg) {
            if (!procedure.equals(DEVICE_LIST_PROCEDURE)) {
                return WampMessageFactory.createError(msg.getMessageType(), -1, new JSONObject(),
                        WampError.NO_SUCH_PROCEDURE);
            }

            return WampMessageFactory.createYield(msg.asInvocationMessage().getRequestId(),
                    new JSONObject(), mListener.OnDeviceListRequired());
        }
    }

    private static class DeviceObserverWampSubscriber extends WampSubscriber {

        public interface OnDeviceInfoListener {
            public void onDeviceAdded(JSONObject deviceInfo);

            public void onDeviceStateChanged(JSONObject deviceInfo);
        }

        private Map<String, JSONObject> mDeviceList = new ConcurrentHashMap<String, JSONObject>();

        private final OnDeviceInfoListener mListener;

        public DeviceObserverWampSubscriber(WampRole next, OnDeviceInfoListener listener) {
            super(next);
            mListener = listener;
        }

        public JSONArray getDeviceList() {
            Collection<JSONObject> devices;
            synchronized (mDeviceList) {
                devices = mDeviceList.values();
            }
            JSONArray deviceList = new JSONArray();
            for (JSONObject device : devices) {
                deviceList.put(device);
            }
            return deviceList;
        }

        private static String getNickName(JSONObject deviceInfo) throws JSONException {
            return deviceInfo.getString(KadecotDeviceInfo.DEVICE_NICKNAME_KEY);
        }

        private static boolean isDeviceStateChanged(JSONObject device1, JSONObject device2) {
            try {
                return device1.getInt(KadecotDeviceInfo.DEVICE_STATUS_KEY) == device2
                        .getInt(KadecotDeviceInfo.DEVICE_STATUS_KEY);
            } catch (JSONException e) {
                return false;
            }
        }

        @Override
        protected void event(String topic, WampMessage msg) {
            if (!topic.equals(KadecotWampTopic.TOPIC_PRIVATE_DEVICE)) {
                return;
            }

            if (!msg.asEventMessage().hasArgumentsKw()) {
                throw new IllegalArgumentException("Illegal device message");
            }
            JSONObject deviceInfo = msg.asEventMessage().getArgumentsKw();
            String nickName;
            try {
                nickName = getNickName(deviceInfo);
            } catch (JSONException e) {
                return;
            }

            boolean deviceAdded = false;
            synchronized (mDeviceList) {
                if (!mDeviceList.containsKey(nickName)) {
                    mDeviceList.put(nickName, deviceInfo);
                    deviceAdded = true;
                }
            }
            if (deviceAdded) {
                mListener.onDeviceAdded(deviceInfo);
                return;
            }

            boolean deviceStateChanged = false;
            synchronized (mDeviceList) {
                deviceStateChanged = isDeviceStateChanged(mDeviceList.get(nickName), deviceInfo);
                if (deviceStateChanged) {
                    mDeviceList.put(nickName, deviceInfo);
                }
            }
            if (deviceStateChanged) {
                mListener.onDeviceStateChanged(deviceInfo);
            }
        }
    }

}
