package com.sonycsl.Kadecot.device.echo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import com.sonycsl.Kadecot.call.KadecotCall;
import com.sonycsl.Kadecot.call.Notification;
import com.sonycsl.Kadecot.device.DeviceManager;
import com.sonycsl.echo.Echo;
import com.sonycsl.echo.eoj.EchoObject;
import com.sonycsl.echo.eoj.device.DeviceObject;
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
