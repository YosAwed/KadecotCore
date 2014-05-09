/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.device;

/**
 * Deviceの情報・状態 主にDatabaseに登録されていない情報
 */
public class DeviceInfo {

    public final boolean active;

    public final String deviceName;

    public final String deviceType;

    public final String parent;

    public DeviceInfo(boolean active, String deviceName, String deviceType, String parent) {
        this.active = active;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.parent = parent;
    }

}
