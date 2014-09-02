/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import com.sonycsl.Kadecot.core.R;
import com.sonycsl.Kadecot.wamp.client.provider.KadecotProviderClient;
import com.sonycsl.Kadecot.wamp.router.KadecotWampRouter;
import com.sonycsl.Kadecot.wamp.util.KadecotWampPeerLocator;

public class KadecotCoreApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (!isAgreedEULA()) {
            Intent i = new Intent(this, EulaActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }

        KadecotWampPeerLocator locator = new KadecotWampPeerLocator();
        locator.setRouter(new KadecotWampRouter(getContentResolver()));
        locator.loadSystemClient(new KadecotProviderClient(this, new Handler()));
        KadecotWampPeerLocator.load(locator);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private boolean isAgreedEULA() {
        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        boolean isAgreed = preferences.getBoolean(EulaActivity.EULA_LABEL, false);

        return isAgreed;
    }
}
