/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.plugin;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.util.Log;

import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.provider.KadecotCoreStore.DeviceTypeData;
import com.sonycsl.Kadecot.provider.KadecotCoreStore.ProtocolData;

import java.io.ByteArrayOutputStream;
import java.util.Set;

public class ProviderAccessObject {

    private static final String TAG = ProviderAccessObject.class.getSimpleName();

    private ContentResolver mResolver;

    public ProviderAccessObject(ContentResolver resolver) {
        mResolver = resolver;
    }

    /**
     * Put plug-in protocol data to memory cache of Kadecot. <br>
     * If this data is already exist on the cache, update the protocol data. <br>
     * Cached data is clear automatically when the kadecot shut down. <br>
     * 
     * @param protocolData
     * @throws IllegalStateException This is occured when this method can not
     *             access to the kadecot in-memory provider
     */
    public void putProtocolInfo(ProtocolData protocolData) throws IllegalStateException {
        ProtocolData fetchedProtocolData = fetchProtocolInfo(protocolData);
        if (fetchedProtocolData == null) {
            insertProtocolInfo(protocolData);
        } else {
            updateProtocolData(protocolData);
        }
    }

    /**
     * Insert plug-in protocol data to memory cache of Kadecot. <br>
     * Cached data is clear automatically when the kadecot shut down.
     * 
     * @param protocol
     * @throws RemoteException
     */
    private void insertProtocolInfo(ProtocolData protocol) {
        ContentProviderClient provider = mResolver.acquireContentProviderClient(
                KadecotCoreStore.Protocols.CONTENT_URI);

        if (provider == null) {
            throw new IllegalStateException("Can not access to kadecot in-memory provider");
        }

        ContentValues protocolData = new ContentValues();
        protocolData.put(KadecotCoreStore.Protocols.ProtocolColumns.PROTOCOL,
                protocol.getProtocol());
        protocolData.put(KadecotCoreStore.Protocols.ProtocolColumns.PACKAGE_NAME,
                protocol.getPackageName());
        protocolData.put(KadecotCoreStore.Protocols.ProtocolColumns.ACTIVITY_NAME,
                protocol.getActivityName());

        try {
            provider.insert(KadecotCoreStore.Protocols.CONTENT_URI, protocolData);
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            provider.release();
        }
    }

    private ProtocolData fetchProtocolInfo(ProtocolData protocolData) {
        ContentProviderClient provider = mResolver.acquireContentProviderClient(
                KadecotCoreStore.Protocols.CONTENT_URI);

        if (provider == null) {
            throw new IllegalStateException("Can not access to kadecot in-memory provider");
        }

        Cursor cursor;
        try {
            cursor = provider.query(KadecotCoreStore.Protocols.CONTENT_URI,
                    new String[] {
                            KadecotCoreStore.Protocols.ProtocolColumns.PROTOCOL,
                            KadecotCoreStore.Protocols.ProtocolColumns.PACKAGE_NAME,
                            KadecotCoreStore.Protocols.ProtocolColumns.ACTIVITY_NAME
                    },
                    KadecotCoreStore.Protocols.ProtocolColumns.PROTOCOL + "=?",
                    new String[] {
                        protocolData.getProtocol(),
                    }, null);
            cursor.moveToFirst();
            final int count = cursor.getCount();
            if (count > 1) {
                throw new IllegalStateException("fatal: Protocol name is not UNIQUE");
            }

            ProtocolData fetchedProtocol = null;

            if (count == 1) {
                final String protocolName = cursor
                        .getString(cursor
                                .getColumnIndex(KadecotCoreStore.Protocols.ProtocolColumns.PROTOCOL));
                final String packageName = cursor
                        .getString(cursor
                                .getColumnIndex(KadecotCoreStore.Protocols.ProtocolColumns.PACKAGE_NAME));
                final String activityName = cursor
                        .getString(cursor
                                .getColumnIndex(KadecotCoreStore.Protocols.ProtocolColumns.ACTIVITY_NAME));

                fetchedProtocol = new ProtocolData(protocolName, packageName, activityName);
            }

            cursor.close();

            return fetchedProtocol;
        } catch (RemoteException e) {
            return null;
        } finally {
            provider.release();
        }
    }

