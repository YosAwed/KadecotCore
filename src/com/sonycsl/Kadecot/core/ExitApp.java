/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.core;

public class ExitApp {

    private KadecotCoreActivity mActivity;

    public ExitApp(KadecotCoreActivity activity) {
        mActivity = activity;
    }

    public void exitActivity() {

        mActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mActivity.finish();
            }

        });
    }
}
