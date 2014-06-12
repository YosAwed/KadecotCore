/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.core;

import android.content.Context;

import com.sonycsl.Kadecot.device.DeviceData;
import com.sonycsl.Kadecot.device.DeviceInfo;
import com.sonycsl.Kadecot.device.DeviceProperty;

public class AppModifiableCoreObject {

    protected final Context mContext;

    public AppModifiableCoreObject(Context context) {
        mContext = context;
    }

    public void onControlProperty(final DeviceData data, DeviceInfo info,
            String accessType, DeviceProperty property) {

    }
}
