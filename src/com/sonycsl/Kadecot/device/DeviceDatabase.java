package com.sonycsl.Kadecot.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sonycsl.Kadecot.utils.DatabaseOpenHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 
 * デバイス全体のデータベース
 * protocol_name : デバイスのプロトコル(どのクラス由来のデバイスか調べるために使う)
 * nickname : デバイスのニックネーム
 * _id : デバイスのid(デバイスの詳細情報を持つデータベースと共有する)
 *
 */
public class DeviceDatabase {
	@SuppressWarnings("unused")
	private static final String TAG = DeviceDatabase.class
			.getSimpleName();
	private final DeviceDatabase self = this;

	private static final String DB_NAME = "device.db";
	private static final int DB_VERSION = 2;
	private static final String TABLE_NAME = "Device";

	private static final String KEY_DEVICE_ID = "_id"; // device_id
	private static final String KEY_NICKNAME = "nickname";
	private static final String KEY_PROTOCOL_NAME = "protocol_name";
	
	private static DeviceDatabase sSingleton = null;
	
	private Context mContext;
	private DatabaseOpenHelper mHelper;
	
	
	private DeviceDatabase(Context context) {

		mContext = context.getApplicationContext();
		
		HashMap<String, String> columns = new HashMap<String, String>();
		columns.put(KEY_DEVICE_ID, DatabaseOpenHelper.DATA_TYPE_INTEGER+DatabaseOpenHelper.OPTION_PRIMARY_KEY_AUTOINCREMENT);
		columns.put(KEY_NICKNAME, DatabaseOpenHelper.DATA_TYPE_TEXT+DatabaseOpenHelper.OPTION_NOT_NULL);
		columns.put(KEY_PROTOCOL_NAME, DatabaseOpenHelper.DATA_TYPE_TEXT+DatabaseOpenHelper.OPTION_NOT_NULL);
		
		mHelper = new DatabaseOpenHelper(mContext, DB_NAME,  DB_VERSION, TABLE_NAME, columns);
		
	}
	
	public static synchronized DeviceDatabase getInstance(Context context) {
		if(sSingleton == null) {
			sSingleton = new DeviceDatabase(context);
		}
		return sSingleton;
	}


	public boolean containsNickname(String nickname) {
		return mHelper.contains(KEY_NICKNAME, nickname);
	}

	// ニックネームを新しいものに変更する．
	protected synchronized boolean update(String oldNickname, String newNickname) {

		if(containsNickname(newNickname)) {
			return false;
		}
		
		ContentValues  values = new ContentValues();
		values.put(KEY_NICKNAME, newNickname);
		mHelper.update(
				new DatabaseOpenHelper.Where(
						new String[]{KEY_NICKNAME}
						, new String[]{oldNickname})
				, values);
		return true;
	}
	
	public synchronized boolean addDeviceData(String nickname, String protocolType) {
		if(containsNickname(nickname)) return false;
		ContentValues  values = new ContentValues();
		values.put(KEY_NICKNAME, nickname);
		values.put(KEY_PROTOCOL_NAME, protocolType);
		Long rowid = mHelper.insert(values);
		if(rowid == null || rowid < 0) return false;
		return true;
	}

	public synchronized boolean deleteDeviceData(long deviceId) {
		mHelper.delete(KEY_DEVICE_ID, Long.toString(deviceId));
		return true;
	}
	
	public synchronized boolean deleteAllDeviceData() {
		mHelper.deleteAll();
		return true;
	}

	private Cursor getCursorByNickname(String nickname) {
		return mHelper.getCursor(KEY_NICKNAME, nickname);
	}
	public DeviceData getDeviceData(String nickname) {
		Cursor c = this.getCursorByNickname(nickname);
		DeviceData device = getDeviceData(c);
		c.close();
		return device;
	}
	

	private Cursor getCursorByDeviceId(long deviceId) {
		return mHelper.getCursor(KEY_DEVICE_ID, Long.toString(deviceId));
	}
	
	public DeviceData getDeviceData(long deviceId) {
		Cursor c = this.getCursorByDeviceId(deviceId);
		DeviceData device = getDeviceData(c);
		c.close();
		return device;
	}
	
	public DeviceData getDeviceData(Cursor cursor) {
		if(cursor.getCount() == 0) {
			cursor.close();
			return null;
		}
		long deviceId = mHelper.getLong(cursor, KEY_DEVICE_ID);
		String nickname = mHelper.getString(cursor, KEY_NICKNAME);
		String protocol = mHelper.getString(cursor, KEY_PROTOCOL_NAME);
		
		return new DeviceData(deviceId, nickname, protocol);
	}
	
	public List<DeviceData> getDeviceDataList() {
		Cursor c = mHelper.getCursor();

		ArrayList<DeviceData> list = new ArrayList<DeviceData>();
		int size = c.getCount();
		for(int i = 0; i < size; i++) {
			DeviceData data = getDeviceData(c);
			
			list.add(data);
			c.moveToNext();
		}
		c.close();
		return list;
	}
	
	
	public class DeviceDatabaseHelper extends DatabaseOpenHelper {

		public DeviceDatabaseHelper(Context context, String dbName,
				int version, String tableName, Map<String, String> columns) {
			super(context, dbName, version, tableName, columns);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			super.onUpgrade(db, oldVersion, newVersion);
			if(oldVersion==1&&newVersion==2) {
				db.execSQL("drop table "+TABLE_NAME+";");
				this.onCreate(db);
			}
		}
		
	}
}
