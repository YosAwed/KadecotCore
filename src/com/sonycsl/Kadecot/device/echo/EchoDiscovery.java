package com.sonycsl.Kadecot.device.echo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sonycsl.Kadecot.call.KadecotCall;
import com.sonycsl.Kadecot.call.Notification;
import com.sonycsl.Kadecot.core.Dbg;
import com.sonycsl.Kadecot.device.DeviceManager;
import com.sonycsl.Kadecot.log.Logger;
import com.sonycsl.echo.Echo;
import com.sonycsl.echo.eoj.EchoObject;
import com.sonycsl.echo.eoj.device.DeviceObject;
import com.sonycsl.echo.eoj.device.housingfacilities.PowerDistributionBoardMetering;
import com.sonycsl.echo.eoj.device.sensor.HumiditySensor;
import com.sonycsl.echo.eoj.device.sensor.TemperatureSensor;
import com.sonycsl.echo.eoj.profile.NodeProfile;
import com.sonycsl.echo.node.EchoNode;

import android.content.Context;
import android.util.Log;

public class EchoDiscovery {
	@SuppressWarnings("unused")
	private static final String TAG = EchoDiscovery.class.getSimpleName();
	private final EchoDiscovery self = this;
	
	private final Context mContext;
	private final Set<DeviceObject> mActiveDevices;
	private final EchoDeviceDatabase mEchoDeviceDatabase;
	private final Logger mLogger;
	
	public EchoDiscovery(Context context) {
		mContext = context.getApplicationContext();
		mActiveDevices = Collections.synchronizedSet(new HashSet<DeviceObject>());
		mEchoDeviceDatabase = EchoDeviceDatabase.getInstance(mContext);
		mLogger = Logger.getInstance(mContext);
	}
	
	protected synchronized void onDiscoverNewActiveDevice(DeviceObject device) {
		EchoDeviceData data;
		
		if(mEchoDeviceDatabase.containsDeviceData(device)) {
			data = mEchoDeviceDatabase.getDeviceData(device);
			//Log.d(TAG,"already " + data.nickname);
		} else {
			data = mEchoDeviceDatabase.addDeviceData(device);
			//Log.d(TAG,"new " + data.nickname);
		}
		

		//Log.d(TAG,DeviceManager.getInstance(mContext).getDeviceInfo(data, 0).toString());
		//onDiscover(device);
		Notification.informAllOnDeviceFound(DeviceManager.getInstance(mContext).getDeviceInfo(data, 0)
				, EchoManager.getInstance(mContext).getAllowedPermissionLevel());
		
		
		// logger
		HashSet<String> propertyNameSet = new HashSet<String>();
		long delay = (Logger.DEFAULT_INTERVAL_MILLS) - (System.currentTimeMillis() % (Logger.DEFAULT_INTERVAL_MILLS));

		switch(device.getEchoClassCode()) {
		case PowerDistributionBoardMetering.ECHO_CLASS_CODE:
			try {
				device.get().reqGetGetPropertyMap().send();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case TemperatureSensor.ECHO_CLASS_CODE:
			propertyNameSet.add(EchoManager.toPropertyName(TemperatureSensor.EPC_MEASURED_TEMPERATURE_VALUE));

			mLogger.watch(data.nickname, propertyNameSet,Logger.DEFAULT_INTERVAL_MILLS, delay);
			break;
		case HumiditySensor.ECHO_CLASS_CODE:
			propertyNameSet.add(EchoManager.toPropertyName(HumiditySensor.EPC_MEASURED_VALUE_OF_RELATIVE_HUMIDITY));
			
			mLogger.watch(data.nickname, propertyNameSet,Logger.DEFAULT_INTERVAL_MILLS, delay);
			break;
		}
	}
	
	
	protected void startDiscovering() {
		if(Echo.isStarted()) {
			// TODO:Why need this?
			EchoNode[] nodes = Echo.getNodes();
			for(EchoNode n : nodes) {
				DeviceObject[] devices = n.getDevices();
				for(DeviceObject d : devices) {
					onDiscover(d);
				}
			}

			try {
				NodeProfile.getG().reqGetSelfNodeInstanceListS().send();
			} catch (IOException e) {
			}
		}
		
	}
	
	
	public void onDiscover(DeviceObject device) {
		if(!mActiveDevices.contains(device)) {
			mActiveDevices.add(device);
			onDiscoverNewActiveDevice(device);
		}
	}
	
	protected synchronized void stopDiscovering() {
		
	}
	
	protected synchronized void clearActiveDevices() {
		for(DeviceObject d : mActiveDevices){
			if(d.isProxy()){
				d.getNode().removeDevice(d);
			}
		}
		mActiveDevices.clear();
	}
	
	protected synchronized void removeActiveDevices(long deviceId) {
		EchoDeviceData data = mEchoDeviceDatabase.getDeviceData(deviceId);
		EchoObject eoj = getEchoObject(data.address, data.echoClassCode, data.instanceCode);
		if(eoj == null) {return;}
		//if(!eoj.isProxy()) {
		//	Echo.getNode().removeDevice((DeviceObject)eoj);
		eoj.getNode().removeDevice((DeviceObject)eoj);

		//}
		mActiveDevices.remove(eoj);
	}
	
	private EchoObject getEchoObject(String address, short echoClassCode, byte instanceCode) {
		EchoNode en = Echo.getNode(address);
		if(en == null) return null;
		return en.getInstance(echoClassCode,instanceCode);
	}

	public synchronized boolean isActiveDevice(String address, short echoClassCode, byte instanceCode) {
		EchoObject eoj = getEchoObject(address, echoClassCode, instanceCode);
		if(eoj == null) {return false;}
		return mActiveDevices.contains(eoj);
	}	

}
