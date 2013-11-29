package com.sonycsl.Kadecot.device;

import org.json.JSONObject;

/**
 * 
 * Deviceの情報・状態
 * 主にDatabaseに登録されていない情報
 *
 */
public class DeviceInfo {
	@SuppressWarnings("unused")
	private static final String TAG = DeviceInfo.class.getSimpleName();
	private final DeviceInfo self = this;
	
	public final boolean active;
	public final String deviceName;
	public final String deviceType;
	public final JSONObject option;
	
	public DeviceInfo(boolean active, String deviceName, String deviceType, JSONObject option) {
		this.active = active;
		this.deviceName = deviceName;
		this.deviceType = deviceType;
		this.option = option;
	}

}
