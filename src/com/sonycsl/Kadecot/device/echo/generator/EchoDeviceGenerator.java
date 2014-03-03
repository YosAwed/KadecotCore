
package com.sonycsl.Kadecot.device.echo.generator;

import com.sonycsl.Kadecot.device.echo.EchoDeviceData;

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
