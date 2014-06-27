
package com.sonycsl.test.Kadecot.core.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.test.ProviderTestCase2;

import com.sonycsl.Kadecot.core.provider.KadecotCoreProvider;
import com.sonycsl.Kadecot.core.provider.KadecotCoreStore;

public class KadecotCoreProviderTestCase extends ProviderTestCase2<KadecotCoreProvider> {

    private static enum DeviceData {

        Device1("protocol1", "type1", "desc1", 1, "nickname1"),
        Device2("protocol2", "type2", "desc2", 1, "nickname2");

        private final String mProtocol;
        private final String mUuid;
        private final String mType;
        private final String mDesc;
        private final int mStatus;
        private final String mNickname;

        DeviceData(String protocol, String type, String desc, int status, String nickname) {
            mProtocol = protocol;
            mUuid = String.valueOf(ordinal());
            mType = type;
            mDesc = desc;
            mStatus = status;
            mNickname = nickname;
        }

        public String getProtocol() {
            return mProtocol;
        }

        public String getUuid() {
            return mUuid;
        }

        public String getType() {
            return mType;
        }

        public String getDesc() {
            return mDesc;
        }

        public int getStatus() {
            return mStatus;
        }

        public String getNickname() {
            return mNickname;
        }

    }

    private static enum TopicData {

        Topic1("protocol1", "name1", "desc1"),
        Topic2("protocol2", "name2", "desc2");

        private final String mProtocol;
        private final String mName;
        private final String mDesc;

        private TopicData(String protocol, String name, String desc) {
            mProtocol = protocol;
            mName = name;
            mDesc = desc;
        }

        public String getProtocol() {
            return mProtocol;
        }

        public String getName() {
            return mName;
        }

        public String getDesc() {
            return mDesc;
        }

    }

    private static enum ProcedureData {

        Proc1("protocol1", "name1", "desc1"),
        Proc2("protocol2", "name2", "desc2");

        private final String mProtocol;
        private final String mName;
        private final String mDesc;

        private ProcedureData(String protocol, String name, String desc) {
            mProtocol = protocol;
            mName = name;
            mDesc = desc;
        }

        public String getProtocol() {
            return mProtocol;
        }

        public String getName() {
            return mName;
        }

        public String getDesc() {
            return mDesc;
        }

    }

    public KadecotCoreProviderTestCase() {
        super(KadecotCoreProvider.class, KadecotCoreStore.AUTHORITY);
    }

    private KadecotCoreProvider mProvider;

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

    public void testDeviceInsert() {
        for (DeviceData data : DeviceData.values()) {
            ContentValues values = new ContentValues();
            values.put(KadecotCoreStore.Devices.DeviceColumns.PROTOCOL, data.getProtocol());
            values.put(KadecotCoreStore.Devices.DeviceColumns.UUID, data.getUuid());
            values.put(KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE, data.getType());
            values.put(KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION, data.getDesc());
            values.put(KadecotCoreStore.Devices.DeviceColumns.STATUS, data.getStatus());
            values.put(KadecotCoreStore.Devices.DeviceColumns.NICKNAME, data.getNickname());
            assertEquals(ContentUris.withAppendedId(KadecotCoreStore.Devices.CONTENT_URI,
                    data.ordinal() + 1),
                    mProvider.insert(KadecotCoreStore.Devices.CONTENT_URI, values));
        }

        Cursor c = mProvider.query(KadecotCoreStore.Devices.CONTENT_URI, null, null, null, null);
        c.moveToFirst();
        assertEquals(DeviceData.values().length, c.getCount());

        for (DeviceData data : DeviceData.values()) {
            assertEquals(
                    data.getProtocol(),
                    c.getString(c
                            .getColumnIndexOrThrow(KadecotCoreStore.Devices.DeviceColumns.PROTOCOL)));
            assertEquals(
                    data.getUuid(),
                    c.getString(c
                            .getColumnIndexOrThrow(KadecotCoreStore.Devices.DeviceColumns.UUID)));
            assertEquals(
                    data.getType(),
                    c.getString(c
                            .getColumnIndexOrThrow(KadecotCoreStore.Devices.DeviceColumns.DEVICE_TYPE)));
            assertEquals(
                    data.getDesc(),
                    c.getString(c
                            .getColumnIndexOrThrow(KadecotCoreStore.Devices.DeviceColumns.DESCRIPTION)));
            assertEquals(
                    data.getStatus(),
                    c.getInt(c
                            .getColumnIndexOrThrow(KadecotCoreStore.Devices.DeviceColumns.STATUS)));
            assertEquals(
                    data.getNickname(),
                    c.getString(c
                            .getColumnIndexOrThrow(KadecotCoreStore.Devices.DeviceColumns.NICKNAME)));
            c.moveToNext();
        }

        c.close();
    }

    public void testDeviceDelete() {
        testDeviceInsert();
        for (DeviceData data : DeviceData.values()) {
            mProvider.delete(KadecotCoreStore.Devices.CONTENT_URI,
                    KadecotCoreStore.Devices.DeviceColumns.UUID
                            + "=?",
                    new String[] {
                        String.valueOf(data.ordinal())
                    });
            Cursor c = mProvider
                    .query(KadecotCoreStore.Devices.CONTENT_URI, null, null, null, null);
            assertEquals(DeviceData.values().length - data.ordinal() - 1, c.getCount());
            c.close();
        }
    }

