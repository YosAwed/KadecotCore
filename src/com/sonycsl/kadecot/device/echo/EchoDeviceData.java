/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.device.echo;

import com.sonycsl.kadecot.device.DeviceData;
import com.sonycsl.kadecot.wamp.KadecotDeviceInfo;

import org.json.JSONException;
import org.json.JSONObject;

public class EchoDeviceData extends DeviceData {
    @SuppressWarnings("unused")
    private static final String TAG = EchoDeviceData.class.getSimpleName();

    private final EchoDeviceData self = this;

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
            j.put("echoClassCode", echoClassCode);
            j.put("instanceCode", instanceCode);
            j.put("parentId", parentId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return j;
    }
}
