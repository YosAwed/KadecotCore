package com.sonycsl.Kadecot.device.echo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.sonycsl.Kadecot.call.ErrorResponse;
import com.sonycsl.Kadecot.device.AccessException;
import com.sonycsl.Kadecot.device.DeviceData;
import com.sonycsl.Kadecot.device.DeviceDatabase;
import com.sonycsl.Kadecot.device.DeviceInfo;
import com.sonycsl.Kadecot.device.DeviceManager;
import com.sonycsl.Kadecot.device.DeviceProperty;
import com.sonycsl.Kadecot.device.DeviceProtocol;
import com.sonycsl.Kadecot.device.echo.generator.EchoDeviceAgent;
import com.sonycsl.Kadecot.device.echo.generator.EchoDeviceGenerator;
import com.sonycsl.Kadecot.log.Logger;
import com.sonycsl.echo.Echo;
import com.sonycsl.echo.EchoFrame;
import com.sonycsl.echo.EchoProperty;
import com.sonycsl.echo.EchoSocket;
import com.sonycsl.echo.EchoUtils;
import com.sonycsl.echo.eoj.EchoObject;
import com.sonycsl.echo.eoj.device.DeviceObject;
import com.sonycsl.echo.eoj.device.housingfacilities.PowerDistributionBoardMetering;
import com.sonycsl.echo.eoj.device.managementoperation.Controller;
import com.sonycsl.echo.eoj.device.sensor.HumiditySensor;
import com.sonycsl.echo.eoj.device.sensor.TemperatureSensor;
import com.sonycsl.echo.eoj.profile.NodeProfile;
import com.sonycsl.echo.node.EchoNode;

public class EchoManager implements DeviceProtocol {
	@SuppressWarnings("unused")
	private static final String TAG = EchoManager.class.getSimpleName();
	private final EchoManager self = this;

	public static final String PROTOCOL_TYPE_ECHO = "ECHONET Lite";

	
	private static EchoManager sInstance = null;

	private final Context mContext;
	
	private final MyNodeProfile mNodeProfile;
	private final MyController mController;
	
	private final Map<String, Callback> mCallbacks;

	static final long CALLBACK_TIME_OUT = 1000*10;
	
	private final Map<String, EchoDeviceGenerator> mGenerators;
	
	private final EchoDiscovery mEchoDiscovery;
	private EchoDeviceDatabase mEchoDeviceDatabase;
	private DeviceManager mDeviceManager;
	private DeviceDatabase mDeviceDatabase;
	
	
	private EchoManager(Context context) {
		mContext = context.getApplicationContext();

		mNodeProfile = new MyNodeProfile();
		mController = new MyController();
		
		mCallbacks = Collections.synchronizedMap(new HashMap<String, Callback>());
		
		mGenerators = Collections.synchronizedMap(new HashMap<String, EchoDeviceGenerator>());
		
		mEchoDiscovery = new EchoDiscovery(mContext);
		
		setup();
	}
	
	public static synchronized EchoManager getInstance(Context context) {
		if(sInstance == null) {
			sInstance = new EchoManager(context);
			sInstance.init();
		}
		return sInstance;
	}
	
	protected void init() {
		mEchoDeviceDatabase = EchoDeviceDatabase.getInstance(mContext);
		mDeviceManager = DeviceManager.getInstance(mContext);
		mDeviceDatabase = DeviceDatabase.getInstance(mContext);
	}
	
