
package com.sonycsl.kadecot.wamp.echonetlite;

import com.sonycsl.kadecot.device.echo.EchoDeviceData;
import com.sonycsl.kadecot.wamp.KadecotDeviceData;

import org.json.JSONException;
import org.json.JSONObject;

public class ECHONETLiteDeviceData extends KadecotDeviceData {
    private final String address;

    private final short echoClassCode;

    private final byte instanceCode;

    private Long parentId;

    private long noWampDeviceId;

    private String noWampNickname;

    public ECHONETLiteDeviceData(JSONObject data, EchoDeviceData echo) throws JSONException {
        super(data);
        this.address = echo.address;
        this.echoClassCode = echo.echoClassCode;
        this.instanceCode = echo.instanceCode;
        this.parentId = echo.parentId;
        this.noWampDeviceId = echo.deviceId;
        this.noWampNickname = echo.nickname;
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

    public long getNoWampDeviceId() {
        return noWampDeviceId;
    }

    public String getNoWampNickname() {
        return noWampNickname;
    }
}
