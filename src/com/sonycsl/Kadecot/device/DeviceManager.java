package com.sonycsl.Kadecot.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sonycsl.Kadecot.call.ErrorResponse;
import com.sonycsl.Kadecot.call.KadecotCall;
import com.sonycsl.Kadecot.call.Notification;
import com.sonycsl.Kadecot.call.Response;
import com.sonycsl.Kadecot.device.echo.EchoManager;
import com.sonycsl.Kadecot.log.Logger;

import android.content.Context;
import android.util.Log;

/**
 * 
 * Deviceを管理するクラス
 *
 */
public class DeviceManager {
	@SuppressWarnings("unused")
	private static final String TAG = DeviceManager.class.getSimpleName();
	private final DeviceManager self = this;
	
	private static DeviceManager sInstance = null;
	
	private final Context mContext;
	private final HashMap<String, DeviceProtocol> mDeviceProtocols;
	private boolean mStarted = false;
	private DeviceDatabase mDeviceDatabase;
	private Logger mLogger;
	
	private DeviceManager(Context context) {
		mContext = context.getApplicationContext();
		mDeviceProtocols = new HashMap<String, DeviceProtocol>();
	}
	
	public static synchronized DeviceManager getInstance(Context context) {
		if(sInstance == null) {
			sInstance = new DeviceManager(context);
			sInstance.init();
		}

		return sInstance;
	}
	
	protected void init() {
		mDeviceDatabase = DeviceDatabase.getInstance(mContext);
		registerDeviceProtocol(EchoManager.getInstance(mContext));
		mLogger = Logger.getInstance(mContext);

	}
	
	public synchronized void start(){
		mStarted = true;
		for(DeviceProtocol protocol : mDeviceProtocols.values()) {
			protocol.start();
		}
	}
	
	public synchronized void stop() {
		for(DeviceProtocol protocol : mDeviceProtocols.values()) {
			protocol.stop();
		}
		mStarted = false;
	}
	
	public boolean isStarted() {
		return mStarted;
	}
	
	public void refreshList() {
		for(DeviceProtocol protocol : mDeviceProtocols.values()) {
			protocol.refreshDeviceList();
		}
		KadecotCall.informAll(Notification.ON_DEVICE_FOUND, Notification.onDeviceFound());
	}

	public void deleteAllDeviceData() {
		boolean started = mStarted;
		stop();
		for(DeviceProtocol protocol : mDeviceProtocols.values()) {
			protocol.deleteAllDeviceData();
		}
		if(started) {
			start();
		}
		KadecotCall.informAll(Notification.ON_DEVICE_FOUND, Notification.onDeviceFound());

	}
	
	public boolean isAllowedPermission(int clientPermissionLevel, int protocolPermissionLevel) {
		return (clientPermissionLevel <= protocolPermissionLevel);
	}
	
	public synchronized JSONArray list(int permissionLevel) {
		JSONArray list = new JSONArray();
		
		List<DeviceData> dataList = mDeviceDatabase.getDeviceDataList();
		
		if(isStarted()) {
			for(DeviceData data : dataList) {
				JSONObject device = getDeviceInfo(data,permissionLevel);
				if(device != null) {
					list.put(device);
				}
			}
		}
		return list;
	}
	
	
	public JSONObject getDeviceInfo(long deviceId, int permissionLevel) {
		DeviceData data = mDeviceDatabase.getDeviceData(deviceId);
		if(data != null) {
			return getDeviceInfo(data, permissionLevel);
		} else {
			return null;
		}
	}
	
	public JSONObject getDeviceInfo(DeviceData data, int permissionLevel) {
		JSONObject device = new JSONObject();
		try {
			device.put("nickname", data.nickname);
			device.put("protocol", data.protocolName);
			
			DeviceProtocol protocol = mDeviceProtocols.get(data.protocolName);

			DeviceInfo info = null;
			if(protocol != null) {
				if(!(isAllowedPermission(permissionLevel, protocol.getAllowedPermissionLevel()))) {
					return null;
				}
				info = protocol.getDeviceInfo(data.deviceId, "ja");
			}
			
			if(protocol != null && info != null) {
				
				device.put("active", info.active);
				device.put("deviceName", info.deviceName);
				device.put("deviceType", info.deviceType);
				device.put("option", info.option);
				
				return device;

			} else {
				//device.put("active", null);
				
				return null;

			}
		} catch (JSONException e) {
			return null;
		}
	}
	
	public void registerDeviceProtocol(DeviceProtocol protocol) {
		mDeviceProtocols.put(protocol.getProtocolName(), protocol);
	}
	
	public Response set(String nickname, ArrayList<DeviceProperty> propertyList, int permissionLevel) {

		final Thread current = Thread.currentThread();
		if(current.isInterrupted()) {
			return new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, "timeout");
		}

