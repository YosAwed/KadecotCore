/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class InMemoryProvider extends ContentProvider {

    public static final String AUTHORITY = "com.sonycsl.kadecot.inmemory.provider";

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int PROTOCOLS = 1;
    private static final int DEVICE_TYPES = 2;

    static {
        URI_MATCHER.addURI(AUTHORITY, "protocols", PROTOCOLS);
        URI_MATCHER.addURI(AUTHORITY, "deviceTypes", DEVICE_TYPES);
    }

    private SQLiteOpenHelper mHelper;

    @Override
    public boolean onCreate() {
        mHelper = new DatabaseHelper(getContext());
        return mHelper != null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteDatabase db = mHelper.getWritableDatabase();

        switch (URI_MATCHER.match(uri)) {
            case PROTOCOLS:
                return db.query(DatabaseHelper.PROTOCOL_TABLE, projection, selection,
                        selectionArgs, null, null, sortOrder);
            case DEVICE_TYPES:
                return db.query(DatabaseHelper.DEVICE_TYPE_TABLE, projection, selection,
                        selectionArgs, null, null, sortOrder);
            default:
                return null;
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case PROTOCOLS:
                return KadecotCoreStore.Protocols.CONTENT_TYPE;
            case DEVICE_TYPES:
                return KadecotCoreStore.DeviceTypes.CONTENT_TYPE;
            default:
                break;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Uri rowUri;
        switch (URI_MATCHER.match(uri)) {
            case PROTOCOLS:
                rowUri = ContentUris.withAppendedId(uri,
                        db.insert(DatabaseHelper.PROTOCOL_TABLE, null, values));
                getContext().getContentResolver().notifyChange(
                        KadecotCoreStore.Protocols.CONTENT_URI, null);
                break;
            case DEVICE_TYPES:
                rowUri = ContentUris.withAppendedId(uri,
                        db.insert(DatabaseHelper.DEVICE_TYPE_TABLE, null, values));
                getContext().getContentResolver().notifyChange(
                        KadecotCoreStore.DeviceTypes.CONTENT_URI, null);
                break;
            default:
                return null;
        }

        return rowUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();

        switch (URI_MATCHER.match(uri)) {
            case PROTOCOLS:
                return db.delete(DatabaseHelper.PROTOCOL_TABLE, selection, selectionArgs);
            case DEVICE_TYPES:
                return db.delete(DatabaseHelper.DEVICE_TYPE_TABLE, selection, selectionArgs);
            default:
                return 0;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int numOfRows = 0;
        SQLiteDatabase db = mHelper.getWritableDatabase();

        switch (URI_MATCHER.match(uri)) {
            case PROTOCOLS:
                numOfRows = db.update(DatabaseHelper.PROTOCOL_TABLE, values, selection,
                        selectionArgs);
                getContext().getContentResolver().notifyChange(
                        KadecotCoreStore.Protocols.CONTENT_URI, null);
                return numOfRows;
            case DEVICE_TYPES:
                numOfRows = db.update(DatabaseHelper.DEVICE_TYPE_TABLE, values, selection,
                        selectionArgs);
                getContext().getContentResolver().notifyChange(
                        KadecotCoreStore.DeviceTypes.CONTENT_URI, null);
                return numOfRows;
            default:
                return 0;
        }
    }

    private static final class DatabaseHelper extends SQLiteOpenHelper {

        private static final int DB_VERSION = 1;

        public static final String PROTOCOL_TABLE = "protocol";
        public static final String DEVICE_TYPE_TABLE = "devicetype";

        public DatabaseHelper(Context context) {
            super(context, null, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + PROTOCOL_TABLE + " ( " +
                    "_id INTEGER, " +
                    KadecotCoreStore.Protocols.ProtocolColumns.PROTOCOL
                    + " TEXT NOT NULL PRIMARY KEY, " +
                    KadecotCoreStore.Protocols.ProtocolColumns.PACKAGE_NAME
                    + " TEXT NOT NULL, " +
                    KadecotCoreStore.Protocols.ProtocolColumns.ACTIVITY_NAME
                    + " TEXT NOT NULL, " +
                    "UNIQUE(" + KadecotCoreStore.Protocols.ProtocolColumns.PROTOCOL + ") "
                    + ");");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + DEVICE_TYPE_TABLE + " ( " +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KadecotCoreStore.DeviceTypes.DeviceTypeColumns.DEVICE_TYPE
                    + " TEXT NOT NULL, " +
                    KadecotCoreStore.DeviceTypes.DeviceTypeColumns.PROTOCOL
                    + " TEXT NOT NULL, " +
                    KadecotCoreStore.DeviceTypes.DeviceTypeColumns.ICON
                    + " BLOB NOT NULL, " +
                    "UNIQUE(" + KadecotCoreStore.DeviceTypes.DeviceTypeColumns.DEVICE_TYPE
                    + ", " + KadecotCoreStore.DeviceTypes.DeviceTypeColumns.PROTOCOL
                    + ") " + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table " + PROTOCOL_TABLE + ";");
            db.execSQL("drop table " + DEVICE_TYPE_TABLE + ";");
            onCreate(db);
        }

    }

}
