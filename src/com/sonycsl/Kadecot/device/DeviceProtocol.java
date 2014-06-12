/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.device;

import java.util.List;

/**
 * デバイスとの情報をやりとりするプロトコルのクラス
 */
public interface DeviceProtocol {

    // デバイスへのアクセス開始
    public void start();

    // デバイスへのアクセス終了
    public void stop();

    // デバイス一覧を取得
    // public List<JSONObject> getDeviceList();
    // デバイス一覧をリフレッシュする(デバイスを全てinactiveにして検索する)
    public void refreshDeviceList();

    // 全てのデバイスのデータを消去する
    public void deleteAllDeviceData();

    // 指定したデバイスのデータを消去する
    public void deleteDeviceData(long deviceId);

    // デバイスにアクセスする
    public List<DeviceProperty> set(long deviceId, List<DeviceProperty> propertyList)
            throws AccessException;

    public List<DeviceProperty> get(long deviceId, List<DeviceProperty> propertyList)
            throws AccessException;

    // デバイスのアクセスを許可するレベルを取得する(1ならどこからでも可，0ならWebViewのみ可)
    public int getAllowedPermissionLevel();

    // デバイスの現在の情報を取得
    public DeviceInfo getDeviceInfo(long deviceId, String locale);

    // プロトコル名を取得
    public String getProtocolName();
}
