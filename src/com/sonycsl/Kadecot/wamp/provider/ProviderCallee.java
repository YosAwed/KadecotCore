
package com.sonycsl.Kadecot.wamp.provider;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.Log;

import com.sonycsl.Kadecot.core.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.wamp.provider.KadecotProviderClient.Procedure;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.message.WampInvocationMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.role.WampCallee;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;

public class ProviderCallee extends WampCallee {
    private ContentResolver mResolver;

    public ProviderCallee(ContentResolver resolver) throws RemoteException {
        mResolver = resolver;
        ContentProviderClient provider = mResolver
                .acquireContentProviderClient(KadecotCoreStore.Devices.CONTENT_URI);

        ContentValues values = new ContentValues();
        values.put(KadecotCoreStore.Devices.DeviceColumns.STATUS, false);
        provider.update(KadecotCoreStore.Devices.CONTENT_URI, values, null, null);
        provider.release();
    }

    private WampMessage createError(WampInvocationMessage msg, String error) {
        return WampMessageFactory.createError(WampMessageType.INVOCATION,
                msg.getRequestId(), new JSONObject(), error);
    }

    @Override
    protected WampMessage invocation(String procedure, WampMessage msg) {

        if (!msg.isInvocationMessage()) {
            throw new IllegalArgumentException();
        }

        WampInvocationMessage iMsg = msg.asInvocationMessage();
        Procedure proc = Procedure.getEnum(procedure);
        if (proc == null) {
            return createError(iMsg, WampError.NO_SUCH_PROCEDURE);
        }

        try {
            return (WampMessage) ProviderCallee.this.getClass()
                    .getDeclaredMethod(proc.getMethod(),
                            WampInvocationMessage.class).invoke(ProviderCallee.this, msg);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return createError(iMsg, WampError.INVALID_ARGUMENT);
    }

    private boolean isDeviceExists(WampInvocationMessage msg) throws RemoteException,
            JSONException {
        ContentProviderClient provider = mResolver.acquireContentProviderClient(
                KadecotCoreStore.Devices.CONTENT_URI);

        JSONObject json = msg.getArgumentsKw();

        Cursor cursor = provider.query(KadecotCoreStore.Devices.CONTENT_URI,
                new String[] {
                    KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID
                },
                KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID + "=?",
                new String[] {
                    json.getString(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID)
                }, null);
        final int count = cursor.getCount();
        cursor.close();
        provider.release();

        if (count > 1) {
            throw new IllegalArgumentException("fatal: DEVICE_ID is not UNIQUE");
        }

        return count == 1 ? true : false;
    }

    private long getDeviceId(WampInvocationMessage msg) throws RemoteException, JSONException {

        ContentProviderClient provider = mResolver.acquireContentProviderClient(
                KadecotCoreStore.Devices.CONTENT_URI);

        JSONObject json = msg.getArgumentsKw();

        Cursor cursor = provider.query(KadecotCoreStore.Devices.CONTENT_URI,
                new String[] {
                    KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID
                },
                KadecotCoreStore.Devices.DeviceColumns.PROTOCOL + "=? and "
                        + KadecotCoreStore.Devices.DeviceColumns.UUID + "=?",
                new String[] {
                        json.getString(KadecotCoreStore.Devices.DeviceColumns.PROTOCOL),
                        json.getString(KadecotCoreStore.Devices.DeviceColumns.UUID)
                }, null);
        cursor.moveToFirst();
        final int count = cursor.getCount();
        final long id = (count == 1) ? cursor.getLong(cursor
                .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID)) : -1;
        cursor.close();
        provider.release();

        if (count > 1) {
            throw new IllegalArgumentException("fatal: DEVICE_ID is not UNIQUE");
        }

        return id;
    }

