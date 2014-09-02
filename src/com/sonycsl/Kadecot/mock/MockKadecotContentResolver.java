/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.mock;

import android.content.ContentProvider;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.test.mock.MockContentResolver;

public class MockKadecotContentResolver extends MockContentResolver {

    public MockKadecotContentResolver(Context context, String authority, ContentProvider provider,
            Class<?> providerClass) {
        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.authority = authority;
        providerInfo.enabled = true;
        provider.attachInfo(context, providerInfo);
        providerInfo.packageName = providerClass.getPackage().getName();
        super.addProvider(authority, provider);
    }

}
