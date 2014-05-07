/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.device.echo.generator;

import com.sonycsl.kadecot.device.echo.EchoDeviceData;

import java.util.List;

public interface EchoDeviceGenerator {

    public void onInitGenerator(List<EchoDeviceData> dataList);

    public void onDeleteEchoDevice(EchoDeviceData data);

    public void onDeleteAllEchoDevice();

    public String getProtocolName();

    public boolean setProperty(EchoDeviceData data, byte epc, byte[] edt);

    public byte[] getProperty(EchoDeviceData data, byte epc);

    public boolean isValidProperty(EchoDeviceData data, byte epc, byte[] edt);

    public byte[] getStatusChangeAnnouncementProperties(EchoDeviceData data);

    public byte[] getSetProperties(EchoDeviceData data);

    public byte[] getGetProperties(EchoDeviceData data);

}
