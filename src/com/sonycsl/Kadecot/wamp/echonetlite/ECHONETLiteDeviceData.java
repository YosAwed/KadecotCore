
package com.sonycsl.Kadecot.wamp.echonetlite;

import com.sonycsl.Kadecot.wamp.KadecotDeviceData;

import org.json.JSONException;
import org.json.JSONObject;

public class ECHONETLiteDeviceData extends KadecotDeviceData {
    public static final String ADDRESS_KEY = "address";

    public static final String CLASS_CODE = "classCode";

    public static final String INSTANCE_CODE = "instanceCode";

    public static final String PARENT_ID = "parentId";

    private final String address;

    private final short echoClassCode;

    private final byte instanceCode;

    private Long parentId;

    public ECHONETLiteDeviceData(JSONObject data) throws JSONException {
        super(data);
        this.address = data.getString(ADDRESS_KEY);
        this.echoClassCode = (short) data.getInt(CLASS_CODE);
        this.instanceCode = (byte) data.getInt(INSTANCE_CODE);
        if (data.has(PARENT_ID)) {
            this.parentId = data.getLong(PARENT_ID);
        } else {
            this.parentId = null;
        }
    }

    public String getAddress() {
        return address;
    }

    public short getClassCode() {
        return echoClassCode;
    }

    public byte getInstanceCode() {
        return instanceCode;
    }

    public Long getParentId() {
        return parentId;
    }

    public static JSONObject createJSONObject(String protocol, String uuid, String description,
            boolean status, String nickname, String address, short cCode, byte iCode, Long parentId)
            throws JSONException {
        JSONObject json = KadecotDeviceData.createJSONObject(protocol, uuid, description,
                ECHONETLiteDeviceType.getType(cCode).getName(), status, nickname);
        json.put(ADDRESS_KEY, address);
        json.put(CLASS_CODE, cCode);
        json.put(INSTANCE_CODE, iCode);
        return json;
    }
}
