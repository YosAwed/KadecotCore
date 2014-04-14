
package com.sonycsl.Kadecot.wamp;

import android.util.Log;

import com.sonycsl.wamp.WampCallee;
import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.WampEventMessage;
import com.sonycsl.wamp.WampInvocationMessage;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessageFactory;
import com.sonycsl.wamp.WampPublisher;
import com.sonycsl.wamp.WampRouter;
import com.sonycsl.wamp.WampSubscribedMessage;
import com.sonycsl.wamp.WampSubscriber;
import com.sonycsl.wamp.WampUnsubscribedMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotDeviceObserver {

    private static final String TAG = KadecotDeviceObserver.class.getSimpleName();

    public static final String DEVICE_LIST_PROCEDURE = "com.sonycsl.Kadecot.procedure.deviceList";

    private DeviceObserverWampPublisher mPublisher;
    private DeviceObserverWampSubscriber mSubscriber;
    private DeviceObserverWampCallee mClientChain;

    private int mRequestId = 0;

    private int mRegistrationId;
    private int mSubscriptionId;

    private CountDownLatch mGoodbyeLatch;

    private boolean mIsStarted = false;

    private DeviceStateListener mDeviceStateListener = new DeviceStateListener() {

        @Override
        public void onDeviceStateChanged(JSONObject deviceInfo) {
            try {
                String nickName = deviceInfo.getString(KadecotDeviceInfo.DEVICE_NICKNAME_KEY);

                Map<String, JSONObject> deviceMap = mClientChain.getDeviceMap();

                synchronized (deviceMap) {
                    JSONObject cashedDevice = deviceMap.get(nickName);
                    if (cashedDevice == null) {
                        Log.d(TAG, "new device found: " + deviceInfo.toString());
                        deviceMap.put(nickName, deviceInfo);
                        mClientChain.putDevice(nickName, deviceInfo);
                        mClientChain.broadcast(WampMessageFactory.createPublish(++mRequestId,
                                new JSONObject(), KadecotWampTopic.TOPIC_DEVICE,
                                new JSONArray(), deviceInfo));
                        return;
                    }

                    if (isSameState(cashedDevice, deviceInfo)) {
                        return;
                    }

                    Log.d(TAG, "device state changed: " + deviceInfo.toString());
                    deviceMap.put(nickName, deviceInfo);
                    mClientChain.putDevice(nickName, deviceInfo);
                    mClientChain.broadcast(WampMessageFactory.createPublish(++mRequestId,
                            new JSONObject(), KadecotWampTopic.TOPIC_DEVICE, new JSONArray(),
                            deviceInfo));
                }
            } catch (JSONException e) {

            }

        }

        private boolean isSameState(JSONObject device1, JSONObject device2)
                throws JSONException {
            return device1.getInt(KadecotDeviceInfo.DEVICE_STATUS_KEY) == device2
                    .getInt(KadecotDeviceInfo.DEVICE_STATUS_KEY);
        }

    };

    public interface DeviceStateListener {
        void onDeviceStateChanged(JSONObject deviceInfo);
    }

    private static class DeviceObserverWampPublisher extends WampPublisher {

        @Override
        protected void onConsumed(WampMessage msg) {
        }

    }

    private static class DeviceObserverWampCallee extends WampCallee {

        private Map<String, JSONObject> mDeviceMap = new ConcurrentHashMap<String, JSONObject>();

        private CountDownLatch mHelloLatch;
        private CountDownLatch mGoodbyeLatch;
        private CountDownLatch mRegisterLatch;
        private CountDownLatch mUnregisterLatch;

        private int mRegistrationId;

        public DeviceObserverWampCallee(WampClient client) {
            super(client);
        }

        public void putDevice(String nickname, JSONObject device) {
            mDeviceMap.put(nickname, device);
        }

        public Map<String, JSONObject> getDeviceMap() {
            return mDeviceMap;
        }

        public void setHelloLatch(CountDownLatch helloLatch) {
            mHelloLatch = helloLatch;
        }

        public void setRegisterLatch(CountDownLatch registerLatch) {
            mRegisterLatch = registerLatch;
        }

        public void setGoodbyeLatch(CountDownLatch goodbyeLatch) {
            mGoodbyeLatch = goodbyeLatch;
        }

        public void setUnregisterLatch(CountDownLatch unregisterLatch) {
            mUnregisterLatch = unregisterLatch;
        }

        public int getRegistrationId() {
            return mRegistrationId;
        }

        @Override
        protected void onConsumed(WampMessage msg) {
            if (msg.isWelcomeMessage()) {
                mHelloLatch.countDown();
            }
            if (msg.isGoodbyeMessage()) {
                mGoodbyeLatch.countDown();
                // stop();
            }
            if (msg.isRegisteredMessage()) {
                mRegistrationId = msg.asRegisteredMessage().getRegistrationId();
                mRegisterLatch.countDown();
            }
            if (msg.isUnregisteredMessage()) {
                mRegistrationId = -1;
                mUnregisterLatch.countDown();
            }
        }

        @Override
        protected WampMessage onInvocation(String procedure, WampInvocationMessage msg) {
            JSONArray deviceList = new JSONArray();
            for (JSONObject device : mDeviceMap.values()) {
                deviceList.put(device);
            }
            return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject(),
                    deviceList);
        }
    }

    private static class DeviceObserverWampSubscriber extends WampSubscriber {

        private DeviceStateListener mListener;

        private CountDownLatch mSubscribeLatch;
        private CountDownLatch mUnsubscribeLatch;

        private int mSubscriptionId;

        public DeviceObserverWampSubscriber(WampClient client) {
            super(client);
        }

        public void setDeviceStateListener(DeviceStateListener listener) {
            mListener = listener;
        }

        public void setSubscribeLatch(CountDownLatch subscribeLatch) {
            mSubscribeLatch = subscribeLatch;
        }

        public void setUnsubscribeLatch(CountDownLatch unsubscribeLatch) {
            mUnsubscribeLatch = unsubscribeLatch;
        }

        public int getSubscriptionId() {
            return mSubscriptionId;
        }

        @Override
        protected void onConsumed(WampMessage msg) {
        }

        @Override
        protected void subscribed(WampSubscribedMessage msg) {
            mSubscriptionId = msg.getSubscriptionId();
            mSubscribeLatch.countDown();
        }

        @Override
        protected void unsubscribed(WampUnsubscribedMessage msg) {
            mSubscriptionId = -1;
            mUnsubscribeLatch.countDown();
        }

        @Override
        protected void event(WampEventMessage msg) {
            if (!msg.hasArgumentsKw()) {
                throw new IllegalArgumentException("Illegal device message");
            }

            if (mListener != null) {
                mListener.onDeviceStateChanged(msg.getArgumentsKw());
            }
        }
    }

    public KadecotDeviceObserver(WampRouter router) {
        mPublisher = new DeviceObserverWampPublisher();
        mSubscriber = new DeviceObserverWampSubscriber(mPublisher);
        mClientChain = new DeviceObserverWampCallee(mSubscriber);

        mClientChain.connect(router);
    }

    public synchronized void start() {
        if (mIsStarted) {
            return;
        }
        initializeWamp();
        mIsStarted = true;
    }

    public synchronized void stop() {
        if (!mIsStarted) {
            return;
        }
        mIsStarted = false;
        releaseWamp();
    }

    private void initializeWamp() {
        mSubscriber.setDeviceStateListener(mDeviceStateListener);

        mGoodbyeLatch = new CountDownLatch(1);
        mClientChain.setGoodbyeLatch(mGoodbyeLatch);

        CountDownLatch helloLatch = new CountDownLatch(1);
        CountDownLatch registerLatch = new CountDownLatch(1);
        CountDownLatch subscribeLatch = new CountDownLatch(1);

        mClientChain.setHelloLatch(helloLatch);
        mSubscriber.setSubscribeLatch(subscribeLatch);
        mClientChain.setRegisterLatch(registerLatch);

        broadcastSyncHello(mClientChain, helloLatch);
        broadcastSyncSubscribe(mClientChain, subscribeLatch, ++mRequestId);
        broadcastSyncRegister(mClientChain, registerLatch, ++mRequestId);

        mSubscriptionId = mSubscriber.getSubscriptionId();
        mRegistrationId = mClientChain.getRegistrationId();
    }

    private void releaseWamp() {
        CountDownLatch unregisterLatch = new CountDownLatch(1);
        CountDownLatch unsubscribeLatch = new CountDownLatch(1);
        mClientChain.setUnregisterLatch(unregisterLatch);
        mSubscriber.setUnsubscribeLatch(unsubscribeLatch);

        broadcastSyncUnregister(mClientChain, unregisterLatch, ++mRequestId, mRegistrationId);
        broadcastSyncUnsubscribe(mClientChain, unsubscribeLatch, ++mRequestId, mSubscriptionId);
        broadcastSyncGoodbye(mClientChain, mGoodbyeLatch);

        mClientChain.getDeviceMap().clear();
        mSubscriber.setDeviceStateListener(null);
    }

    private static void broadcastSyncHello(WampClient client, CountDownLatch helloLatch) {
        client.broadcast(WampMessageFactory.createHello("realm", new JSONObject()));
        try {
            if (!helloLatch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Router returns no Welcome message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void broadcastSyncSubscribe(WampClient client, CountDownLatch subscribeLatch,
            int requestId) {
        client.broadcast(WampMessageFactory.createSubscribe(requestId, new JSONObject(),
                KadecotWampTopic.TOPIC_PRIVATE_DEVICE));
        try {
            if (!subscribeLatch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Router returns no Subscribed message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void broadcastSyncRegister(WampClient client, CountDownLatch registerLatch,
            int requestId) {
        client.broadcast(WampMessageFactory.createRegister(requestId, new JSONObject(),
                DEVICE_LIST_PROCEDURE));
        try {
            if (!registerLatch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Router returns no Subscribed message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void broadcastSyncUnsubscribe(WampClient client,
            CountDownLatch unsubscribeLatch, int requestId, int subscriptionId) {
        client.broadcast(WampMessageFactory.createUnsubscribe(requestId, subscriptionId));
        try {
            if (!unsubscribeLatch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Router returns no Unsubscribed message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void broadcastSyncUnregister(WampClient client, CountDownLatch unregisterLatch,
            int requestId, int registrationId) {
        client.broadcast(WampMessageFactory.createUnregister(requestId, registrationId));
        try {
            if (!unregisterLatch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Router returns no Unregistered message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void broadcastSyncGoodbye(WampClient client, CountDownLatch goodbyeLatch) {
        client.broadcast(WampMessageFactory.createGoodbye(new JSONObject(), ""));
        try {
            if (!goodbyeLatch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Router returns no Goodbye message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
