
package com.sonycsl.kadecot.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class KadecotSQLiteOpenHelper extends SQLiteOpenHelper {

    public static final String DEVICE_TABLE = "device_table";
    public static final String DEVICE_ID = "_id";
    public static final String DEVICE_PROTOCOL = "device_protocol";
    public static final String DEVICE_UUID = "device_uuid";
    public static final String DEVICE_TYPE = "device_type";
    public static final String DEVICE_DESCRIPTION = "device_description";
    public static final String DEVICE_STATUS = "device_status";
    public static final String DEVICE_NICKNAME = "device_nickname";

    public static final String TOPIC_TABLE = "topic_table";
    private static final String TOPIC_ID = "_id";
    public static final String TOPIC_PROTOCOL = "topic_protocol";
    public static final String TOPIC_NAME = "topic_name";
    public static final String TOPIC_DESCRIPTION = "topic_description";

    public static final String PROCEDURE_TABLE = "procedure_table";
    private static final String PROCEDURE_ID = "_id";
    public static final String PROCEDURE_PROTOCOL = "procedure_protocol";
    public static final String PROCEDURE_NAME = "procedure_name";
    public static final String PROCEDURE_DESCRIPTION = "procedure_description";

    private static final String DB_NAME = "kadecotcore.db";
    private static final int DB_VERSION = 1;

    public KadecotSQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + DEVICE_TABLE + " ( " +
                DEVICE_ID + " integer primary key autoincrement, " +
                DEVICE_PROTOCOL + " text not null, " +
                DEVICE_UUID + " text not null, " +
                DEVICE_TYPE + " text not null, " +
                DEVICE_DESCRIPTION + " text not null, " +
                DEVICE_STATUS + " integer not null, " +
                DEVICE_NICKNAME + " text not null, " +
                "UNIQUE(" + DEVICE_PROTOCOL + ", " + DEVICE_UUID + ") " +
                ");");

        db.execSQL("create table " + TOPIC_TABLE + " ( " +
                TOPIC_ID + " integer primary key autoincrement, " +
                TOPIC_PROTOCOL + " text not null, " +
                TOPIC_NAME + " text not null, " +
                TOPIC_DESCRIPTION + " text not null, " +
                "UNIQUE(" + TOPIC_NAME + ") " +
                ");");

        db.execSQL("create table " + PROCEDURE_TABLE + " ( " +
                PROCEDURE_ID + " integer primary key autoincrement, " +
                PROCEDURE_PROTOCOL + " text not null, " +
                PROCEDURE_NAME + " text not null, " +
                PROCEDURE_DESCRIPTION + " text not null, " +
                "UNIQUE(" + PROCEDURE_NAME + ") " +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table " + DEVICE_TABLE + ";");
        db.execSQL("drop table " + TOPIC_TABLE + ";");
        db.execSQL("drop table " + PROCEDURE_TABLE + ";");
        onCreate(db);
    }

}
