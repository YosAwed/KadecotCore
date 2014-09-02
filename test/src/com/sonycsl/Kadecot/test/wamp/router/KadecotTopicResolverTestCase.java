/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.test.wamp.router;

import android.database.Cursor;
import android.test.ProviderTestCase2;

import com.sonycsl.Kadecot.mock.MockKadecotContentResolver;
import com.sonycsl.Kadecot.mock.MockTopicContentProvider;
import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.provider.OnDiskProvider;
import com.sonycsl.Kadecot.wamp.router.KadecotTopicResolver;

public class KadecotTopicResolverTestCase extends ProviderTestCase2<MockTopicContentProvider> {

    public KadecotTopicResolverTestCase() {
        super(MockTopicContentProvider.class, OnDiskProvider.AUTHORITY);
    }

    private static final String TEST_TOPIC = "com.sonycsl.kadecot.echonetlite.topic.0x80";

    private MockKadecotContentResolver mMockResolver;
    private KadecotTopicResolver mTopicResolver;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMockResolver = new MockKadecotContentResolver(getMockContext(),
                OnDiskProvider.AUTHORITY, getProvider(), MockTopicContentProvider.class);
        mTopicResolver = new KadecotTopicResolver(mMockResolver);
    }

    public void testConstructor() {
        assertNotNull(mMockResolver);
        assertNotNull(mTopicResolver);
    }

    public void testOnsubscribe() {
        assertEquals(0, getReferenceCount());
        mTopicResolver.onSubscribe(TEST_TOPIC);
        assertEquals(1, getReferenceCount());
        mTopicResolver.onSubscribe(TEST_TOPIC);
        assertEquals(2, getReferenceCount());
        mTopicResolver.onUnsubscribe(TEST_TOPIC);
        assertEquals(1, getReferenceCount());
    }

    private int getReferenceCount() {
        Cursor cursor = mMockResolver.query(KadecotCoreStore.Topics.CONTENT_URI,
                new String[] {
                        KadecotCoreStore.Topics.TopicColumns.NAME,
                        KadecotCoreStore.Topics.TopicColumns.REFERENCE_COUNT
                },
                KadecotCoreStore.Topics.TopicColumns.NAME + " =?",
                new String[] {
                    TEST_TOPIC
                }, null);

        if (cursor.getCount() != 1) {
            cursor.close();
            return -1;
        }

        cursor.moveToFirst();
        int refCount = cursor.getInt(cursor
                .getColumnIndex(KadecotCoreStore.Topics.TopicColumns.REFERENCE_COUNT));
        cursor.close();
        return refCount;
    }
}
