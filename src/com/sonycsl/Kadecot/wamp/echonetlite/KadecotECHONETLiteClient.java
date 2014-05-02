
package com.sonycsl.Kadecot.wamp.echonetlite;

import android.content.Context;
import android.util.Log;

import com.sonycsl.Kadecot.device.AccessException;
import com.sonycsl.Kadecot.device.DeviceProperty;
import com.sonycsl.Kadecot.device.echo.EchoDeviceData;
import com.sonycsl.Kadecot.device.echo.EchoDiscovery;
import com.sonycsl.Kadecot.device.echo.EchoManager;
import com.sonycsl.Kadecot.wamp.KadecotWampTopic;
import com.sonycsl.wamp.WampClient;
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

public class KadecotECHONETLiteClient extends WampClient {

    private final String TAG = KadecotECHONETLiteClient.class.getSimpleName();

    private int mSubscriptionId;
    private int mRegistrationId;

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
    protected void OnConnected(WampPeer peer) {
    }

    @Override
    protected void OnTransmitted(WampPeer peer, WampMessage msg) {
    }

    @Override
    protected void OnReceived(WampMessage msg) {
        if (msg.isWelcomeMessage()) {
            transmit(WampMessageFactory.createSubscribe(WampRequestIdGenerator.getId(),
                    new JSONObject(), KadecotWampTopic.TOPIC_PRIVATE_SEARCH));

            for (KadecotECHONETLiteProcedure procedure : KadecotECHONETLiteProcedure.values()) {
                transmit(WampMessageFactory.createRegister(WampRequestIdGenerator.getId(),
                        new JSONObject(), procedure.toString()));
            }

            /**
             * remove this comment out and stop comment out after remove
             * DeviceManager.
             */
            // startDiscovery();
        } else if (msg.isSubscribedMessage()) {
            mSubscriptionId = msg.asSubscribedMessage().getSubscriptionId();
        } else if (msg.isRegisteredMessage()) {
            mRegistrationId = msg.asRegisteredMessage().getRegistrationId();
        } else if (msg.isGoodbyeMessage()) {
            transmit(WampMessageFactory.createUnregister(WampRequestIdGenerator.getId(),
                    mRegistrationId));
            transmit(WampMessageFactory.createUnsubscribe(WampRequestIdGenerator.getId(),
                    mSubscriptionId));
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
        transmit(WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                KadecotWampTopic.TOPIC_PRIVATE_DEVICE, new JSONArray(),
                deviceInfo.toJSONObject()));
    }

    protected void publishOnPropertyChanged(EchoDeviceData data, List<DeviceProperty> list) {

        try {
            for (DeviceProperty dp : list) {
                JSONObject options = new JSONObject();
                options.put("nickname", data.nickname);
                /**
                 * exmaple : com.sonycsl.Kadecot.echonetlite.aircon.power. TODO
                 * : now com.sonycsl.kadecot.echonetlite.80.power
                 */
                String topic = "com.sonycsl.Kadecot.echonetlite." + data.echoClassCode + "."
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

            JSONArray arguments = new JSONArray();
            try {
                arguments = resolveInvocationMsg(enumProcedure, invMsg);
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
                    arguments);
        }

        private JSONArray resolveInvocationMsg(KadecotECHONETLiteProcedure procedure,
                WampInvocationMessage msg) throws JSONException, AccessException {
            String nickname = msg.getDetails().getString("nickname");
            EchoDeviceData data = mManager.getDeviceData(nickname);
            JSONArray params = msg.getArguments();
            List<DeviceProperty> response = new ArrayList<DeviceProperty>();

            switch (procedure) {
                case GET:
                    response = callGet(data, params);
                    break;
                case SET:
                    response = callSet(data, params);
                    break;
                case DELETE_DEVICE_DATA:
                    mManager.deleteDeviceData(data.deviceId);
                    break;
                case NOT_PROCEDURE:
                    /**
                     * This is already dealed with.
                     */
            }
            return propertyListToJSONArray(response);
        }

        private JSONArray propertyListToJSONArray(List<DeviceProperty> list) throws JSONException {
            JSONArray jArray = new JSONArray();

            for (DeviceProperty dp : list) {
                JSONArray j = new JSONArray();
                j.put(dp.name);
                j.put(dp.value);
                jArray.put(j);
            }

            return jArray;
        }

        /**
         * @param data
         * @param params [propName1, propName2, ...]
         * @return
         */
        private List<DeviceProperty> callGet(EchoDeviceData data, JSONArray params)
                throws JSONException, AccessException {

            /**
             * TODO: After refactoring DeviceProtocol, cancel comment out of
             * below codes and delete propList and makePropertyListForGet
             * method.
             */

            /**
             * List<String> propertyNameList = makePropertyNameList(data,
             * params); return mManager.get(data.deviceId, propertyNameList);
             */

            List<DeviceProperty> propList = makePropertyListForGet(data, params);
            return mManager.get(data.deviceId, propList);
        }

        /**
         * @param data
         * @param params [[propName1, propValue1], [propName2, propValue2], ...]
         * @return
         */
        private List<DeviceProperty> callSet(EchoDeviceData data, JSONArray params)
                throws JSONException, AccessException {
            List<DeviceProperty> propertyList = makePropertyListForSet(data, params);
            return mManager.set(data.deviceId, propertyList);
        }

        /**
         * @param data
         * @param params [propName1, propName2, ...]
         * @return
         */
        private List<String> makePropertyNameList(EchoDeviceData data, JSONArray params)
                throws JSONException {
            ArrayList<String> propertyList = new ArrayList<String>();

            // make propertyList
            for (int i = 0; i < params.length(); i++) {
                propertyList.add(params.getString(i));
            }

            return propertyList;
        }

        /**
         * This method is temporary. After DeviceProtocol delete its get method,
         * this method must be deleted.
         * 
         * @param data
         * @param params [propName1, propName2, ...]
         * @return
         */
        private List<DeviceProperty> makePropertyListForGet(EchoDeviceData data, JSONArray params)
                throws JSONException {
            ArrayList<DeviceProperty> propertyList = new ArrayList<DeviceProperty>();

            // make propertyList
            for (int i = 0; i < params.length(); i++) {
                DeviceProperty dp = new DeviceProperty(params.getString(i), null);
                propertyList.add(dp);
            }

            return propertyList;
        }

        /**
         * @param data
         * @param params [[propName1, propValue1], [propName2, propValue2], ...]
         * @return
         */
        private List<DeviceProperty> makePropertyListForSet(EchoDeviceData data, JSONArray params)
                throws JSONException {
            ArrayList<DeviceProperty> propertyList = new ArrayList<DeviceProperty>();

            // make propertyList
            for (int i = 0; i < params.length(); i++) {
                JSONArray propElemArray = params.getJSONArray(i);
                DeviceProperty dp = new DeviceProperty(propElemArray.getString(0),
                        propElemArray.get(1));
                propertyList.add(dp);
            }

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
}
