/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.wamp.echonetlite;

import java.util.List;

public interface ECHONETLiteDeviceGenerator {

    public void onInitGenerator(List<ECHONETLiteDeviceData> dataList);

    public void onDeleteEchoDevice(ECHONETLiteDeviceData data);

    public void onDeleteAllEchoDevice();

    public String getProtocolName();

    public boolean setProperty(ECHONETLiteDeviceData data, byte epc, byte[] edt);

    public byte[] getProperty(ECHONETLiteDeviceData data, byte epc);

    public boolean isValidProperty(ECHONETLiteDeviceData data, byte epc, byte[] edt);

    public byte[] getStatusChangeAnnouncementProperties(ECHONETLiteDeviceData data);

    public byte[] getSetProperties(ECHONETLiteDeviceData data);

    public byte[] getGetProperties(ECHONETLiteDeviceData data);

}
