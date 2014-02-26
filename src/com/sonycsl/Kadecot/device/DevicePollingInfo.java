package com.sonycsl.Kadecot.device;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * This class has polling information about which application polls ,polling interval time and which properties is polled .
 * One instance corresponds to one device.
 * @author Kosuke.Mita
 *
 */
public class DevicePollingInfo {
    private static final String TAG = DevicePollingInfo.class.getSimpleName();
    private static final int DONT_EXIST = -1;
    public List<PropertyPollingInfo> pollingList = new ArrayList<PropertyPollingInfo>();
    private int pollingIntervalSec = -1;

    /**
     * This is called to update polling interval time.
     * Polling interval time is updated when request time is shorter
     *
     * @param property property to be polled. It needs value if get method needs argument.
     * @param intervalSec polling interval time(second)
     */
    public void setPolling(String appName, DeviceProperty property, Integer intervalSec) {
        int index = getIndexOfPollingList(appName, property);

        // remove old element to update
        if (index != DONT_EXIST) {
            pollingList.remove(index);
        }

        pollingList.add(new PropertyPollingInfo(appName, property, intervalSec));
        if(pollingIntervalSec < 0 || intervalSec < pollingIntervalSec) {
            pollingIntervalSec = intervalSec;
        }
    }

    /**
     * get polling target property name list
     * @return polling target property name list
     */
    public List<DeviceProperty> getPollingTargetList() {
        List<DeviceProperty> pollingTargetList = new ArrayList<DeviceProperty>();

        for(PropertyPollingInfo ppi : pollingList) {
            pollingTargetList.add(ppi.deviceProperty);
        }

        return pollingTargetList;
    }

    /**
     * get index where PropertyPollinInfo is in pollingList.
     *
     * @param appName
     * @param property
     * @return index of pollingList
     */
    private int getIndexOfPollingList(String appName, DeviceProperty property) {
        int index = 0;
        for(PropertyPollingInfo ppi : pollingList) {
            if (ppi.appName.equals(appName) && ppi.deviceProperty.equalsNameValue(property)) {
                return index;
            }
            index++;
        }

        return DONT_EXIST;
    }

    /**
     * This class has information of polling property.
     * This corresponds to one property.
     * @author Kosuke.Mita
     *
     */
    private class PropertyPollingInfo {
        public String appName;
        public DeviceProperty deviceProperty;
        public int intervalSec;

        PropertyPollingInfo(String appName, DeviceProperty prop, int intervalSec){
            if(intervalSec < 0) {
                Log.e(TAG, "Illegal argument");
                return;
            }

            this.appName = appName;
            this.deviceProperty = prop;
            this.intervalSec = intervalSec;
        }
    }
}
