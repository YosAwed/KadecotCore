
package com.sonycsl.Kadecot.device.echo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sonycsl.Kadecot.device.DeviceData;
import com.sonycsl.Kadecot.device.DeviceDatabase;
import com.sonycsl.Kadecot.utils.DatabaseOpenHelper;
import com.sonycsl.echo.eoj.EchoObject;
import com.sonycsl.echo.eoj.device.DeviceObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EchoDeviceDatabase {
    @SuppressWarnings("unused")
    private static final String TAG = EchoDeviceDatabase.class.getSimpleName();

    private final EchoDeviceDatabase self = this;

    protected static final String DB_NAME = "echo_device.db";

    protected static final int DB_VERSION = 2;

    protected static final String TABLE_NAME = "EchoDevice";

    protected static final String KEY_ID = "_id";

    protected static final String KEY_DEVICE_ID = "device_id";

    protected static final String KEY_ADDRESS = "address";

    protected static final String KEY_ECHO_CLASS_CODE = "echo_class_code";

    protected static final String KEY_INSTANCE_CODE = "instance_code";

    protected static final String KEY_PARENT_ID = "parent_id";

    public static final String LOCAL_ADDRESS = "127.0.0.1";

    public static final int MIN_INSTANCE_CODE = 0x01;

    public static final int MAX_INSTANCE_CODE = 0x7F;

    private static EchoDeviceDatabase sInstance = null;

    private Context mContext;

    private EchoDeviceDatabaseHelper mHelper;

    private DeviceDatabase mDeviceDatabase = null;

    private EchoDeviceDatabase(Context context) {
        mContext = context.getApplicationContext();

        HashMap<String, String> columns = new HashMap<String, String>();

        columns.put(KEY_ID, DatabaseOpenHelper.DATA_TYPE_INTEGER
                + DatabaseOpenHelper.OPTION_PRIMARY_KEY_AUTOINCREMENT);
        columns.put(KEY_DEVICE_ID, DatabaseOpenHelper.DATA_TYPE_INTEGER
                + DatabaseOpenHelper.OPTION_NOT_NULL);
        columns.put(KEY_ADDRESS, DatabaseOpenHelper.DATA_TYPE_TEXT
                + DatabaseOpenHelper.OPTION_NOT_NULL);
        columns.put(KEY_ECHO_CLASS_CODE, DatabaseOpenHelper.DATA_TYPE_INTEGER
                + DatabaseOpenHelper.OPTION_NOT_NULL);
        columns.put(KEY_INSTANCE_CODE, DatabaseOpenHelper.DATA_TYPE_INTEGER
                + DatabaseOpenHelper.OPTION_NOT_NULL);

        columns.put(KEY_PARENT_ID, DatabaseOpenHelper.DATA_TYPE_INTEGER);

        mHelper = new EchoDeviceDatabaseHelper(mContext, DB_NAME, DB_VERSION, TABLE_NAME, columns);

    }

    public synchronized static EchoDeviceDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new EchoDeviceDatabase(context);
        }
        return sInstance;
    }

    public synchronized EchoDeviceData addDeviceData(DeviceObject device) {
        if (containsDeviceData(device)) {
            return null;
        }

        String name = EchoDeviceUtils.getClassName(device.getEchoClassCode());

        if (!device.isProxy()) {
            // local
            name = "My" + name;
        }

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            String nickname = name;
            if (i != 0) {
                nickname += i;
            }
            if (!getDeviceDatabase().containsNickname(nickname)) {
                return addDeviceData(nickname, device);
            }
        }
        return null;
    }

    public synchronized EchoDeviceData addLocalDeviceData(short echoClassCode, byte instanceCode,
            long generatorId) {

        String name = EchoDeviceUtils.getClassName(echoClassCode);

        DeviceData generator = getDeviceDatabase().getDeviceData(generatorId);
        if (generator == null) {
            return null;
        }

        name = "My" + name + "(" + generator.nickname + ")";

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            String nickname = name;
            if (i != 0) {
                nickname += i;
            }
            if (!getDeviceDatabase().containsNickname(nickname)) {
                boolean b =
                        getDeviceDatabase().addDeviceData(nickname, EchoManager.PROTOCOL_TYPE_ECHO);
                if (!b) {
                    return null;
                }
                DeviceData data = getDeviceDatabase().getDeviceData(nickname);

                if (containsDeviceId(data.deviceId)) {
                    return null;
                }

                ContentValues values = new ContentValues();
                values.put(KEY_ADDRESS, LOCAL_ADDRESS);
                values.put(KEY_ECHO_CLASS_CODE, echoClassCode & 0xFFFF);
                values.put(KEY_INSTANCE_CODE, instanceCode & 0xFF);
                values.put(KEY_DEVICE_ID, data.deviceId);
                values.put(KEY_PARENT_ID, generator.deviceId);
                mHelper.insert(values);

                return this.getDeviceData(data);
            }
        }
        return null;
    }

    private synchronized EchoDeviceData addDeviceData(String nickname, DeviceObject device) {
        boolean result =
                getDeviceDatabase().addDeviceData(nickname, EchoManager.PROTOCOL_TYPE_ECHO);
        if (!result) {
            return null;
        }

        String address;
        if (device.isProxy()) {
            address = device.getNode().getAddress().getHostAddress();
        } else {
            // local
            address = LOCAL_ADDRESS;
        }
        DeviceData d = getDeviceDatabase().getDeviceData(nickname);

        if (containsDeviceId(d.deviceId)) {
            return null;
        }

        ContentValues values = new ContentValues();
        values.put(KEY_ADDRESS, address);
        values.put(KEY_ECHO_CLASS_CODE, device.getEchoClassCode() & 0xFFFF);
        values.put(KEY_INSTANCE_CODE, device.getInstanceCode() & 0xFF);
        values.put(KEY_DEVICE_ID, d.deviceId);

        Long rowid = mHelper.insert(values);
        if (rowid == null || rowid < 0)
            return null;
        return getDeviceData(mHelper.getCursorByRowId(rowid));

    }

    public synchronized boolean deleteDeviceData(long deviceId) {
        mHelper.delete(KEY_DEVICE_ID, Long.toString(deviceId));
        return true;
    }

    public synchronized void deleteAllDeviceData() {
        mHelper.deleteAll();
    }

    protected boolean containsDeviceId(long deviceId) {
        return mHelper.contains(KEY_DEVICE_ID, Long.toString(deviceId));
    }

    public boolean containsDeviceData(DeviceObject device) {

        String address;
        if (device.isProxy()) {
            address = device.getNode().getAddress().getHostAddress();
        } else {
            address = LOCAL_ADDRESS;
        }
        return containsDeviceData(address, device.getEchoClassCode() & 0xFFFF, device
                .getInstanceCode() & 0xFF);
    }

    private boolean containsDeviceData(String address, int echoClassCode, int instanceCode) {
        return mHelper.contains(new DatabaseOpenHelper.Where(new String[] {
                KEY_ADDRESS, KEY_ECHO_CLASS_CODE, KEY_INSTANCE_CODE
        }, new String[] {
                address, Integer.toString(echoClassCode), Integer.toString(instanceCode)
        }));
    }

    public EchoDeviceData getDeviceData(EchoObject device) {

        String address;
        if (device.isProxy()) {
            address = device.getNode().getAddress().getHostAddress();
        } else {
            address = LOCAL_ADDRESS;
        }
        return getDeviceData(address, device.getEchoClassCode() & 0xFFFF,
                device.getInstanceCode() & 0xFF);
    }

    public EchoDeviceData getDeviceData(String address, int echoClassCode, int instanceCode) {
        Cursor c = mHelper.getCursor(new DatabaseOpenHelper.Where(new String[] {
                KEY_ADDRESS, KEY_ECHO_CLASS_CODE, KEY_INSTANCE_CODE
        }, new String[] {
                address, Integer.toString(echoClassCode), Integer.toString(instanceCode)
        }));
        EchoDeviceData data = getDeviceData(c);
        c.close();
        return data;
    }

    public EchoDeviceData getDeviceData(String nickname) {
        DeviceData device = getDeviceDatabase().getDeviceData(nickname);
        if (device == null)
            return null;
        return getDeviceData(device);
    }

    public EchoDeviceData getDeviceData(long deviceId) {
        DeviceData device = getDeviceDatabase().getDeviceData(deviceId);
        if (device == null)
            return null;
        return getDeviceData(device);
    }

    public EchoDeviceData getDeviceData(Cursor cursor) {
        if (cursor.getCount() == 0) {
            return null;
        }
        long deviceId = mHelper.getLong(cursor, KEY_DEVICE_ID);

        DeviceData data = getDeviceDatabase().getDeviceData(deviceId);
        if (data == null)
            return null;
        return getDeviceData(data, cursor);
    }

    public EchoDeviceData getDeviceData(DeviceData data) {
        if (!data.protocolName.equals(EchoManager.PROTOCOL_TYPE_ECHO)) {
            return null;
        }
        Cursor c = mHelper.getCursor(KEY_DEVICE_ID, Long.toString(data.deviceId));
        EchoDeviceData d = getDeviceData(data, c);
        c.close();
        return d;

    }

    public EchoDeviceData getDeviceData(DeviceData data, Cursor c) {
        if (!data.protocolName.equals(EchoManager.PROTOCOL_TYPE_ECHO)) {
            return null;
        }
        if (c.getCount() == 0) {
            return null;
        }
        String address = mHelper.getString(c, KEY_ADDRESS);
        int echoClassCode = mHelper.getInt(c, KEY_ECHO_CLASS_CODE);
        int instanceCode = mHelper.getInt(c, KEY_INSTANCE_CODE);
        Long parentId = mHelper.getLong(c, KEY_PARENT_ID);

        return new EchoDeviceData(data, address, (short) echoClassCode, (byte) instanceCode,
                parentId);

    }

    public List<DeviceData> getDeviceDataList() {

        List<DeviceData> ret = new ArrayList<DeviceData>();
        Cursor cursor = mHelper.getCursor();
        int size = cursor.getCount();
        for (int i = 0; i < size; i++) {
            EchoDeviceData dev = getDeviceData(cursor);
            if (dev != null) {
                ret.add(dev);
            }
            cursor.moveToNext();
        }

        cursor.close();
        return ret;
    }

    public List<EchoDeviceData> getDeviceDataList(String protocolName) {

        List<EchoDeviceData> ret = new ArrayList<EchoDeviceData>();
        Cursor cursor = mHelper.getCursor();
        int size = cursor.getCount();
        for (int i = 0; i < size; i++) {
            EchoDeviceData data = getDeviceData(cursor);
            if (data != null && data.parentId != null) {
                DeviceData d = getDeviceDatabase().getDeviceData(data.parentId);
                if (d != null && d.protocolName.equals(protocolName)) {
                    ret.add(data);
                }
            }
            cursor.moveToNext();
        }

        cursor.close();
        return ret;
    }

    public List<EchoDeviceData> getDeviceDataListByParentId(long parentId) {

        List<EchoDeviceData> ret = new ArrayList<EchoDeviceData>();
        Cursor cursor = mHelper.getCursor(KEY_PARENT_ID, Long.toString(parentId));
        int size = cursor.getCount();
        for (int i = 0; i < size; i++) {
            EchoDeviceData data = getDeviceData(cursor);
            ret.add(data);
            cursor.moveToNext();
        }

        cursor.close();
        return ret;
    }

    public List<Integer> getLocalDeviceInstanceCodeList(short echoClassCode) {

        List<Integer> ret = new ArrayList<Integer>();
        Cursor cursor = mHelper.getCursor(new DatabaseOpenHelper.Where(new String[] {
                KEY_ADDRESS, KEY_ECHO_CLASS_CODE
        }, new String[] {
                LOCAL_ADDRESS, Integer.toString(echoClassCode & 0xFFFF)
        }), new DatabaseOpenHelper.OrderBy(KEY_INSTANCE_CODE, DatabaseOpenHelper.OrderBy.ASC));
        int size = cursor.getCount();
        for (int i = 0; i < size; i++) {
            ret.add(mHelper.getInt(cursor, KEY_INSTANCE_CODE));
            cursor.moveToNext();
        }

        cursor.close();
        return ret;
    }

    public class EchoDeviceDatabaseHelper extends DatabaseOpenHelper {

        public EchoDeviceDatabaseHelper(Context context, String dbName, int version,
                String tableName, Map<String, String> columns) {
            super(context, dbName, version, tableName, columns);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            super.onUpgrade(db, oldVersion, newVersion);
            if (oldVersion == 1 && newVersion == 2) {
                db.execSQL("drop table " + TABLE_NAME + ";");
                this.onCreate(db);
            }
        }
    }

    private DeviceDatabase getDeviceDatabase() {
        if (mDeviceDatabase == null) {
            mDeviceDatabase = DeviceDatabase.getInstance(mContext);
        }
        return mDeviceDatabase;
    }
}
