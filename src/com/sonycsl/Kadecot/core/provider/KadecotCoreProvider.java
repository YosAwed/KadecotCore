
package com.sonycsl.Kadecot.core.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class KadecotCoreProvider extends ContentProvider {
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int DEVICE = 1;
    private static final int TOPIC = 2;
    private static final int PROC = 3;

    static {
        URI_MATCHER.addURI(KadecotCoreStore.AUTHORITY, "devices", DEVICE);
        URI_MATCHER.addURI(KadecotCoreStore.AUTHORITY, "topics", TOPIC);
        URI_MATCHER.addURI(KadecotCoreStore.AUTHORITY, "procedures", PROC);
    }

    private SQLiteOpenHelper mHelper;

    @Override
    public boolean onCreate() {
        mHelper = new DatabaseHelper(getContext());
        return mHelper != null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();

        switch (URI_MATCHER.match(uri)) {
            case DEVICE:
                return db.delete(DatabaseHelper.DEVICE_TABLE, selection, selectionArgs);
            case TOPIC:
                return db.delete(DatabaseHelper.TOPIC_TABLE, selection, selectionArgs);
            case PROC:
                return db.delete(DatabaseHelper.PROC_TABLE, selection, selectionArgs);
            default:
                return 0;
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case DEVICE:
                return KadecotCoreStore.Devices.CONTENT_TYPE;
            case TOPIC:
                return KadecotCoreStore.Topics.CONTENT_TYPE;
            case PROC:
                return KadecotCoreStore.Procedures.CONTENT_TYPE;
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
            case DEVICE:
                rowUri = ContentUris.withAppendedId(uri,
                        db.insert(DatabaseHelper.DEVICE_TABLE, null, values));
                getContext().getContentResolver().notifyChange(
                        KadecotCoreStore.Devices.CONTENT_URI, null);
                break;
            case TOPIC:
                rowUri = ContentUris.withAppendedId(uri,
                        db.insert(DatabaseHelper.TOPIC_TABLE, null, values));
                getContext().getContentResolver().notifyChange(
                        KadecotCoreStore.Topics.CONTENT_URI, null);
                break;
            case PROC:
                rowUri = ContentUris.withAppendedId(uri,
                        db.insert(DatabaseHelper.PROC_TABLE, null, values));
                getContext().getContentResolver().notifyChange(
                        KadecotCoreStore.Procedures.CONTENT_URI, null);
                break;
            default:
                return null;
        }

        return rowUri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case DEVICE:
                return db.query(DatabaseHelper.DEVICE_TABLE, projection, selection,
                        selectionArgs, null, null, sortOrder);
            case TOPIC:
                return db.query(DatabaseHelper.TOPIC_TABLE, projection, selection,
                        selectionArgs, null, null, sortOrder);
            case PROC:
                return db.query(DatabaseHelper.PROC_TABLE, projection, selection,
                        selectionArgs, null, null, sortOrder);
            default:
                return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int numOfRows = 0;
        SQLiteDatabase db = mHelper.getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case DEVICE:
                numOfRows = db.update(DatabaseHelper.DEVICE_TABLE, values, selection,
                        selectionArgs);
                getContext().getContentResolver().notifyChange(
                        KadecotCoreStore.Devices.CONTENT_URI, null);
                return numOfRows;
            case TOPIC:
                numOfRows = db.update(DatabaseHelper.TOPIC_TABLE, values, selection,
                        selectionArgs);
                getContext().getContentResolver().notifyChange(
                        KadecotCoreStore.Topics.CONTENT_URI, null);
                return numOfRows;
            default:
                return 0;
        }
    }

    public static final class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DB_NAME = "kadecotcore.db";
        private static final int DB_VERSION = 1;

        public static final String DEVICE_TABLE = "devices";
        public static final String TOPIC_TABLE = "topics";
        public static final String PROC_TABLE = "procs";

        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DEVICE_TABLE + " ( " +
                    "_id INTEGER, " +
                    KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KadecotCoreStore.Devices.DeviceColumns.PROTOCOL + " TEXT NOT NULL, " +
                    KadecotCoreStore.Devices.DeviceColumns.UUID + " TEXT NOT NULL, " +
                    KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE + " TEXT NOT NULL, " +
                    KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION + " TEXT NOT NULL, " +
                    KadecotCoreStore.Devices.DeviceColumns.STATUS + " INTEGER NOT NULL, " +
                    KadecotCoreStore.Devices.DeviceColumns.NICKNAME + " TEXT NOT NULL, " +
                    KadecotCoreStore.Devices.DeviceColumns.UTC_UPDATED
                    + " DATETIME DEFAULT (datetime('now')), " +
                    KadecotCoreStore.Devices.DeviceColumns.LOCAL_UPDATED
                    + " DATETIME DEFAULT (datetime('now', 'localtime')), " +
                    "UNIQUE(" + KadecotCoreStore.Devices.DeviceColumns.PROTOCOL + ", "
                    + KadecotCoreStore.Devices.DeviceColumns.UUID + ") " +
                    ");");

            db.execSQL("CREATE TRIGGER itrig AFTER INSERT ON " + DEVICE_TABLE +
                    " BEGIN" +
                    " UPDATE " + DEVICE_TABLE + " SET " +
                    "_id" + " = new." + KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID +
                    " where " + KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID + " = new."
                    + KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID + ";" +
                    " END");

            db.execSQL("CREATE TRIGGER utrig AFTER UPDATE ON " + DEVICE_TABLE +
                    " BEGIN" +
                    " UPDATE " + DEVICE_TABLE + " SET " +
                    KadecotCoreStore.Devices.DeviceColumns.UTC_UPDATED + " = datetime('now'), " +
                    KadecotCoreStore.Devices.DeviceColumns.LOCAL_UPDATED
                    + " = datetime('now', 'localtime')" +
                    " where " + KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID + " = new."
                    + KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID + ";" +
                    " END");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TOPIC_TABLE + " ( " +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KadecotCoreStore.Topics.TopicColumns.PROTOCOL + " TEXT NOT NULL, " +
                    KadecotCoreStore.Topics.TopicColumns.NAME + " TEXT NOT NULL, " +
                    KadecotCoreStore.Topics.TopicColumns.DESCRIPTION + " TEXT NOT NULL, " +
                    KadecotCoreStore.Topics.TopicColumns.REFERENCE_COUNT + " INTEGER DEFAULT 0, " +
                    "UNIQUE(" + KadecotCoreStore.Topics.TopicColumns.NAME + ") " +
                    ");");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + PROC_TABLE + " ( " +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KadecotCoreStore.Procedures.ProcedureColumns.PROTOCOL + " TEXT NOT NULL, " +
                    KadecotCoreStore.Procedures.ProcedureColumns.NAME + " TEXT NOT NULL, " +
                    KadecotCoreStore.Procedures.ProcedureColumns.DESCRIPTION + " TEXT NOT NULL, " +
                    "UNIQUE(" + KadecotCoreStore.Procedures.ProcedureColumns.NAME + ") " +
                    ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table " + DEVICE_TABLE + ";");
            db.execSQL("drop table " + TOPIC_TABLE + ";");
            db.execSQL("drop table " + PROC_TABLE + ";");
            onCreate(db);
        }

    }

}
