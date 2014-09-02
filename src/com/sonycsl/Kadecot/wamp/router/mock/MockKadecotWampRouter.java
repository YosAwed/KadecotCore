/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp.router.mock;

import android.content.ContentResolver;

import com.sonycsl.Kadecot.wamp.router.KadecotWampRouter;

public class MockKadecotWampRouter extends KadecotWampRouter {

    public MockKadecotWampRouter(ContentResolver resolver) {
        super(resolver);
    }
}
