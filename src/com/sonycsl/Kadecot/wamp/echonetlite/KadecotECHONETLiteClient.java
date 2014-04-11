
package com.sonycsl.Kadecot.wamp.echonetlite;

import com.sonycsl.Kadecot.wamp.KadecotWampClient;
import com.sonycsl.Kadecot.wamp.KadecotWampTopic;
import com.sonycsl.wamp.WampCallee;
import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampEventMessage;
import com.sonycsl.wamp.WampInvocationMessage;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessageFactory;
import com.sonycsl.wamp.WampPublisher;
import com.sonycsl.wamp.WampSubscribedMessage;
import com.sonycsl.wamp.WampSubscriber;
import com.sonycsl.wamp.WampUnsubscribedMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotECHONETLiteClient extends KadecotWampClient {

    private final String REALM = "realm";
    private final int PROCEDURE_NUM = KadecotECHONETLiteProcedure.values().length;

    private KadecotECHONETLiteClient mInstance;

    private boolean mIsStarted = false;
    private Map<KadecotECHONETLiteProcedure, Integer> mRegistrationIdMap;
    private Map<Integer, KadecotECHONETLiteProcedure> mRegistrationRequestIdMap;
    private int mSubscriptionId;

    private WampClient mClientChain;

    private CountDownLatch mHelloLatch;
    private CountDownLatch mRegisterLatch;
    private CountDownLatch mSubscribeLatch;

    private CountDownLatch mGoodbyeLatch;
    private CountDownLatch mUnregisterLatch;
    private CountDownLatch mUnsubscribeLatch;

    private KadecotECHONETLiteClient() {
        super();
        mClientChain = new ECHONETLiteCallee(new ECHONETLitePublisher(new ECHONETLiteSubscriber()));
    }

    public KadecotECHONETLiteClient getInstance() {
        if (mInstance != null) {
            mInstance = new KadecotECHONETLiteClient();
        }

        return mInstance;
    }

    public synchronized void start() {
        if (mIsStarted) {
            return;
        }

        initialize();
        broadcastSyncHello();
        broadcastSyncRegisterAllProcedures();
        broadcastSyncSubscribe();
        mIsStarted = true;
    }

    public void stop() {
        if (!mIsStarted) {
            return;
        }

        mIsStarted = false;
        broadcastSyncUnsubscribe();
        broadcastSyncUnregisterAllProcedures();
        broadcastSyncGoodbye();
        release();
    }

    private void initialize() {
        mRegistrationIdMap.clear();
        mRegistrationRequestIdMap.clear();
        mSubscriptionId = -1;

        mHelloLatch = new CountDownLatch(1);
        mRegisterLatch = new CountDownLatch(PROCEDURE_NUM);
        mSubscribeLatch = new CountDownLatch(1);

        mGoodbyeLatch = new CountDownLatch(1);
        mUnregisterLatch = new CountDownLatch(PROCEDURE_NUM);
        mUnsubscribeLatch = new CountDownLatch(1);
    }

    private void release() {
        mRegistrationIdMap.clear();
        mRegistrationRequestIdMap.clear();
        mSubscriptionId = -1;

        mHelloLatch = null;
        mRegisterLatch = null;
        mSubscribeLatch = null;

        mGoodbyeLatch = null;
        mUnregisterLatch = null;
        mUnsubscribeLatch = null;
    }

    private void broadcastSyncHello() {
        try {
            JSONObject myRoles = new JSONObject(
                    "{\"roles\" : {\"callee\" : {}, \"publisher\" : {}, \"subscriber\" : {} } }");
            mClientChain.broadcast(WampMessageFactory.createHello(REALM, myRoles));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            if (!mHelloLatch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Router returns no Welcome message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void broadcastSyncRegisterAllProcedures() {
        KadecotECHONETLiteProcedure[] procedures = KadecotECHONETLiteProcedure.values();

        int id = 1;
        for (KadecotECHONETLiteProcedure p : procedures) {
            broadcastSyncRegister(id++, p);
        }
    }

    /**
     * @param procedure target procedure to register
     */
    private void broadcastSyncRegister(int requestId, KadecotECHONETLiteProcedure procedure) {
        mRegistrationRequestIdMap.put(requestId, procedure);

        mClientChain.broadcast(WampMessageFactory.createRegister(requestId, new JSONObject(),
                procedure.getString()));
        try {
            if (!mRegisterLatch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Router returns no Subscribed message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void broadcastSyncSubscribe() {
        mClientChain.broadcast(WampMessageFactory.createSubscribe(1, new JSONObject(),
                KadecotWampTopic.TOPIC_PRIVATE_SEARCH));
        try {
            if (!mSubscribeLatch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Router returns no Subscribed message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void broadcastSyncGoodbye() {
        mClientChain.broadcast(WampMessageFactory.createGoodbye(new JSONObject(),
                WampError.CLOSE_REALM));
        try {
            if (!mGoodbyeLatch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Router returns no Goodbye message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void broadcastSyncUnregisterAllProcedures() {
        KadecotECHONETLiteProcedure[] procedures = KadecotECHONETLiteProcedure.values();

        int id = 1;
        for (KadecotECHONETLiteProcedure p : procedures) {
            broadcastSyncUnregister(id++, p);
        }
    }

    private void broadcastSyncUnsubscribe() {
        mClientChain.broadcast(WampMessageFactory.createUnsubscribe(1, mSubscriptionId));
        try {
            if (!mUnsubscribeLatch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Router returns no Unsubscribed message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param procedure target procedure to register
     */
    private void broadcastSyncUnregister(int requestId, KadecotECHONETLiteProcedure procedure) {
        mClientChain.broadcast(WampMessageFactory.createUnregister(requestId,
                mRegistrationIdMap.get(procedure)));
        try {
            if (!mRegisterLatch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Router returns no Subscribed message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, JSONObject option) {
        WampMessage msg = WampMessageFactory.createPublish(0, option, topic);
        mClientChain.broadcast(msg);
    }

    @Override
    protected boolean consumeRoleMessage(WampMessenger friend, WampMessage msg) {

        return false;
    }

    @Override
    protected boolean consumeRoleBroadcast(WampMessage msg) {

        return false;
    }

    @Override
    protected void onConsumed(WampMessage msg) {
        if (msg.isWelcomeMessage()) {
            mHelloLatch.countDown();
        } else if (msg.isGoodbyeMessage()) {
            mGoodbyeLatch.countDown();
        } else if (msg.isRegisteredMessage()) {
            int reqId = msg.asRegisteredMessage().getRequestId();
            KadecotECHONETLiteProcedure p = mRegistrationRequestIdMap.get(reqId);
            int regId = msg.asRegisteredMessage().getRegistrationId();
            mRegistrationIdMap.put(p, regId);
            mRegisterLatch.countDown();
        } else if (msg.isUnregisteredMessage()) {
            mUnregisterLatch.countDown();
        } else if (msg.isSubscribedMessage()) {
            mSubscribeLatch.countDown();
        } else if (msg.isUnsubscribedMessage()) {
            mUnsubscribeLatch.countDown();
        }
    }

    private class ECHONETLiteCallee extends WampCallee {

        public ECHONETLiteCallee() {
            super();
        }

        public ECHONETLiteCallee(WampClient next) {
            super(next);
        }

        @Override
        protected WampMessage onInvocation(String procedure, WampInvocationMessage msg) {
            // TODO
            return null;
        }

        @Override
        protected void onConsumed(WampMessage msg) {
            if (msg.isWelcomeMessage()) {
                mHelloLatch.countDown();
            }
            if (msg.isGoodbyeMessage()) {
                mGoodbyeLatch.countDown();
            }
            if (msg.isRegisteredMessage()) {
                int reqId = msg.asRegisteredMessage().getRequestId();
                KadecotECHONETLiteProcedure p = mRegistrationRequestIdMap.get(reqId);
                int regId = msg.asRegisteredMessage().getRegistrationId();
                mRegistrationIdMap.put(p, regId);
                mRegisterLatch.countDown();
            }
            if (msg.isUnregisteredMessage()) {
                mUnregisterLatch.countDown();
            }
        }
    }

    private class ECHONETLitePublisher extends WampPublisher {

        public ECHONETLitePublisher() {
            super();
        }

        public ECHONETLitePublisher(WampClient next) {
            super(next);
        }

        @Override
        protected void onConsumed(WampMessage msg) {
            if (msg.isHelloMessage()) {
                mHelloLatch.countDown();
            } else if (msg.isGoodbyeMessage()) {
                mGoodbyeLatch.countDown();
            } else if (msg.isPublishedMessage()) {

            }
        }

    }

    private class ECHONETLiteSubscriber extends WampSubscriber {

        public ECHONETLiteSubscriber() {
            super();
        }

        public ECHONETLiteSubscriber(WampClient next) {
            super(next);
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

        /**
         * This method request only DeviceDiscovery.
         * 
         * @param msg DeviceDiscovery Request
         */
        @Override
        protected void event(WampEventMessage msg) {
            // TODO
        }

        @Override
        protected void onConsumed(WampMessage msg) {
            if (msg.isHelloMessage()) {
                mHelloLatch.countDown();
            } else if (msg.isGoodbyeMessage()) {
                mGoodbyeLatch.countDown();
            } else if (msg.isSubscribedMessage()) {
                mSubscriptionId = msg.asSubscribedMessage().getSubscriptionId();
                mSubscribeLatch.countDown();
            } else if (msg.isUnsubscribedMessage()) {
                mSubscriptionId = -1;
                mUnsubscribeLatch.countDown();
            }
        }
    }
}
