/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.core;

import android.app.Application;

public class KadecotCoreApplication extends Application {
    @SuppressWarnings("unused")
    private static final String TAG = KadecotCoreApplication.class
            .getSimpleName();
    private final KadecotCoreApplication self = this;

    protected AppModifiableCoreObject mModifiableCoreObject;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mModifiableCoreObject = new AppModifiableCoreObject(this);
    }

    public AppModifiableCoreObject getModifiableObject() {
        return mModifiableCoreObject;
    }

}
