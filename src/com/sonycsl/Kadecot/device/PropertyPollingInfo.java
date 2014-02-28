
package com.sonycsl.Kadecot.device;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.UUID;

/**
 * This class has polling information about which application polls ,polling
 * interval time and which properties is polled . One instance corresponds to
 * one device.
 * 
 * @author Kosuke.Mita
 */
public class PropertyPollingInfo {
    private static final String TAG = PropertyPollingInfoElement.class.getSimpleName();
    private static final int DONT_EXIST = -1;

    private DeviceProtocol mDeviceProtocol;

    /**
     * Thid device property is needed for get method. So, mDeviceProperty.value
     * is used as a get method argument.
     */
    private DeviceProperty mDeviceProperty;

    private int mPollingIntervalSec;
    private long mDeviceId;
    private List<PropertyPollingInfoElement> mPollingList;
    private PropertyPollingTimerTask mPollingTimerTask;
    private Timer mPollingTimer;

    /**
     * constructor
     */
    public PropertyPollingInfo(long deviceId, DeviceProperty dp, DeviceProtocol dProtocol) {
        mDeviceId = deviceId;
        mPollingList = new ArrayList<PropertyPollingInfoElement>();
        mDeviceProperty = dp;
        mPollingIntervalSec = -1;
        mDeviceProtocol = dProtocol;
    }

    public boolean hasSameTarget(DeviceProperty dp) {
        return this.mDeviceProperty.equals(dp);
    }

    /**
     * This is called to update polling interval time. Polling interval time is
     * updated when request time is shorter
     * 
     * @param client Client who wants to poll property
     * @param property property to be polled. It needs value if get method needs
     *            argument.
     * @param intervalSec polling interval time(second)
     */
    public int addPollingElement(UUID client, DeviceProperty property, int intervalSec) {
        PropertyPollingInfoElement ppie = new PropertyPollingInfoElement(client, property,
                intervalSec);

        int index = getIndexOfPPIE(ppie);

        // remove element to update
        if (index != DONT_EXIST) {
            mPollingList.remove(index);
        }
        mPollingList.add(ppie);

        updatePollingIntervalSec(ppie);
        startTimer();
        return mPollingIntervalSec;
    }

    /**
     * remove target PropertyPollingElement from mPollingList. If a target is
     * the last element, it terminates timer.
     * 
     * @param client
     */
    public void removePollingElement(String client) {
        int index = 0;
        for (PropertyPollingInfoElement ppie : mPollingList) {
            if (ppie.getClientId().equals(client)) {
                mPollingList.remove(index);
                break;
            }
            index++;
        }

        if (mPollingList.size() == 0) {
            endTimer();
        }
    }

    /**
     * get index where PropertyPollinInfo is in pollingList.
     * 
     * @param appName
     * @param property
     * @return index of pollingList
     */
    private int getIndexOfPPIE(PropertyPollingInfoElement ppiTarget) {
        int index = 0;
        for (PropertyPollingInfoElement ppi : mPollingList) {
            if (ppiTarget.equalsTarget(ppi)) {
                return index;
            }
            index++;
        }

        return DONT_EXIST;
    }

    /**
     * This mehotd update pollingIntervalSecMap if new polling time is shorter
     * than current polling time or shortest polling timer updated.
     * 
     * @param targetPpi is a target to add polling target list.
     * @return pollingIntervalSec
     */
    private int updatePollingIntervalSec(PropertyPollingInfoElement targetPpi) {
        int shortestSec = mPollingList.get(0).getIntervalSec();

        for (PropertyPollingInfoElement ppi : mPollingList) {
            if (shortestSec > ppi.getIntervalSec()) {
                shortestSec = ppi.getIntervalSec();
            }
        }

        mPollingIntervalSec = shortestSec;
        return shortestSec;
    }

    /**
     * start polling
     * 
     * @param task polling Task
     */
    private void startTimer() {
        Object val;
        if (mPollingTimer != null) {
            val = mPollingTimerTask.getCurPropValue();
            mPollingTimer.cancel();
            mPollingTimerTask.cancel();
        } else {
            List<DeviceProperty> pList = new ArrayList<DeviceProperty>();
            pList.add(mDeviceProperty);
            try {
                val = mDeviceProtocol.get(mDeviceId, pList).get(0).value;
            } catch (AccessException e) {
                e.printStackTrace();
                return;
            }
        }

        mPollingTimer = new Timer();
        mPollingTimerTask = new PropertyPollingTimerTask(mDeviceProtocol, mDeviceId,
                mDeviceProperty, val);
        mPollingTimer.schedule(mPollingTimerTask, 1000, 1000 * mPollingIntervalSec);
    }

    /**
     * end polling
     */
    private void endTimer() {
        mPollingTimer.cancel();
    }

    /**
     * This class has information of polling property. This corresponds to one
     * property.
     * 
     * @author Kosuke.Mita
     */
    private class PropertyPollingInfoElement {
        private UUID client;
        private DeviceProperty deviceProperty;
        private int intervalSec;

        PropertyPollingInfoElement(UUID client, DeviceProperty prop, int intervalSec) {
            if (intervalSec < 0) {
                Log.e(TAG, "Illegal argument");
                return;
            }

            this.client = client;
            this.deviceProperty = prop;
            this.intervalSec = intervalSec;
        }

        public boolean equalsTarget(PropertyPollingInfoElement ppie) {
            return this.client.equals(ppie.getClientId())
                    && this.deviceProperty.equals(ppie.getDeviceProperty());
        }

        public UUID getClientId() {
            return client;
        }

        public DeviceProperty getDeviceProperty() {
            return deviceProperty;
        }

        public int getIntervalSec() {
            return intervalSec;
        }
    }
}
