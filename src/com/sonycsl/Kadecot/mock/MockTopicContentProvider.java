/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.mock;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.sonycsl.Kadecot.provider.KadecotCoreStore;

import java.util.LinkedHashMap;

public class MockTopicContentProvider extends ContentProvider {

    private final String[] COLUMNS = {
            KadecotCoreStore.Topics.TopicColumns.PROTOCOL,
            KadecotCoreStore.Topics.TopicColumns.NAME,
            KadecotCoreStore.Topics.TopicColumns.DESCRIPTION,
            KadecotCoreStore.Topics.TopicColumns.REFERENCE_COUNT,
    };

    private LinkedHashMap<String, Object> mColumns = new LinkedHashMap<String, Object>() {
        /**
         * 
         */
        private static final long serialVersionUID = -973078798984086011L;

        {
            put(KadecotCoreStore.Topics.TopicColumns.PROTOCOL, "scalarwebapi");
            put(KadecotCoreStore.Topics.TopicColumns.NAME,
                    "com.sonycsl.kadecot.echonetlite.topic.0x80");
            put(KadecotCoreStore.Topics.TopicColumns.DESCRIPTION, "OperationStatus");
            put(KadecotCoreStore.Topics.TopicColumns.REFERENCE_COUNT, 0);
        }
    };

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        MatrixCursor result = new MatrixCursor(projection);
        Object[] values = new Object[projection.length];
        for (int i = 0; i < projection.length; i++) {

            for (int j = 0; j < COLUMNS.length; j++) {
                if (COLUMNS[j].equals(projection[i])) {
                    values[i] = mColumns.get(COLUMNS[j]);
                }
            }
        }
        result.addRow(values);
        return result;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        for (String key : values.keySet()) {
            Object value = values.get(key);
            mColumns.put(key, value);
        }
        return values.size();
    }

}
