/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.wamp;

import android.content.Context;

import com.sonycsl.kadecot.database.KadecotDAO;
import com.sonycsl.kadecot.database.KadecotDAO.OnDeviceTableUpdatedListener;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampInvocationMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.role.WampCallee;
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

public final class KadecotProviderWampClient extends KadecotWampClient {

    private static final String PREFIX = "com.sonycsl.kadecot.provider";

    public static final class Topics {

        public static final String DEVICE = PREFIX + ".topic.device";

    }

    public static final class Procedures {

        private static final String PROCEDURE_PREFIX = PREFIX + ".procedure";

        public static final String PUT_DEVICE = PROCEDURE_PREFIX + ".putDevice";

        public static final String REMOVE_DEVICE = PROCEDURE_PREFIX + ".removeDevice";

        public static final String GET_DEVICE_LIST = PROCEDURE_PREFIX + ".getDeviceList";

        public static final String CHANGE_NICKNAME = PROCEDURE_PREFIX + ".changeNickname";

        public static final String PUT_TOPIC = PROCEDURE_PREFIX + ".putTopic";

        public static final String REMOVE_TOPIC = PROCEDURE_PREFIX + ".removeTopic";

        public static final String PUT_PROCEDURE = PROCEDURE_PREFIX + ".putProcedure";

        public static final String REMOVE_PROCEDURE = PROCEDURE_PREFIX + ".removeProcedure";

        public static final String GET_TOPIC_LIST = PROCEDURE_PREFIX + ".getTopicList";

        public static final String GET_PROCEDURE_LIST = PROCEDURE_PREFIX + ".getProcedureList";

    }

    private final KadecotDAO mDao;

    public KadecotProviderWampClient(Context context) {
        super();
        mDao = new KadecotDAO(context);
    }

    @Override
    protected Set<WampRole> getClientRoleSet() {
        Set<WampRole> roles = new HashSet<WampRole>();
        roles.add(new WampPublisher());
        roles.add(new ProviderCallee(mDao));

        return roles;
    }

    @Override
    protected void onConnected(WampPeer peer) {
    }

    @Override
    protected void onTransmitted(WampPeer peer, WampMessage msg) {
    }

    @Override
    protected void onReceived(WampMessage msg) {
        if (msg.isWelcomeMessage()) {
            mDao.setOnDeviceTableUpdatedListener(new OnDeviceTableUpdatedListener() {

                @Override
                public void onDeviceStateChanged(JSONObject deviceInfo) {
                    publishDevice(deviceInfo);
                }

                @Override
                public void onDeviceAdded(JSONObject deviceInfo) {
                    publishDevice(deviceInfo);
                }
            });
        }
    }

    @Override
    public Set<String> getSubscribableTopics() {
        Set<String> topics = new HashSet<String>();
        return topics;
    }

    @Override
    public Set<String> getRegisterableProcedures() {
        Set<String> procs = new HashSet<String>();
        procs.add(Procedures.PUT_DEVICE);
        procs.add(Procedures.REMOVE_DEVICE);
        procs.add(Procedures.GET_DEVICE_LIST);
        procs.add(Procedures.CHANGE_NICKNAME);
        procs.add(Procedures.PUT_TOPIC);
        procs.add(Procedures.REMOVE_TOPIC);
        procs.add(Procedures.PUT_PROCEDURE);
        procs.add(Procedures.REMOVE_PROCEDURE);
        procs.add(Procedures.GET_TOPIC_LIST);
        procs.add(Procedures.GET_PROCEDURE_LIST);
        return procs;
    }

