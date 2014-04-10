
package com.sonycsl.Kadecot.wamp;

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

    private Map<String, JSONObject> mDeviceMap = new ConcurrentHashMap<String, JSONObject>();

    public static final String DEVICE_LIST_PROCEDURE = "com.sonycsl.Kadecot.procedure.deviceList";

    private WampClient mClientChain;

    private int mRequestId = 0;

    private int mRegistrationId = -1;
    private int mSubscriptionId = -1;

    private CountDownLatch mWelcomeLatch = new CountDownLatch(1);
    private CountDownLatch mSubscribeLatch = new CountDownLatch(1);
    private CountDownLatch mRegisterLatch = new CountDownLatch(1);

    private CountDownLatch mGoodbyeLatch = new CountDownLatch(1);
    private CountDownLatch mUnsubscribeLatch = new CountDownLatch(1);
    private CountDownLatch mUnregisterLatch = new CountDownLatch(1);

    private class DeviceObserverWampPublisher extends WampPublisher {

        @Override
        protected void onConsumed(WampMessage msg) {
        }

    }

    private class DeviceObserverWampCallee extends WampCallee {

        public DeviceObserverWampCallee(WampClient client) {
            super(client);
        }

        @Override
        protected void onConsumed(WampMessage msg) {
            if (msg.isWelcomeMessage()) {
                mWelcomeLatch.countDown();
            }
            if (msg.isWelcomeMessage()) {
                mGoodbyeLatch.countDown();
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

    private class DeviceObserverWampSubscriber extends WampSubscriber {

        public DeviceObserverWampSubscriber(WampClient client) {
            super(client);
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

            try {
                JSONObject deviceInfo = msg.getArgumentsKw();
                String nickName = deviceInfo.getString(KadecotDeviceInfo.DEVICE_NICKNAME_KEY);

                synchronized (mDeviceMap) {
                    JSONObject cashedDevice = mDeviceMap.get(nickName);
                    if (cashedDevice == null) {
                        mDeviceMap.put(nickName, deviceInfo);
                        mClientChain.broadcast(WampMessageFactory.createPublish(++mRequestId,
                                new JSONObject(),
                                KadecotWampTopic.TOPIC_DEVICE));
                        return;
                    }

                    if (isSameState(cashedDevice, deviceInfo)) {
                        return;
                    }

                    mDeviceMap.put(nickName, deviceInfo);
                    mClientChain.broadcast(WampMessageFactory.createPublish(++mRequestId,
                            new JSONObject(),
                            KadecotWampTopic.TOPIC_DEVICE));
                }
            } catch (JSONException e) {
                throw new IllegalStateException("Illegal device message");
            }
        }
    }

    private boolean isSameState(JSONObject device1, JSONObject device2) throws JSONException {
        return device1.getInt(KadecotDeviceInfo.DEVICE_STATUS_KEY) == device2
                .getInt(KadecotDeviceInfo.DEVICE_STATUS_KEY);
    }

    public KadecotDeviceObserver(WampRouter router) {
        mClientChain = new DeviceObserverWampCallee(new DeviceObserverWampSubscriber(
                new DeviceObserverWampPublisher()));
        mClientChain.connect(router);
    }

    public void start() {
        broadcastSyncHello();
        broadcastSyncSubscribe();
        broadcastSyncRegister();
    }

    public void stop() {
        broadcastSyncUnregister();
        broadcastSyncUnsubscribe();
        broadcastSyncGoodbye();
    }

    private void broadcastSyncHello() {
        mClientChain.broadcast(WampMessageFactory.createHello("relm", new JSONObject()));
        try {
            if (!mWelcomeLatch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Router returns no Welcome message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void broadcastSyncSubscribe() {
        mClientChain.broadcast(WampMessageFactory.createSubscribe(++mRequestId, new JSONObject(),
                KadecotWampTopic.TOPIC_PRIVATE_DEVICE));
        try {
            if (!mSubscribeLatch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Router returns no Subscribed message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void broadcastSyncRegister() {
        mClientChain.broadcast(WampMessageFactory.createRegister(1, new JSONObject(),
                DEVICE_LIST_PROCEDURE));
        try {
            if (!mRegisterLatch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Router returns no Subscribed message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void broadcastSyncUnsubscribe() {
        mClientChain.broadcast(WampMessageFactory.createUnsubscribe(1, mSubscriptionId));
        try {
            if (!mUnregisterLatch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Router returns no Unsubscribed message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void broadcastSyncUnregister() {
        mClientChain.broadcast(WampMessageFactory.createUnregister(1, mRegistrationId));
        try {
            if (!mUnregisterLatch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Router returns no Unregistered message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void broadcastSyncGoodbye() {
        mClientChain.broadcast(WampMessageFactory.createGoodbye(new JSONObject(), ""));
        try {
            if (!mUnregisterLatch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Router returns no Goodbye message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        mDeviceMap.clear();
        mDeviceMap = null;
    }

}