    private void updateProtocolData(ProtocolData protocolData) {
        ContentProviderClient provider = mResolver.acquireContentProviderClient(
                KadecotCoreStore.Protocols.CONTENT_URI);

        if (provider == null) {
            throw new IllegalStateException("Can not access to kadecot in-memory provider");
        }

        try {
            int numOfRows = provider.update(KadecotCoreStore.Protocols.CONTENT_URI,
                    convertTo(protocolData),
                    KadecotCoreStore.Protocols.ProtocolColumns.PROTOCOL + "=?",
                    new String[] {
                        protocolData.getProtocol()
                    });
            if (numOfRows != 1) {
                throw new IllegalStateException("fatal: Protocol name is not UNIQUE");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            provider.release();
        }
    }

    private ContentValues convertTo(ProtocolData protocolData) {
        ContentValues values = new ContentValues();
        values.put(KadecotCoreStore.Protocols.ProtocolColumns.PROTOCOL,
                protocolData.getProtocol());
        values.put(KadecotCoreStore.Protocols.ProtocolColumns.PACKAGE_NAME,
                protocolData.getPackageName());
        values.put(KadecotCoreStore.Protocols.ProtocolColumns.ACTIVITY_NAME,
                protocolData.getActivityName());
        return values;
    }

    /**
     * @param deviceTypes
     * @throws IllegalStateException This is occured when this method can not
     *             access to the kadecot in-memory provider
     */
    public void putDeviceTypesInfo(Set<DeviceTypeData> deviceTypes) throws IllegalArgumentException {
        for (DeviceTypeData data : deviceTypes) {
            DeviceTypeData fetchedDeviceType = fetchDeviceType(data);
            if (fetchedDeviceType == null) {
                insertDeviceTypesInfo(data);
            } else {
                updateDeviceTypesInfo(data);
            }
        }
    }

    private DeviceTypeData fetchDeviceType(DeviceTypeData deviceTypeData) {
        ContentProviderClient provider = mResolver.acquireContentProviderClient(
                KadecotCoreStore.DeviceTypes.CONTENT_URI);

        if (provider == null) {
            throw new IllegalStateException("Can not access to kadecot in-memory provider");
        }

        try {
            final Cursor cursor = provider.query(KadecotCoreStore.DeviceTypes.CONTENT_URI,
                    new String[] {
                            KadecotCoreStore.DeviceTypes.DeviceTypeColumns.DEVICE_TYPE,
                            KadecotCoreStore.DeviceTypes.DeviceTypeColumns.PROTOCOL,
                            KadecotCoreStore.DeviceTypes.DeviceTypeColumns.ICON
                    },
                    KadecotCoreStore.DeviceTypes.DeviceTypeColumns.DEVICE_TYPE + "=? and "
                            + KadecotCoreStore.DeviceTypes.DeviceTypeColumns.PROTOCOL
                            + "=?",
                    new String[] {
                            deviceTypeData.getDeviceType(),
                            deviceTypeData.getProtocol()
                    }, null);
            cursor.moveToFirst();
            final int count = cursor.getCount();
            if (count > 1) {
                throw new IllegalStateException(
                        "fatal: Device type and Protocol name is not UNIQUE");
            }

            DeviceTypeData fetchedDeviceType = null;
            if (count == 1) {
                final String deviceType = cursor
                        .getString(cursor
                                .getColumnIndex(KadecotCoreStore.DeviceTypes.DeviceTypeColumns.DEVICE_TYPE));
                final String protocolName = cursor
                        .getString(cursor
                                .getColumnIndex(KadecotCoreStore.DeviceTypes.DeviceTypeColumns.PROTOCOL));
                final byte[] iconByte = cursor
                        .getBlob(cursor
                                .getColumnIndex(KadecotCoreStore.DeviceTypes.DeviceTypeColumns.ICON));
                final Bitmap icon = BitmapFactory.decodeByteArray(iconByte, 0, iconByte.length);
                fetchedDeviceType = new DeviceTypeData(deviceType, protocolName, icon);
            }

            cursor.close();

            return fetchedDeviceType;
        } catch (RemoteException e) {
            return null;
        } finally {
            provider.release();
        }
    }

    private void updateDeviceTypesInfo(DeviceTypeData deviceType) {
        ContentProviderClient provider = mResolver.acquireContentProviderClient(
                KadecotCoreStore.DeviceTypes.CONTENT_URI);

        if (provider == null) {
            throw new IllegalStateException("Can not access to kadecot in-memory provider");
        }

        try {
            int numOfRows = provider.update(KadecotCoreStore.DeviceTypes.CONTENT_URI,
                    convertTo(deviceType),
                    KadecotCoreStore.DeviceTypes.DeviceTypeColumns.DEVICE_TYPE + "=? and "
                            + KadecotCoreStore.DeviceTypes.DeviceTypeColumns.PROTOCOL
                            + "=?",
                    new String[] {
                            deviceType.getDeviceType(),
                            deviceType.getProtocol()
                    });
            if (numOfRows != 1) {
                throw new IllegalStateException("fatal: Protocol name is not UNIQUE");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            provider.release();
        }
    }

    private ContentValues convertTo(DeviceTypeData deviceType) {
        ContentValues values = new ContentValues();
        values.put(KadecotCoreStore.DeviceTypes.DeviceTypeColumns.DEVICE_TYPE,
                deviceType.getDeviceType());
        values.put(KadecotCoreStore.DeviceTypes.DeviceTypeColumns.PROTOCOL,
                deviceType.getProtocol());

        final byte[] iconByteArray = toByteArray(deviceType.getIcon());
        if (iconByteArray != null) {
            values.put(KadecotCoreStore.DeviceTypes.DeviceTypeColumns.ICON, iconByteArray);
        }
        return values;
    }

    private void insertDeviceTypesInfo(DeviceTypeData devType) {
        ContentProviderClient provider = mResolver
                .acquireContentProviderClient(KadecotCoreStore.DeviceTypes.CONTENT_URI);

        try {
            ContentValues deviceTypeData = new ContentValues();
            deviceTypeData.put(
                    KadecotCoreStore.DeviceTypes.DeviceTypeColumns.DEVICE_TYPE,
                    devType.getDeviceType());
            deviceTypeData.put(KadecotCoreStore.DeviceTypes.DeviceTypeColumns.PROTOCOL,
                    devType.getProtocol());

            byte[] iconByte = toByteArray(devType.getIcon());

            if (iconByte != null) {
                deviceTypeData.put(KadecotCoreStore.DeviceTypes.DeviceTypeColumns.ICON,
                        iconByte);
            }

            provider.insert(KadecotCoreStore.DeviceTypes.CONTENT_URI,
                    deviceTypeData);

        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            provider.release();
        }
    }

    private byte[] toByteArray(Bitmap icon) {
        if (icon == null) {
            return null;
        }
        ByteArrayOutputStream iconByteStream = new ByteArrayOutputStream();
        if (!icon.compress(Bitmap.CompressFormat.PNG, 100, iconByteStream)) {
            Log.e(TAG, "Icon is not a PNG format");
        }
        return iconByteStream.toByteArray();
    }
}