    private void publishDevice(JSONObject deviceInfo) {
        transmit(WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                Topics.DEVICE, new JSONArray(),
                deviceInfo));
    }

    private static class ProviderCallee extends WampCallee {

        private interface InvocationMethod {
            WampMessage invoke(WampInvocationMessage msg);
        }

        private KadecotDAO mDao;
        private Map<String, InvocationMethod> mMethods;

        public ProviderCallee(KadecotDAO dao) {
            mDao = dao;
            mMethods = new HashMap<String, KadecotProviderWampClient.ProviderCallee.InvocationMethod>();
            mMethods.put(KadecotProviderWampClient.Procedures.PUT_DEVICE, new InvocationMethod() {
                @Override
                public WampMessage invoke(WampInvocationMessage msg) {
                    return putDevice(msg);
                }
            });
            mMethods.put(KadecotProviderWampClient.Procedures.REMOVE_DEVICE,
                    new InvocationMethod() {
                        @Override
                        public WampMessage invoke(WampInvocationMessage msg) {
                            return removeDevice(msg);
                        }
                    });
            mMethods.put(KadecotProviderWampClient.Procedures.GET_DEVICE_LIST,
                    new InvocationMethod() {
                        @Override
                        public WampMessage invoke(WampInvocationMessage msg) {
                            return getDeviceList(msg);
                        }
                    });
            mMethods.put(KadecotProviderWampClient.Procedures.CHANGE_NICKNAME,
                    new InvocationMethod() {
                        @Override
                        public WampMessage invoke(WampInvocationMessage msg) {
                            return changeNickname(msg);
                        }
                    });
            mMethods.put(KadecotProviderWampClient.Procedures.PUT_TOPIC,
                    new InvocationMethod() {
                        @Override
                        public WampMessage invoke(WampInvocationMessage msg) {
                            return putTopic(msg);
                        }
                    });
            mMethods.put(KadecotProviderWampClient.Procedures.REMOVE_TOPIC,
                    new InvocationMethod() {
                        @Override
                        public WampMessage invoke(WampInvocationMessage msg) {
                            return removeTopic(msg);
                        }
                    });
            mMethods.put(KadecotProviderWampClient.Procedures.PUT_PROCEDURE,
                    new InvocationMethod() {
                        @Override
                        public WampMessage invoke(WampInvocationMessage msg) {
                            return putProcedure(msg);
                        }
                    });
            mMethods.put(KadecotProviderWampClient.Procedures.REMOVE_PROCEDURE,
                    new InvocationMethod() {
                        @Override
                        public WampMessage invoke(WampInvocationMessage msg) {
                            return removeProcedure(msg);
                        }
                    });
            mMethods.put(KadecotProviderWampClient.Procedures.GET_TOPIC_LIST,
                    new InvocationMethod() {
                        @Override
                        public WampMessage invoke(WampInvocationMessage msg) {
                            return getTopicList(msg);
                        }
                    });
            mMethods.put(KadecotProviderWampClient.Procedures.GET_PROCEDURE_LIST,
                    new InvocationMethod() {
                        @Override
                        public WampMessage invoke(WampInvocationMessage msg) {
                            return getProcedureList(msg);
                        }
                    });
        }

        @Override
        protected WampMessage invocation(String procedure, WampMessage msg) {

            if (!msg.isInvocationMessage()) {
                throw new IllegalArgumentException();
            }

            WampInvocationMessage iMsg = msg.asInvocationMessage();
            InvocationMethod method = mMethods.get(procedure);
            if (mMethods == null) {
                return WampMessageFactory.createError(msg.getMessageType(),
                        iMsg.getRequestId(), new JSONObject(), WampError.NO_SUCH_PROCEDURE);
            }

            return method.invoke(iMsg);
        }

        private WampMessage createInvocationError(WampInvocationMessage msg) {
            return WampMessageFactory.createError(WampMessageType.INVOCATION,
                    msg.getRequestId(), new JSONObject(), WampError.INVALID_ARGUMENT);
        }

        private WampMessage putDevice(WampInvocationMessage msg) {
            if (!msg.hasArgumentsKw()) {
                return createInvocationError(msg);
            }

            JSONObject json = msg.getArgumentsKw();
            long deviceId;
            try {
                String protocol = json.getString(KadecotDAO.DEVICE_PROTOCOL);
                String uuid = json.getString(KadecotDAO.DEVICE_UUID);
                String deviceType = json.getString(KadecotDAO.DEVICE_TYPE);
                String description = json.getString(KadecotDAO.DEVICE_DESCRIPTION);
                boolean status = json.getBoolean(KadecotDAO.DEVICE_STATUS);
                deviceId = mDao.putDevice(protocol, uuid, deviceType, description, status);
            } catch (JSONException e) {
                e.printStackTrace();
                return createInvocationError(msg);
            }

            try {
                return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject(),
                        new JSONArray(), new JSONObject().put(KadecotDAO.DEVICE_ID, deviceId));
            } catch (JSONException e) {
                e.printStackTrace();
                return createInvocationError(msg);
            }
        }

        private WampMessage removeDevice(WampInvocationMessage msg) {
            if (!msg.hasArgumentsKw()) {
                return createInvocationError(msg);
            }

            try {
                mDao.removeDevice(msg.getArgumentsKw().getLong(KadecotDAO.DEVICE_ID));
            } catch (JSONException e) {
                e.printStackTrace();
                return createInvocationError(msg);
            }

            return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
        }

        private WampMessage getDeviceList(WampInvocationMessage msg) {
            try {
                return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject(),
                        new JSONArray(),
                        new JSONObject().put("deviceList", mDao.getDeviceList()));
            } catch (JSONException e) {
                e.printStackTrace();
                return createInvocationError(msg);
            }
        }

        private WampMessage changeNickname(WampInvocationMessage msg) {
            if (!msg.hasArgumentsKw()) {
                return createInvocationError(msg);
            }

            try {
                JSONObject json = msg.getArgumentsKw();
                long deviceId = json.getLong(KadecotDAO.DEVICE_ID);
                String nickname = json.getString(KadecotDAO.DEVICE_NICKNAME);
                mDao.changeNickname(deviceId, nickname);
            } catch (JSONException e) {
                e.printStackTrace();
                return createInvocationError(msg);

            }

            return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
        }

        private WampMessage putTopic(WampInvocationMessage msg) {
            if (!msg.hasArgumentsKw()) {
                return createInvocationError(msg);
            }
            try {
                JSONObject json = msg.getArgumentsKw();
                String topic = json.getString(KadecotDAO.TOPIC_NAME);
                String description = json.getString(KadecotDAO.TOPIC_DESCRIPTION);
                mDao.putTopic(topic, description);
            } catch (JSONException e) {
                e.printStackTrace();
                return createInvocationError(msg);
            }

            return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
        }

        private WampMessage removeTopic(WampInvocationMessage msg) {
            if (!msg.hasArgumentsKw()) {
                return createInvocationError(msg);
            }

            try {
                JSONObject json = msg.getArgumentsKw();
                String topic = json.getString(KadecotDAO.TOPIC_NAME);
                mDao.removeTopic(topic);
            } catch (JSONException e) {
                e.printStackTrace();
                return createInvocationError(msg);
            }

            return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
        }

        private WampMessage putProcedure(WampInvocationMessage msg) {
            if (!msg.hasArgumentsKw()) {
                return createInvocationError(msg);
            }
            try {
                JSONObject json = msg.getArgumentsKw();
                String procedure = json.getString(KadecotDAO.PROCEDURE_NAME);
                String description = json.getString(KadecotDAO.PROCEDURE_DESCRIPTION);
                mDao.putProcedure(procedure, description);
            } catch (JSONException e) {
                e.printStackTrace();
                return createInvocationError(msg);
            }

            return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
        }

        private WampMessage removeProcedure(WampInvocationMessage msg) {
            if (!msg.hasArgumentsKw()) {
                return createInvocationError(msg);
            }

            try {
                JSONObject json = msg.getArgumentsKw();
                String procedure = json.getString(KadecotDAO.PROCEDURE_NAME);
                mDao.removeProcedure(procedure);
            } catch (JSONException e) {
                e.printStackTrace();
                return createInvocationError(msg);
            }

            return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
        }

        private WampMessage getTopicList(WampInvocationMessage msg) {
            if (!msg.hasArgumentsKw()) {
                return createInvocationError(msg);
            }

            try {
                return WampMessageFactory.createYield(
                        msg.getRequestId(),
                        new JSONObject(),
                        new JSONArray(),
                        new JSONObject().put(
                                "topicList",
                                mDao.getTopicList(msg.getArgumentsKw().getString(
                                        KadecotDAO.TOPIC_PROTOCOL))));
            } catch (JSONException e) {
                e.printStackTrace();
                return createInvocationError(msg);
            }
        }

        private WampMessage getProcedureList(WampInvocationMessage msg) {
            if (!msg.hasArgumentsKw()) {
                return createInvocationError(msg);
            }

            try {
                return WampMessageFactory.createYield(
                        msg.getRequestId(),
                        new JSONObject(),
                        new JSONArray(),
                        new JSONObject().put(
                                "procedureList",
                                mDao.getProcedureList(msg.getArgumentsKw().getString(
                                        KadecotDAO.PROCEDURE_PROTOCOL))));
            } catch (JSONException e) {
                e.printStackTrace();
                return createInvocationError(msg);
            }
        }
    }

}
