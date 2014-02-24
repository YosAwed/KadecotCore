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

import com.sonycsl.Kadecot.call.CannotProcessRequestException;
import com.sonycsl.Kadecot.call.ErrorResponse;
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
		registerDeviceProtocol(EchoManager.getInstance(mContext));

	}
	
	public static synchronized DeviceManager getInstance(Context context) {
		if(sInstance == null) {
			sInstance = new DeviceManager(context);
		}

		return sInstance;
	}
	
	public synchronized void start(){
		mStarted = true;
		
		for(DeviceProtocol protocol : mDeviceProtocols.values()) {
			protocol.start();
		}
		
		refreshList(0);
	}
	
	public synchronized void stop() {
		for(DeviceProtocol protocol : mDeviceProtocols.values()) {
			protocol.stop();
		}
		getLogger().unwatchAll();
		
		Notification.informAllOnUpdateList(mContext);
		
		mStarted = false;
	}
	
	public boolean isStarted() {
		return mStarted;
	}
	
	public void refreshList(int permissionLevel) {
		if(isStarted() == false) {
			return;
		}

		//Notification.informAllEmptyOnUpdateList(mContext);
		Notification.informAllInactiveDeviceList(mContext);
		for(DeviceProtocol protocol : mDeviceProtocols.values()) {
			protocol.refreshDeviceList();
		}
		//Notification.informAllOnUpdateList(mContext);
		
	}

	public synchronized void deleteAllDeviceData() {
		boolean started = mStarted;
		stop();
		for(DeviceProtocol protocol : mDeviceProtocols.values()) {
			protocol.deleteAllDeviceData();
		}
		getDeviceDatabase().deleteAllDeviceData();
		if(started) {
			start();
		}
	}
	
	/**
	 * clientPermission が 0 なら 全ての操作を許可
	 *                    1 なら 一部の操作の許可
	 * protocolPermission が 0 なら 一部のClientのみ許可
	 *                       1 なら 全部のClientを許可
	 * clientPermission : {0,1}
	 * protocolPermission : {0,1}
	 * @param clientPermissionLevel
	 * @param protocolPermissionLevel
	 * @return
	 */
	public static boolean isAllowedPermission(int clientPermissionLevel, int protocolPermissionLevel) {
		return (clientPermissionLevel <= protocolPermissionLevel);
	}
	
	public JSONArray list(int permissionLevel) {
		if(isStarted() == false) {
			return new JSONArray();
		}
		JSONArray list = new JSONArray();
		
		List<DeviceData> dataList = getDeviceDatabase().getDeviceDataList();
		
		for(DeviceData data : dataList) {
			JSONObject device = getDeviceInfo(data,permissionLevel);
			if(device != null) {
				list.put(device);
			}
		}
		
		return list;
	}
	
	
	public JSONObject getDeviceInfo(long deviceId, int permissionLevel) {
		DeviceData data = getDeviceDatabase().getDeviceData(deviceId);
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

		if(isStarted() == false) {
			return new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, "Cannot access device");
		}

		DeviceData data = getDeviceDatabase().getDeviceData(nickname);
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
					getLogger().insertLog(data, info, Logger.ACCESS_TYPE_SET, p);
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

		if(isStarted() == false) {
			return new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, "Cannot access device");
		}
		
		DeviceData data = getDeviceDatabase().getDeviceData(nickname);
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
					getLogger().insertLog(data, info, Logger.ACCESS_TYPE_GET, p);

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

		if(isStarted() == false) {
			return new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, "Cannot access device");
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
		
		DeviceData data = getDeviceDatabase().getDeviceData(nickname);
		if(data == null) {
			return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, "nickname not found");
		}
		
		return deleteDeviceData(data);
	}
	
	public synchronized Response deleteDeviceData(DeviceData data) {

		DeviceProtocol protocol =  mDeviceProtocols.get(data.protocolName);
		CannotProcessRequestException cpre = null;
		
		try {
			protocol.deleteDeviceData(data.deviceId);
		} catch(CannotProcessRequestException e) {
			cpre = e;
		}

		boolean b = getDeviceDatabase().deleteDeviceData(data.deviceId);
		if(b) {
			Notification.informAllOnDeviceDeleted(data.nickname, protocol.getAllowedPermissionLevel());
		}
		if(cpre != null) {
			// error
			return cpre.getErrorResponse();
		} else {
			return new Response(null);
		}
	}
	
	public synchronized Response deleteInactiveDevices(int permissionLevel) {

		if(isStarted() == false) {
			return new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, "Cannot access device");
		}
		
		
		List<DeviceData> dataList = getDeviceDatabase().getDeviceDataList();
		for(DeviceData data : dataList) {
			DeviceInfo info = null;
			DeviceProtocol protocol = mDeviceProtocols.get(data.protocolName);
			if(protocol != null) {
				info = protocol.getDeviceInfo(data.deviceId, "jp");
				if(info != null && !info.active) {
					protocol.deleteDeviceData(data.deviceId);
					getDeviceDatabase().deleteDeviceData(data.deviceId);
				}
			}
		}
		Notification.informAllOnUpdateList(mContext);
		return new Response(null);
	}
	
	public synchronized Response changeNickname(JSONArray params) {

		if(isStarted() == false) {
			return new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, "Cannot access device");
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
		DeviceData data = getDeviceDatabase().getDeviceData(oldNickname);
		if(data == null) {
			return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, "nickname not found");
		}
		boolean result = getDeviceDatabase().update(oldNickname, newNickname);
		DeviceProtocol protocol =  mDeviceProtocols.get(data.protocolName);
		Notification.informAllOnNicknameChanged(oldNickname, newNickname, protocol.getAllowedPermissionLevel());
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
			Notification.informAllOnPropertyChanged(obj, 1);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// log
		DeviceProtocol protocol =  mDeviceProtocols.get(data.protocolName);
		DeviceInfo info = protocol.getDeviceInfo(data.deviceId, "jp");
		for(DeviceProperty p : list) {
			getLogger().insertLog(data, info, Logger.ACCESS_TYPE_GET, p);
		}

		
	}
	
	private DeviceDatabase getDeviceDatabase() {
		if(mDeviceDatabase == null) {
			mDeviceDatabase = DeviceDatabase.getInstance(mContext);
		}
		return mDeviceDatabase;
	}
	
	private Logger getLogger() {
		if(mLogger == null) {
			mLogger = Logger.getInstance(mContext);
		}
		return mLogger;
	}
}
