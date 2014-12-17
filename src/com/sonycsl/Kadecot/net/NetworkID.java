/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.net;

public class NetworkID {
    public static final String ETHER_SSID = "Ethernet";
    public static final String ETHER_BSSID = "Ether_dummy_BSSID";

    private String mSsid;
    private String mBssid;

    public NetworkID(String ssid, String bssid) {
        mSsid = ssid;
        mBssid = bssid;
    }

    public String getSsid() {
        return mSsid;
    }

    public String getBssid() {
        return mBssid;
    }
}
