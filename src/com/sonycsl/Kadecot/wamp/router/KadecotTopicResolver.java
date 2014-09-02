/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp.router;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.RemoteException;

import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.wamp.role.WampBroker.PubSubMessageHandler;

public class KadecotTopicResolver implements PubSubMessageHandler {

    private final ContentResolver mResolver;

    public KadecotTopicResolver(ContentResolver resolver) {
        mResolver = resolver;
    }

    private void updateDB(String topic, boolean countUp) {

        ContentProviderClient provider = mResolver
                .acquireContentProviderClient(KadecotCoreStore.Topics.CONTENT_URI);

        if (provider == null) {
            return;
        }

        try {
            Cursor cursor = provider.query(KadecotCoreStore.Topics.CONTENT_URI,
                    new String[] {
                            KadecotCoreStore.Topics.TopicColumns.NAME,
                            KadecotCoreStore.Topics.TopicColumns.REFERENCE_COUNT
                    },
                    KadecotCoreStore.Topics.TopicColumns.NAME + " =?",
                    new String[] {
                        topic
                    }, null);

            if (cursor.getCount() != 1) {
                cursor.close();
                return;
            }

            cursor.moveToFirst();
            int refCount = cursor.getInt(cursor
                    .getColumnIndex(KadecotCoreStore.Topics.TopicColumns.REFERENCE_COUNT));
            cursor.close();

            if (countUp) {
                refCount++;
            } else {
                refCount--;
            }

            if (refCount < 0) {
                refCount = 0;
            }

            ContentValues values = new ContentValues();
            values.put(KadecotCoreStore.Topics.TopicColumns.REFERENCE_COUNT, refCount);
            provider.update(KadecotCoreStore.Topics.CONTENT_URI, values,
                    KadecotCoreStore.Topics.TopicColumns.NAME + " =?",
                    new String[] {
                        topic
                    });
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            provider.release();
        }
    }

    @Override
    public void onSubscribe(String topic) {
        updateDB(topic, true);
    }

    @Override
    public void onUnsubscribe(String topic) {
        updateDB(topic, false);
    }

}
