
package com.sonycsl.kadecot.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class KadecotDAO {

    private final KadecotSQLiteOpenHelper mHelper;
    private final SQLiteDatabase mWdb;
    private final SQLiteDatabase mRdb;

    public KadecotDAO(Context context) {
        mHelper = new KadecotSQLiteOpenHelper(context);
        mWdb = mHelper.getWritableDatabase();
        mRdb = mHelper.getReadableDatabase();
    }

    public long putDevice(String protocol, String uuid, String deviceType, String description,
            boolean status) {

        ContentValues values = new ContentValues();
        values.put(KadecotSQLiteOpenHelper.DEVICE_PROTOCOL, protocol);
        values.put(KadecotSQLiteOpenHelper.DEVICE_UUID, uuid);
        values.put(KadecotSQLiteOpenHelper.DEVICE_TYPE, deviceType);
        values.put(KadecotSQLiteOpenHelper.DEVICE_DESCRIPTION, description);
        values.put(KadecotSQLiteOpenHelper.DEVICE_STATUS, status ? 1 : 0);
        values.put(KadecotSQLiteOpenHelper.DEVICE_NICKNAME, description);

        Cursor c = mRdb.query(KadecotSQLiteOpenHelper.DEVICE_TABLE,
                new String[] {
                    KadecotSQLiteOpenHelper.DEVICE_ID
                },
                KadecotSQLiteOpenHelper.DEVICE_PROTOCOL + "=? and "
                        + KadecotSQLiteOpenHelper.DEVICE_UUID + "=?",
                new String[] {
                        protocol, uuid
                }, null, null, null);
        c.moveToFirst();

        final int deviceCount = c.getCount();
        if (deviceCount == 0) {
            c.close();
            return mWdb.insert(KadecotSQLiteOpenHelper.DEVICE_TABLE, null, values);
        }

        if (deviceCount > 1) {
            c.close();
            throw new IllegalArgumentException("DEVICE_ID is not UNIQUE");
        }

        final int deviceId = c.getInt(c.getColumnIndex(KadecotSQLiteOpenHelper.DEVICE_ID));
        c.close();

        mWdb.update(KadecotSQLiteOpenHelper.DEVICE_TABLE, values, KadecotSQLiteOpenHelper.DEVICE_ID
                + "=?", new String[] {
                String.valueOf(deviceId)
        });

        return deviceId;
    }

    public void removeDevice(long deviceId) {
        Cursor c = mRdb.query(KadecotSQLiteOpenHelper.DEVICE_TABLE,
                new String[] {
                    KadecotSQLiteOpenHelper.DEVICE_ID
                },
                KadecotSQLiteOpenHelper.DEVICE_ID + "=?",
                new String[] {
                    String.valueOf(deviceId)
                }, null, null, null);

        final int count = c.getCount();
        c.close();

        if (count != 1) {
            return;
        }

        mWdb.delete(KadecotSQLiteOpenHelper.DEVICE_TABLE,
                KadecotSQLiteOpenHelper.DEVICE_ID + "=?",
                new String[] {
                    String.valueOf(deviceId)
                });
    }

    public JSONArray getDeviceList() {
        final JSONArray deviceList = new JSONArray();

        Cursor c = mRdb.query(KadecotSQLiteOpenHelper.DEVICE_TABLE,
                null, null, null, null, null, null);
        c.moveToFirst();

        if (c.getCount() == 0) {
            return deviceList;
        }

        do {
            JSONObject json = new JSONObject();
            try {
                json.put(KadecotSQLiteOpenHelper.DEVICE_ID,
                        c.getInt(c.getColumnIndex(KadecotSQLiteOpenHelper.DEVICE_ID)));
                json.put(KadecotSQLiteOpenHelper.DEVICE_PROTOCOL,
                        c.getString(c.getColumnIndex(KadecotSQLiteOpenHelper.DEVICE_PROTOCOL)));
                json.put(KadecotSQLiteOpenHelper.DEVICE_TYPE,
                        c.getString(c.getColumnIndex(KadecotSQLiteOpenHelper.DEVICE_TYPE)));
                json.put(KadecotSQLiteOpenHelper.DEVICE_DESCRIPTION,
                        c.getString(c.getColumnIndex(KadecotSQLiteOpenHelper.DEVICE_DESCRIPTION)));
                json.put(
                        KadecotSQLiteOpenHelper.DEVICE_STATUS,
                        c.getInt(c.getColumnIndex(KadecotSQLiteOpenHelper.DEVICE_STATUS)) == 1 ? "on"
                                : "off");
                json.put(KadecotSQLiteOpenHelper.DEVICE_NICKNAME,
                        c.getString(c.getColumnIndex(KadecotSQLiteOpenHelper.DEVICE_NICKNAME)));
            } catch (JSONException e) {
                continue;
            }
            deviceList.put(json);
        } while (c.moveToNext());
        return deviceList;
    }

    public void changeNickname(long deviceId, String nickname) {
        Cursor c = mRdb.query(KadecotSQLiteOpenHelper.DEVICE_TABLE,
                new String[] {
                    KadecotSQLiteOpenHelper.DEVICE_ID
                },
                KadecotSQLiteOpenHelper.DEVICE_ID + "=?",
                new String[] {
                    String.valueOf(deviceId)
                }, null, null, null);

        final int count = c.getCount();
        c.close();

        if (count != 1) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(KadecotSQLiteOpenHelper.DEVICE_NICKNAME, nickname);
        mWdb.update(KadecotSQLiteOpenHelper.DEVICE_TABLE, values, KadecotSQLiteOpenHelper.DEVICE_ID
                + "=?", new String[] {
                String.valueOf(deviceId)
        });
    }

    /* TODO : 共通化 */
    public void putTopic(String topic, String description) {
        Cursor c = mRdb.query(KadecotSQLiteOpenHelper.TOPIC_TABLE,
                new String[] {
                    KadecotSQLiteOpenHelper.TOPIC_NAME
                },
                KadecotSQLiteOpenHelper.TOPIC_NAME + "=?",
                new String[] {
                    topic
                }, null, null, null);

        final int count = c.getCount();
        c.close();

        if (count > 0) {
            return;
        }

        String protocol = topic.split("\\.", 5)[3];

        ContentValues values = new ContentValues();
        values.put(KadecotSQLiteOpenHelper.TOPIC_NAME, topic);
        values.put(KadecotSQLiteOpenHelper.TOPIC_DESCRIPTION, description);
        values.put(KadecotSQLiteOpenHelper.TOPIC_PROTOCOL, protocol);

        mWdb.insert(KadecotSQLiteOpenHelper.TOPIC_TABLE, null, values);
    }

    public void removeTopic(String topic) {
        Cursor c = mRdb.query(KadecotSQLiteOpenHelper.TOPIC_TABLE,
                new String[] {
                    KadecotSQLiteOpenHelper.TOPIC_NAME
                },
                KadecotSQLiteOpenHelper.TOPIC_NAME + "=?",
                new String[] {
                    topic
                }, null, null, null);

        final int count = c.getCount();
        c.close();

        if (count != 1) {
            return;
        }

        mWdb.delete(KadecotSQLiteOpenHelper.TOPIC_TABLE,
                KadecotSQLiteOpenHelper.TOPIC_NAME + "=?",
                new String[] {
                    topic
                });
    }

    public void putProcedure(String procedure, String description) {
        Cursor c = mRdb.query(KadecotSQLiteOpenHelper.PROCEDURE_TABLE,
                new String[] {
                    KadecotSQLiteOpenHelper.PROCEDURE_NAME
                },
                KadecotSQLiteOpenHelper.PROCEDURE_NAME + "=?",
                new String[] {
                    procedure
                }, null, null, null);

        final int count = c.getCount();
        c.close();

        if (count > 0) {
            return;
        }

        String protocol = procedure.split("\\.", 5)[3];

        ContentValues values = new ContentValues();
        values.put(KadecotSQLiteOpenHelper.PROCEDURE_NAME, procedure);
        values.put(KadecotSQLiteOpenHelper.PROCEDURE_DESCRIPTION, description);
        values.put(KadecotSQLiteOpenHelper.PROCEDURE_PROTOCOL, protocol);

        mWdb.insert(KadecotSQLiteOpenHelper.PROCEDURE_TABLE, null, values);
    }

    public void removeProcedure(String procedure) {
        Cursor c = mRdb.query(KadecotSQLiteOpenHelper.PROCEDURE_TABLE,
                new String[] {
                    KadecotSQLiteOpenHelper.PROCEDURE_NAME
                },
                KadecotSQLiteOpenHelper.PROCEDURE_NAME + "=?",
                new String[] {
                    procedure
                }, null, null, null);

        final int count = c.getCount();
        c.close();

        if (count != 1) {
            return;
        }

        mWdb.delete(KadecotSQLiteOpenHelper.PROCEDURE_TABLE,
                KadecotSQLiteOpenHelper.PROCEDURE_NAME + "=?",
                new String[] {
                    procedure
                });
    }

    public List<String> getTopicList(String protocol) {
        List<String> topics = new ArrayList<String>();

        Cursor c = mRdb.query(KadecotSQLiteOpenHelper.TOPIC_TABLE,
                new String[] {
                    KadecotSQLiteOpenHelper.TOPIC_NAME
                }, KadecotSQLiteOpenHelper.TOPIC_PROTOCOL + "=?",
                new String[] {
                    protocol
                }, null, null, null);
        c.moveToFirst();

        if (c.getCount() == 0) {
            c.close();
            return topics;
        }

        do {
            topics.add(c.getString(c.getColumnIndex(KadecotSQLiteOpenHelper.TOPIC_NAME)));
        } while (c.moveToNext());

        c.close();
        return topics;
    }

    public List<String> getProcedureList(String protocol) {
        List<String> procs = new ArrayList<String>();

        Cursor c = mRdb.query(KadecotSQLiteOpenHelper.PROCEDURE_TABLE,
                new String[] {
                    KadecotSQLiteOpenHelper.PROCEDURE_NAME
                }, KadecotSQLiteOpenHelper.PROCEDURE_PROTOCOL + "=?",
                new String[] {
                    protocol
                }, null, null, null);
        c.moveToFirst();

        if (c.getCount() == 0) {
            c.close();
            return procs;
        }

        do {
            procs.add(c.getString(c.getColumnIndex(KadecotSQLiteOpenHelper.PROCEDURE_NAME)));
        } while (c.moveToNext());

        c.close();
        return procs;
    }

}
