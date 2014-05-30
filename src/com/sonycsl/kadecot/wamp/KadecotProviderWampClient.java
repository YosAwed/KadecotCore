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

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

public final class KadecotProviderWampClient extends KadecotWampClient {

    private static final String PREFIX = "com.sonycsl.kadecot.provider";

    public static enum Topic {
        DEVICE("device");

        private final String mUri;

        Topic(String name) {
            mUri = PREFIX + ".topic." + name;
        }

        public String getUri() {
            return mUri;
        }
    }

    public static enum Procedure {
        PUT_DEVICE("putDevice"),
        REMOVE_DEVICE("removeDevice"),
        GET_DEVICE_LIST("getDeviceList"),
        CHANGE_NICKNAME("changeNickname"),
        PUT_TOPIC("putTopic"),
        REMOVE_TOPIC("removeTopic"),
        GET_TOPIC_LIST("getTopicList"),
        PUT_PROCEDURE("putProcedure"),
        REMOVE_PROCEDURE("removeProcedure"),
        GET_PROCEDURE_LIST("getProcedureList");

        private final String mMethod;
        private final String mUri;

        Procedure(String method) {
            mMethod = method;
            mUri = PREFIX + ".procedure." + method;
        }

        public String getUri() {
            return mUri;
        }

        public String getMethod() {
            return mMethod;
        }

        public static Procedure getEnum(String procedure) {
            for (Procedure p : Procedure.values()) {
                if (p.getUri().equals(procedure)) {
                    return p;
                }
            }
            return null;
        }
    }

    public static JSONObject createPutDeviceArgsKw(String protocol, String uuid, String deviceType,
            String description, boolean status) throws JSONException {
        JSONObject info = new JSONObject();
        info.put(KadecotDAO.DEVICE_PROTOCOL, protocol);
        info.put(KadecotDAO.DEVICE_UUID, uuid);
        info.put(KadecotDAO.DEVICE_TYPE, deviceType);
        info.put(KadecotDAO.DEVICE_DESCRIPTION, description);
        info.put(KadecotDAO.DEVICE_STATUS, status);
        return info;
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

        for (Procedure p : Procedure.values()) {
            procs.add(p.getUri());
        }
        return procs;
    }

    private void publishDevice(JSONObject deviceInfo) {
        transmit(WampMessageFactory.createPublish(WampRequestIdGenerator.getId(), new JSONObject(),
                Topic.DEVICE.getUri(), new JSONArray(),
                deviceInfo));
    }

    private static class ProviderCallee extends WampCallee {

        private KadecotDAO mDao;

        public ProviderCallee(KadecotDAO dao) {
            mDao = dao;
        }

        @Override
        protected WampMessage invocation(String procedure, WampMessage msg) {

            if (!msg.isInvocationMessage()) {
                throw new IllegalArgumentException();
            }

            WampInvocationMessage iMsg = msg.asInvocationMessage();
            Procedure proc = Procedure.getEnum(procedure);
            if (proc == null) {
                return createError(iMsg, WampError.NO_SUCH_PROCEDURE);
            }

            try {
                return (WampMessage) ProviderCallee.this.getClass()
                        .getDeclaredMethod(proc.getMethod(),
                                WampInvocationMessage.class).invoke(ProviderCallee.this, msg);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            return createError(iMsg, WampError.INVALID_ARGUMENT);
        }

        private WampMessage createError(WampInvocationMessage msg, String error) {
            return WampMessageFactory.createError(WampMessageType.INVOCATION,
                    msg.getRequestId(), new JSONObject(), error);
        }

        @SuppressWarnings("unused")
        private WampMessage putDevice(WampInvocationMessage msg) {
            if (!msg.hasArgumentsKw()) {
                return createError(msg, WampError.INVALID_ARGUMENT);
            }

            JSONObject json;
            try {
                json = new JSONObject(msg.getArgumentsKw().toString());
            } catch (JSONException e) {
                e.printStackTrace();
                return createError(msg, WampError.INVALID_ARGUMENT);
            }
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
                return createError(msg, WampError.INVALID_ARGUMENT);
            }

            try {
                return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject(),
                        new JSONArray(), json.put(KadecotDAO.DEVICE_ID, deviceId));
            } catch (JSONException e) {
                e.printStackTrace();
                return createError(msg, WampError.INVALID_ARGUMENT);
            }
        }

