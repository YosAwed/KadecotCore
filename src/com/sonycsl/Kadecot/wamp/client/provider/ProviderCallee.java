/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp.client.provider;

import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.RemoteException;

import com.sonycsl.Kadecot.net.ConnectivityManagerUtil;
import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.wamp.client.provider.WampProviderAccessHelper.Procedure;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProviderCallee extends WampCallee {

    private static final String PROVIDER_INTERNAL_ERROR = "provider internal error";

    private final Context mContext;

    public ProviderCallee(Context context) throws RemoteException {
        mContext = context;
    }

    private WampMessage createError(WampInvocationMessage msg, String error) {
        return WampMessageFactory.createError(WampMessageType.INVOCATION,
                msg.getRequestId(), new JSONObject(), error);
    }

    @Override
    protected void invocation(String procedure, WampMessage msg,
            WampInvocationReplyListener listener) {

        if (!msg.isInvocationMessage()) {
            throw new IllegalArgumentException();
        }

        WampInvocationMessage iMsg = msg.asInvocationMessage();
        Procedure proc = Procedure.getEnum(procedure);
        if (proc == null) {
            listener.replyError(createError(iMsg, WampError.NO_SUCH_PROCEDURE).asErrorMessage());
            return;
        }

        try {
            WampMessage reply = (WampMessage) ProviderCallee.this.getClass()
                    .getDeclaredMethod(proc.getMethod(),
                            WampInvocationMessage.class).invoke(ProviderCallee.this, msg);
            if (reply.isYieldMessage()) {
                listener.replyYield(reply.asYieldMessage());
                return;
            } else if (reply.isErrorMessage()) {
                listener.replyError(reply.asErrorMessage());
                return;
            } else {
                listener.replyError(WampMessageFactory.createError(msg.getMessageType(), msg
                        .asInvocationMessage().getRequestId(), new JSONObject(),
                        PROVIDER_INTERNAL_ERROR).asErrorMessage());
                return;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        listener.replyError(createError(iMsg, WampError.INVALID_ARGUMENT).asErrorMessage());
    }

    private boolean isDeviceExists(WampInvocationMessage msg) throws RemoteException,
            JSONException {
        ContentProviderClient provider = mContext.getContentResolver()
                .acquireContentProviderClient(KadecotCoreStore.Devices.CONTENT_URI);

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

        ContentProviderClient provider = mContext.getContentResolver()
                .acquireContentProviderClient(KadecotCoreStore.Devices.CONTENT_URI);

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
        if (json.has(KadecotCoreStore.Devices.DeviceColumns.PROTOCOL)) {
            values.put(KadecotCoreStore.Devices.DeviceColumns.PROTOCOL,
                    json.getString(KadecotCoreStore.Devices.DeviceColumns.PROTOCOL));
        }
        if (json.has(KadecotCoreStore.Devices.DeviceColumns.UUID)) {
            values.put(KadecotCoreStore.Devices.DeviceColumns.UUID,
                    json.getString(KadecotCoreStore.Devices.DeviceColumns.UUID));
        }
        if (json.has(KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE)) {
            values.put(KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE,
                    json.getString(KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE));
        }
        if (json.has(KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION)) {
            values.put(KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION,
                    json.getString(KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION));
            /* fail-safe */
            values.put(KadecotCoreStore.Devices.DeviceColumns.NICKNAME,
                    json.getString(KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION));
        }
        if (json.has(KadecotCoreStore.Devices.DeviceColumns.STATUS)) {
            values.put(KadecotCoreStore.Devices.DeviceColumns.STATUS,
                    json.getBoolean(KadecotCoreStore.Devices.DeviceColumns.STATUS) ? 1 : 0);
        }
        if (json.has(KadecotCoreStore.Devices.DeviceColumns.NICKNAME)) {
            values.put(KadecotCoreStore.Devices.DeviceColumns.NICKNAME,
                    json.getString(KadecotCoreStore.Devices.DeviceColumns.NICKNAME));
        }
        if (json.has(KadecotCoreStore.Devices.DeviceColumns.IP_ADDR)) {
            values.put(KadecotCoreStore.Devices.DeviceColumns.IP_ADDR,
                    json.getString(KadecotCoreStore.Devices.DeviceColumns.IP_ADDR));
        }
        if (json.has(KadecotCoreStore.Devices.DeviceColumns.BSSID)) {
            values.put(KadecotCoreStore.Devices.DeviceColumns.BSSID,
                    json.getString(KadecotCoreStore.Devices.DeviceColumns.BSSID));
        } else {
            values.put(KadecotCoreStore.Devices.DeviceColumns.BSSID,
                    ConnectivityManagerUtil.getWifiInfo(mContext).getBSSID());
        }

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

        ContentProviderClient provider = mContext.getContentResolver()
                .acquireContentProviderClient(KadecotCoreStore.Devices.CONTENT_URI);
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
        values.remove(KadecotCoreStore.Devices.DeviceColumns.NICKNAME);

        ContentProviderClient provider = mContext.getContentResolver()
                .acquireContentProviderClient(KadecotCoreStore.Devices.CONTENT_URI);
        try {
            Cursor cursor = provider.query(KadecotCoreStore.Devices.CONTENT_URI,
                    new String[] {
                        KadecotCoreStore.Devices.DeviceColumns.STATUS
                    },
                    KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID + "=?",
                    new String[] {
                        String.valueOf(deviceId)
                    }, null);

            cursor.moveToFirst();
            boolean status = cursor.getInt(cursor
                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.STATUS)) == 1 ? true
                    : false;
            cursor.close();
            if (status ^ (values.getAsInteger(KadecotCoreStore.Devices.DeviceColumns.STATUS) == 1)) {
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

        ContentProviderClient provider = mContext.getContentResolver()
                .acquireContentProviderClient(KadecotCoreStore.Devices.CONTENT_URI);
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
        ContentProviderClient provider = mContext.getContentResolver()
                .acquireContentProviderClient(KadecotCoreStore.Devices.CONTENT_URI);
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
                    json.put(
                            KadecotCoreStore.Devices.DeviceColumns.IP_ADDR,
                            cursor.getString(cursor
                                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.IP_ADDR)));

                    JSONObject location = new JSONObject();
                    location.put("main", cursor.getString(cursor
                            .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.LOCATION)));
                    location.put("sub", cursor.getString(cursor
                            .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.SUB_LOCATION)));
                    json.put(KadecotCoreStore.Devices.DeviceColumns.LOCATION, location);
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

        ContentProviderClient provider = mContext.getContentResolver()
                .acquireContentProviderClient(KadecotCoreStore.Devices.CONTENT_URI);

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

    private String getProtocol(WampInvocationMessage msg) {
        Iterator<?> protocolKeys = msg.getArgumentsKw().keys();
        return (String) protocolKeys.next();
    }

    private boolean isTopicExists(WampInvocationMessage msg) throws RemoteException,
            JSONException {

        ContentProviderClient provider = mContext.getContentResolver()
                .acquireContentProviderClient(KadecotCoreStore.Topics.CONTENT_URI);

        Cursor cursor = provider.query(KadecotCoreStore.Topics.CONTENT_URI,
                new String[] {
                    KadecotCoreStore.Topics.TopicColumns.PROTOCOL
                },
                KadecotCoreStore.Topics.TopicColumns.PROTOCOL + "=?",
                new String[] {
                    getProtocol(msg)
                }, null);
        provider.release();
        final int count = cursor.getCount();
        cursor.close();

        return (count > 0) ? true : false;
    }

    @SuppressWarnings("unused")
    private WampMessage putTopics(WampInvocationMessage msg) {
        if (!msg.hasArgumentsKw() || msg.getArgumentsKw().length() != 1) {
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

        List<ContentValues> valuesList = new ArrayList<ContentValues>();
        String protocol = getProtocol(msg);

        try {
            JSONObject topics = msg.getArgumentsKw().getJSONObject(protocol);
            Iterator<?> topicKeys = topics.keys();
            while (topicKeys.hasNext()) {
                String topic = (String) topicKeys.next();
                ContentValues values = new ContentValues();
                values.put(KadecotCoreStore.Topics.TopicColumns.PROTOCOL, protocol);
                values.put(KadecotCoreStore.Topics.TopicColumns.NAME, topic);
                values.put(KadecotCoreStore.Topics.TopicColumns.DESCRIPTION,
                        topics.getString(topic));
                valuesList.add(values);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        ContentProviderClient provider = mContext.getContentResolver()
                .acquireContentProviderClient(KadecotCoreStore.Topics.CONTENT_URI);
        try {
            provider.bulkInsert(KadecotCoreStore.Topics.CONTENT_URI,
                    valuesList.toArray(new ContentValues[valuesList.size()]));
        } catch (RemoteException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } finally {
            provider.release();
        }

        return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
    }

    @SuppressWarnings("unused")
    private WampMessage removeTopics(WampInvocationMessage msg) {
        if (!msg.hasArgumentsKw() || msg.getArgumentsKw().length() != 1) {
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        ContentProviderClient provider = mContext.getContentResolver()
                .acquireContentProviderClient(KadecotCoreStore.Topics.CONTENT_URI);
        try {
            provider.delete(
                    KadecotCoreStore.Topics.CONTENT_URI,
                    KadecotCoreStore.Topics.TopicColumns.PROTOCOL + "=?",
                    new String[] {
                        msg.getArgumentsKw().getString(
                                KadecotCoreStore.Topics.TopicColumns.PROTOCOL)
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

        ContentProviderClient provider = mContext.getContentResolver()
                .acquireContentProviderClient(KadecotCoreStore.Procedures.CONTENT_URI);

        Cursor cursor = provider.query(
                KadecotCoreStore.Procedures.CONTENT_URI,
                new String[] {
                    KadecotCoreStore.Procedures.ProcedureColumns.PROTOCOL
                },
                KadecotCoreStore.Procedures.ProcedureColumns.PROTOCOL + "=?",
                new String[] {
                    getProtocol(msg)
                }, null);
        provider.release();
        final int count = cursor.getCount();
        cursor.close();

        return (count > 0) ? true : false;
    }

    @SuppressWarnings("unused")
    private WampMessage putProcedures(WampInvocationMessage msg) {
        if (!msg.hasArgumentsKw() || msg.getArgumentsKw().length() != 1) {
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

        List<ContentValues> valuesList = new ArrayList<ContentValues>();
        String protocol = getProtocol(msg);

        try {
            JSONObject procs = msg.getArgumentsKw().getJSONObject(protocol);
            Iterator<?> procKeys = procs.keys();
            while (procKeys.hasNext()) {
                String proc = (String) procKeys.next();
                ContentValues values = new ContentValues();
                values.put(KadecotCoreStore.Procedures.ProcedureColumns.PROTOCOL, protocol);
                values.put(KadecotCoreStore.Procedures.ProcedureColumns.NAME, proc);
                values.put(KadecotCoreStore.Procedures.ProcedureColumns.DESCRIPTION,
                        procs.getString(proc));
                valuesList.add(values);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        ContentProviderClient provider = mContext.getContentResolver()
                .acquireContentProviderClient(KadecotCoreStore.Procedures.CONTENT_URI);
        try {
            provider.bulkInsert(KadecotCoreStore.Procedures.CONTENT_URI,
                    valuesList.toArray(new ContentValues[valuesList.size()]));
        } catch (RemoteException e) {
            e.printStackTrace();
            return createError(msg, WampError.INVALID_ARGUMENT);
        } finally {
            provider.release();
        }

        return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
    }

    @SuppressWarnings("unused")
    private WampMessage removeProcedures(WampInvocationMessage msg) {
        if (!msg.hasArgumentsKw() || msg.getArgumentsKw().length() != 1) {
            return createError(msg, WampError.INVALID_ARGUMENT);
        }

        ContentProviderClient provider = mContext.getContentResolver()
                .acquireContentProviderClient(KadecotCoreStore.Procedures.CONTENT_URI);
        try {
            provider.delete(
                    KadecotCoreStore.Procedures.CONTENT_URI,
                    KadecotCoreStore.Procedures.ProcedureColumns.PROTOCOL + "=?",
                    new String[] {
                        msg.getArgumentsKw().getString(
                                KadecotCoreStore.Procedures.ProcedureColumns.PROTOCOL)
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
        ContentProviderClient provider = mContext.getContentResolver()
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
        ContentProviderClient provider = mContext.getContentResolver()
                .acquireContentProviderClient(KadecotCoreStore.Procedures.CONTENT_URI);
        Cursor cursor;
        try {
            cursor = provider.query(KadecotCoreStore.Procedures.CONTENT_URI,
                    new String[] {
                            KadecotCoreStore.Procedures.ProcedureColumns.NAME,
                            KadecotCoreStore.Procedures.ProcedureColumns.DESCRIPTION
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

        try {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    JSONObject procedure = new JSONObject();
                    procedure.put("procedure", cursor.getString(cursor
                            .getColumnIndex(KadecotCoreStore.Procedures.ProcedureColumns.NAME)));
                    procedure
                            .put("description",
                                    cursor.getString(cursor
                                            .getColumnIndex(KadecotCoreStore.Procedures.ProcedureColumns.DESCRIPTION)));
                    topics.put(procedure);
                } while (cursor.moveToNext());
            }
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
