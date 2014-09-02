/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.test.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.ProviderTestCase2;

import com.sonycsl.Kadecot.provider.InMemoryProvider;
import com.sonycsl.Kadecot.provider.KadecotCoreStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class InMemoryProviderTestCase extends ProviderTestCase2<InMemoryProvider> {

    private static enum ProtocolData {

        Protocol1("protocol1", "com.sonycsl.Kadecot.plugin1",
                "com.sonycsl.Kadecot.plugin1.MainActivity"),
        Protocol2("protocol2", "com.sonycsl.Kadecot.plugin2",
                "com.sonycsl.Kadecot.plugin2.MainActivity");

        private String mProtocol;
        private String mPackageName;
        private String mActivityName;

        ProtocolData(String protocol, String packageName, String activityName) {
            mProtocol = protocol;
            mPackageName = packageName;
            mActivityName = activityName;
        }

        public String getProtocol() {
            return mProtocol;
        }

        public String getPackageName() {
            return mPackageName;
        }

        public String getActivityName() {
            return mActivityName;
        }
    }

    private static class DeviceTypeData {

        private String mDeviceType;
        private String mProtocol;
        private Bitmap mIcon;

        DeviceTypeData(String deviceType, String protocol, Bitmap icon) {
            mDeviceType = deviceType;
            mProtocol = protocol;
            mIcon = icon;
        }

        public String getDeviceType() {
            return mDeviceType;
        }

        public String getProtocol() {
            return mProtocol;
        }

        public Bitmap getIcon() {
            return mIcon;
        }
    }

    public InMemoryProviderTestCase() {
        super(InMemoryProvider.class, InMemoryProvider.AUTHORITY);
    }

    private InMemoryProvider mProvider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mProvider = getProvider();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCtor() {
        assertNotNull(mProvider);
    }

    public void testOnCreate() {
        assertTrue(mProvider.onCreate());
    }

    public void testProtocolInsert() {
        for (ProtocolData data : ProtocolData.values()) {
            ContentValues values = new ContentValues();
            values.put(KadecotCoreStore.Protocols.ProtocolColumns.PROTOCOL,
                    data.getProtocol());
            values.put(KadecotCoreStore.Protocols.ProtocolColumns.PACKAGE_NAME,
                    data.getPackageName());
            values.put(KadecotCoreStore.Protocols.ProtocolColumns.ACTIVITY_NAME,
                    data.getActivityName());
            assertEquals(ContentUris.withAppendedId(KadecotCoreStore.Protocols.CONTENT_URI,
                    data.ordinal() + 1),
                    mProvider.insert(KadecotCoreStore.Protocols.CONTENT_URI, values));
        }

        Cursor c = mProvider.query(KadecotCoreStore.Protocols.CONTENT_URI, null, null,
                null, null);
        c.moveToFirst();
        assertEquals(ProtocolData.values().length, c.getCount());

        for (ProtocolData data : ProtocolData.values()) {
            assertEquals(
                    data.getProtocol(),
                    c.getString(c
                            .getColumnIndexOrThrow(KadecotCoreStore.Protocols.ProtocolColumns.PROTOCOL)));
            assertEquals(
                    data.getPackageName(),
                    c.getString(c
                            .getColumnIndexOrThrow(KadecotCoreStore.Protocols.ProtocolColumns.PACKAGE_NAME)));
            assertEquals(
                    data.getActivityName(),
                    c.getString(c
                            .getColumnIndexOrThrow(KadecotCoreStore.Protocols.ProtocolColumns.ACTIVITY_NAME)));

            c.moveToNext();
        }

        c.close();
    }

    public void testProtocolDelete() {
        testProtocolInsert();
        for (ProtocolData data : ProtocolData.values()) {
            mProvider.delete(KadecotCoreStore.Protocols.CONTENT_URI,
                    KadecotCoreStore.Protocols.ProtocolColumns.PROTOCOL
                            + "=?",
                    new String[] {
                        String.valueOf(data.getProtocol())
                    });
            Cursor c = mProvider
                    .query(KadecotCoreStore.Protocols.CONTENT_URI, null, null, null, null);
            assertEquals(ProtocolData.values().length - data.ordinal() - 1, c.getCount());
            c.close();
        }
    }

    public void testProtocolUpdate() {
        testProtocolInsert();
        String renamedActivity = "renamedactivity";

        for (ProtocolData data : ProtocolData.values()) {
            ContentValues values = new ContentValues();
            values.put(KadecotCoreStore.Protocols.ProtocolColumns.ACTIVITY_NAME,
                    renamedActivity);
            assertEquals(1,
                    mProvider.update(KadecotCoreStore.Protocols.CONTENT_URI, values,
                            KadecotCoreStore.Protocols.ProtocolColumns.PROTOCOL
                                    + "=?",
                            new String[] {
                                String.valueOf(data.getProtocol())
                            }));
        }
        Cursor c = mProvider.query(KadecotCoreStore.Protocols.CONTENT_URI, null,
                null, null, null);
        c.moveToFirst();
        assertEquals(ProtocolData.values().length, c.getCount());
        do {
            assertEquals(
                    renamedActivity,
                    c.getString(c
                            .getColumnIndex(KadecotCoreStore.Protocols.ProtocolColumns.ACTIVITY_NAME)));
        } while (c.moveToNext());
        c.close();
    }

    public void testDeviceTypeInsert() {
        DeviceTypeData[] devTypeData = new DeviceTypeData[2];
        try {
            devTypeData[0] = new DeviceTypeData("measure", "echonetlite",
                    BitmapFactory.decodeStream(getContext()
                            .getAssets().open(
                                    "icons/measure.png")));
            devTypeData[1] = new DeviceTypeData("lighting", "scalarwebapi",
                    BitmapFactory.decodeStream(getContext()
                            .getAssets().open(
                                    "icons/lighting.png")));
        } catch (IOException e) {
            fail();
        }

        int cnt = 0;
        for (DeviceTypeData data : devTypeData) {
            ContentValues values = new ContentValues();
            values.put(KadecotCoreStore.DeviceTypes.DeviceTypeColumns.DEVICE_TYPE,
                    data.getDeviceType());
            values.put(KadecotCoreStore.DeviceTypes.DeviceTypeColumns.PROTOCOL,
                    data.getProtocol());

            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            if (data.getIcon().compress(Bitmap.CompressFormat.PNG, 100,
                    byteArray)) {
                values.put(KadecotCoreStore.DeviceTypes.DeviceTypeColumns.ICON,
                        byteArray.toByteArray());
            } else {
                fail();
            }
            assertEquals(
                    ContentUris.withAppendedId(KadecotCoreStore.DeviceTypes.CONTENT_URI,
                            cnt++ + 1),
                    mProvider.insert(KadecotCoreStore.DeviceTypes.CONTENT_URI, values));
        }

        Cursor c = mProvider.query(KadecotCoreStore.DeviceTypes.CONTENT_URI, null, null,
                null, null);
        c.moveToFirst();
        assertEquals(devTypeData.length, c.getCount());

        for (DeviceTypeData data : devTypeData) {
            assertEquals(
                    data.getDeviceType(),
                    c.getString(c
                            .getColumnIndexOrThrow(KadecotCoreStore.DeviceTypes.DeviceTypeColumns.DEVICE_TYPE)));
            assertEquals(
                    data.getProtocol(),
                    c.getString(c
                            .getColumnIndexOrThrow(KadecotCoreStore.DeviceTypes.DeviceTypeColumns.PROTOCOL)));

            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            if (data.getIcon().compress(Bitmap.CompressFormat.PNG, 100,
                    byteArray)) {
                assertTrue(Arrays
                        .equals(byteArray.toByteArray(),
                                c.getBlob(c
                                        .getColumnIndexOrThrow(KadecotCoreStore.DeviceTypes.DeviceTypeColumns.ICON))));
            } else {
                fail();
            }

            c.moveToNext();
        }

        c.close();
    }

    public void testDeviceTypeDelete() {
        testDeviceTypeInsert();
        DeviceTypeData[] devTypeData = new DeviceTypeData[2];
        try {
            devTypeData[0] = new DeviceTypeData("measure", "echonetlite",
                    BitmapFactory.decodeStream(getContext()
                            .getAssets().open(
                                    "icons/measure.png")));
            devTypeData[1] = new DeviceTypeData("lighting", "scalarwebapi",
                    BitmapFactory.decodeStream(getContext()
                            .getAssets().open(
                                    "icons/lighting.png")));
        } catch (IOException e) {
            fail();
        }
        int cnt = 0;
        for (DeviceTypeData data : devTypeData) {
            mProvider.delete(KadecotCoreStore.DeviceTypes.CONTENT_URI,
                    KadecotCoreStore.DeviceTypes.DeviceTypeColumns.DEVICE_TYPE
                            + "=?",
                    new String[] {
                        String.valueOf(data.getDeviceType())
                    });
            Cursor c = mProvider
                    .query(KadecotCoreStore.DeviceTypes.CONTENT_URI, null, null, null,
                            null);
            assertEquals(devTypeData.length - cnt++ - 1,
                    c.getCount());
            c.close();
        }
    }

    public void testDeviceTypeUpdate() {
        testDeviceTypeInsert();
        DeviceTypeData[] devTypeData = new DeviceTypeData[2];
        try {
            devTypeData[0] = new DeviceTypeData("measure", "echonetlite",
                    BitmapFactory.decodeStream(getContext()
                            .getAssets().open(
                                    "icons/measure.png")));
            devTypeData[1] = new DeviceTypeData("lighting", "scalarwebapi",
                    BitmapFactory.decodeStream(getContext()
                            .getAssets().open(
                                    "icons/lighting.png")));
        } catch (IOException e) {
            fail();
        }
        String renamedProtocol = "renameprotocol";

        for (DeviceTypeData data : devTypeData) {
            ContentValues values = new ContentValues();
            values.put(KadecotCoreStore.DeviceTypes.DeviceTypeColumns.PROTOCOL,
                    renamedProtocol);
            assertEquals(1,
                    mProvider.update(KadecotCoreStore.DeviceTypes.CONTENT_URI, values,
                            KadecotCoreStore.DeviceTypes.DeviceTypeColumns.DEVICE_TYPE
                                    + "=?",
                            new String[] {
                                String.valueOf(data.getDeviceType())
                            }));
        }
        Cursor c =
                mProvider.query(KadecotCoreStore.DeviceTypes.CONTENT_URI, null,
                        null, null, null);
        c.moveToFirst();
        assertEquals(devTypeData.length, c.getCount());
        do {
            assertEquals(
                    renamedProtocol,
                    c.getString(c
                            .getColumnIndex(KadecotCoreStore.DeviceTypes.DeviceTypeColumns.PROTOCOL)));
        } while (c.moveToNext());
        c.close();
    }
}
