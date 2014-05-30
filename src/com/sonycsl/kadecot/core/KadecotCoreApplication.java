/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.core;

import android.app.Application;

import com.sonycsl.kadecot.wamp.KadecotProviderWampClient;
import com.sonycsl.kadecot.wamp.KadecotTopicTimer;
import com.sonycsl.kadecot.wamp.KadecotWampClientLocator;
import com.sonycsl.kadecot.wamp.KadecotWampTopic;
import com.sonycsl.kadecot.wamp.echonetlite.KadecotECHONETLiteClient;

import java.util.concurrent.TimeUnit;

public class KadecotCoreApplication extends Application {

    protected AppModifiableCoreObject mModifiableCoreObject;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mModifiableCoreObject = new AppModifiableCoreObject(this);

        KadecotWampClientLocator locator = new KadecotWampClientLocator();
        locator.loadClient(new KadecotProviderWampClient(this));
        locator.loadClient(new KadecotECHONETLiteClient(this));
        locator.loadClient(new KadecotTopicTimer(KadecotWampTopic.TOPIC_PRIVATE_SEARCH, 5,
                TimeUnit.SECONDS));
        KadecotWampClientLocator.load(locator);
    }

    public AppModifiableCoreObject getModifiableObject() {
        return mModifiableCoreObject;
    }

}
