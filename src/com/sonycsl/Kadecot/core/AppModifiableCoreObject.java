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
    @SuppressWarnings("unused")
    private static final String TAG = AppModifiableCoreObject.class.getSimpleName();
    private final AppModifiableCoreObject self = this;

    protected final Context mContext;

    public AppModifiableCoreObject(Context context) {
        mContext = context;
    }

    public boolean acceptWebSocketOrigin(String origin) {
        Dbg.print("WebSocket origin:" + origin);
        return true;
    }

    public void onControlProperty(final DeviceData data, DeviceInfo info,
            String accessType, DeviceProperty property) {

    }
}
