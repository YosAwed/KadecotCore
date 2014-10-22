
package com.sonycsl.Kadecot.plugin;

import android.util.Log;
import android.util.SparseArray;

import com.sonycsl.Kadecot.plugin.KadecotProtocolSubscriber.EventListener;
import com.sonycsl.Kadecot.plugin.KadecotProtocolSubscriber.TopicSubscriptionListener;
import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.wamp.KadecotWampTopic;
import com.sonycsl.Kadecot.wamp.client.KadecotWampClient;
import com.sonycsl.Kadecot.wamp.client.provider.WampProviderAccessHelper;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampCallMessage;
import com.sonycsl.wamp.message.WampErrorMessage;
import com.sonycsl.wamp.message.WampInvocationMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.WampResultMessage;
import com.sonycsl.wamp.role.WampCallee;
import com.sonycsl.wamp.role.WampCallee.WampInvocationReplyListener;
import com.sonycsl.wamp.role.WampCaller;
import com.sonycsl.wamp.role.WampPublisher;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class KadecotProtocolClient extends KadecotWampClient implements
        KadecotProtocolSubscriber.ProtocolSearchEventListener {

    private static final String TAG = KadecotProtocolClient.class.getSimpleName();

    private static final String DETAILS_CAUSE = "cause";
    private static final String DETAILS_NO_SUCH_DEVICE = "No such device";

    private WampCallee mCallee;
    private WampPublisher mPublisher;
    private WampCaller mCaller;
    private Collection<Integer> mInitRequestIdStore;

    private InitializeListener mInitListener;
    private CountDownLatch mInitLatch = null;

    private KadecotProtocolSubscriber mSubscriber;

    private ConcurrentHashMap<Long, String> mDeviceIdUuidMap;

    private SparseArray<JSONObject> mReqIdDeviceMap;

    private DeviceRegistrationListener mDeviceRegListener;

    private static final Collection<String> sSystemTopics = Collections
            .unmodifiableCollection(new HashSet<String>() {
                /**
                 * 
                 */
                private static final long serialVersionUID = 1L;

                {
                    add(KadecotWampTopic.TOPIC_PRIVATE_SEARCH);
                    add(WampProviderAccessHelper.Topic.START.getUri());
                    add(WampProviderAccessHelper.Topic.STOP.getUri());
                }
            });

    public interface InitializeListener {
        public void onInitialized();

        public void onError();
    }

    public interface DeviceRegistrationListener {
        public void onRegistered(long deviceId, String uuid);
    }

    public KadecotProtocolClient() {
        mReqIdDeviceMap = new SparseArray<JSONObject>();
        mDeviceIdUuidMap = new ConcurrentHashMap<Long, String>();
        mCallee = new WampCallee() {

            @Override
            protected void invocation(String procedure, WampMessage msg,
                    WampInvocationReplyListener listener) {
                WampInvocationMessage invocMsg = msg.asInvocationMessage();

                try {
                    long deviceId = invocMsg.getDetails().getLong(
                            KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID);

                    String uuid = mDeviceIdUuidMap.get(deviceId);
                    if (uuid == null) {
                        listener.replyError(WampMessageFactory.createError(
                                WampMessageType.INVOCATION,
                                invocMsg.getRequestId(),
                                new JSONObject().put(DETAILS_CAUSE, DETAILS_NO_SUCH_DEVICE),
                                WampError.INVALID_ARGUMENT).asErrorMessage());
                        return;
                    }

                    onInvocation(invocMsg.getRequestId(), procedure, uuid,
                            invocMsg.getArgumentsKw(), listener);
                } catch (JSONException e) {
                    listener.replyError(WampMessageFactory.createError(msg.getMessageType(),
                            invocMsg.getRequestId(), new JSONObject(), WampError.INVALID_ARGUMENT)
                            .asErrorMessage());
                }
            }
        };
        mSubscriber = new KadecotProtocolSubscriber(this);
        mPublisher = new WampPublisher();
        mCaller = new WampCaller();

        mInitRequestIdStore = new HashSet<Integer>();
    }

    public void setInitializeListener(InitializeListener listener) {
        mInitListener = listener;
    }

    public void setDeviceRegistrationListener(DeviceRegistrationListener listener) {
        mDeviceRegListener = listener;
    }

    public void setSubscribeListener(TopicSubscriptionListener listener) {
        mSubscriber.setSubscribeListener(listener);
    }

    public void setEventListener(EventListener listener) {
        mSubscriber.setEventListener(listener);
    }

    protected abstract void onInvocation(int requestId, String procedure, String uuid,
            JSONObject argumentsKw, WampInvocationReplyListener listener);

    @Override
    protected final Set<WampRole> getClientRoleSet() {
        Set<WampRole> roleSet = new HashSet<WampRole>();
        roleSet.add(mCaller);
        roleSet.add(mCallee);
        roleSet.add(mSubscriber);
        roleSet.add(mPublisher);

        return roleSet;
    }

    @Override
    protected final void preTransmitted(WampPeer transmitter, WampMessage msg) {
        if (msg.isCallMessage()) {
            if (isPutDeviceCall(msg.asCallMessage())) {
                WampCallMessage callMsg = msg.asCallMessage();
                mReqIdDeviceMap.put(msg.asCallMessage().getRequestId(),
                        callMsg.getArgumentsKw());
            }
        }
    }

    @Override
    protected final void postTransmitted(WampPeer peer, WampMessage msg) {
    }

    @Override
    protected final void onConnected(WampPeer peer) {
    }

    @Override
    protected final void onReceived(WampMessage msg) {
        if (msg.isWelcomeMessage()) {
            mInitRequestIdStore.clear();
            mInitLatch = new CountDownLatch(sSystemTopics.size() + getTopicsToSubscribe().size()
                    + getRegisterableProcedures().size());

            for (String topic : sSystemTopics) {
                final int requestId = WampRequestIdGenerator.getId();
                mInitRequestIdStore.add(requestId);
                transmit(WampMessageFactory.createSubscribe(requestId, new JSONObject(), topic));
            }
            for (String topic : getTopicsToSubscribe()) {
                final int requestId = WampRequestIdGenerator.getId();
                mInitRequestIdStore.add(requestId);
                transmit(WampMessageFactory.createSubscribe(requestId, new JSONObject(), topic));
            }
            for (String procedure : getRegisterableProcedures().keySet()) {
                final int requestId = WampRequestIdGenerator.getId();
                mInitRequestIdStore.add(requestId);
                transmit(WampMessageFactory.createRegister(requestId, new JSONObject(), procedure));
            }
            checkOnComplete();
            return;
        }

        if (msg.isSubscribedMessage()) {
            if (mInitRequestIdStore.contains(msg.asSubscribedMessage().getRequestId())) {
                mInitLatch.countDown();
            }
            return;
        }

        if (msg.isRegisteredMessage()) {
            if (mInitRequestIdStore.contains(msg.asRegisteredMessage().getRequestId())) {
                mInitLatch.countDown();
            }
            return;
        }

        /**
         * Handle already REGISTERED message.
         */
        if (msg.isErrorMessage()) {
            WampErrorMessage errorMsg = msg.asErrorMessage();
            if (mInitRequestIdStore.contains(errorMsg.getRequestId())) {
                if (errorMsg.getRequestType() == WampMessageType.REGISTER) {
                    if (WampError.PROCEDURE_ALREADY_EXISTS.equals(errorMsg.getUri())) {
                        mInitLatch.countDown();
                    }
                }
            }
            return;
        }

        if (msg.isResultMessage()) {
            if (!isPutDeviceResult(msg.asResultMessage())) {
                return;
            }

            WampResultMessage resMsg = msg.asResultMessage();
            JSONObject device = mReqIdDeviceMap.get(resMsg.getRequestId());
            if (device == null) {
                Log.e(TAG, "Can not get device info. msg=" + msg);
                return;
            }
            mReqIdDeviceMap.remove(resMsg.getRequestId());

            JSONObject argsKw = resMsg.getArgumentsKw();
            try {
                String uuid = argsKw.getString(KadecotCoreStore.Devices.DeviceColumns.UUID);
                long deviceId = argsKw
                        .getLong(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID);

                mDeviceIdUuidMap.put(deviceId, uuid);
                if (mDeviceRegListener != null) {
                    mDeviceRegListener.onRegistered(deviceId, uuid);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Never happens. no deviceId or no uuid.");
            }
        }
    }

    private static boolean isPutDeviceCall(WampCallMessage callMsg) {
        if (WampProviderAccessHelper.Procedure.PUT_DEVICE.getUri().equals(
                callMsg.getProcedure())) {
            return true;
        }
        return false;
    }

    private boolean isPutDeviceResult(WampResultMessage resultMsg) {
        JSONObject devMap = mReqIdDeviceMap.get(resultMsg.getRequestId());
        if (devMap == null) {
            return false;
        }
        if (resultMsg.hasArgumentsKw()) {
            return true;
        }

        return false;
    }

    private void checkOnComplete() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    if (mInitLatch.await(10, TimeUnit.SECONDS)) {
                        if (mInitListener != null) {
                            mInitListener.onInitialized();
                        }
                        return;
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "Initializer thread is interrupted");
                }
                if (mInitListener != null) {
                    mInitListener.onError();
                }
            }
        }).start();
    }

    /**
     * CALL 'com.sonycsl.kadecot.provider.procedure.putDevice' <br>
     * to register a device information into kadecot. <br>
     * 
     * @param device device data
     */
    public void registerDevice(DeviceData device) {
        int reqId = WampRequestIdGenerator.getId();
        try {
            transmit(WampMessageFactory.createCall(reqId, new JSONObject(),
                    WampProviderAccessHelper.Procedure.PUT_DEVICE.getUri(),
                    new JSONArray(), WampProviderAccessHelper.createPutDeviceArgsKw(
                            device.getProtocol(), device.getUuid(),
                            device.getDeviceType(), device.getDescription(), device.getStatus(),
                            device.getNetworkAddress())));
        } catch (JSONException e) {
            throw new IllegalArgumentException("Invalid arguments");
        }
    }

    public void sendPublish(String uuid, String topic, JSONArray arguments, JSONObject argumentsKw) {
        if (uuid == null) {
            throw new IllegalArgumentException("sendPublish, but uuid is null.");
        }

        long deviceId = getDeviceId(uuid);

        JSONObject options;
        try {
            options = new JSONObject().put(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID,
                    deviceId);
        } catch (JSONException e) {
            /**
             * Never happens.
             */
            throw new IllegalStateException();
        }

        transmit(WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), options, topic,
                arguments, argumentsKw));
    }

    /**
     * Check the device which is registered or not.
     * 
     * @param uuid Uuid which was used for
     *            {@link #registerDevice(String protocol, String uuid, String deviceType, String description, boolean status, String ipAddr)}
     * @return true if the uuid is registered, false otherwise.
     */
    public boolean isRegistered(String uuid) {
        return mDeviceIdUuidMap.values().contains(uuid);
    }

    private long getDeviceId(String uuid) {
        for (Long key : mDeviceIdUuidMap.keySet()) {
            if (uuid.equals(mDeviceIdUuidMap.get(key))) {
                return key;
            }
        }
        throw new IllegalArgumentException("No matching deviceId. uuid:" + uuid);
    }
}