	private void setup() {
		Echo.addEventListener(new Echo.EventListener(){

			@Override
			public void onNewDeviceObject(DeviceObject device) {
				mEchoDiscovery.onDiscover(device);
			}

			@Override
			public void receiveEvent(EchoFrame frame) {
				EchoObject eoj = frame.getSeoj();
				EchoProperty[] properties = frame.getProperties();
				short tid = frame.getTid();
				String callbackId = getCallbackId(tid, eoj);

				switch(frame.getEsv()) {
				case EchoFrame.ESV_SET_RES: case EchoFrame.ESV_SETI_SNA: case EchoFrame.ESV_SETC_SNA:
				case EchoFrame.ESV_GET_RES: case EchoFrame.ESV_GET_SNA:

					if(mCallbacks.containsKey(callbackId)) {
						Callback callback = mCallbacks.get(callbackId);
						if(callback != null) {
							mCallbacks.remove(callbackId);

							callback.run(properties);
						}
					}
					
					break;
				case EchoFrame.ESV_INF: case EchoFrame.ESV_INF_SNA:
				case EchoFrame.ESV_INFC:
					onReceiveInformerFrame(eoj, properties);
					break;
				}
			}

			
			public void onReceiveInformerFrame(EchoObject eoj, EchoProperty[] properties) {
				EchoDeviceData data = mEchoDeviceDatabase.getDeviceData(eoj);
				if(data == null) {
					return;
				}
				List<DeviceProperty> list = new ArrayList<DeviceProperty>();
				for(EchoProperty p : properties) {
					DeviceProperty prop = new DeviceProperty();
					prop.name = toPropertyName(p.epc);
					prop.success = (p.edt != null);
					prop.value = prop.success ? toPropertyValue(p.edt) : null;
					list.add(prop);
				}
				mDeviceManager.onPropertyChanged(data, list);
			}

			@Override
			public void onGetProperty(EchoObject eoj, short tid, byte esv,
					EchoProperty property, boolean success) {
				super.onGetProperty(eoj, tid, esv, property, success);
				
				if(success && (property.epc == DeviceObject.EPC_GET_PROPERTY_MAP)) {
					byte[] properties = EchoUtils.propertyMapToProperties(property.edt);
					HashSet<String> watchingPropertySet = new HashSet<String>();
					switch(eoj.getEchoClassCode()) {
					case PowerDistributionBoardMetering.ECHO_CLASS_CODE:
						watchingPropertySet.add(toPropertyName(PowerDistributionBoardMetering.EPC_MEASURED_CUMULATIVE_AMOUNT_OF_ELECTRIC_ENERGY_NORMAL_DIRECTION));
						watchingPropertySet.add(toPropertyName(PowerDistributionBoardMetering.EPC_MEASURED_CUMULATIVE_AMOUNT_OF_ELECTRIC_ENERGY_REVERSE_DIRECTION));
						watchingPropertySet.add(toPropertyName(PowerDistributionBoardMetering.EPC_UNIT_FOR_CUMULATIVE_AMOUNTS_OF_ELECTRIC_ENERGY));
						for(byte p : properties) {
							int i = p & 0xFF;
							if(i >= (PowerDistributionBoardMetering.EPC_MEASUREMENT_CHANNEL1 & 0xFF)
									&& i <= (PowerDistributionBoardMetering.EPC_MEASUREMENT_CHANNEL32 & 0xFF)) {
								watchingPropertySet.add(toPropertyName(p));
							}
						}
						EchoDeviceData data = mEchoDeviceDatabase.getDeviceData(eoj);
						if(data != null) {
							long delay = (Logger.DEFAULT_INTERVAL_MILLS) - (System.currentTimeMillis() % (Logger.DEFAULT_INTERVAL_MILLS));
							Logger.getInstance(mContext).watch(data.nickname, watchingPropertySet,Logger.DEFAULT_INTERVAL_MILLS, delay);
						}
						break;
					}
				}
			}
			
			
		});
	}
	
