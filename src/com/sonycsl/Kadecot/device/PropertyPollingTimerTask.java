
package com.sonycsl.Kadecot.device;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * This class polls device properties. This can poll some properties of a device
 * one time.
 * 
 * @author Kosuke.Mita
 */
public class PropertyPollingTimerTask extends TimerTask {
    private static final String TAG = PropertyPollingTimerTask.class.getSimpleName();
    private DeviceProtocol mDeviceProtocol;
    private long mDeviceId;
    private List<DeviceProperty> mTargetDP;
    private Object mCurPropValue;

    /**
     * constructor
     * 
     * @param protocol
     * @param deviceId
     */
    public PropertyPollingTimerTask(DeviceProtocol protocol, long deviceId, DeviceProperty dp,
            Object propValue) {
        this.mDeviceProtocol = protocol;
        this.mDeviceId = deviceId;
        this.mTargetDP = new ArrayList<DeviceProperty>();
        mTargetDP.add(dp);
        this.mCurPropValue = propValue;
    }

    /**
     * polling action
     * 
     * @throws AccessException
     */
    public void run() {
        Log.i(TAG, "polling " + mTargetDP.get(0).name);

        try {
            List<DeviceProperty> newDPVal = mDeviceProtocol.get(mDeviceId, mTargetDP);
            if (!mCurPropValue.equals(newDPVal)) {
                DeviceData data = mDeviceProtocol.getDeviceData(mDeviceId);
                mDeviceProtocol.onPropertyChanged(data, newDPVal);
            }
        } catch (AccessException e) {
            e.printStackTrace();
            return;
        }

    }

    public Object getCurPropValue() {
        return mCurPropValue;
    }
}
