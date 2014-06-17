/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp;

import com.sonycsl.Kadecot.core.provider.KadecotCoreStore;

import org.json.JSONException;
import org.json.JSONObject;

public class KadecotDeviceData {
    private final long deviceId;
    private final String protocol;
    private final String uuid;
    private final String deviceType;
    private final String description;
    private boolean status;
    private String nickname;

    public KadecotDeviceData(long deviceId, String protocol, String uuid, String deviceType,
            String description, boolean status, String nickname) {
        this.deviceId = deviceId;
        this.protocol = protocol;
        this.uuid = uuid;
        this.deviceType = deviceType;
        this.description = description;
        this.status = status;
        this.nickname = nickname;
    }

    public KadecotDeviceData(JSONObject device) throws JSONException {
        deviceId = device.getLong(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID);
        protocol = device.getString(KadecotCoreStore.Devices.DeviceColumns.PROTOCOL);
        uuid = device.getString(KadecotCoreStore.Devices.DeviceColumns.UUID);
        deviceType = device.getString(KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE);
        description = device.getString(KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION);
        status = device.getBoolean(KadecotCoreStore.Devices.DeviceColumns.STATUS);
        if (device.has(KadecotCoreStore.Devices.DeviceColumns.NICKNAME)) {
            nickname = device.getString(KadecotCoreStore.Devices.DeviceColumns.NICKNAME);
        }
    }

    public void rename(String newName) {
        nickname = newName;
    }

    public void updateStatus(boolean newStatus) {
        status = newStatus;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getUuid() {
        return uuid;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getDescription() {
        return description;
    }

    public boolean getStatus() {
        return status;
    }

    public String getNickname() {
        return nickname;
    }

    /**
     * This methods create JSONObject to inform device data to
     * KadecotProviderUtil. So, JSONObject this method creates has no device id.
     * 
     * @param protocol
     * @param uuid
     * @param description
     * @param status
     * @param nickname
     * @return
     * @throws JSONException
     */
    public static JSONObject createJSONObject(String protocol, String uuid, String description,
            String deviceType, boolean status, String nickname) throws JSONException {
        JSONObject json = new JSONObject();

        json.put(KadecotCoreStore.Devices.DeviceColumns.PROTOCOL, protocol);
        json.put(KadecotCoreStore.Devices.DeviceColumns.UUID, uuid);
        json.put(KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION, description);
        json.put(KadecotCoreStore.Devices.DeviceColumns.STATUS, status);
        json.put(KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE, deviceType);
        json.put(KadecotCoreStore.Devices.DeviceColumns.NICKNAME, nickname);

        return json;
    }
}
