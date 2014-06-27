/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.core;

import android.app.Application;
import android.os.Handler;

import com.sonycsl.Kadecot.wamp.KadecotTopicTimer;
import com.sonycsl.Kadecot.wamp.KadecotWampPeerLocator;
import com.sonycsl.Kadecot.wamp.KadecotWampRouter;
import com.sonycsl.Kadecot.wamp.KadecotWampTopic;
import com.sonycsl.Kadecot.wamp.echonetlite.ECHONETLiteClient;
import com.sonycsl.Kadecot.wamp.provider.KadecotProviderClient;

import java.util.concurrent.TimeUnit;

public class KadecotCoreApplication extends Application {

    protected AppModifiableCoreObject mModifiableCoreObject;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mModifiableCoreObject = new AppModifiableCoreObject(this);

        KadecotWampPeerLocator locator = new KadecotWampPeerLocator();
        locator.setRouter(new KadecotWampRouter(getContentResolver()));

        locator.loadSystemClient(new KadecotProviderClient(this, new Handler()));

        locator.loadSystemClient(new KadecotTopicTimer(KadecotWampTopic.TOPIC_PRIVATE_SEARCH, 5,
                TimeUnit.SECONDS));

        locator.loadProtocolClient(new ECHONETLiteClient(this));
        KadecotWampPeerLocator.load(locator);
    }

    public AppModifiableCoreObject getModifiableObject() {
        return mModifiableCoreObject;
    }

}