    private ContentValues convertTo(JSONObject json) throws JSONException {
        ContentValues values = new ContentValues();
        values.put(KadecotCoreStore.Devices.DeviceColumns.PROTOCOL,
                json.getString(KadecotCoreStore.Devices.DeviceColumns.PROTOCOL));
        values.put(KadecotCoreStore.Devices.DeviceColumns.UUID,
                json.getString(KadecotCoreStore.Devices.DeviceColumns.UUID));
        values.put(KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE,
                json.getString(KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE));
        values.put(KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION,
                json.getString(KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION));
        values.put(KadecotCoreStore.Devices.DeviceColumns.STATUS,
                json.getBoolean(KadecotCoreStore.Devices.DeviceColumns.STATUS) ? 1 : 0);
        values.put(KadecotCoreStore.Devices.DeviceColumns.NICKNAME,
                json.getString(KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION));
        return values;
    }

    private WampMessage insertDevice(WampInvocationMessage msg) {
        ContentValues values;
        try {
            values = convertTo(msg.getArgumentsKw());
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        ContentProviderClient provider = mResolver.acquireContentProviderClient(
                KadecotCoreStore.Devices.CONTENT_URI);
        long deviceId = -1;
        try {
            deviceId = ContentUris
                    .parseId(provider.insert(
                            KadecotCoreStore.Devices.CONTENT_URI, values));
        } catch (RemoteException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } finally {
            provider.release();
        }

        if (deviceId < 0) {
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        try {
            return WampMessageFactory.createYield(
                    msg.getRequestId(),
                    new JSONObject(),
                    new JSONArray(),
                    new JSONObject(msg.getArgumentsKw().toString()).put(
                            KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID, deviceId));
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        }
    }

    private WampMessage updateDevice(WampInvocationMessage msg, long deviceId) {

        ContentValues values;

        try {
            values = convertTo(msg.getArgumentsKw());
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        ContentProviderClient provider = mResolver.acquireContentProviderClient(
                KadecotCoreStore.Devices.CONTENT_URI);

        try {
            Cursor cursor = provider.query(KadecotCoreStore.Devices.CONTENT_URI,
                    new String[] {
                        KadecotCoreStore.Devices.DeviceColumns.STATUS
                    },
                    KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID + "=?",
                    new String[] {
                        String.valueOf(deviceId)
                    }, null);

            assert cursor.getCount() == 1;
            cursor.moveToFirst();
            boolean status = cursor.getInt(cursor
                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.STATUS)) == 1 ? true
                    : false;
            cursor.close();
            if (status ^ values.getAsBoolean(KadecotCoreStore.Devices.DeviceColumns.STATUS)) {
                int numOfRows = 0;
                numOfRows = provider.update(KadecotCoreStore.Devices.CONTENT_URI, values,
                        KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID + "=?",
                        new String[] {
                            String.valueOf(deviceId)
                        });
                if (numOfRows != 1) {
                    return createError(msg, WampError.INVALID_ARGUMENT);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } finally {
            provider.release();
        }

        try {
            return WampMessageFactory.createYield(
                    msg.getRequestId(),
                    new JSONObject(),
                    new JSONArray(),
                    new JSONObject(msg.getArgumentsKw().toString()).put(
                            KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID, deviceId));
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        }
    }

    @SuppressWarnings("unused")
    private WampMessage putDevice(WampInvocationMessage msg) {
        if (!msg.hasArgumentsKw()) {
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        long deviceId;
        try {
            deviceId = getDeviceId(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        WampMessage reply;
        if (deviceId > 0) {
            return updateDevice(msg, deviceId);
        } else {
            return insertDevice(msg);
        }
    }

    @SuppressWarnings("unused")
    private WampMessage removeDevice(WampInvocationMessage msg) {
        if (!msg.hasArgumentsKw()) {
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        try {
            if (!isDeviceExists(msg)) {
                return createError(msg, WampError.INVALID_ARGUMENT);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        ContentProviderClient provider = mResolver.acquireContentProviderClient(
                KadecotCoreStore.Devices.CONTENT_URI);
        try {
            provider.delete(
                    KadecotCoreStore.Devices.CONTENT_URI,
                    KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID + "=?",
                    new String[] {
                        String.valueOf(msg.getArgumentsKw().getLong(
                                KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID))
                    });
        } catch (RemoteException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } finally {
            provider.release();
        }

        return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
    }

    @SuppressWarnings("unused")
    private WampMessage getDeviceList(WampInvocationMessage msg) {
        ContentProviderClient provider = mResolver.acquireContentProviderClient(
                KadecotCoreStore.Devices.CONTENT_URI);
        Cursor cursor;
        try {
            cursor = provider.query(KadecotCoreStore.Devices.CONTENT_URI, null, null,
                    null, null);
        } catch (RemoteException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } finally {
            provider.release();
        }

        final JSONArray deviceList = new JSONArray();
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            do {
                JSONObject json = new JSONObject();
                try {
                    json.put(
                            KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID,
                            cursor.getLong(cursor
                                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID)));
                    json.put(
                            KadecotCoreStore.Devices.DeviceColumns.PROTOCOL,
                            cursor.getString(cursor
                                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.PROTOCOL)));
                    json.put(
                            KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE,
                            cursor.getString(cursor
                                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE)));
                    json.put(
                            KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION,
                            cursor.getString(cursor
                                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION)));
                    json.put(
                            KadecotCoreStore.Devices.DeviceColumns.STATUS,
                            cursor.getInt(cursor
                                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.STATUS)) == 1 ? true
                                    : false);
                    json.put(
                            KadecotCoreStore.Devices.DeviceColumns.NICKNAME,
                            cursor.getString(cursor
                                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.NICKNAME)));
                } catch (JSONException e) {
                    continue;
                }
                deviceList.put(json);
            } while (cursor.moveToNext());
        }
        cursor.close();

        try {
            return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject(),
                    new JSONArray(),
                    new JSONObject().put("deviceList", deviceList));
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        }
    }

    @SuppressWarnings("unused")
    private WampMessage changeNickname(WampInvocationMessage msg) {
        if (!msg.hasArgumentsKw()) {
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        try {
            if (!isDeviceExists(msg)) {
                return createError(msg, WampError.INVALID_ARGUMENT);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        ContentProviderClient provider = mResolver.acquireContentProviderClient(
                KadecotCoreStore.Devices.CONTENT_URI);

        ContentValues values = new ContentValues();
        JSONObject json = msg.getArgumentsKw();
        int numOfRows = 0;
        try {
            long deviceId = json.getLong(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID);
            values.put(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID, deviceId);
            values.put(KadecotCoreStore.Devices.DeviceColumns.NICKNAME,
                    json.getString(KadecotCoreStore.Devices.DeviceColumns.NICKNAME));
            numOfRows = provider.update(KadecotCoreStore.Devices.CONTENT_URI, values,
                    KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID + "=?",
                    new String[] {
                        String.valueOf(deviceId)
                    });
        } catch (RemoteException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } finally {
            provider.release();
        }

        if (numOfRows != 1) {
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
    }

    private boolean isTopicExists(WampInvocationMessage msg) throws RemoteException,
            JSONException {

        ContentProviderClient provider = mResolver.acquireContentProviderClient(
                KadecotCoreStore.Topics.CONTENT_URI);

        Cursor cursor = provider.query(KadecotCoreStore.Topics.CONTENT_URI,
                new String[] {
                    KadecotCoreStore.Topics.TopicColumns.NAME
                },
                KadecotCoreStore.Topics.TopicColumns.NAME + "=?",
                new String[] {
                    msg.getArgumentsKw().getString(KadecotCoreStore.Topics.TopicColumns.NAME)
                }, null);
        provider.release();
        final int count = cursor.getCount();
        cursor.close();

        if (count > 1) {
            throw new IllegalArgumentException("fatal: NAME is not UNIQUE");
        }

        return (count == 1) ? true : false;
    }

    @SuppressWarnings("unused")
    private WampMessage putTopic(WampInvocationMessage msg) {
        if (!msg.hasArgumentsKw()) {
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        try {
            if (isTopicExists(msg)) {
                return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        ContentValues values = new ContentValues();
        try {
            String topic = msg.getArgumentsKw().getString(
                    KadecotCoreStore.Topics.TopicColumns.NAME);
            values.put(KadecotCoreStore.Topics.TopicColumns.NAME, topic);

            values.put(KadecotCoreStore.Topics.TopicColumns.DESCRIPTION,
                    msg.getArgumentsKw()
                            .getString(KadecotCoreStore.Topics.TopicColumns.DESCRIPTION));
            values.put(KadecotCoreStore.Topics.TopicColumns.PROTOCOL, topic.split("\\.", 5)[3]);
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        ContentProviderClient provider = mResolver
                .acquireContentProviderClient(KadecotCoreStore.Topics.CONTENT_URI);
        try {
            provider.insert(KadecotCoreStore.Topics.CONTENT_URI, values);
        } catch (RemoteException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } finally {
            provider.release();
        }

        return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
    }

    @SuppressWarnings("unused")
    private WampMessage removeTopic(WampInvocationMessage msg) {
        if (!msg.hasArgumentsKw()) {
            return createError(msg, WampError.INVALID_ARGUMENT);
        }
        try {
            if (isTopicExists(msg)) {
                return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        ContentProviderClient provider = mResolver
                .acquireContentProviderClient(KadecotCoreStore.Topics.CONTENT_URI);
        try {
            provider.delete(
                    KadecotCoreStore.Topics.CONTENT_URI,
                    KadecotCoreStore.Topics.TopicColumns.NAME + "=?",
                    new String[] {
                        msg.getArgumentsKw().getString(
                                KadecotCoreStore.Topics.TopicColumns.NAME)
                    });
        } catch (RemoteException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } finally {
            provider.release();
        }

        return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
    }

    private boolean isProcedureExists(WampInvocationMessage msg) throws RemoteException,
            JSONException {

        ContentProviderClient provider = mResolver.acquireContentProviderClient(
                KadecotCoreStore.Procedures.CONTENT_URI);

        Cursor cursor = provider.query(
                KadecotCoreStore.Procedures.CONTENT_URI,
                new String[] {
                    KadecotCoreStore.Procedures.ProcedureColumns.NAME
                },
                KadecotCoreStore.Procedures.ProcedureColumns.NAME + "=?",
                new String[] {
                    msg.getArgumentsKw().getString(
                            KadecotCoreStore.Procedures.ProcedureColumns.NAME)
                }, null);
        provider.release();
        final int count = cursor.getCount();
        cursor.close();

        if (count > 1) {
            throw new IllegalArgumentException("fatal: NAME is not UNIQUE");
        }

        return (count == 1) ? true : false;
    }

    @SuppressWarnings("unused")
    private WampMessage putProcedure(WampInvocationMessage msg) {
        if (!msg.hasArgumentsKw()) {
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        try {
            if (isProcedureExists(msg)) {
                return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        ContentValues values = new ContentValues();
        try {
            String procedure = msg.getArgumentsKw().getString(
                    KadecotCoreStore.Procedures.ProcedureColumns.NAME);
            values.put(
                    KadecotCoreStore.Procedures.ProcedureColumns.NAME, procedure);
            values.put(
                    KadecotCoreStore.Procedures.ProcedureColumns.DESCRIPTION,
                    msg.getArgumentsKw()
                            .getString(KadecotCoreStore.Procedures.ProcedureColumns.DESCRIPTION));
            try {
                values.put(
                        KadecotCoreStore.Procedures.ProcedureColumns.PROTOCOL,
                        procedure.split("\\.", 5)[3]);
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e("ProviderCallee", "procedure=" + procedure + "msg=" + msg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        ContentProviderClient provider = mResolver
                .acquireContentProviderClient(KadecotCoreStore.Procedures.CONTENT_URI);
        try {
            provider.insert(KadecotCoreStore.Procedures.CONTENT_URI, values);
        } catch (RemoteException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } finally {
            provider.release();
        }

        return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
    }

    @SuppressWarnings("unused")
    private WampMessage removeProcedure(WampInvocationMessage msg) {
        if (!msg.hasArgumentsKw()) {
            return createError(msg, WampError.INVALID_ARGUMENT);
        }
        try {
            if (isProcedureExists(msg)) {
                return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        ContentProviderClient provider = mResolver
                .acquireContentProviderClient(KadecotCoreStore.Procedures.CONTENT_URI);
        try {
            provider.delete(
                    KadecotCoreStore.Procedures.CONTENT_URI,
                    KadecotCoreStore.Procedures.ProcedureColumns.NAME + "=?",
                    new String[] {
                        msg.getArgumentsKw().getString(
                                KadecotCoreStore.Procedures.ProcedureColumns.NAME)
                    });
        } catch (RemoteException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } finally {
            provider.release();
        }

        return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
    }

    @SuppressWarnings("unused")
    private WampMessage getTopicList(WampInvocationMessage msg) {
        if (!msg.hasArgumentsKw()) {
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        final JSONArray topics = new JSONArray();
        ContentProviderClient provider = mResolver
                .acquireContentProviderClient(KadecotCoreStore.Topics.CONTENT_URI);
        Cursor cursor;
        try {
            cursor = provider.query(KadecotCoreStore.Topics.CONTENT_URI,
                    new String[] {
                        KadecotCoreStore.Topics.TopicColumns.NAME
                    },
                    KadecotCoreStore.Topics.TopicColumns.PROTOCOL + "=?",
                    new String[] {
                        msg.getArgumentsKw().getString(
                                KadecotCoreStore.Topics.TopicColumns.PROTOCOL)
                    }, null);
        } catch (RemoteException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            do {
                topics.put(cursor.getString(cursor
                        .getColumnIndex(KadecotCoreStore.Topics.TopicColumns.NAME)));
            } while (cursor.moveToNext());
        }

        try {
            return WampMessageFactory.createYield(
                    msg.getRequestId(),
                    new JSONObject(),
                    new JSONArray(),
                    new JSONObject().put("topicList", topics));
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        }
    }

    @SuppressWarnings("unused")
    private WampMessage getProcedureList(WampInvocationMessage msg) {
        if (!msg.hasArgumentsKw()) {
            return createError(msg, WampError.INVALID_ARGUMENT);
        }
        final JSONArray topics = new JSONArray();
        ContentProviderClient provider = mResolver
                .acquireContentProviderClient(KadecotCoreStore.Procedures.CONTENT_URI);
        Cursor cursor;
        try {
            cursor = provider.query(KadecotCoreStore.Procedures.CONTENT_URI,
                    new String[] {
                        KadecotCoreStore.Procedures.ProcedureColumns.NAME
                    },
                    KadecotCoreStore.Procedures.ProcedureColumns.PROTOCOL + "=?",
                    new String[] {
                        msg.getArgumentsKw().getString(
                                KadecotCoreStore.Procedures.ProcedureColumns.PROTOCOL)
                    }, null);
        } catch (RemoteException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } finally {
            provider.release();
        }

        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            do {
                topics.put(cursor.getString(cursor
                        .getColumnIndex(KadecotCoreStore.Procedures.ProcedureColumns.NAME)));
            } while (cursor.moveToNext());
        }

        try {
            return WampMessageFactory.createYield(
                    msg.getRequestId(),
                    new JSONObject(),
                    new JSONArray(),
                    new JSONObject().put("procedureList", topics));
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        }
    }
}
