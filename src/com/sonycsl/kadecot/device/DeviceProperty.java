/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.device;

/**
 * デバイスのプロパティ 名前と値の組 successとmessageはsetやgetの返答の際に使う． messageはエラーメッセージなどが入る
 */
public class DeviceProperty {

    public String name;

    public Object value;

    // result
    public boolean success;

    public Object message;

    public DeviceProperty(String name) {
        this.name = name;
        this.value = null;
    }

    public DeviceProperty(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public DeviceProperty(String name, Object value, boolean success) {
        this.name = name;
        this.value = value;
        this.success = success;
    }
}
