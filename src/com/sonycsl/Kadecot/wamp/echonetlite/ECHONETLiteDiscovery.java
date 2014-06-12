/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp.echonetlite;

import com.sonycsl.echo.Echo;
import com.sonycsl.echo.eoj.EchoObject;
import com.sonycsl.echo.eoj.device.DeviceObject;
import com.sonycsl.echo.eoj.profile.NodeProfile;
import com.sonycsl.echo.node.EchoNode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ECHONETLiteDiscovery {

    public static final String LOCAL_ADDRESS = "127.0.0.1";

    private final Set<DeviceObject> mActiveDevices;

    // <deviceId, deviceData>
    private Map<Long, ECHONETLiteDeviceData> mDeviceMap;

    private OnEchoDeviceInfoListener mListener;

    public interface OnEchoDeviceInfoListener {
        public void onDeviceAdded(JSONObject data);

        public void onDeviceStateChanged(JSONObject data);
    }

    public ECHONETLiteDiscovery() {
        mActiveDevices = Collections.synchronizedSet(new HashSet<DeviceObject>());
        mListener = null;
    }

    public void setListener(OnEchoDeviceInfoListener listener) {
        mListener = listener;
    }

    private JSONObject convertToJSONObject(DeviceObject device) throws JSONException {
        String address;
        if (device.isProxy()) {
            address = device.getNode().getAddress().getHostAddress();
        } else {
            address = LOCAL_ADDRESS;
        }
        String uuid = address + ":" + device.getEchoClassCode();
        String nickname = uuid;
        return ECHONETLiteDeviceData.createJSONObject(ECHONETLiteManager.PROTOCOL_TYPE_ECHO,
                uuid, uuid, true, nickname, address, (short) (device.getEchoClassCode() & 0xFFFF),
                (byte) (device.getInstanceCode() & 0xFF), null);
    }

    protected synchronized void onDiscoverNewActiveDevice(DeviceObject device) {
        try {
            JSONObject data = convertToJSONObject(device);

            if (mListener != null) {
                mListener.onDeviceAdded(data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void startDiscovering() {
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
        ECHONETLiteDeviceData data = mDeviceMap.get(deviceId);
        EchoObject eoj = getEchoObject(data.getAddress(), data.getClassCode(),
                data.getInstanceCode());
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