		DeviceData data = mDeviceDatabase.getDeviceData(nickname);
		if(data == null) {
			return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, "nickname not found");
		}
		
		DeviceProtocol protocol =  mDeviceProtocols.get(data.protocolName);
		
		if(isAllowedPermission(permissionLevel, protocol.getAllowedPermissionLevel())) {
			try {
				List<DeviceProperty> list = protocol.set(data.deviceId, propertyList);

				// log
				DeviceInfo info = protocol.getDeviceInfo(data.deviceId, "jp");
				for(DeviceProperty p : list) {
					mLogger.log(data, info, Logger.ACCESS_TYPE_SET, p);

				}
				
				return toAccessResponse(nickname, list);

			} catch (AccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return e.getErrorResponse();
			}
		} else {
			return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, "permission denied");
		}

	}

	
	public Response get(String nickname, ArrayList<String> propertyNameList, int permissionLevel) {

		final Thread current = Thread.currentThread();
		if(current.isInterrupted()) {
			return new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, "timeout");
		}

		DeviceData data = mDeviceDatabase.getDeviceData(nickname);
		if(data == null) {
			return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, "nickname not found");
		}
		
		DeviceProtocol protocol =  mDeviceProtocols.get(data.protocolName);
		
		if(isAllowedPermission(permissionLevel, protocol.getAllowedPermissionLevel())) {
			try {

				List<DeviceProperty> list = protocol.get(data.deviceId, propertyNameList);

				// log
				DeviceInfo info = protocol.getDeviceInfo(data.deviceId, "jp");
				for(DeviceProperty p : list) {
					mLogger.log(data, info, Logger.ACCESS_TYPE_GET, p);

				}

				return toAccessResponse(nickname, list);
			} catch (AccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return e.getErrorResponse();
			}
		} else {
			return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, "permission denied");
		}
	}
	
	private Response toAccessResponse(String nickname, List<DeviceProperty> list) {

		JSONObject result = new JSONObject();
		try {
			result.put("nickname", nickname);
			JSONArray array = new JSONArray();
			for(DeviceProperty p : list) {
				JSONObject prop = new JSONObject();

				prop.put("name", p.name);
				prop.put("value", p.value);
				prop.put("success", p.success);
				prop.put("message", p.message);
				
				array.put(prop);
			}
			result.put("property", array);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new Response(result);
	}
	
	public synchronized Response deleteDeviceData(JSONArray params) {
		final Thread current = Thread.currentThread();
		if(current.isInterrupted()) {
			return new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, "timeout");
		}
		if(params == null || params.length() < 1) {
			return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
		}

		String nickname = null;
		try {
			nickname = params.getString(0);
		} catch (JSONException e) {
			e.printStackTrace();
			return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
		}
		DeviceData data = mDeviceDatabase.getDeviceData(nickname);
		if(data == null) {
			return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, "nickname not found");
		}
		DeviceProtocol protocol =  mDeviceProtocols.get(data.protocolName);
		
		JSONObject result = protocol.deleteDeviceData(data.deviceId);

		mDeviceDatabase.deleteDeviceData(data.deviceId);
		KadecotCall.informAll(Notification.ON_DEVICE_FOUND, Notification.onDeviceFound());
		
		if(result != null && !result.isNull("code")) {
			// error
			return new ErrorResponse(result);
		} else {
			return new Response(result);
		}
	}
	
	public synchronized Response deleteInactiveDevices() {

		List<DeviceData> dataList = mDeviceDatabase.getDeviceDataList();
		for(DeviceData data : dataList) {
			DeviceInfo info = null;
			DeviceProtocol protocol = mDeviceProtocols.get(data.protocolName);
			if(protocol != null) {
				info = protocol.getDeviceInfo(data.deviceId, "jp");
				if(info != null && !info.active) {
					protocol.deleteDeviceData(data.deviceId);
					mDeviceDatabase.deleteDeviceData(data.deviceId);
				}
			}
		}
		KadecotCall.informAll(Notification.ON_DEVICE_FOUND, Notification.onDeviceFound());
		return new Response(null);
	}
	
	public synchronized Response changeNickname(JSONArray params) {
		final Thread current = Thread.currentThread();
		if(current.isInterrupted()) {
			return new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, "timeout");
		}
		if(params == null || params.length() < 2) {
			return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
		}

		String oldNickname = null;
		String newNickname = null;
		try {
			oldNickname = params.getString(0);
			newNickname = params.getString(1);
		} catch (JSONException e) {
			e.printStackTrace();
			return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
		}
		DeviceData data = mDeviceDatabase.getDeviceData(oldNickname);
		if(data == null) {
			return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, "nickname not found");
		}
		boolean result = mDeviceDatabase.update(oldNickname, newNickname);
		KadecotCall.informAll(Notification.ON_DEVICE_FOUND, Notification.onDeviceFound());
		if(result) {
			return new Response(null);
		} else {
			return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, "New nickname exists.");
		}
	}
	
	
	public void onPropertyChanged(DeviceData data, List<DeviceProperty> list) {
		JSONObject obj = new JSONObject();
		if(data == null) {
			return;
		}
		try {
			obj.put("nickname", data.nickname);
			JSONArray array = new JSONArray();
			for(DeviceProperty p : list) {
				JSONObject prop = new JSONObject();

				prop.put("name", p.name);
				prop.put("value", p.value);
				prop.put("success", p.success);
				
				array.put(prop);
			}
			obj.put("property", array);
			KadecotCall.informAll(Notification.ON_PROPERTY_CHANGED, Notification.onPropertyChanged(obj));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// log
		DeviceProtocol protocol =  mDeviceProtocols.get(data.protocolName);
		DeviceInfo info = protocol.getDeviceInfo(data.deviceId, "jp");
		for(DeviceProperty p : list) {
			mLogger.log(data, info, Logger.ACCESS_TYPE_GET, p);

		}

		
	}
}
