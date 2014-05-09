/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.device;

/**
 * Databaseに保存されているデータ
 */
public class DeviceData {

    public final long deviceId;

    public final String nickname;

    public final String protocolName;

    public DeviceData(long deviceId, String nickname, String protocolName) {
        this.deviceId = deviceId;
        this.nickname = nickname;
        this.protocolName = protocolName;
    }

}
