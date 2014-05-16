/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.device.echo;

import com.sonycsl.echo.EchoUtils;
import com.sonycsl.kadecot.device.DeviceData;
import com.sonycsl.kadecot.wamp.KadecotDeviceInfo;

import org.json.JSONException;
import org.json.JSONObject;

public class EchoDeviceData extends DeviceData {

    public final String address;

    public final short echoClassCode;

    public final byte instanceCode;

    public final Long parentId;

    public EchoDeviceData(DeviceData d, String address, short echoClassCode, byte instanceCode,
            Long parentId) {
        super(d.deviceId, d.nickname, d.protocolName);
        this.address = address;
        this.echoClassCode = echoClassCode;
        this.instanceCode = instanceCode;
        this.parentId = parentId;
    }

    public JSONObject toJSONObject() {
        JSONObject j = new JSONObject();
        try {
            j.put(KadecotDeviceInfo.DEVICE_NICKNAME_KEY, nickname);
            j.put("deviceId", deviceId);
            j.put(KadecotDeviceInfo.DEVICE_PROTOCOL_KEY, protocolName);
            j.put("address", address);
            j.put("deviceName", EchoDeviceUtils.getClassName(echoClassCode));
            j.put("deviceType", "0x" + EchoUtils.toHexString(echoClassCode));
            j.put(KadecotDeviceInfo.DEVICE_PARENT_KEY, String.valueOf(parentId));
            j.put(KadecotDeviceInfo.DEVICE_STATUS_KEY, KadecotDeviceInfo.DEVICE_STATE_AVAILABLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return j;
    }
}