	@Override
	public synchronized void start() {
		if(Echo.getNode() != null) {
			try {
				Echo.restart();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		} else {
			ArrayList<DeviceObject> deviceList = new ArrayList<DeviceObject>();
			
			deviceList.add(mController);

			
			for(String protocolName : mGenerators.keySet()) {
				EchoDeviceGenerator gen = mGenerators.get(protocolName);
				List<EchoDeviceData> agentDataList = mEchoDeviceDatabase.getDeviceDataList(protocolName);
				
				gen.onInitGenerator(agentDataList);

				for(EchoDeviceData data : agentDataList) {
					EchoDeviceAgent agent = new EchoDeviceAgent(data, gen);
					deviceList.add(agent);
				}
			}

			try {
				Echo.start(mNodeProfile, deviceList.toArray(new DeviceObject[] {}));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		mEchoDiscovery.startDiscovering();
		

	}

	@Override
	public synchronized void stop() {
		try {
			Echo.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public synchronized void refreshDeviceList() {
		mEchoDiscovery.clearActiveDevices();
		mEchoDiscovery.startDiscovering();
	}

	@Override
	public synchronized void deleteAllDeviceData() {
		try {
			Echo.stop();
			Echo.clear();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mEchoDiscovery.clearActiveDevices();
		mEchoDeviceDatabase.deleteAllDeviceData();

		
		for(String protocolName : mGenerators.keySet()) {
			mGenerators.get(protocolName).onDeleteAllEchoDevice();
		}
		
		setup();
	}

	public String getCallbackId(short tid, final EchoObject eoj, final byte epc) {
		return tid+","+eoj.getNode().getAddress()+","+eoj.getEchoObjectCode();
	}

	public String getCallbackId(short tid, final EchoObject eoj) {
		return tid+","+eoj.getNode().getAddress()+","+eoj.getEchoObjectCode();
	}
	
	public class Callback {
		public EchoProperty[] properties = null;
		public void run(EchoProperty[] properties) {
			this.properties = properties;
		}
	}

	public static String toPropertyName(byte epc) {
		return "0x"+EchoUtils.toHexString(epc);
	}
	private static JSONArray toPropertyValue(byte[] edt) {
		JSONArray edtAry = new JSONArray();
		if(edt!=null) {
			for(int i = 0; i < edt.length; i++) {
				edtAry.put(edt[i]&0xff);
			}
		}
		return edtAry;
	}

	@Override
	public int getAllowedPermissionLevel() {
		return 1;
	}

	
	public static JSONObject convertPropertyAsJSON(String nickname, String propertyName, Object propertyData) {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("nickname", nickname);
			jsonObj.put("property", propertyName);
			jsonObj.put("data", propertyData);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObj;
		
	}

	@Override
	public JSONObject deleteDeviceData(long deviceId) {
		EchoDeviceData data = mEchoDeviceDatabase.getDeviceData(deviceId);
		if(data == null) {
			return new JSONObject();
		}
		mEchoDiscovery.removeActiveDevices(deviceId);
		mEchoDeviceDatabase.deleteDeviceData(deviceId);
		
		if(data.parentId == null) {
			return new JSONObject();
		}
		
		DeviceData parentData = mDeviceDatabase.getDeviceData(data.parentId);
		if(parentData == null) {
			return new JSONObject();
		}
		for(String protocolName : mGenerators.keySet()) {
			if(protocolName.equals(parentData.protocolName)) {
				mGenerators.get(protocolName).onDeleteEchoDevice(data);
				break;
			}
		}
		return new JSONObject();
	}

	public EchoObject getEchoObject(long deviceId) throws UnknownHostException {

		EchoDeviceData data = mEchoDeviceDatabase.getDeviceData(deviceId);
		if(data == null) {
			return null;
		}
		
		InetAddress address = null;
		
		if(data.address.equals(EchoDeviceDatabase.LOCAL_ADDRESS)) {
			// local
			address = Echo.getNode().getAddress();
		} else {
			// remote
			address = InetAddress.getByName(data.address);
		}

		EchoObject eoj =  Echo.getInstance(address, data.echoClassCode, data.instanceCode);
		return eoj;
	}
	
	@Override
	public List<DeviceProperty> set(long deviceId, List<DeviceProperty> propertyList) throws AccessException {
		EchoObject eoj = null;
		try {
			eoj = getEchoObject(deviceId);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new AccessException(new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, "unknown host"));
		}
		
		if(eoj == null) {
			throw new AccessException(new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, "Not found echo object"));
		}
		
		ArrayList<EchoProperty> list = new ArrayList<EchoProperty>();
		try {
			for(DeviceProperty p : propertyList) {
				byte epc = Integer.decode(p.name).byteValue();
				if( p.value instanceof Integer ){
					JSONArray ja = new JSONArray() ;
					ja.put( ((Integer)p.value).intValue() ) ;
					p.value = ja ;
				}
				if(!(p.value instanceof JSONArray)) {
					throw new AccessException(new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE));
				}
				JSONArray value = (JSONArray)p.value;
				byte[] edt = new byte[value.length()];
				for(int i = 0; i < value.length(); i++) {
					edt[i] = (byte)value.getInt(i);
				}
				list.add(new EchoProperty(epc, edt));
			}
		} catch(Exception e) {
			throw new AccessException(new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e));
		}
		return setProperty(eoj, list);
	}
	
	public List<DeviceProperty> setProperty(EchoObject eoj, List<EchoProperty> propertyList) throws AccessException {
		try {
			final Thread current = Thread.currentThread();
			Callback callback = new Callback() {
				@Override
				public void run(EchoProperty[] properties) {
					super.run(properties);
					current.interrupt();
				}
				
			};
			
			HashMap<Byte, byte[]> map = new HashMap<Byte, byte[]>();
			
			// send
			String id;
			synchronized(EchoSocket.class) {
				EchoObject.Setter setter = eoj.set();
				for(EchoProperty p : propertyList) {
					setter.reqSetProperty(p.epc, p.edt);
					map.put((Byte)p.epc, p.edt);
				}
				short tid = setter.send();
				id = getCallbackId(tid, eoj);
				mCallbacks.put(id, callback);
			}
			
			// sleep
			try {
				Thread.sleep(CALLBACK_TIME_OUT);
			} catch (InterruptedException e) {
			}
				
			if(mCallbacks.containsKey(id)) {
				mCallbacks.remove(id);
				// timeout
				throw new AccessException(new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, "ECHONET Lite Timeout"));
			} else {
				if(callback.properties != null) {
					List<DeviceProperty> list = new ArrayList<DeviceProperty>();

					for(EchoProperty p : callback.properties) {
						DeviceProperty prop = new DeviceProperty();
						prop.name = toPropertyName(p.epc);
						prop.value = toPropertyValue(map.get(p.epc));
						prop.success = (p.edt == null);
						list.add(prop);
					}
					return list;
				} else {
					throw new AccessException(new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE));
				}
			}
				
		} catch (IOException e) {
			e.printStackTrace();
			throw new AccessException(new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, e));
		}
	}

	@Override
	public List<DeviceProperty> get(long deviceId, List<String> propertyNameList) throws AccessException {
		EchoObject eoj = null;
		try {
			eoj = getEchoObject(deviceId);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new AccessException(new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, "unknown host"));
		}
		
		if(eoj == null) {
			throw new AccessException(new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, "Not found echo object"));
		}
		
		ArrayList<Byte> list = new ArrayList<Byte>();
		try {
			for(String name : propertyNameList) {
				byte epc = Integer.decode(name).byteValue();
				list.add(epc);
			}
		} catch(Exception e) {
			throw new AccessException(new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e));
		}
		return getProperty(eoj, list);
	}

	public List<DeviceProperty> getProperty(EchoObject eoj, List<Byte> epcList) throws AccessException {
		try {
			final Thread current = Thread.currentThread();
			Callback callback = new Callback() {
				@Override
				public void run(EchoProperty[] properties) {
					super.run(properties);
					current.interrupt();
				}
				
			};
			
			// send
			String id;
			synchronized(EchoSocket.class) {
				EchoObject.Getter getter = eoj.get();
				for(Byte b : epcList) {
					getter.reqGetProperty(b);
				}
				short tid = getter.send();
				id = getCallbackId(tid, eoj);
				mCallbacks.put(id, callback);
			}
			
			// sleep
			try {
				Thread.sleep(CALLBACK_TIME_OUT);
			} catch (InterruptedException e) {
			}
				
			if(mCallbacks.containsKey(id)) {
				mCallbacks.remove(id);
				// timeout
				throw new AccessException(new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, "ECHONET Lite Timeout"));
			} else {
				if(callback.properties != null) {

					List<DeviceProperty> list = new ArrayList<DeviceProperty>();

					for(EchoProperty p : callback.properties) {
						DeviceProperty prop = new DeviceProperty();
						prop.name = toPropertyName(p.epc);

						if(p.edt != null) {
							prop.value = toPropertyValue(p.edt);
						} else {
							prop.value = null;
						}
						
						prop.success = (p.edt != null);
						list.add(prop);
					}
					return list;
				} else {
					throw new AccessException(new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE));
				}
			}
				
		} catch (IOException e) {
			e.printStackTrace();
			throw new AccessException(new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, e));
		}
	}
	
	
	public void addEchoDeviceGenerator(EchoDeviceGenerator generator) {
		mGenerators.put(generator.getProtocolName(), generator);
	}

	@Override
	public DeviceInfo getDeviceInfo(long deviceId, String locale) {

		EchoDeviceData data = mEchoDeviceDatabase.getDeviceData(deviceId);
		if(data == null) {
			return null;
		}
		boolean active = mEchoDiscovery.isActiveDevice(data.address, data.echoClassCode, data.instanceCode);
		JSONObject option = new JSONObject();
		if(data.parentId != null) {
			JSONObject obj = mDeviceManager.getDeviceInfo(data.parentId, 0);

			if(obj != null) {
				try {
					option.put("parent", obj);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		DeviceInfo info = new DeviceInfo(
				active
				, EchoDeviceUtils.getClassName(data.echoClassCode)
				, "0x"+EchoUtils.toHexString(data.echoClassCode)
				, option);
		return info;
	}

	@Override
	public String getProtocolName() {
		return PROTOCOL_TYPE_ECHO;
	}
	
	public byte generateDevice(short echoClassCode, long parentId) {
		int instanceCode;
		EchoNode node = Echo.getNode();

		EchoDeviceData data;
		
		if(node == null) {
			return 0;
		}

		DeviceData parentData = mDeviceDatabase.getDeviceData(parentId);
		if(parentData == null) {
			return 0;
		}
		
		EchoDeviceGenerator gen = mGenerators.get(parentData.protocolName);
		if(gen == null) {
			return 0;
		}
		
		synchronized(mEchoDeviceDatabase) {
			/// instance codeを決める
			List<Integer> instanceCodeList = mEchoDeviceDatabase.getLocalDeviceInstanceCodeList(echoClassCode);
			if(instanceCodeList.size() == 0) {
				instanceCode = EchoDeviceDatabase.MIN_INSTANCE_CODE;
			} else {
				instanceCode = instanceCodeList.get(instanceCodeList.size() - 1);
				if(instanceCode >= EchoDeviceDatabase.MAX_INSTANCE_CODE) {
					instanceCode = EchoDeviceDatabase.MIN_INSTANCE_CODE;

					if(instanceCodeList.contains(instanceCode)) {

						for(int code : instanceCodeList) {
							instanceCode = code + 1;
							if(!instanceCodeList.contains(instanceCode)) {
								break;
							}
						}
					}
					
					if(instanceCode >= EchoDeviceDatabase.MAX_INSTANCE_CODE) {
						return 0;
					}
				}
			}
			///
			data = mEchoDeviceDatabase.addLocalDeviceData(echoClassCode, (byte)instanceCode, parentId);
			if(data == null) {
				return 0;
			}
		}
		node.addDevice(new EchoDeviceAgent(data, gen));
		try {
			node.getNodeProfile().inform().reqInformSelfNodeInstanceListS().send();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (byte)instanceCode;
	}

}