    public void testDeviceUpdate() {
        testDeviceInsert();
        for (DeviceData data : DeviceData.values()) {
            ContentValues values = new ContentValues();
            values.put(KadecotCoreStore.Devices.DeviceColumns.STATUS, 0);
            assertEquals(1,
                    mProvider.update(KadecotCoreStore.Devices.CONTENT_URI, values,
                            KadecotCoreStore.Devices.DeviceColumns.UUID
                                    + "=?",
                            new String[] {
                                String.valueOf(data.ordinal())
                            }));
        }
        Cursor c = mProvider.query(KadecotCoreStore.Devices.CONTENT_URI, null, null, null, null);
        c.moveToFirst();
        assertEquals(DeviceData.values().length, c.getCount());
        do {
            assertEquals(0,
                    c.getInt(c.getColumnIndex(KadecotCoreStore.Devices.DeviceColumns.STATUS)));
        } while (c.moveToNext());
        c.close();
    }

    public void testTopicInsert() {
        for (TopicData data : TopicData.values()) {
            ContentValues values = new ContentValues();
            values.put(KadecotCoreStore.Topics.TopicColumns.PROTOCOL, data.getProtocol());
            values.put(KadecotCoreStore.Topics.TopicColumns.NAME, data.getName());
            values.put(KadecotCoreStore.Topics.TopicColumns.DESCRIPTION, data.getDesc());
            assertEquals(ContentUris.withAppendedId(KadecotCoreStore.Topics.CONTENT_URI,
                    data.ordinal() + 1),
                    mProvider.insert(KadecotCoreStore.Topics.CONTENT_URI, values));
        }

        Cursor c = mProvider.query(KadecotCoreStore.Topics.CONTENT_URI, null, null, null, null);
        c.moveToFirst();
        assertEquals(DeviceData.values().length, c.getCount());

        for (TopicData data : TopicData.values()) {
            assertEquals(
                    data.getProtocol(),
                    c.getString(c
                            .getColumnIndexOrThrow(KadecotCoreStore.Topics.TopicColumns.PROTOCOL)));
            assertEquals(
                    data.getName(),
                    c.getString(c
                            .getColumnIndexOrThrow(KadecotCoreStore.Topics.TopicColumns.NAME)));
            assertEquals(
                    data.getDesc(),
                    c.getString(c
                            .getColumnIndexOrThrow(KadecotCoreStore.Topics.TopicColumns.DESCRIPTION)));
            c.moveToNext();
        }

        c.close();
    }

    public void testTopicDelete() {
        testTopicInsert();
        for (TopicData data : TopicData.values()) {
            mProvider.delete(KadecotCoreStore.Topics.CONTENT_URI,
                    KadecotCoreStore.Topics.TopicColumns.NAME
                            + "=?",
                    new String[] {
                        data.getName()
                    });
            Cursor c = mProvider
                    .query(KadecotCoreStore.Topics.CONTENT_URI, null, null, null, null);
            assertEquals(TopicData.values().length - data.ordinal() - 1, c.getCount());
            c.close();
        }
    }

    public void testProcedureInsert() {
        for (ProcedureData data : ProcedureData.values()) {
            ContentValues values = new ContentValues();
            values.put(KadecotCoreStore.Procedures.ProcedureColumns.PROTOCOL, data.getProtocol());
            values.put(KadecotCoreStore.Procedures.ProcedureColumns.NAME, data.getName());
            values.put(KadecotCoreStore.Procedures.ProcedureColumns.DESCRIPTION, data.getDesc());
            assertEquals(ContentUris.withAppendedId(KadecotCoreStore.Procedures.CONTENT_URI,
                    data.ordinal() + 1),
                    mProvider.insert(KadecotCoreStore.Procedures.CONTENT_URI, values));
        }

        Cursor c = mProvider.query(KadecotCoreStore.Procedures.CONTENT_URI, null, null, null, null);
        c.moveToFirst();
        assertEquals(DeviceData.values().length, c.getCount());

        for (ProcedureData data : ProcedureData.values()) {
            assertEquals(
                    data.getProtocol(),
                    c.getString(c
                            .getColumnIndexOrThrow(KadecotCoreStore.Procedures.ProcedureColumns.PROTOCOL)));
            assertEquals(
                    data.getName(),
                    c.getString(c
                            .getColumnIndexOrThrow(KadecotCoreStore.Procedures.ProcedureColumns.NAME)));
            assertEquals(
                    data.getDesc(),
                    c.getString(c
                            .getColumnIndexOrThrow(KadecotCoreStore.Procedures.ProcedureColumns.DESCRIPTION)));
            c.moveToNext();
        }

        c.close();
    }

    public void testProcedureDelete() {
        testProcedureInsert();
        for (ProcedureData data : ProcedureData.values()) {
            mProvider.delete(KadecotCoreStore.Procedures.CONTENT_URI,
                    KadecotCoreStore.Procedures.ProcedureColumns.NAME
                            + "=?",
                    new String[] {
                        data.getName()
                    });
            Cursor c = mProvider
                    .query(KadecotCoreStore.Procedures.CONTENT_URI, null, null, null, null);
            assertEquals(ProcedureData.values().length - data.ordinal() - 1, c.getCount());
            c.close();
        }
    }
}
