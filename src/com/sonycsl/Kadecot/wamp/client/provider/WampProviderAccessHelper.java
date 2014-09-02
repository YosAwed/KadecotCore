/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp.client.provider;

import com.sonycsl.Kadecot.provider.KadecotCoreStore;

import org.json.JSONException;
import org.json.JSONObject;

public class WampProviderAccessHelper {

    private static final String PREFIX = "com.sonycsl.kadecot.provider";

    public static enum Topic {
        DEVICE("device"),
        START("start"),
        STOP("stop");

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
        PUT_TOPICS("putTopics"),
        REMOVE_TOPICS("removeTopics"),
        GET_TOPIC_LIST("getTopicList"),
        PUT_PROCEDURES("putProcedures"),
        REMOVE_PROCEDURES("removeProcedures"),
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
            String description, boolean status, String ipAddr) throws JSONException {
        JSONObject info = new JSONObject();
        info.put(KadecotCoreStore.Devices.DeviceColumns.PROTOCOL, protocol);
        info.put(KadecotCoreStore.Devices.DeviceColumns.UUID, uuid);
        info.put(KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE, deviceType);
        info.put(KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION, description);
        info.put(KadecotCoreStore.Devices.DeviceColumns.STATUS, status);
        info.put(KadecotCoreStore.Devices.DeviceColumns.IP_ADDR, ipAddr);
        return info;
    }

}
