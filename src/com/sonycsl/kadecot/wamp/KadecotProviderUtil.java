
package com.sonycsl.kadecot.wamp;

import org.json.JSONException;
import org.json.JSONObject;

public class KadecotProviderUtil {
    private static final String PREFIX = "com.sonycsl.kadecot.provider";

    public static final String DEVICE_ID = "deviceId";
    public static final String DEVICE_PROTOCOL = "protocol";
    public static final String DEVICE_UUID = "uuid";
    public static final String DEVICE_TYPE = "deviceType";
    public static final String DEVICE_DESCRIPTION = "description";
    public static final String DEVICE_STATUS = "status";
    public static final String DEVICE_NICKNAME = "nickname";

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
        info.put(DEVICE_PROTOCOL, protocol);
        info.put(DEVICE_UUID, uuid);
        info.put(DEVICE_TYPE, deviceType);
        info.put(DEVICE_DESCRIPTION, description);
        info.put(DEVICE_STATUS, status);
        return info;
    }

}
