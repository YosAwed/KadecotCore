/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.device;

/**
 * Databaseに保存されているデータ
 */
public class DeviceData {
    @SuppressWarnings("unused")
    private static final String TAG = DeviceData.class.getSimpleName();

    private final DeviceData self = this;

    public final long deviceId;

    public final String nickname;

    public final String protocolName;

    public DeviceData(long deviceId, String nickname, String protocolName) {
        this.deviceId = deviceId;
        this.nickname = nickname;
        this.protocolName = protocolName;
    }

}
