/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.device.echo;

import com.sonycsl.Kadecot.device.DeviceData;

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
        return j;
    }
}
