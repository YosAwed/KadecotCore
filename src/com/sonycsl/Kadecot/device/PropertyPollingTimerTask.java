
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
    private DeviceProtocol dProtocol;
    private long deviceId;
    private List<DeviceProperty> targetDP;
    private Object curPropValue;

    /**
     * constructor
     * 
     * @param protocol
     * @param deviceId
     */
    public PropertyPollingTimerTask(DeviceProtocol protocol, long deviceId, DeviceProperty dp,
            Object propValue) {
        this.dProtocol = protocol;
        this.deviceId = deviceId;
        this.targetDP = new ArrayList<DeviceProperty>();
        targetDP.add(dp);
        this.curPropValue = propValue;
    }

    /**
     * polling action
     * 
     * @throws AccessException
     */
    public void run() {
        Log.i(TAG, "polling " + targetDP.get(0).name);

        try {
            List<DeviceProperty> newDPVal = dProtocol.get(deviceId, targetDP);
            if (!curPropValue.equals(newDPVal)) {
                DeviceData data = dProtocol.getDeviceData(deviceId);
                dProtocol.onPropertyChanged(data, newDPVal);
            }
        } catch (AccessException e) {
            e.printStackTrace();
            return;
        }

    }

    public Object getCurPropValue() {
        return curPropValue;
    }
}
