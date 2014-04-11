package com.sonycsl.Kadecot.device.echo;

import com.sonycsl.Kadecot.device.DeviceData;

public class EchoDeviceData extends DeviceData {
	@SuppressWarnings("unused")
	private static final String TAG = EchoDeviceData.class.getSimpleName();
	private final EchoDeviceData self = this;

	
	public final String address;
	public final short echoClassCode;
	public final byte instanceCode;
	public final Long parentId;
	

	public EchoDeviceData(DeviceData d, String address, short echoClassCode, byte instanceCode, Long parentId) {
		super(d.deviceId, d.nickname, d.protocolName);
		this.address = address;
		this.echoClassCode = echoClassCode;
		this.instanceCode = instanceCode;
		this.parentId = parentId;
	}
	
	
}
