/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp.client.provider;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.RemoteException;

import com.sonycsl.Kadecot.provider.KadecotCoreStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class TopicObserver {

    public interface OnSubscriberListener {
        public void onAppeared(String topic);

        public void onDisappeared(String topic);
    }

    private Map<String, Integer> mRefCounts;
    private final ContentResolver mResolver;
    private OnSubscriberListener mListener;

    private Map<String, Integer> getRefCounts() {
        Map<String, Integer> refCounts = new ConcurrentHashMap<String, Integer>();

        ContentProviderClient provider = mResolver
                .acquireContentProviderClient(KadecotCoreStore.Topics.CONTENT_URI);

        Cursor cursor;
        try {
            cursor = provider.query(KadecotCoreStore.Topics.CONTENT_URI,
                    new String[] {
                            KadecotCoreStore.Topics.TopicColumns.NAME,
                            KadecotCoreStore.Topics.TopicColumns.REFERENCE_COUNT
                    },
                    KadecotCoreStore.Topics.TopicColumns.REFERENCE_COUNT + " == 0 OR " +
                            KadecotCoreStore.Topics.TopicColumns.REFERENCE_COUNT + " == 1",
                    null, null);
        } catch (RemoteException e) {
            e.printStackTrace();
            return refCounts;
        } finally {
            provider.release();
        }

        if (cursor.getCount() <= 0) {
            cursor.close();
            return refCounts;
        }

        cursor.moveToFirst();
        do {
            refCounts.put(cursor.getString(cursor
                    .getColumnIndex(KadecotCoreStore.Topics.TopicColumns.NAME)),
                    cursor.getInt(cursor
                            .getColumnIndex(KadecotCoreStore.Topics.TopicColumns.REFERENCE_COUNT)));
        } while (cursor.moveToNext());
        cursor.close();

        return refCounts;
    }

    public TopicObserver(ContentResolver resolver, Handler handler) {
        mRefCounts = new HashMap<String, Integer>();

        mResolver = resolver;
        ContentProviderClient provider = mResolver
                .acquireContentProviderClient(KadecotCoreStore.Topics.CONTENT_URI);

        ContentValues values = new ContentValues();
        values.put(KadecotCoreStore.Topics.TopicColumns.REFERENCE_COUNT, 0);
        try {
            provider.update(KadecotCoreStore.Topics.CONTENT_URI, values, null, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            provider.release();
        }

        mRefCounts = getRefCounts();

        mResolver.registerContentObserver(KadecotCoreStore.Topics.CONTENT_URI, true,
                new ContentObserver(handler) {

                    @Override
                    public void onChange(boolean selfChange) {
                        Map<String, Integer> newRefCounts = getRefCounts();

                        for (Entry<String, Integer> entry : newRefCounts.entrySet()) {

                            String topic = entry.getKey();
                            if (!mRefCounts.containsKey(topic)) {
                                if (mListener != null && newRefCounts.get(topic) != 0) {
                                    mListener.onAppeared(topic);
                                }
                                continue;
                            }

                            int newRef = entry.getValue();
                            int oldRef = mRefCounts.get(topic);

                            if (oldRef == newRef) {
                                continue;
                            }

                            if (mListener != null) {
                                if (oldRef < newRef) {
                                    mListener.onAppeared(topic);
                                } else {
                                    mListener.onDisappeared(topic);
                                }
                            }
                        }
                        mRefCounts = newRefCounts;
                    }

                });
    }

    public void setOnSubscriberListener(OnSubscriberListener listener) {
        mListener = listener;
    }

}
