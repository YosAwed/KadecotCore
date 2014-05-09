/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.device.echo.generator;

import android.content.Context;

import com.sonycsl.echo.EchoUtils;
import com.sonycsl.kadecot.core.Dbg;
import com.sonycsl.kadecot.device.DeviceDatabase;
import com.sonycsl.kadecot.device.DeviceManager;
import com.sonycsl.kadecot.device.echo.EchoDeviceData;
import com.sonycsl.kadecot.device.echo.EchoDeviceDatabase;
import com.sonycsl.kadecot.device.echo.EchoManager;

import org.json.JSONObject;

public class EchoHandler {

    protected final Context mContext;

    private static EchoHandler sInstance = null;

    private EchoDeviceDatabase mEchoDeviceDatabase;

    private EchoManager mEchoManager;

    private EchoHandler(Context context) {
        mContext = context.getApplicationContext();
    }

    public static synchronized EchoHandler getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new EchoHandler(context);
            sInstance.init();
        }
        return sInstance;
    }

    protected void init() {
        mEchoDeviceDatabase = EchoDeviceDatabase.getInstance(mContext);
        mEchoManager = EchoManager.getInstance(mContext);
    }

    // 返り値はinstanceCode．0はfault
    // EchoDeviceAgentを作ってDatabaseに加える
    public byte generateDevice(short echoClassCode, long generatorId) {
        Dbg.print(generatorId);
        return mEchoManager.generateDevice(echoClassCode, generatorId);
    }

    public void addEchoDeviceGenerator(EchoDeviceGenerator gen) {
        mEchoManager.addEchoDeviceGenerator(gen);
    }

    public EchoDeviceData getDeviceData(String nickname) {
        return mEchoDeviceDatabase.getDeviceData(DeviceDatabase.getInstance(mContext)
                .getDeviceData(nickname));
    }

    public EchoDeviceData getDeviceData(long deviceId) {
        return mEchoDeviceDatabase.getDeviceData(DeviceDatabase.getInstance(mContext)
                .getDeviceData(deviceId));
    }

    public EchoDeviceData getLocalDeviceData(int echoObjectCode) {
        return mEchoDeviceDatabase.getDeviceData(EchoDeviceDatabase.LOCAL_ADDRESS, EchoUtils
                .getEchoClassCodeFromObjectCode(echoObjectCode) & 0xFFFF, EchoUtils
                .getInstanceCodeFromObjectCode(echoObjectCode) & 0xFF);
    }

    public JSONObject getDeviceInfo(EchoDeviceData data) {
        return DeviceManager.getInstance(mContext).getDeviceInfo(data.deviceId, 0);
    }

}
