/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.device.echo.generator;

import com.sonycsl.Kadecot.core.Dbg;
import com.sonycsl.Kadecot.device.echo.EchoDeviceData;
import com.sonycsl.echo.EchoProperty;
import com.sonycsl.echo.eoj.device.DeviceObject;

public class EchoDeviceAgent extends DeviceObject {

    private final EchoDeviceData mData;

    private final EchoDeviceGenerator mGenerator;

    public EchoDeviceAgent(EchoDeviceData data, EchoDeviceGenerator gen) {
        mData = data;
        mGenerator = gen;

        byte[] props = mGenerator.getStatusChangeAnnouncementProperties(mData);
        for (byte p : props) {
            addStatusChangeAnnouncementProperty(p);
        }
        props = mGenerator.getSetProperties(mData);
        for (byte p : props) {
            addSetProperty(p);
        }
        props = mGenerator.getGetProperties(mData);
        for (byte p : props) {
            addGetProperty(p);
        }
    }

    @Override
    protected void setupPropertyMaps() {
        // super.setupPropertyMaps();
    }

    @Override
    public byte getInstanceCode() {
        return mData.instanceCode;
    }

    @Override
    protected byte[] getProperty(byte epc) {
        // return super.getProperty(epc);
        Dbg.print(epc);

        return mGenerator.getProperty(mData, epc);
    }

    @Override
    protected boolean isValidProperty(EchoProperty property) {
        // return super.isValidProperty(property);

        boolean b = mGenerator.isValidProperty(mData, property.epc, property.edt);
        return b;

    }

    @Override
    protected boolean setProperty(EchoProperty property) {
        // return super.setProperty(property);
        return mGenerator.setProperty(mData, property.epc, property.edt);
    }

    @Override
    protected byte[] getFaultStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected byte[] getInstallationLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected byte[] getManufacturerCode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected byte[] getOperationStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean setInstallationLocation(byte[] arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public short getEchoClassCode() {
        return mData.echoClassCode;
    }

}
