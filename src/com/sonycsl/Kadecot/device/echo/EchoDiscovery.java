package com.sonycsl.Kadecot.device.echo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import com.sonycsl.Kadecot.call.KadecotCall;
import com.sonycsl.Kadecot.call.Notification;
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

public class EchoDiscovery {
	@SuppressWarnings("unused")
	private static final String TAG = EchoDiscovery.class.getSimpleName();
	private final EchoDiscovery self = this;
	
	private final Context mContext;
	private final Set<DeviceObject> mActiveDevices;
	private final EchoDeviceDatabase mEchoDeviceDatabase;
	
	public EchoDiscovery(Context context) {
		mContext = context.getApplicationContext();
		mActiveDevices = new HashSet<DeviceObject>();
		mEchoDeviceDatabase = EchoDeviceDatabase.getInstance(mContext);
	}
	
	protected synchronized void onDiscoverNewDevice(DeviceObject device) {
		mEchoDeviceDatabase.addDeviceData(device);
		
		EchoDeviceData data = mEchoDeviceDatabase.getDeviceData(device);

		Notification.informAllOnDeviceFound(DeviceManager.getInstance(mContext).getDeviceInfo(data, 0)
				, EchoManager.getInstance(mContext).getAllowedPermissionLevel());

		HashSet<String> propertyNameSet = new HashSet<String>();
		long delay = (1000*60*30) - (System.currentTimeMillis() % (1000*60*30));

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

			Logger.getInstance(mContext).watch(data.nickname, propertyNameSet,60*1000*30, delay);
			break;
		case HumiditySensor.ECHO_CLASS_CODE:
			propertyNameSet.add(EchoManager.toPropertyName(HumiditySensor.EPC_MEASURED_VALUE_OF_RELATIVE_HUMIDITY));
			
			Logger.getInstance(mContext).watch(data.nickname, propertyNameSet,60*1000*30, delay);
			break;
		}
	}
	
	
	protected synchronized void startDiscovering() {
		if(Echo.isStarted()) {
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
	
	
	public synchronized void onDiscover(DeviceObject device) {
		if(!mActiveDevices.contains(device)) {
			mActiveDevices.add(device);
			onDiscoverNewDevice(device);
		}
	}
	
	protected synchronized void stopDiscovering() {
		
	}
	
	protected synchronized void clearActiveDevices() {
		mActiveDevices.clear();
	}
	
	protected synchronized void removeActiveDevices(long deviceId) {
		EchoDeviceData data = mEchoDeviceDatabase.getDeviceData(deviceId);
		EchoObject eoj = getEchoObject(data.address, data.echoClassCode, data.instanceCode);
		if(eoj == null) {return;}
		if(!eoj.isProxy()) {
			Echo.getNode().removeDevice((DeviceObject)eoj);
		}
		mActiveDevices.remove(eoj);
	}
	
	private EchoObject getEchoObject(String address, short echoClassCode, byte instanceCode) {

		InetAddress inetAddress = null;
		if(address.equals(EchoDeviceDatabase.LOCAL_ADDRESS)) {
			if(Echo.getNode() == null) return null;
			inetAddress = Echo.getNode().getAddress();
		} else {
			try {
				inetAddress = InetAddress.getByName(address);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return null;
			}
		}
		if(inetAddress==null) {return null;}
		EchoObject eoj = Echo.getInstance(inetAddress, echoClassCode, instanceCode);
		return eoj;
	}

	public /*synchronized*/ boolean isActiveDevice(String address, short echoClassCode, byte instanceCode) {
		EchoObject eoj = getEchoObject(address, echoClassCode, instanceCode);
		if(eoj == null) {return false;}

		return mActiveDevices.contains(eoj);
	}	

}
