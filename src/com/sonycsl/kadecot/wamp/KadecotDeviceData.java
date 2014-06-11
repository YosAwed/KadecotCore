/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.wamp;

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
        deviceId = device.getLong(KadecotProviderUtil.DEVICE_ID);
        protocol = device.getString(KadecotProviderUtil.DEVICE_PROTOCOL);
        uuid = device.getString(KadecotProviderUtil.DEVICE_UUID);
        deviceType = device.getString(KadecotProviderUtil.DEVICE_TYPE);
        description = device.getString(KadecotProviderUtil.DEVICE_DESCRIPTION);
        status = device.getBoolean(KadecotProviderUtil.DEVICE_STATUS);
        if (device.has(KadecotProviderUtil.DEVICE_NICKNAME)) {
            nickname = device.getString(KadecotProviderUtil.DEVICE_NICKNAME);
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

        json.put(KadecotProviderUtil.DEVICE_PROTOCOL, protocol);
        json.put(KadecotProviderUtil.DEVICE_UUID, uuid);
        json.put(KadecotProviderUtil.DEVICE_DESCRIPTION, description);
        json.put(KadecotProviderUtil.DEVICE_STATUS, status);
        json.put(KadecotProviderUtil.DEVICE_TYPE, deviceType);
        json.put(KadecotProviderUtil.DEVICE_NICKNAME, nickname);

        return json;
    }
}
