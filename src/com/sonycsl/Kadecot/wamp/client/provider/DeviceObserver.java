/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp.client.provider;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.RemoteException;
import android.text.format.DateFormat;

import com.sonycsl.Kadecot.provider.KadecotCoreStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class DeviceObserver {

    public interface OnDeviceChangedListener {
        public void onDeviceFound(JSONObject device);
    }

    private final ContentResolver mResolver;
    private String mLastUpdated;
    private OnDeviceChangedListener mListener;

    public DeviceObserver(ContentResolver resolver, Handler handler) {
        mResolver = resolver;
        mLastUpdated = DateFormat.format("yyyy-MM-dd kk:mm:ss", Calendar.getInstance())
                .toString();
        mResolver.registerContentObserver(KadecotCoreStore.Devices.CONTENT_URI, true,
                new ContentObserver(handler) {

                    @Override
                    public void onChange(boolean selfChange) {
                        ContentProviderClient provider = mResolver
                                .acquireContentProviderClient(KadecotCoreStore.Devices.CONTENT_URI);
                        Cursor cursor;
                        try {
                            cursor = provider.query(KadecotCoreStore.Devices.CONTENT_URI,
                                    new String[] {
                                            KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID,
                                            KadecotCoreStore.Devices.DeviceColumns.PROTOCOL,
                                            KadecotCoreStore.Devices.DeviceColumns.UUID,
                                            KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE,
                                            KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION,
                                            KadecotCoreStore.Devices.DeviceColumns.STATUS,
                                            KadecotCoreStore.Devices.DeviceColumns.NICKNAME,
                                            KadecotCoreStore.Devices.DeviceColumns.LOCAL_UPDATED
                                    },
                                    KadecotCoreStore.Devices.DeviceColumns.LOCAL_UPDATED + " >= ?",
                                    new String[] {
                                        mLastUpdated
                                    },
                                    KadecotCoreStore.Devices.DeviceColumns.LOCAL_UPDATED + " asc");
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            return;

                        }
                        if (cursor.getCount() == 0) {
                            cursor.close();
                            return;
                        }
                        if (mListener != null) {
                            cursor.moveToFirst();
                            do {
                                JSONObject device = new JSONObject();
                                try {
                                    device.put(
                                            KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID,
                                            cursor.getLong(cursor
                                                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID)));
                                    device.put(
                                            KadecotCoreStore.Devices.DeviceColumns.PROTOCOL,
                                            cursor.getString(cursor
                                                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.PROTOCOL)));
                                    device.put(
                                            KadecotCoreStore.Devices.DeviceColumns.UUID,
                                            cursor.getString(cursor
                                                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.UUID)));

                                    device.put(
                                            KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE,
                                            cursor.getString(cursor
                                                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE)));

                                    device.put(
                                            KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION,
                                            cursor.getString(cursor
                                                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION)));

                                    device.put(
                                            KadecotCoreStore.Devices.DeviceColumns.STATUS,
                                            cursor.getInt(cursor
                                                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.STATUS)) == 1 ? true
                                                    : false);
                                    device.put(
                                            KadecotCoreStore.Devices.DeviceColumns.NICKNAME,
                                            cursor.getString(cursor
                                                    .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.NICKNAME)));
                                    mListener.onDeviceFound(device);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } while (cursor.moveToNext());
                        }

                        cursor.moveToLast();
                        mLastUpdated = cursor.getString(cursor
                                .getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.LOCAL_UPDATED));

                        cursor.close();
                    }

                });
    }

    public void setOnDeviceChangedListener(OnDeviceChangedListener listener) {
        mListener = listener;
    }
}