        @SuppressWarnings("unused")
        private WampMessage removeDevice(WampInvocationMessage msg) {
            if (!msg.hasArgumentsKw()) {
                return createError(msg, WampError.INVALID_ARGUMENT);
            }

            try {
                mDao.removeDevice(msg.getArgumentsKw().getLong(KadecotDAO.DEVICE_ID));
            } catch (JSONException e) {
                e.printStackTrace();
                return createError(msg, WampError.INVALID_ARGUMENT);
            }

            return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
        }

        @SuppressWarnings("unused")
        private WampMessage getDeviceList(WampInvocationMessage msg) {
            try {
                return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject(),
                        new JSONArray(),
                        new JSONObject().put("deviceList", mDao.getDeviceList()));
            } catch (JSONException e) {
                e.printStackTrace();
                return createError(msg, WampError.INVALID_ARGUMENT);
            }
        }

        @SuppressWarnings("unused")
        private WampMessage changeNickname(WampInvocationMessage msg) {
            if (!msg.hasArgumentsKw()) {
                return createError(msg, WampError.INVALID_ARGUMENT);
            }

            try {
                JSONObject json = msg.getArgumentsKw();
                long deviceId = json.getLong(KadecotDAO.DEVICE_ID);
                String nickname = json.getString(KadecotDAO.DEVICE_NICKNAME);
                mDao.changeNickname(deviceId, nickname);
            } catch (JSONException e) {
                e.printStackTrace();
                return createError(msg, WampError.INVALID_ARGUMENT);
            }

            return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
        }

        @SuppressWarnings("unused")
        private WampMessage putTopic(WampInvocationMessage msg) {
            if (!msg.hasArgumentsKw()) {
                return createError(msg, WampError.INVALID_ARGUMENT);
            }
            try {
                JSONObject json = msg.getArgumentsKw();
                String topic = json.getString(KadecotDAO.TOPIC_NAME);
                String description = json.getString(KadecotDAO.TOPIC_DESCRIPTION);
                mDao.putTopic(topic, description);
            } catch (JSONException e) {
                e.printStackTrace();
                return createError(msg, WampError.INVALID_ARGUMENT);
            }

            return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
        }

        @SuppressWarnings("unused")
        private WampMessage removeTopic(WampInvocationMessage msg) {
            if (!msg.hasArgumentsKw()) {
                return createError(msg, WampError.INVALID_ARGUMENT);
            }

            try {
                JSONObject json = msg.getArgumentsKw();
                String topic = json.getString(KadecotDAO.TOPIC_NAME);
                mDao.removeTopic(topic);
            } catch (JSONException e) {
                e.printStackTrace();
                return createError(msg, WampError.INVALID_ARGUMENT);
            }

            return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
        }

        @SuppressWarnings("unused")
        private WampMessage putProcedure(WampInvocationMessage msg) {
            if (!msg.hasArgumentsKw()) {
                return createError(msg, WampError.INVALID_ARGUMENT);
            }
            try {
                JSONObject json = msg.getArgumentsKw();
                String procedure = json.getString(KadecotDAO.PROCEDURE_NAME);
                String description = json.getString(KadecotDAO.PROCEDURE_DESCRIPTION);
                mDao.putProcedure(procedure, description);
            } catch (JSONException e) {
                e.printStackTrace();
                return createError(msg, WampError.INVALID_ARGUMENT);
            }

            return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
        }

        @SuppressWarnings("unused")
        private WampMessage removeProcedure(WampInvocationMessage msg) {
            if (!msg.hasArgumentsKw()) {
                return createError(msg, WampError.INVALID_ARGUMENT);
            }

            try {
                JSONObject json = msg.getArgumentsKw();
                String procedure = json.getString(KadecotDAO.PROCEDURE_NAME);
                mDao.removeProcedure(procedure);
            } catch (JSONException e) {
                e.printStackTrace();
                return createError(msg, WampError.INVALID_ARGUMENT);
            }

            return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
        }

        @SuppressWarnings("unused")
        private WampMessage getTopicList(WampInvocationMessage msg) {
            if (!msg.hasArgumentsKw()) {
                return createError(msg, WampError.INVALID_ARGUMENT);
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
                return createError(msg, WampError.INVALID_ARGUMENT);
            }
        }

        @SuppressWarnings("unused")
        private WampMessage getProcedureList(WampInvocationMessage msg) {
            if (!msg.hasArgumentsKw()) {
                return createError(msg, WampError.INVALID_ARGUMENT);
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
                return createError(msg, WampError.INVALID_ARGUMENT);
            }
        }
    }

}
