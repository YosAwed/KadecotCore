/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.device.echo;

import android.content.Context;

import com.sonycsl.echo.Echo;
import com.sonycsl.echo.eoj.EchoObject;
import com.sonycsl.echo.eoj.device.DeviceObject;
import com.sonycsl.echo.eoj.device.housingfacilities.PowerDistributionBoardMetering;
import com.sonycsl.echo.eoj.device.sensor.HumiditySensor;
import com.sonycsl.echo.eoj.device.sensor.TemperatureSensor;
import com.sonycsl.echo.eoj.profile.NodeProfile;
import com.sonycsl.echo.node.EchoNode;
import com.sonycsl.kadecot.call.Notification;
import com.sonycsl.kadecot.device.DeviceManager;
import com.sonycsl.kadecot.device.DeviceProperty;
import com.sonycsl.kadecot.log.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EchoDiscovery {

    private final Context mContext;

    private final Set<DeviceObject> mActiveDevices;

    private final EchoDeviceDatabase mEchoDeviceDatabase;

    private final Logger mLogger;

    private OnEchoDeviceInfoListener mListener;

    public interface OnEchoDeviceInfoListener {
        public void onDeviceAdded(EchoDeviceData deviceInfo);

        public void onDeviceStateChanged(EchoDeviceData deviceInfo);
    }

    public EchoDiscovery(Context context) {
        mContext = context.getApplicationContext();
        mActiveDevices = Collections.synchronizedSet(new HashSet<DeviceObject>());
        mEchoDeviceDatabase = EchoDeviceDatabase.getInstance(mContext);
        mLogger = Logger.getInstance(mContext);
        mListener = null;
    }

    public void setListener(OnEchoDeviceInfoListener listener) {
        mListener = listener;
    }

    protected synchronized void onDiscoverNewActiveDevice(DeviceObject device) {
        EchoDeviceData data;

        if (mEchoDeviceDatabase.containsDeviceData(device)) {
            data = mEchoDeviceDatabase.getDeviceData(device);
            // Log.d(TAG,"already " + data.nickname);
        } else {
            data = mEchoDeviceDatabase.addDeviceData(device);
            // Log.d(TAG,"new " + data.nickname);
        }

        // Log.d(TAG,DeviceManager.getInstance(mContext).getDeviceInfo(data,
        // 0).toString());
        // onDiscover(device);

        /**
         * TODO: delete when completed wamp implementation.
         */
        Notification.informAllOnDeviceFound(
                DeviceManager.getInstance(mContext).getDeviceInfo(data, 0), EchoManager
                        .getInstance(mContext).getAllowedPermissionLevel());

        if (mListener != null) {
            mListener.onDeviceAdded(data);
        }

        // logger
        HashSet<DeviceProperty> propertySet = new HashSet<DeviceProperty>();
        long delay =
                (Logger.DEFAULT_INTERVAL_MILLS)
                        - (System.currentTimeMillis() % (Logger.DEFAULT_INTERVAL_MILLS));

        switch (device.getEchoClassCode()) {
            case PowerDistributionBoardMetering.ECHO_CLASS_CODE:
                try {
                    device.get().reqGetGetPropertyMap().send();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            case TemperatureSensor.ECHO_CLASS_CODE:
                propertySet.add(new DeviceProperty(EchoManager
                        .toPropertyName(TemperatureSensor.EPC_MEASURED_TEMPERATURE_VALUE), null));

                mLogger.watch(data.nickname, propertySet, Logger.DEFAULT_INTERVAL_MILLS, delay);
                break;
            case HumiditySensor.ECHO_CLASS_CODE:
                propertySet.add(new DeviceProperty(EchoManager
                        .toPropertyName(HumiditySensor.EPC_MEASURED_VALUE_OF_RELATIVE_HUMIDITY),
                        null));

                mLogger.watch(data.nickname, propertySet, Logger.DEFAULT_INTERVAL_MILLS, delay);
                break;
        }
    }

    protected void startDiscovering() {
        if (Echo.isStarted()) {
            // TODO:Why need this?
            EchoNode[] nodes = Echo.getNodes();
            for (EchoNode n : nodes) {
                DeviceObject[] devices = n.getDevices();
                for (DeviceObject d : devices) {
                    onDiscover(d);
                }
            }

            try {
                NodeProfile.getG().reqGetSelfNodeInstanceListS().send();
            } catch (IOException e) {
            }
        }

    }

    public void onDiscover(DeviceObject device) {
        if (!mActiveDevices.contains(device)) {
            mActiveDevices.add(device);
            onDiscoverNewActiveDevice(device);
        }
    }

    protected synchronized void stopDiscovering() {

    }

    protected synchronized void clearActiveDevices() {
        for (DeviceObject d : mActiveDevices) {
            if (d.isProxy()) {
                d.getNode().removeDevice(d);
            }
        }
        mActiveDevices.clear();
    }

    protected synchronized void removeActiveDevices(long deviceId) {
        EchoDeviceData data = mEchoDeviceDatabase.getDeviceData(deviceId);
        EchoObject eoj = getEchoObject(data.address, data.echoClassCode, data.instanceCode);
        if (eoj == null) {
            return;
        }
        // if(!eoj.isProxy()) {
        // Echo.getNode().removeDevice((DeviceObject)eoj);
        eoj.getNode().removeDevice((DeviceObject) eoj);

        // }
        mActiveDevices.remove(eoj);
    }

    private EchoObject getEchoObject(String address, short echoClassCode, byte instanceCode) {
        EchoNode en = Echo.getNode(address);
        if (en == null)
            return null;
        return en.getInstance(echoClassCode, instanceCode);
    }

    public synchronized boolean isActiveDevice(String address, short echoClassCode,
            byte instanceCode) {
        EchoObject eoj = getEchoObject(address, echoClassCode, instanceCode);
        if (eoj == null) {
            return false;
        }
        return mActiveDevices.contains(eoj);
    }

}
