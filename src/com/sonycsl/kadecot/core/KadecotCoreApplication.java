/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.core;

import android.app.Application;

import com.sonycsl.kadecot.wamp.KadecotProviderWampClient;
import com.sonycsl.kadecot.wamp.KadecotTopicTimer;
import com.sonycsl.kadecot.wamp.KadecotWampPeerLocator;
import com.sonycsl.kadecot.wamp.KadecotWampRouter;
import com.sonycsl.kadecot.wamp.KadecotWampTopic;
import com.sonycsl.kadecot.wamp.echonetlite.ECHONETLiteClient;

import java.util.concurrent.TimeUnit;

public class KadecotCoreApplication extends Application {

    protected AppModifiableCoreObject mModifiableCoreObject;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mModifiableCoreObject = new AppModifiableCoreObject(this);

        KadecotWampPeerLocator locator = new KadecotWampPeerLocator();
        locator.setRouter(new KadecotWampRouter());
        locator.loadClient(new KadecotProviderWampClient(this));
        locator.loadClient(new ECHONETLiteClient(this));
        locator.loadClient(new KadecotTopicTimer(KadecotWampTopic.TOPIC_PRIVATE_SEARCH, 5,
                TimeUnit.SECONDS));
        KadecotWampPeerLocator.load(locator);
    }

    public AppModifiableCoreObject getModifiableObject() {
        return mModifiableCoreObject;
    }

}
